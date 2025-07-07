package com.hscloud.hs.cost.account;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.common.utils.CollectionUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bestvike.linq.Linq;
import com.hscloud.hs.cost.account.constant.enums.kpi.CodePrefixEnum;
import com.hscloud.hs.cost.account.mapper.kpi.KpiCategoryMapper;
import com.hscloud.hs.cost.account.mapper.kpi.KpiUserAttendanceMapper;
import com.hscloud.hs.cost.account.model.dto.kpi.*;
import com.hscloud.hs.cost.account.model.entity.kpi.*;
import com.hscloud.hs.cost.account.model.vo.userAttendance.CustomFieldVO;
import com.hscloud.hs.cost.account.service.impl.kpi.KpiAccountTaskService;
import com.hscloud.hs.cost.account.service.kpi.CommCodeService;
import com.hscloud.hs.cost.account.utils.kpi.InvokeUtil;
import com.pig4cloud.pigx.admin.api.entity.SysDictItem;
import com.pig4cloud.pigx.admin.api.feign.RemoteDictService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@SpringBootTest
public class CodeTest {

    @Autowired
    private CommCodeService commCodeService;
    @Autowired
    private KpiCategoryMapper kpiCategoryMapper;
    @Autowired
    private RemoteDictService remoteDictService;
    @Autowired
    private KpiUserAttendanceMapper kpiUserAttendanceMapper;
    @Autowired
    private KpiAccountTaskService kpiAccountTaskService;

    @Test
    public void test22() {
        List<KpiCategory> li = new ArrayList<>();
        KpiCategory category = new KpiCategory();
        category.setCategoryCode("111");
        li.add(category);
        KpiCategory category2 = new KpiCategory();
        category2.setCategoryCode("222");
        li.add(category2);
        kpiCategoryMapper.insertBatchSomeColumn(li);
    }

    @Test
    public void test23() {
        List<SysDictItem> pmcKpiCalculateGrouping = remoteDictService.getDictByType("kpi_calculate_grouping").getData();
        System.out.println(pmcKpiCalculateGrouping);
    }

    @Test
    public void test66() {
        List<UserIdAndDeptId> userDept = kpiUserAttendanceMapper.getUserDept();
        System.out.println("1");
    }

    @Test
    public void test() {
        String abc = commCodeService.commCode(CodePrefixEnum.ITEM);
        System.out.println(abc);
        System.out.println(abc);
    }

    public String convertToBase56(long decimalNumber) {
        StringBuilder base56 = new StringBuilder();
        while (decimalNumber > 0) {
            int remainder = (int) (decimalNumber % 56);
            char digitChar;
            if (remainder >= 10) {
                digitChar = (char) (remainder - 10 + 'A');
            } else {
                digitChar = (char) (remainder + '0');
            }
            base56.append(digitChar);
            decimalNumber /= 56;
        }
        return base56.reverse().toString(); // 使用StringBuilder的reverse方法翻转字符串
    }

