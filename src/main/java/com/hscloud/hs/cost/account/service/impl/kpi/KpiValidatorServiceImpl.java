package com.hscloud.hs.cost.account.service.impl.kpi;

import cn.hutool.core.date.DatePattern;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.bestvike.linq.Linq;
import com.hscloud.hs.cost.account.constant.enums.FieldEnum;
import com.hscloud.hs.cost.account.constant.enums.kpi.ItemResultEnum;
import com.hscloud.hs.cost.account.constant.enums.kpi.RoundingEnum;
import com.hscloud.hs.cost.account.mapper.dataReport.CostReportDetailCostMapper;
import com.hscloud.hs.cost.account.mapper.dataReport.CostReportDetailInfoMapper;
import com.hscloud.hs.cost.account.mapper.dataReport.CostReportItemMapper;
import com.hscloud.hs.cost.account.mapper.dataReport.CostReportRecordMapper;
import com.hscloud.hs.cost.account.mapper.kpi.KpiItemMapper;
import com.hscloud.hs.cost.account.mapper.kpi.KpiMemberMapper;
import com.hscloud.hs.cost.account.model.dto.kpi.KpiItemCondDto;
import com.hscloud.hs.cost.account.model.dto.kpi.KpiValidatorDTO;
import com.hscloud.hs.cost.account.model.entity.dataReport.*;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiAccountUnit;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiMember;
import com.hscloud.hs.cost.account.model.vo.ValidatorResultVo;
import com.hscloud.hs.cost.account.service.dataReport.ICostClusterUnitService;
import com.hscloud.hs.cost.account.service.kpi.KpiAccountUnitService;
import com.hscloud.hs.cost.account.service.kpi.KpiValidatorService;
import com.hscloud.hs.cost.account.utils.CommonUtils;
import com.hscloud.hs.cost.account.utils.SqlUtil;
import com.hscloud.hs.cost.account.utils.kpi.StringChangeUtil;
import com.hscloud.hs.cost.account.validator.RuleConfig;
import com.hscloud.hs.cost.account.validator.ValidatorHolder;
import com.pig4cloud.pigx.admin.api.feign.RemoteUserService;
import com.pig4cloud.pigx.admin.api.vo.UserCoreVo;
import com.pig4cloud.pigx.common.core.constant.SecurityConstants;
import com.pig4cloud.pigx.common.core.exception.BizException;
import io.netty.util.internal.StringUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Administrator
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class KpiValidatorServiceImpl implements KpiValidatorService {
    private final KpiItemMapper kpiItemMapper;
    private final KpiMemberMapper kpiMemberMapper;
    private final KpiAccountUnitService kpiAccountUnitService;
    private final RemoteUserService remoteUserService;
    private final ICostClusterUnitService costClusterUnitService;
    private final CostReportItemMapper costReportItemMapper;
    private final CostReportRecordMapper costReportRecordMapper;
    private final CostReportDetailInfoMapper costReportDetailInfoMapper;
    private final CostReportDetailCostMapper costReportDetailCostMapper;

    private static final String RELATION_AND = "等于";
    private static final String RELATION_FEI = "不等于";
    private static final String $_WHERE = "${where}";
    private static final String $_START_DATE = "${start_date}";
    private static final String $_END_DATE = "${end_date}";
    private final SqlUtil sqlUtil;
    private final ValidatorHolder validatorHolder;

    @Override
    public ValidatorResultVo itemValidator(KpiValidatorDTO dto, Boolean changeFlag, List<KpiMember> members, List<KpiAccountUnit> units) {
        ValidatorResultVo resultVo = new ValidatorResultVo();
        long startTime = System.currentTimeMillis();

        try {
            List<LinkedHashMap<String, Object>> linkedHashMaps;
            List<KpiValidatorDTO.SqlValidatorParam> params = dto.getParams();
            if (StringUtils.hasLength(dto.getSql())) {
                HashMap<String, Object> sqlInputParam = getSqlInputParam(dto, changeFlag, params);
                linkedHashMaps = kpiItemMapper.executeSql(sqlInputParam);
            } else if (null != dto.getReportId()) {
                linkedHashMaps = getReportData(dto.getReportId(), params, members, units, dto.getCaliber());
            } else {
                throw new BizException("校验参数异常，请确保存在口径颗粒度（sql）或上报项");
            }
            String result = changeData(dto.getRetainDecimal(), dto.getCarryRule(), linkedHashMaps, changeFlag, null);
            resultVo.setResult(result);
        } catch (BizException e) {
            resultVo.setErrorMsg(e.getMessage());
        } catch (Exception e) {
            log.error("校验异常：", e);
            resultVo.setErrorMsg("校验异常：" + CommonUtils.getEroLog(e));
        }
        long endTime = System.currentTimeMillis();
        resultVo.setExecuteTime((int) (endTime - startTime));
        return resultVo;
    }

    @Override
    public HashMap<String, Object> getSqlInputParam(KpiValidatorDTO dto, Boolean changeFlag, List<KpiValidatorDTO.SqlValidatorParam> params) {
        String dtoSql = dto.getSql();
        String whereSql = StringUtils.hasText(dto.getWhereSql()) ? dto.getWhereSql() : "";
        List<KpiItemCondDto> condList = dto.getCondList();

        if (dtoSql.contains($_WHERE)) {
            if (!CollectionUtils.isEmpty(condList) && !StringUtils.hasText(whereSql)) {
                // 处理查询条件格式
                sqlUtil.dealCond(condList);

                // 校验查询条件
                RuleConfig ruleConfig = new RuleConfig();
                ruleConfig.setType("SQL_COND");
                ruleConfig.setContent(JSON.toJSONString(condList));
                ValidatorResultVo vo = validatorHolder.getValidatorByType("SQL_COND").validate(ruleConfig);
                if (StringUtils.hasLength(vo.getErrorMsg())) {
                    throw new BizException(vo.getErrorMsg());
                }

                whereSql = " and " + sqlUtil.buildWhereCondition(condList);
            }
            dtoSql = dtoSql.replace($_WHERE, whereSql);
        }

        if (dtoSql.contains($_START_DATE)) {
            String startDate = StringChangeUtil.getFirstDayOfPeriod(dto.getPeriod()) + " 00:00:00";
            dtoSql = dtoSql.replace($_START_DATE, "'" + startDate + "'");

        }

        if (dtoSql.contains($_END_DATE)) {
            String endDate = StringChangeUtil.getLastDayOfPeriod(dto.getPeriod()) + " 23:59:59";
            dtoSql = dtoSql.replace($_END_DATE, "'" + endDate + "'");
        }

        boolean safeSql = SqlUtil.isDdlSql(dtoSql);
        if (safeSql) {
            throw new BizException("sql存在非法操作，已被禁止");
        }
        String sql = StringChangeUtil.removeSqlComment(dtoSql);

        return checkSqlParams(sql, params, changeFlag);
    }

    /**
     * sql入参校验
     *
     * @param sql    sql
     * @param params 入参
     */
    public static HashMap<String, Object> checkSqlParams(String sql,
                                                         List<KpiValidatorDTO.SqlValidatorParam> params,
                                                         boolean check) {
        if (CollectionUtils.isEmpty(params)) {
            throw new BizException("校验参数异常，请确保存在查询入参");
        }
        sql = "select * from (" + sql + ") t where 1 = 1 ";

        List<KpiValidatorDTO.SqlValidatorParam> var1 = new ArrayList<>(32);
        createNewSqlParam(params, var1);
        StringBuilder builder = new StringBuilder(sql);
        HashMap<String, Object> sqlInputParam = new HashMap<>(16);
        for (KpiValidatorDTO.SqlValidatorParam param : var1) {
            String relation = param.getRelation();
            if (!StringUtils.hasLength(param.getKey()) || !StringUtils.hasLength(param.getValue()) || !StringUtils.hasLength(param.getType())) {
                throw new BizException("校验参数异常，请确保查询入参完整");
            }
            String value = param.getValue();
            boolean isNeedAddSqlParam = true;
            if (FieldEnum.DATE.getCode().equals(param.getType())) {
                value = param.getValue().replaceAll("-", "").substring(0, 6);
                if (param.getKey().equals(ItemResultEnum.PERIOD.getType())) {
                    builder.append(" and period = #{period}");
                }
            } else if (FieldEnum.PARAM.getCode().equals(param.getType())) {
                isNeedAddSqlParam = false;
                if (RELATION_AND.equals(relation)) {
                    builder.append(" and ").append(param.getKey()).append(" = ").append(value);
                }
                if (RELATION_FEI.equals(relation)) {
                    builder.append(" and ").append(param.getKey()).append(" != ").append(value);
                }
            } else if (FieldEnum.STRING.getCode().equals(param.getType())) {
                isNeedAddSqlParam = false;
                String[] valueList = value.split(",");
                StringBuilder valueBuilder = new StringBuilder("(");
                for (String var6 : valueList) {
                    valueBuilder.append("'").append(var6).append("',");
                }
                value = valueBuilder.substring(0, valueBuilder.length() - 1) + ")";
                String var4 = "#{" + param.getKey() + "}";
                String var5 = "${" + param.getKey() + "}";
                if (!sql.contains(var4) && !sql.contains(var5)) {
                    var4 = value;
                    if (RELATION_AND.equals(relation)) {
                        builder.append(" and ").append(param.getKey()).append(" in ").append(var4);
                    }
                    if (RELATION_FEI.equals(relation)) {
                        builder.append(" and ").append(param.getKey()).append(" not in ").append(var4);
                    }
                }
            }
            if (isNeedAddSqlParam) {
                sqlInputParam.put(param.getKey(), value);
            }
        }
        sql = builder.toString();
        if (check) {
            sql = sql + " limit 20";
        }
        sqlInputParam.put("sql", sql);
        return sqlInputParam;
    }

    @Override
    public String changeData(Integer retainDecimal, String carryRule, List<LinkedHashMap<String, Object>> map, Boolean changeFlag, String busiType) {
        Map<Long, String> userMap = new HashMap<>(1024);
        Map<Long, String> deptMap = new HashMap<>(512);
        Map<Long, String> imputationDeptMap = new HashMap<>(512);
        if (changeFlag) {
            List<UserCoreVo> var1 = remoteUserService.listMainDetails(SecurityConstants.FROM_IN).getData();
            userMap = var1.stream().collect(Collectors.toMap(UserCoreVo::getUserId, UserCoreVo::getName));
            deptMap = kpiAccountUnitService.getUnitMap(busiType);
            List<CostClusterUnit> var2 = costClusterUnitService.list();
            imputationDeptMap = var2.stream().collect(Collectors.toMap(CostClusterUnit::getId, CostClusterUnit::getName));
        }

        JSONArray jsonArray = new JSONArray();
        List<String> fieldList = Arrays.stream(ItemResultEnum.values()).map(ItemResultEnum::getType).collect(Collectors.toList());
        try {
            for (LinkedHashMap<String, Object> object : map) {
                JSONObject jsonObject = new JSONObject();
                for (String key : object.keySet()) {
                    Object objValue = object.get(key);
                    String value = null == objValue ? "" : String.valueOf(objValue);
                    key = StringChangeUtil.camelCaseToSnakeCase(key);
                    if (ItemResultEnum.PERIOD.getType().equals(key)) {
                        value = StringChangeUtil.periodChange(value, DatePattern.NORM_MONTH_PATTERN);
                    }
                    if (changeFlag && StringUtils.hasLength(value)) {
                        //if (fieldList.contains(key)) {
                        ItemResultEnum itemResultEnum = ItemResultEnum.findEnumByType(key);
                        if (itemResultEnum == null) {
                            continue;
                        }
                        switch (itemResultEnum) {
                            case ZDYS:
                            case KZYS:
                            case EMP:
                                value = userMap.get(Long.valueOf(value));
                                break;
                            case BRKS:
                            case BRBQ:
                            case ZDYSKS:
                            case KZYSKS:
                            case WARD:
                            case DEPT:
                                value = deptMap.get(Long.valueOf(value));
                                break;
                            case IMPUTATION_DEPT:
                                value = imputationDeptMap.get(Long.valueOf(value));
                                break;
                            default:
                                break;
                        }
                        //}
                    }
                    if (ItemResultEnum.VALUE.getType().equals(key)) {
                        if (StringUtils.hasLength(value)) {
                            BigDecimal var1 = new BigDecimal(value);
                            value = var1.setScale(retainDecimal, RoundingEnum.getCodeByDesc(carryRule)).toString();
                        }
                    }
                    if (null == value) {
                        value = "";
                    }
                    jsonObject.put(key, value);
                }
                jsonArray.add(jsonObject);
            }
        } catch (Exception e) {
            log.error("数据转换异常：", e);
            throw new BizException("数据转换异常");
        }
        return jsonArray.toString();
    }

    public List<LinkedHashMap<String, Object>> getReportData(Long reportId, List<KpiValidatorDTO.SqlValidatorParam> params, List<KpiMember> members, List<KpiAccountUnit> units, String caliber) {
        CostReportItem item = costReportItemMapper.selectById(reportId);
        if (null == item) {
            throw new BizException("无此上报项数据");
        }
        Optional<KpiValidatorDTO.SqlValidatorParam> optional = params.stream().filter(param -> param.getKey().equals(ItemResultEnum.PERIOD.getType())).findFirst();
        if (!optional.isPresent()) {
            throw new BizException("请选择周期");
        }
        String period = "";
        if (optional.get().getValue().length() == 6) {
            period = optional.get().getValue().substring(0, 4) + "-" + optional.get().getValue().substring(4);
        } else {
            period = optional.get().getValue().substring(0, 7);
        }
        String var1 = "\"id\":\"" + reportId + "\"";
        String reportCaliber = item.getReportType();
        CostReportRecord record = costReportRecordMapper.selectOne(Wrappers.<CostReportRecord>lambdaQuery()
                .eq(CostReportRecord::getCalculateCircle, period)
                .in(CostReportRecord::getStatus, Arrays.asList("2", "5"))
                .eq(CostReportRecord::getReportType, reportCaliber)
                .like(CostReportRecord::getItemList, var1)
        );
        if (null == record) {
            throw new BizException("当前周期没有上报数据-RECORD");
        }
        List<CostReportDetailInfo> infoList = costReportDetailInfoMapper.selectList(Wrappers.<CostReportDetailInfo>lambdaQuery()
                .eq(CostReportDetailInfo::getRecordId, record.getId())
        );
        if (CollectionUtils.isEmpty(infoList)) {
            throw new BizException("当前周期没有上报数据-RECORD_INFO");
        }
        List<CostReportDetailCost> costList = costReportDetailCostMapper.selectList(Wrappers.<CostReportDetailCost>lambdaQuery()
                .eq(CostReportDetailCost::getRecordId, record.getId())
                .eq(CostReportDetailCost::getItemId, reportId)
                .isNotNull(CostReportDetailCost::getAmt)
        );
        if (CollectionUtils.isEmpty(costList)) {
            throw new BizException("当前周期没有上报数据-RECORD_COST");
        }
        Map<Long, BigDecimal> costMap = costList.stream().collect(Collectors.toMap(CostReportDetailCost::getDetailInfoId, CostReportDetailCost::getAmt));
        period = StringChangeUtil.periodChange(period, DatePattern.SIMPLE_MONTH_PATTERN);
        List<KpiValidatorDTO.SqlValidatorParam> var2 = params.stream().filter(s -> !s.getKey().equals(ItemResultEnum.PERIOD.getType())).collect(Collectors.toList());
        List<LinkedHashMap<String, Object>> linkedHashMaps = new ArrayList<>(128);

        List<CostReportDetailInfo> infoList2 = new ArrayList<>();
        infoList2.addAll(infoList);
        List<CostReportDetailInfo> hosList = Linq.of(infoList).where(x -> "HOSPITALIZATION".equals(x.getDeptType())).toList();

        if ("1".equals(item.getIsDeptDistinguished())) {
            for (CostReportDetailInfo detailInfo : hosList) {
                JSONArray objects = JSONObject.parseArray(detailInfo.getMeasureUnit());
                if (objects.isEmpty()) {
                    continue;
                }
                JSONObject obj = objects.getJSONObject(0);
                String id = obj.getString("id");
                //有护理组
                KpiMember hlz = Linq.of(members).firstOrDefault(x -> x.getMemberId() != null && id.equals(x.getMemberId().toString()));
                CostReportDetailInfo fo = new CostReportDetailInfo();
                fo.setRecordId(detailInfo.getRecordId())
                        .setDeptType(detailInfo.getDeptType())
                        .setNote(detailInfo.getNote())
                        .setAccountingUnitType(detailInfo.getAccountingUnitType());
                JSONObject o = new JSONObject();
                if (hlz != null && !Linq.of(infoList).any(x -> !StringUtil.isNullOrEmpty(x.getMeasureUnit()) && x.getMeasureUnit().contains(hlz.getHostId().toString()))) {
                    KpiAccountUnit first = Linq.of(units).firstOrDefault(r -> r.getId().equals(hlz.getHostId()));
                    if (first != null) {
                        o.put("name", first.getName());
                        o.put("id", first.getId());
                        fo.setMeasureUnit("[" + JSONObject.toJSONString(o) + "]")
                                .setMeasureGroup("{\"label\":\"护理组\",\"value\":\"HSDX002\"}")
                                .setId(detailInfo.getId() * 10);
                        infoList2.add(fo);
                        costMap.put(detailInfo.getId() * 10, Linq.of(costList).firstOrDefault(x -> x.getDetailInfoId().equals(detailInfo.getId())).getAmt());
                    }
                }
                //有医生组
                KpiMember ysz = Linq.of(members).firstOrDefault(x -> x.getHostId() != null && id.equals(x.getHostId().toString()));
                if (ysz != null && !Linq.of(infoList).any(x -> !StringUtil.isNullOrEmpty(x.getMeasureUnit()) && x.getMeasureUnit().contains(ysz.getMemberId().toString()))) {
                    KpiAccountUnit first = Linq.of(units).firstOrDefault(r -> r.getId().equals(ysz.getMemberId()));
                    if (first != null) {
                        o.put("name", first.getName());
                        o.put("id", first.getId());
                        fo.setMeasureUnit("[" + JSONObject.toJSONString(o) + "]")
                                .setMeasureGroup("{\"label\":\"护理组\",\"value\":\"HSDX002\"}")
                                .setId(detailInfo.getId() * 10);
                        infoList2.add(fo);
                        costMap.put(detailInfo.getId() * 10, Linq.of(costList).firstOrDefault(x -> x.getDetailInfoId().equals(detailInfo.getId())).getAmt());
                    }
                }
            }
        }
        for (CostReportDetailInfo detailInfo : infoList2) {
            LinkedHashMap<String, Object> var3 = new LinkedHashMap<>();
            var3.put(ItemResultEnum.PERIOD.getType(), period);
            String measureUnit = detailInfo.getMeasureUnit();
            reportDataDeal(measureUnit, var2, var3, ItemResultEnum.DEPT.getType());

            String clusterUnits = detailInfo.getClusterUnits();
            reportDataDeal(clusterUnits, var2, var3, ItemResultEnum.IMPUTATION_DEPT.getType());

            String user = detailInfo.getUser();
            reportDataDeal(user, var2, var3, ItemResultEnum.EMP.getType());
            //1人2科室3归集4固定值
            String key = null;
            if (caliber.equals("1")) {
                key = ItemResultEnum.EMP.getType();
            } else if (caliber.equals("2")) {
                key = ItemResultEnum.DEPT.getType();
            } else if (caliber.equals("3")) {
                key = ItemResultEnum.IMPUTATION_DEPT.getType();
            }
            if (key != null && (var3.get(key) == null || StringUtil.isNullOrEmpty(var3.get(key).toString()))) {
                continue;
            }
            BigDecimal data = costMap.get(detailInfo.getId());
            var3.put(ItemResultEnum.VALUE.getType(), data);
            linkedHashMaps.add(var3);
        }
        return linkedHashMaps;
    }

    public static boolean reportDataDeal(String var1, List<KpiValidatorDTO.SqlValidatorParam> var2, LinkedHashMap<String, Object> var3, String keyName) {
        boolean flag = false;
        if (StringUtils.hasLength(var1)) {
            JSONArray jsonArray = JSONArray.parseArray(var1);
            if (!CollectionUtils.isEmpty(jsonArray)) {
                for (Object jsonObject : jsonArray) {
                    JSONObject obj = (JSONObject) jsonObject;
                    String id = obj.getString("id");
                    if (!CollectionUtils.isEmpty(var2)) {
                        Optional<KpiValidatorDTO.SqlValidatorParam> var4 = var2.stream().filter(s -> s.getValue().equals(id)).findFirst();
                        if (var4.isPresent()) {
                            KpiValidatorDTO.SqlValidatorParam var5 = var4.get();
                            var3.put(keyName, var5.getValue());
                        } else {
                            flag = true;
                            break;
                        }
                    } else {
                        var3.put(keyName, id);
                    }
                }
            }
        }
        return flag;
    }

    /**
     * 创建新的参数 合并掉同个key和relation的参数值
     *
     * @param params       入参
     * @param newParamList 新参数
     */
    public static void createNewSqlParam(List<KpiValidatorDTO.SqlValidatorParam> params, List<KpiValidatorDTO.SqlValidatorParam> newParamList) {
        List<String> checkParam = new ArrayList<>(32);
        for (KpiValidatorDTO.SqlValidatorParam param : params) {
            if (checkParam.size() > 0 && checkParam.contains(param.getRelation() + param.getKey())) {
                continue;
            }
            List<KpiValidatorDTO.SqlValidatorParam> var2 = params.stream().filter(s -> s.getRelation().equals(param.getRelation())
                            && s.getKey().equals(param.getKey()))
                    .collect(Collectors.toList());
            if (var2.size() > 1) {
                KpiValidatorDTO.SqlValidatorParam var3 = var2.get(0);
                String valueJoin = var2.stream().map(KpiValidatorDTO.SqlValidatorParam::getValue).collect(Collectors.joining(","));
                var3.setValue(valueJoin);
                newParamList.add(var3);
            } else {
                newParamList.add(param);
            }
            checkParam.add(param.getRelation() + param.getKey());
        }
    }
}
