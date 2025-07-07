package com.hscloud.hs.cost.account.service.impl.report;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bestvike.linq.Linq;
import com.hscloud.hs.cost.account.constant.Constant;
import com.hscloud.hs.cost.account.mapper.kpi.KpiReportConfigMapper;
import com.hscloud.hs.cost.account.mapper.kpi.KpiReportConfigPowerMapper;
import com.hscloud.hs.cost.account.mapper.report.ReportMapper;
import com.hscloud.hs.cost.account.model.dto.report.*;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiReportConfig;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiReportConfigPower;
import com.hscloud.hs.cost.account.model.entity.report.*;
import com.hscloud.hs.cost.account.model.entity.second.GrantUnit;
import com.hscloud.hs.cost.account.model.pojo.report.ParamMapping;
import com.hscloud.hs.cost.account.model.vo.report.ParamVo;
import com.hscloud.hs.cost.account.model.vo.report.ReportCellDataVo;
import com.hscloud.hs.cost.account.model.vo.report.ReportTableDataVo;
import com.hscloud.hs.cost.account.model.vo.report.SonReportParamVo;
import com.hscloud.hs.cost.account.service.report.IReportDbService;
import com.hscloud.hs.cost.account.service.report.IReportFieldService;
import com.hscloud.hs.cost.account.service.report.IReportFieldSonReportService;
import com.hscloud.hs.cost.account.service.report.IReportService;
import com.hscloud.hs.cost.account.service.second.IGrantUnitService;
import com.hscloud.hs.cost.account.utils.EnhanceSqlUtils;
import com.hscloud.hs.cost.account.utils.JdbcUtil;
import com.hscloud.hs.cost.account.utils.ReportDataCalcUtils;
import com.pig4cloud.pigx.admin.api.entity.SysDictItem;
import com.pig4cloud.pigx.admin.api.feign.RemoteDictService;
import com.pig4cloud.pigx.common.core.exception.BizException;
import com.pig4cloud.pigx.common.core.util.R;
import com.pig4cloud.pigx.common.security.service.PigxUser;
import com.pig4cloud.pigx.common.security.util.SecurityUtils;
import io.netty.util.internal.StringUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 报表设计表 服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportService extends ServiceImpl<ReportMapper, Report> implements IReportService {

    private final IReportDbService reportDbService;

    private final IReportFieldService reportFieldService;

    private final IReportFieldSonReportService sonReportService;

    private final JdbcUtil jdbcUtil;

    private final ReportDataCalcUtils reportDataCalcUtils;

    private final RemoteDictService remoteDictService;
    private final KpiReportConfigPowerMapper kpiReportConfigPowerMapper;
    private final IGrantUnitService grantUnitService;
    private final KpiReportConfigMapper kpiReportConfigMapper;
    // private final ReportDbService reportDbService2;


    /**
     * @param dto
     * @return {@link ReportTableDataVo }
     */
    // @CacheEvict(cacheManager = "localCacheManager", value = {"reportFieldFormula", "reportFieldFormulaDetails", "reportFieldCommonFormula"}, allEntries = true, beforeInvocation = true)
    @Override
    public ReportTableDataVo reportData(ReportDataDto dto) {

        // 1、获取报表数据
        String reportCode = dto.getReportCode();
        Report report = getByReportCode(reportCode);
        if (Objects.isNull(report)) {
            throw new BizException("报表不存在");
        }

        // 2、处理sql
        ReportDb reportDb = reportDbService.loadDbAllDataByReportId(report.getId());
        if (Objects.isNull(reportDb)) {
            throw new BizException("找不到数据集");
        }
        String dbDynSql = reportDb.getDbDynSql();
        if (StrUtil.isBlank(dbDynSql)) {
            throw new BizException("数据集sql为空");
        }
        if (Objects.equals("1", dto.getIsPreview())) {
            log.debug("移除where条件");
            dbDynSql = EnhanceSqlUtils.removeWhere(dbDynSql);
        } else {
            log.debug("替换内置sql条件");
            dbDynSql = EnhanceSqlUtils.replaceWhere(dbDynSql);
        }
        log.info("dbDynSql:{}", dbDynSql);
        fillParam(dto, reportDb);
        checkAndRemoveCustomParam(dto, reportDb);
        specialLogic(dto);
        if (dto.getBaseReportId() != null) {
            specialLogic2(dto,reportDb);
        }
        log.info("dto:{}", dto);
        ReportTableDataVo resultVo = new ReportTableDataVo();
        // 3、查询数据
        List<Map<String, Object>> dbData;
        if (Objects.equals("1", reportDb.getIsPage())) {
            log.info("需要分页");
            resultVo.setCurrent(dto.getCurrent());
            resultVo.setSize(dto.getSize());
            // 增强为count sql
            Long count = jdbcUtil.count(dbDynSql, dto);
            resultVo.setTotal(count);
            dbData = jdbcUtil.queryPage(dbDynSql, dto, resultVo);
        } else {
            dbData = jdbcUtil.query(dbDynSql, dto, resultVo);
        }
        if (CollUtil.isEmpty(dbData)) {
            resultVo.setRecords(new ArrayList<>());
            return resultVo;
        }

        // 将计算改为使用forkjonpool
        // 拆分到每次计算100条数据
        long l = System.currentTimeMillis();
        List<Map<String, Object>> maps = reportDataCalcUtils.calcData(dbData, reportDb);
        resultVo.setRows(maps);

       // List<Map<String, Object>> calcFieldFormula = dbData.stream().map(e ->
       //         reportFieldService.caclFieldFormula(e, reportDb.getReportFields())).collect(Collectors.toList());
       // resultVo.setRows(calcFieldFormula);
        log.info("计算表格用时:{}", (System.currentTimeMillis() - l) / 1000D);
        return resultVo;
    }

    /**
     * 特殊逻辑
     * 如果在字典中配置了这个人可以看到全部报表，那么就返回全部单元信息
     *
     * @param dto DTO
     */
    private void specialLogic(ReportDataDto dto) {
        PigxUser user = SecurityUtils.getUser();
        if (Objects.isNull(user)) {
            log.info("user is null");
            return;
        }
        Long currentUserId = user.getId();

        // 特殊逻辑，如果在字典中配置了这个人可以看到全部报表，那么就返回全部单元信息
        R<List<SysDictItem>> unitPrivilegeGrant = remoteDictService.getDictByType("unit_privilege_grant");
        if (!unitPrivilegeGrant.isOk()) {
            throw new BizException("字典获取失败");
        }
        boolean b = unitPrivilegeGrant.getData().stream().anyMatch(
                item -> Objects.equals(item.getLabel(), currentUserId.toString()) && StrUtil.contains(item.getItemValue(), dto.getReportCode()));
        if (b && CollUtil.isNotEmpty(dto.getCustomParams())) {
            // 进入特殊逻辑
            List<CustomParamDto> collect = dto.getCustomParams().stream()
                    .filter(e -> Objects.nonNull(e) && !Objects.equals(e.getCode(), Constant.ACCOUNT_UNIT_ID)).collect(Collectors.toList());
            dto.setCustomParams(collect);
            log.info("specialLogic customParams:{}", JSON.toJSONString(dto.getCustomParams()));
        }
    }

    private void specialLogic2(ReportDataDto dto,ReportDb reportDb) {
        PigxUser user = SecurityUtils.getUser();
        if (Objects.isNull(user)) {
            log.info("user is null");
            return;
        }
        //kpiReportConfigMapper 一次报表不用过滤  找一下字段有没有
        KpiReportConfig config = kpiReportConfigMapper.selectById(dto.getBaseReportId());
        if ("1".equals(config.getType())){
            return;
        }
        // reportDbService2.fillReportDb(reportDb);
        //不存在科室id
        if (!Linq.of(reportDb.getDbFields()).select(x -> x.getFieldName()).any(x -> x.equalsIgnoreCase(Constant.ACCOUNT_UNIT_ID))){
            return;
        }

        List<Long> depts = new ArrayList<>();
        //权限
        if (!SecurityUtils.getUser().getAdminFlag()){
            List<KpiReportConfigPower> list = kpiReportConfigPowerMapper.selectList(
                    new QueryWrapper<KpiReportConfigPower>()
                            .eq("user_id", SecurityUtils.getUser().getId())
                            .eq("report_id",dto.getBaseReportId())
                            .eq("type","3")
            );
            if (!CollectionUtil.isEmpty(list)){
                depts.addAll(Linq.of(list).select(x->x.getDeptId()).toList());
            }
            List<GrantUnit> list1 = grantUnitService.list(new QueryWrapper<GrantUnit>().like("leader_ids", SecurityUtils.getUser().getId()).select("ks_unit_ids","ks_unit_ids_non_staff"));
            for (GrantUnit grantUnit : list1) {
                if(grantUnit != null) {
                    if (!StringUtil.isNullOrEmpty(grantUnit.getKsUnitIdsNonStaff())) {
                        for (String s : grantUnit.getKsUnitIdsNonStaff().split(",")) {
                            if (!StringUtil.isNullOrEmpty(s)) {
                                depts.add(Long.parseLong(s));
                            }
                        }
                    }
                    if (!StringUtil.isNullOrEmpty(grantUnit.getKsUnitIds())) {
                        for (String s : grantUnit.getKsUnitIds().split(",")) {
                            if (!StringUtil.isNullOrEmpty(s)) {
                                depts.add(Long.parseLong(s));
                            }
                        }
                    }
                }
            }
            depts=depts.stream().distinct().collect(Collectors.toList());
        }else{
            depts.add(-200L);
        }
        if (CollectionUtil.isEmpty(depts)) {
            depts.add(-1L);
        }

        if (!CollectionUtil.isEmpty(depts)){
            if(!Linq.of(depts).any(t->t.equals(-200L)))
            {
                CustomParamDto t = new CustomParamDto();
                t.setCode(Constant.ACCOUNT_UNIT_ID);
                t.setValue(depts);
                t.setOperator("in");
                if (dto.getCustomParams() == null){
                    dto.setCustomParams(new ArrayList<>());
                }
                dto.getCustomParams().add(t);
            }
        }
    }

    /**
     * 将数据及字段不存在的自定义字段移除
     *
     * @param dto
     * @param reportDb
     */
    private void checkAndRemoveCustomParam(ReportDataDto dto, ReportDb reportDb) {
        if (CollUtil.isEmpty(reportDb.getDbFields()) || CollUtil.isEmpty(dto.getCustomParams())) {
            return;
        }
        List<String> collect = reportDb.getDbFields().stream().map(ReportField::getFieldName).collect(Collectors.toList());
        if (CollUtil.isEmpty(collect)) {
            dto.setCustomParams(new ArrayList<>());
        }
        // 移除
        dto.getCustomParams().removeIf(e -> !collect.contains(e.getCode()));
    }

    private static void fillParam(ReportDataDto dto, ReportDb reportDb) {
        // 外部未传入值，则取入参默认值
        if (CollUtil.isNotEmpty(reportDb.getReportDbParams()) && !Objects.equals("1", dto.getIsPreview())) {
            if (CollUtil.isNotEmpty(dto.getParams())) {
                for (ReportDbParam reportDbParam : reportDb.getReportDbParams()) {
                    boolean isExist = false;
                    for (ParamDto param : dto.getParams()) {
                        if (Objects.equals(reportDbParam.getCode(), param.getCode())) {
                            isExist = true;
                            break;
                        }
                    }
                    if (!isExist) {
                        if (StrUtil.isBlank(reportDbParam.getDefaultValue())) {
                            throw new BizException("参数" + reportDbParam.getNote() + "未传入值");
                        }
                        ParamDto paramDto = new ParamDto();
                        paramDto.setCode(reportDbParam.getCode());
                        paramDto.setValue(reportDbParam.getDefaultValue());
                        dto.getParams().add(paramDto);
                    }
                }
            }
        }
    }

    @Override
    public Report getByReportCode(String reportCode) {
        return super.getOne(Wrappers.<Report>lambdaQuery().eq(Report::getCode, reportCode));
    }

    @Override
    public ReportFieldFormula fieldUseFormula(FieldUseFormulaDto dto) {
        return reportFieldService.fieldUseFormula(dto);
    }

    @Override
    public SonReportParamVo rowConvert2SonParams(RowConvert2SonParamsDto dto) {
        Map<String, Object> rowData = dto.getRowData();
        String accountUnitCode = rowData.get(Constant.ACCOUNT_UNIT_ID) == null ? null : rowData.get(Constant.ACCOUNT_UNIT_ID) + "";
        ReportFieldSonReport sonReport = sonReportService.findByFieldId(dto.getFieldId(), accountUnitCode);
        if (Objects.isNull(sonReport)) {
            throw new BizException("子报表关联配置不存在");
        }

        List<ParamVo> paramVos = new ArrayList<>();
        if (StrUtil.isNotBlank(sonReport.getParamMappingJson())) {
            List<ParamMapping> paramMapping = JSONObject.parseArray(sonReport.getParamMappingJson(), ParamMapping.class);
            for (ParamMapping entry : paramMapping) {
                String parentKey = entry.getParentCode();
                String childKey = entry.getChildCode();
                String parentText = entry.getParentText();
                if (!rowData.containsKey(parentKey)) {
                    throw new BizException(String.format("父报表缺少参数%s[%s]", parentText, parentKey));
                }
                ParamVo paramVo = new ParamVo();
                paramVo.setCode(childKey);
                paramVo.setValue(rowData.get(parentKey));
                paramVos.add(paramVo);
            }
        }

        Long sonReportId = sonReport.getSonReportId();
        Report byId = getById(sonReportId);
        if (Objects.isNull(byId)) {
            throw new BizException("子报表不存在");
        }
        SonReportParamVo sonReportParamVo = new SonReportParamVo();
        sonReportParamVo.setReportCode(byId.getCode());
        sonReportParamVo.setReportId(byId.getId());
        sonReportParamVo.setParams(paramVos);
        return sonReportParamVo;
    }

//    @Override
//    public List<ReportConditionVo> reportCondition(Long id) {
//        Report report = super.getById(id);
//        if (Objects.isNull(report)) {
//            throw new BizException("报表不存在");
//        }
//        ReportDb reportDb = reportDbService.loadDbAllDataByReportId(id);
//        if (Objects.isNull(reportDb)) {
//            throw new BizException("找不到数据集");
//        }
//        if (CollUtil.isEmpty(reportDb.getReportFields())) {
//            return new ArrayList<>();
//        }
//        return reportDb.getReportFields().stream().filter(e -> Objects.equals("1", e.getSearchFlag())).map(e -> {
//            ReportConditionVo reportConditionVo = new ReportConditionVo();
//            reportConditionVo.setParamCode(e.getFieldName());
//            reportConditionVo.setParamName(e.getFieldViewAlias());
//            return reportConditionVo;
//        }).collect(Collectors.toList());
//    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean createOrEdit(Report report) {
        // 保存或更新时这个报表是否和其他报表重名，忽略本身
        boolean existFlag = super.exists(Wrappers.<Report>lambdaQuery()
                .eq(Report::getName, report.getName())
                .ne(Objects.nonNull(report.getId()), Report::getId, report.getId()));
        if (existFlag) {
            throw new BizException("报表名称已存在");
        }
        if (Objects.isNull(report.getId())) {
            report.setCode(UUID.fastUUID().toString(true));
        }
        return super.saveOrUpdate(report);
    }

    @Override
    public ReportCellDataVo cellData(ReportCellDataDto dto) {
        ReportDataDto reportDataDto = new ReportDataDto();
        reportDataDto.setReportCode(dto.getReportCode());
        reportDataDto.setParams(dto.getParams());
        ReportTableDataVo reportTableDataVo = reportData(reportDataDto);
        List<Map<String, Object>> rows = reportTableDataVo.getRows();
        if (CollUtil.isEmpty(rows)) {
            log.warn("没有查询到数据");
            ReportCellDataVo reportCellDataVo = new ReportCellDataVo();
            reportCellDataVo.setCellMap(new HashMap<>());
            return reportCellDataVo;
        }
        if (rows.size() > 1) {
            log.warn("查询到多条数据，只取第一条：{}", rows.size());
        }
        Map<String, Object> cellMap = new HashMap<>();
        if(CollectionUtil.isNotEmpty(dto.getFieldNameList())) {
            for (String fieldName : dto.getFieldNameList()) {
                cellMap.put(fieldName, rows.get(0).get(fieldName));
            }
        } else {
            cellMap = rows.get(0);
        }
        ReportCellDataVo reportCellDataVo = new ReportCellDataVo();
        reportCellDataVo.setCellMap(cellMap);
        return reportCellDataVo;
    }

    @Override
    public Map<String, ReportCellDataVo> cellDataList(ReportCellDataDto dto) {
        Map<String, ReportCellDataVo> rtn = new HashMap<>();
        ReportDataDto reportDataDto = new ReportDataDto();
        reportDataDto.setReportCode(dto.getReportCode());
        reportDataDto.setParams(dto.getParams());
        ReportTableDataVo reportTableDataVo = reportData(reportDataDto);
        List<Map<String, Object>> rows = reportTableDataVo.getRows();
        if (CollUtil.isEmpty(rows)) {
            log.warn("没有查询到数据");
            return rtn;
        }

        for (Map<String, Object> row : rows){
            String cPeriod = row.get("cPeriod")+"";
            Map<String, Object> cellMap = new HashMap<>();
            if(CollectionUtil.isNotEmpty(dto.getFieldNameList())) {
                for (String fieldName : dto.getFieldNameList()) {
                    cellMap.put(fieldName, row.get(fieldName));
                }
            } else {
                cellMap = row;
            }
            ReportCellDataVo reportCellDataVo = new ReportCellDataVo();
            reportCellDataVo.setCellMap(cellMap);
            rtn.put(cPeriod, reportCellDataVo);
        }
        return rtn;
    }
}
