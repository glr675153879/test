package com.hscloud.hs.cost.account.service.impl.userAttendance;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.nacos.common.utils.CollectionUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bestvike.linq.Linq;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hscloud.hs.cost.account.mapper.userAttendance.FirstDistributionAttendanceFormulaMapper;
import com.hscloud.hs.cost.account.model.dto.userAttendance.FirstDistributionAccountFormulaDto;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiAccountUnit;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiUserAttendance;
import com.hscloud.hs.cost.account.model.entity.userAttendance.CostUserAttendance;
import com.hscloud.hs.cost.account.model.entity.userAttendance.CostUserAttendanceCustomFields;
import com.hscloud.hs.cost.account.model.entity.userAttendance.FirstDistributionAccountFormulaParam;
import com.hscloud.hs.cost.account.model.entity.userAttendance.FirstDistributionAttendanceFormula;
import com.hscloud.hs.cost.account.model.vo.ValidatorResultVo;
import com.hscloud.hs.cost.account.model.vo.userAttendance.CustomFieldVO;
import com.hscloud.hs.cost.account.service.impl.kpi.KpiAccountUnitService;
import com.hscloud.hs.cost.account.service.impl.kpi.KpiCategoryService;
import com.hscloud.hs.cost.account.service.userAttendance.IFirstDistributionAttendanceFormulaService;
import com.hscloud.hs.cost.account.utils.ExpressionCheckHelper;
import com.pig4cloud.pigx.common.core.util.R;
import jodd.util.StringUtil;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * 一次分配考勤公式配置表 服务实现类
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FirstDistributionAttendanceFormulaService extends ServiceImpl<FirstDistributionAttendanceFormulaMapper, FirstDistributionAttendanceFormula> implements IFirstDistributionAttendanceFormulaService {

    private static final String KQZTS = "KQZTS"; // 考勤组天数编码
    private static final String SJCQTS = "SJCQTS";// 实际出勤天数编码
    private static final String YCXCQTS = "YCXCQTS";// 一次性出勤天数编码
    private static final String SJCQ = "实际出勤天数";
    private static final String YCXCQ = "一次性出勤天数";
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("[a-zA-Z]+[a-zA-Z0-9]*");
    private final FirstDistributionAccountFormulaParamService firstDistributionAccountFormulaParamService;
    private final CostUserAttendanceCustomFieldsService costUserAttendanceCustomFieldsService;
    @Autowired
    private KpiAccountUnitService kpiAccountUnitService;
    @Autowired
    private KpiCategoryService kpiCategoryService;

    private static List<String> expressionQuantity(String expression) {
        List<String> list = new LinkedList<>();
        if (expression != null) {
            int index = expression.indexOf("=");
            if (index > -1) {
                expression = expression.substring(index + 1);
            }
            Matcher matcher = VARIABLE_PATTERN.matcher(expression);
            while (matcher.find()) {
                String item = matcher.group();
                list.add(item);
            }
        }
        return list;
    }

    @Override
    public String calculateAttendDays(Long planId, CostUserAttendance costUserAttendance) {
        LambdaQueryWrapper<FirstDistributionAttendanceFormula> qr = new LambdaQueryWrapper<>();
        qr.eq(FirstDistributionAttendanceFormula::getPlanId, planId);
        qr.eq(FirstDistributionAttendanceFormula::getDt, costUserAttendance.getDt());
        if (StringUtil.isNotBlank(costUserAttendance.getAccountUnit())) {
            try {
                String unitInfo = costUserAttendance.getAccountUnit();
                ObjectMapper mapper = new ObjectMapper();
                JsonNode rootNode = mapper.readTree(unitInfo);
                JsonNode jsonObject = rootNode.get(0);
                String name = jsonObject.get("name").asText();
                String id = jsonObject.get("id").asText();
                qr.like(FirstDistributionAttendanceFormula::getUnitName, name)
                        .like(FirstDistributionAttendanceFormula::getUnitId, id)
                        .last("limit 1");
            } catch (Exception e) {
                qr.eq(FirstDistributionAttendanceFormula::getFormulaType, "1").last("limit 1");
            }
        } else {
            qr.eq(FirstDistributionAttendanceFormula::getFormulaType, "1").last("limit 1");
        }
        FirstDistributionAttendanceFormula firstDistributionAttendanceFormula = this.getOne(qr);
        if (firstDistributionAttendanceFormula == null) {
            firstDistributionAttendanceFormula = this.getOne(new QueryWrapper<FirstDistributionAttendanceFormula>()
                    .eq("plan_id", planId)
                    .eq("formula_type", "1")
                    .eq("dt", costUserAttendance.getDt())
                    .last("limit 1"));
        }
        try {
            String expression = firstDistributionAttendanceFormula.getAttendanceFormula();
            ValidatorResultVo vo = getVerificationIndex(expression, costUserAttendance);
            return vo.getResult();
        } catch (Exception e) {
            return "0.0";
        }
    }

    @Override
    public String calculateAttendDays2(Long planId, KpiUserAttendance kpiUserAttendance
            , List<KpiAccountUnit> accountUnitList, List<FirstDistributionAttendanceFormula> firstDistributionAttendanceFormulas
            , List<FirstDistributionAccountFormulaParam> firstDistributionAccountFormulaParams, List<CostUserAttendanceCustomFields> costUserAttendanceCustomFields) {
//        List<KpiAccountUnit> accountUnitList = kpiAccountUnitService.list(new LambdaQueryWrapper<KpiAccountUnit>()
//                .eq(KpiAccountUnit::getStatus, "0").eq(KpiAccountUnit::getDelFlag, "0"));
//        LambdaQueryWrapper<FirstDistributionAttendanceFormula> qr = new LambdaQueryWrapper<>();
//        qr.eq(FirstDistributionAttendanceFormula::getPlanId, planId);
//        qr.eq(FirstDistributionAttendanceFormula::getDt, kpiUserAttendance.getPeriod());
        firstDistributionAttendanceFormulas = Linq.of(firstDistributionAttendanceFormulas)
                .where(t -> Objects.equals(t.getPlanId(), planId) && t.getDt().equals(String.valueOf(kpiUserAttendance.getPeriod()))).toList();
        FirstDistributionAttendanceFormula firstDistributionAttendanceFormula = null;
        if (kpiUserAttendance.getAccountUnit() != null) {
            try {
                Long unitInfo = kpiUserAttendance.getAccountUnit();
                KpiAccountUnit kpiAccountUnit = Linq.of(accountUnitList).where(t -> t.getId().equals(unitInfo)).firstOrDefault();
                if (kpiAccountUnit != null) {
                    firstDistributionAttendanceFormula = Linq.of(firstDistributionAttendanceFormulas).where(t -> t.getUnitName().contains(kpiAccountUnit.getName()) &&
                            t.getUnitId().contains(String.valueOf(unitInfo))).firstOrDefault();
                }
//                qr.like(FirstDistributionAttendanceFormula::getUnitName, kpiAccountUnit.getName())
//                        .like(FirstDistributionAttendanceFormula::getUnitId, unitInfo)
//                        .last("limit 1");
            } catch (Exception e) {
                //qr.eq(FirstDistributionAttendanceFormula::getFormulaType, "1").last("limit 1");
                firstDistributionAttendanceFormula = Linq.of(firstDistributionAttendanceFormulas).where(t -> t.getFormulaType().equals("1")).firstOrDefault();
            }
        } else {
            // qr.eq(FirstDistributionAttendanceFormula::getFormulaType, "1").last("limit 1");
            firstDistributionAttendanceFormula = Linq.of(firstDistributionAttendanceFormulas).where(t -> t.getFormulaType().equals("1")).firstOrDefault();
        }
        //FirstDistributionAttendanceFormula firstDistributionAttendanceFormula = this.getOne(qr);
        if (firstDistributionAttendanceFormula == null) {
            firstDistributionAttendanceFormula = Linq.of(firstDistributionAttendanceFormulas).where(t -> t.getFormulaType().equals("1")).firstOrDefault();
//            firstDistributionAttendanceFormula = this.getOne(new QueryWrapper<FirstDistributionAttendanceFormula>()
//                    .eq("plan_id", planId)
//                    .eq("formula_type", "1")
//                    .eq("dt", kpiUserAttendance.getPeriod())
//                    .last("limit 1"));
        }
        try {
            String expression = firstDistributionAttendanceFormula.getAttendanceFormula();
            ValidatorResultVo vo = getVerificationIndex2(expression, kpiUserAttendance, firstDistributionAccountFormulaParams, costUserAttendanceCustomFields);
            return vo.getResult();
        } catch (Exception e) {
            return "0.0";
        }
    }

    @Override
    public List<FirstDistributionAccountFormulaDto> formulaList(QueryWrapper<FirstDistributionAttendanceFormula> wrapper) {
        List<FirstDistributionAccountFormulaDto> rstList = new ArrayList<>();
        FirstDistributionAccountFormulaDto firstDistributionAccountFormulaDto1 = new FirstDistributionAccountFormulaDto();
        firstDistributionAccountFormulaDto1.setId(1L);
        firstDistributionAccountFormulaDto1.setPlanId(1L);
        firstDistributionAccountFormulaDto1.setPlanName(SJCQ);
        FirstDistributionAccountFormulaDto firstDistributionAccountFormulaDto2 = new FirstDistributionAccountFormulaDto();
        firstDistributionAccountFormulaDto2.setId(2L);
        firstDistributionAccountFormulaDto2.setPlanId(2L);
        firstDistributionAccountFormulaDto2.setPlanName(YCXCQ);
        rstList.add(firstDistributionAccountFormulaDto1);
        rstList.add(firstDistributionAccountFormulaDto2);
        return rstList;
    }

    @Override
    public R saveData(FirstDistributionAttendanceFormula firstDistributionAttendanceFormula) {
        List<FirstDistributionAttendanceFormula> list = list(new QueryWrapper<FirstDistributionAttendanceFormula>().eq("unit", firstDistributionAttendanceFormula.getUnitName()));
        if (ObjectUtil.isNotEmpty(list)) {
            return R.failed("该科室单元下的公式已经存在: " + firstDistributionAttendanceFormula.getUnitName());
        }
        return R.ok(save(firstDistributionAttendanceFormula));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R updateData(FirstDistributionAttendanceFormula firstDistributionAttendanceFormula) {
        // 检查科室单元是否有重复
        if (ObjectUtil.isNotEmpty(firstDistributionAttendanceFormula.getFormulaType()) && firstDistributionAttendanceFormula.getFormulaType().equals("2")) { // 2为定制科室
            LambdaQueryWrapper<FirstDistributionAttendanceFormula> qr = new LambdaQueryWrapper<>();
            qr.eq(FirstDistributionAttendanceFormula::getPlanId, firstDistributionAttendanceFormula.getPlanId());
            qr.eq(FirstDistributionAttendanceFormula::getDt, firstDistributionAttendanceFormula.getDt());
            qr.ne(FirstDistributionAttendanceFormula::getId, firstDistributionAttendanceFormula.getId());
            List<FirstDistributionAttendanceFormula> list = list(qr);
            for (FirstDistributionAttendanceFormula item : list) {
                if (StringUtils.isNotEmpty(item.getUnitName()) &&
                        (item.getUnitName().contains(firstDistributionAttendanceFormula.getUnitName()) ||
                                firstDistributionAttendanceFormula.getUnitName().contains(item.getUnitName()))) {
                    return R.failed("该科室单元下的公式已经存在: " + item.getUnitName());
                }
            }
        }
        firstDistributionAttendanceFormula.setDescription(firstDistributionAttendanceFormula.getAttendanceFormula());
        firstDistributionAttendanceFormula.setPlanName(firstDistributionAttendanceFormula.getParamList().get(0).getParamName());
        return R.ok(saveOrUpdate(firstDistributionAttendanceFormula));
    }

    @Override
    public List<FirstDistributionAttendanceFormula> listData(QueryWrapper<FirstDistributionAttendanceFormula> wrapper) {
        List<FirstDistributionAttendanceFormula> rtnList = list(wrapper);
        if (CollectionUtils.isEmpty(rtnList)) {
            return rtnList;
        }
        List<CostUserAttendanceCustomFields> list = costUserAttendanceCustomFieldsService
                .list(new LambdaQueryWrapper<CostUserAttendanceCustomFields>().eq(CostUserAttendanceCustomFields::getDt, rtnList.get(0).getDt()));
        for (FirstDistributionAttendanceFormula item : rtnList) {
            String formula = item.getAttendanceFormula();
            List<String> keys = expressionQuantity(formula);
            List<FirstDistributionAccountFormulaDto> paramList = new ArrayList<>();
            for (String key : keys) {
                FirstDistributionAccountFormulaDto firstDistributionAccountFormulaDto = new FirstDistributionAccountFormulaDto();
                FirstDistributionAccountFormulaParam formulaParam = firstDistributionAccountFormulaParamService.getOne(new QueryWrapper<FirstDistributionAccountFormulaParam>().eq("param_key", key));
                if (key.equals(KQZTS)) {
                    firstDistributionAccountFormulaDto.setParamKey(KQZTS);
                    firstDistributionAccountFormulaDto.setParamName("考勤组天数");
                    firstDistributionAccountFormulaDto.setId(0L);
                    paramList.add(firstDistributionAccountFormulaDto);
                } else if (formulaParam == null) {
                    // 自定义字段
                    CostUserAttendanceCustomFields costUserAttendanceCustomFields = Linq.of(list).where(t -> t.getCode().equals(key)).firstOrDefault();
//                    CostUserAttendanceCustomFields costUserAttendanceCustomFields =
//                            costUserAttendanceCustomFieldsService.getOne(new LambdaQueryWrapper<CostUserAttendanceCustomFields>()
//                                    .eq(CostUserAttendanceCustomFields::getCode, key).last("limit 1"));
                    if (costUserAttendanceCustomFields == null) {
                        continue;
                    }
                    firstDistributionAccountFormulaDto.setId(costUserAttendanceCustomFields.getId());
                    firstDistributionAccountFormulaDto.setParamKey(costUserAttendanceCustomFields.getCode());
                    firstDistributionAccountFormulaDto.setParamName(costUserAttendanceCustomFields.getName());
                    paramList.add(firstDistributionAccountFormulaDto);
                } else {
                    BeanUtils.copyProperties(formulaParam, firstDistributionAccountFormulaDto);
                    paramList.add(firstDistributionAccountFormulaDto);
                }
                item.setParamList(paramList);
            }
        }
        return rtnList;
    }

    /**
     * 此方法用于校验计算核算指标的值
     *
     * @param expression 表达式
     */
    public ValidatorResultVo getVerificationIndex(String expression, CostUserAttendance costUserAttendance) {
        ValidatorResultVo vo = new ValidatorResultVo();
        Map<String, Double> map = new HashMap<>();
        // 拿到指标公式解析,获取到key集合
        Set<String> keys = ExpressionCheckHelper.expressionQuantity(expression);
        // 遍历key,拿到配置项
        for (String key : keys) {
            // 根据key去查询核算配置项
            FirstDistributionAccountFormulaParam firstDistributionAccountFormulaParam =
                    firstDistributionAccountFormulaParamService.getOne(new QueryWrapper<FirstDistributionAccountFormulaParam>().eq("param_key", key));
            // 查询到参数项
            if (firstDistributionAccountFormulaParam != null) {
                // 如果是配置项,则直接取值 构造计算map (目前版本没有用到)
                map.put(key, Double.valueOf(firstDistributionAccountFormulaParam.getParamValue()));
            } else if (key.equals(KQZTS)) {// 如果是考勤组天数,则直接取值
                // 构造计算map
                map.put(key, costUserAttendance.getAttendanceGroupDays().doubleValue());
            } else if (key.equals(SJCQTS)) {// 如果是实际出勤天数,则直接取值
                // 构造计算map
                map.put(key, costUserAttendance.getAttendDays().doubleValue());
            } else if (key.equals(YCXCQTS)) {// 如果是一次性出勤天数,则直接取值
                // 构造计算map
                map.put(key, costUserAttendance.getOneKpiAttendDays().doubleValue());
            } else {// 如果是自定义字段，则从考勤表中取值
                List<CustomFieldVO> customFieldVOS = costUserAttendance.getCustomFieldList();
                try {
                    CostUserAttendanceCustomFields costUserAttendanceCustomField =
                            costUserAttendanceCustomFieldsService.getOne(new LambdaQueryWrapper<CostUserAttendanceCustomFields>().eq(CostUserAttendanceCustomFields::getCode, key)
                                    .eq(CostUserAttendanceCustomFields::getDt, costUserAttendance.getDt()).last("limit 1"));
                    for (CustomFieldVO item : customFieldVOS) {
                        if (item.getId().equals(costUserAttendanceCustomField.getColumnId())) {
                            map.put(key, NumberUtil.parseDouble(item.getNum()));
                            break;
                        } else {
                            map.put(key, Double.valueOf("0"));
                        }
                    }
                } catch (Exception e) {
                    log.error("自定义字段解析异常,抛弃", e);
                }
            }
        }
        // 计算值
        String newExpression = expression.replace("%", "/100");
        // todo 替换为通用方法 CommonUtils.caclByEval(expression);
        try {
            vo.setResult(ExpressionCheckHelper.checkAndCalculate(map, newExpression, null, 6, null));
        } catch (Exception e) {
            log.error("计算异常", e);
            vo.setResult("0.0");
        }
        return vo;
    }

    /**
     * 此方法用于校验计算核算指标的值
     *
     * @param expression 表达式
     */
    public ValidatorResultVo getVerificationIndex2(String expression, KpiUserAttendance kpiUserAttendance
            , List<FirstDistributionAccountFormulaParam> firstDistributionAccountFormulaParams, List<CostUserAttendanceCustomFields> costUserAttendanceCustomFields) {
        ValidatorResultVo vo = new ValidatorResultVo();
        Map<String, Double> map = new HashMap<>();
        // 拿到指标公式解析,获取到key集合
        Set<String> keys = ExpressionCheckHelper.expressionQuantity(expression);
        // 遍历key,拿到配置项
        for (String key : keys) {
            // 根据key去查询核算配置项
            FirstDistributionAccountFormulaParam firstDistributionAccountFormulaParam =
                    Linq.of(firstDistributionAccountFormulaParams).where(t -> t.getParamKey().equals(key)).firstOrDefault();
//            FirstDistributionAccountFormulaParam firstDistributionAccountFormulaParam =
//                    firstDistributionAccountFormulaParamService.getOne(new QueryWrapper<FirstDistributionAccountFormulaParam>().eq("param_key", key));
            // 查询到参数项
            if (firstDistributionAccountFormulaParam != null) {
                // 如果是配置项,则直接取值 构造计算map (目前版本没有用到)
                map.put(key, Double.valueOf(firstDistributionAccountFormulaParam.getParamValue()));
            } else if (key.equals(KQZTS)) {// 如果是考勤组天数,则直接取值
                // 构造计算map
                map.put(key, kpiUserAttendance.getAttendanceGroupDays().doubleValue());
            } else if (key.equals(SJCQTS)) {// 如果是实际出勤天数,则直接取值
                // 构造计算map
                map.put(key, kpiUserAttendance.getAttendDays().doubleValue());
            } else if (key.equals(YCXCQTS)) {// 如果是一次性出勤天数,则直接取值
                // 构造计算map
                map.put(key, kpiUserAttendance.getOneKpiAttendDays().doubleValue());
            } else {// 如果是自定义字段，则从考勤表中取值
                List<CustomFieldVO> customFieldVOS = kpiUserAttendance.getCustomFieldList();
                try {
                    CostUserAttendanceCustomFields costUserAttendanceCustomField =
                            Linq.of(costUserAttendanceCustomFields).where(t -> t.getCode().equals(key) && t.getDt()
                                    .equals(String.valueOf(kpiUserAttendance.getPeriod()))).firstOrDefault();
//                    CostUserAttendanceCustomFields costUserAttendanceCustomField =
//                            costUserAttendanceCustomFieldsService.getOne(new LambdaQueryWrapper<CostUserAttendanceCustomFields>().eq(CostUserAttendanceCustomFields::getCode, key)
//                                    .eq(CostUserAttendanceCustomFields::getDt, kpiUserAttendance.getPeriod()).last("limit 1"));
                    for (CustomFieldVO item : customFieldVOS) {
                        if (item.getId().equals(costUserAttendanceCustomField.getColumnId())) {
                            map.put(key, NumberUtil.parseDouble(item.getNum()));
                            break;
                        } else {
                            map.put(key, Double.valueOf("0"));
                        }
                    }
                } catch (Exception e) {
                    log.error("自定义字段解析异常,抛弃", e);
                }
            }
        }
        // 计算值
        String newExpression = expression.replace("%", "/100");
        // todo 替换为通用方法 CommonUtils.caclByEval(expression);
        try {
//            for (Map.Entry<String, Double> entry : map.entrySet()) {
//                System.out.println("key = " + entry.getKey() + ", value = " + entry.getValue());
//            }
            vo.setResult(ExpressionCheckHelper.checkAndCalculate(map, newExpression, null, 6, null));
        } catch (Exception e) {
            log.error("计算异常", e);
            vo.setResult("0.0");
        }
        return vo;
    }
}