    @Test
    public void test667() {
        String a = "{\n" +
                "    \"rangeType\": \"filter\",\n" +
                "    \"range\": {\n" +
                "        \"paramType\": \"15\",\n" +
                "        \"paramValues\": [\n" +
                "            {\n" +
                "                \"value\": \"外包\",\n" +
                "                \"label\": \"外包\"\n" +
                "            },\n" +
                "            {\n" +
                "                \"value\": \"测试\",\n" +
                "                \"label\": \"测试\"\n" +
                "            }\n" +
                "        ]\n" +
                "    },\n" +
                "    \"filter\": [\n" +
                "        {\n" +
                "            \"key\": \"gzxz\",\n" +
                "            \"value\": [\n" +
                "                {\n" +
                "                    \"value\": \"外包\",\n" +
                "                    \"label\": \"外包\"\n" +
                "                },\n" +
                "                {\n" +
                "                    \"value\": \"测试\",\n" +
                "                    \"label\": \"测试\"\n" +
                "                }\n" +
                "            ]\n" +
                "        }\n" +
                "    ],\n" +
                "    \"mapValues\": [\n" +
                "        {\n" +
                "            \"type\": \"system\",\n" +
                "            \"value\": \"0\",\n" +
                "            \"label\": \"是否拿奖金\",\n" +
                "            \"code\": \"reward\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"type\": \"system\",\n" +
                "            \"value\": \"0\",\n" +
                "            \"label\": \"奖金系数\",\n" +
                "            \"code\": \"rewardIndex\"\n" +
                "        }\n" +
                "    ]\n" +
                "}";
        String b = "{\n" +
                "    \"rangeType\": \"filter\",\n" +
                "    \"range\": {\n" +
                "        \"paramType\": \"12\",\n" +
                "        \"paramValues\": [\n" +
                "            {\n" +
                "                \"value\": \"GJ_RYLX011\",\n" +
                "                \"label\": \"药剂科主任\"\n" +
                "            },\n" +
                "            {\n" +
                "                \"value\": \"GJ_RYLX010\",\n" +
                "                \"label\": \"医技科主任\"\n" +
                "            },\n" +
                "            {\n" +
                "                \"value\": \"GJ_RYLX009\",\n" +
                "                \"label\": \"临床科主任\"\n" +
                "            }\n" +
                "        ]\n" +
                "    },\n" +
                "    \"filter\": [\n" +
                "        {\n" +
                "            \"key\": \"jobNature\",\n" +
                "            \"value\": [\n" +
                "                {\n" +
                "                    \"value\": \"在编\",\n" +
                "                    \"label\": \"外包\"\n" +
                "                },\n" +
                "                {\n" +
                "                    \"value\": \"测试\",\n" +
                "                    \"label\": \"测试\"\n" +
                "                }\n" +
                "            ]\n" +
                "        }\n" +
                "    ],\n" +
                "    \"mapValues\": [\n" +
                "        {\n" +
                "            \"type\": \"system\",\n" +
                "            \"value\": \"0\",\n" +
                "            \"label\": \"是否拿奖金\",\n" +
                "            \"code\": \"reward\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"type\": \"system\",\n" +
                "            \"value\": \"1\",\n" +
                "            \"label\": \"奖金系数\",\n" +
                "            \"code\": \"rewardIndex\"\n" +
                "        }\n" +
                "        {\n" +
                "            \"type\": \"custom\",\n" +
                "            \"value\": \"2000\",\n" +
                "            \"label\": \"中治室\",\n" +
                "            \"code\": \"1726432234812055553\"\n" +
                "        }\n" +
                "    ]\n" +
                "}";
        List<KpiUserAttendance> rtnList = kpiUserAttendanceMapper.selectList(new LambdaQueryWrapper<KpiUserAttendance>().eq(KpiUserAttendance::getPeriod, 202411L));
        KpiUserCalculationRuleDto kpiUserCalculationRuleDto = JSON.parseObject(b, KpiUserCalculationRuleDto.class);
        List<KpiUserCalculationRuleValueDto> mapValues = kpiUserCalculationRuleDto.getMapValues();
        List<KpiMemberCopy> kpiMemberCopies = kpiAccountTaskService.planMemberCopy(202411L);
        List<KpiUserAttendanceCopy> kpiUserAttendanceCopies = kpiAccountTaskService.planUserCopy2(202411L);
        List<KpiAccountUnitCopy> kpiAccountUnitCopies = kpiAccountTaskService.planUnitCopy2();
        List<Long> userIdsList = new ArrayList<>();
        String rangeType = kpiUserCalculationRuleDto.getRangeType();
        if (rangeType.equals("range")) {
            KpiFormulaDto2.FieldListDTO d = new KpiFormulaDto2.FieldListDTO();
            BeanUtil.copyProperties(kpiUserCalculationRuleDto.getRange(), d);
            List<Long> memberListComm = kpiAccountTaskService.getMemberListComm(d, kpiMemberCopies, kpiUserAttendanceCopies, kpiAccountUnitCopies);
            userIdsList.addAll(memberListComm);
        }
        if (rangeType.equals("filter")) {
            List<KpiUserCalculationRuleFilterDto> filter = kpiUserCalculationRuleDto.getFilter();
            for (KpiUserCalculationRuleFilterDto filterDto : filter) {
                //字段名
                String key = filterDto.getKey();
                List<KpiFormulaDto2.MemberListDTO> value = filterDto.getValue();
                if (CollectionUtils.isNotEmpty(value)) {
                    String Matching_values = value.get(0).getValue();
                    List<KpiUserAttendance> list = Linq.of(rtnList).where(t -> {
                        try {
                            return Objects.equals(InvokeUtil.getGetMethod(t, key), Matching_values);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }).toList();
                    if (CollectionUtils.isNotEmpty(list)) {
                        List<Long> list1 = Linq.of(list).select(KpiUserAttendance::getUserId).toList();
                        userIdsList.addAll(list1);
                    }
                }
            }
        }
        //String jsonString = JSON.toJSONString(userIdsList);
        //在选定人员范围内进行赋值
        for (KpiUserAttendance costUserAttendance : rtnList) {
            if (CollectionUtils.isNotEmpty(userIdsList) && userIdsList.contains(costUserAttendance.getUserId())) {
                Map<String, List<KpiUserCalculationRuleValueDto>> collect = mapValues.stream().collect(Collectors.groupingBy(KpiUserCalculationRuleValueDto::getType));
                //custom 自定义字段 - system 系统字段
                ArrayList<String> types = new ArrayList<>(collect.keySet());
                for (String type : types) {
                    List<KpiUserCalculationRuleValueDto> mapValuesChild = collect.get(type);
                    if (type.equals("custom")) {
                        //找到自定义字段的id 根据得出的user_id去赋值
                        // 自定义字段数值读取
                        String oldCustomFields = costUserAttendance.getCustomFields();
                        String oldStr = "[" + oldCustomFields.replaceAll("}(?=,)", "},") + "]";
                        List<CustomFieldVO> oldCustomFieldVOS = JSON.parseArray(oldStr, CustomFieldVO.class);
                        for (KpiUserCalculationRuleValueDto child : mapValuesChild) {
                            //自定义字段ID
                            String code = child.getCode();
                            try {
                                for (CustomFieldVO oldCustomFieldVO : oldCustomFieldVOS) {
                                    if (oldCustomFieldVO.getId().equals(code)) {
                                        String newValue = child.getValue();
                                        oldCustomFieldVO.setNum(newValue);
                                    }
                                }
                            } catch (Exception e) {
                                //log.info("生成自定义字段变更日志失败", e);
                            }
                        }
                        //改回原本的json格式
                        String jsonString = JSON.toJSONString(oldCustomFieldVOS);
                        jsonString = jsonString.replace("[", "");
                        jsonString = jsonString.replace("]", "");
                        costUserAttendance.setCustomFields(jsonString);
                    }
                    if (type.equals("system")) {
                        //系统字段数值读取
                        for (KpiUserCalculationRuleValueDto child : mapValuesChild) {
                            //自定义字段名称
                            String code = child.getCode();
                            try {
                                Class<? extends KpiUserAttendance> tClass = costUserAttendance.getClass();
                                Field declaredField = tClass.getDeclaredField(code);
                                String field_type = declaredField.getType().getSimpleName();
                                declaredField.setAccessible(true);
                                if ("String".equals(field_type)) {
                                    declaredField.set(costUserAttendance, child.getValue());
                                }
                                if ("Integer".equals(field_type)) {
                                    declaredField.set(costUserAttendance, Integer.parseInt(child.getValue()));
                                }
                                if ("Double".equals(field_type)) {
                                    declaredField.set(costUserAttendance, Double.parseDouble(child.getValue()));
                                }
                                if ("Long".equals(field_type)) {
                                    declaredField.set(costUserAttendance, Long.parseLong(child.getValue()));
                                }
                                if ("BigDecimal".equals(field_type)) {
                                    declaredField.set(costUserAttendance, new BigDecimal(child.getValue()));
                                }
                            } catch (Exception e) {
                                System.out.println("e" + e.getMessage());
                            }
                        }
                    }
                }
            }
        }
        System.out.println("1");
    }
}
