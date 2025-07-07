package com.hscloud.hs.cost.account;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.common.utils.CollectionUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bestvike.linq.Linq;
import com.hscloud.hs.cost.account.constant.enums.kpi.CodePrefixEnum;
import com.hscloud.hs.cost.account.mapper.kpi.KpiCategoryMapper;
import com.hscloud.hs.cost.account.mapper.kpi.KpiUserAttendanceMapper;
import com.hscloud.hs.cost.account.mapper.kpi.KpiUserCalculationRuleMapper;
import com.hscloud.hs.cost.account.model.dto.kpi.*;
import com.hscloud.hs.cost.account.model.dto.userAttendance.AccountUnitDto;
import com.hscloud.hs.cost.account.model.dto.userAttendance.EmpAttendMonthDto;
import com.hscloud.hs.cost.account.model.entity.kpi.*;
import com.hscloud.hs.cost.account.model.vo.kpi.KpiKeyValueVO;
import com.hscloud.hs.cost.account.model.vo.userAttendance.CustomFieldVO;
import com.hscloud.hs.cost.account.service.impl.kpi.KpiAccountTaskService;
import com.hscloud.hs.cost.account.service.kpi.CommCodeService;
import com.hscloud.hs.cost.account.utils.DmoUtil;
import com.hscloud.hs.cost.account.utils.kpi.InvokeUtil;
import com.pig4cloud.pigx.admin.api.entity.SysDictItem;
import com.pig4cloud.pigx.admin.api.feign.RemoteDictService;
import com.pig4cloud.pigx.admin.api.feign.RemoteUserService;
import com.pig4cloud.pigx.admin.api.vo.UserCoreVo;
import com.pig4cloud.pigx.common.core.constant.SecurityConstants;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.xml.DomUtils;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@SpringBootTest
public class KpiUserAttendanceTest {

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

    @Autowired
    private KpiUserCalculationRuleMapper kpiUserCalculationRuleMapper;

    @Autowired
    private RemoteUserService remoteUserService;


    @Autowired
    private DmoUtil dmoUtil;

    @Test
    public void test668() {
        //"        \"work_nature_nm\": \"在编\",\n" +
        String a = " [\n" +
                "      {\n" +
                "        \"id\": \"1864339083241734145\",\n" +
                "        \"dt\": \"202411\",\n" +
                "        \"name\": \"徐弋\",\n" +
                "        \"job_name\": \"\",\n" +
                "        \"title_name\": \"主管护师\",\n" +
                "        \"work_nature_nm\": \"在编\",\n" +
                "        \"post_name\": \"行政（护理）\",\n" +
                "        \"reward_rmk\": \"1\",\n" +
                "        \"reward_rate\": \"1.0\",\n" +
                "        \"no_reward_reason\": \"\",\n" +
                "        \"organization_name\": \"护理部\",\n" +
                "        \"group_name\": \"护理部\",\n" +
                "        \"attend_days\": \"30\",\n" +
                "        \"tab_infos\": \"{\\\"id\\\": 1663783100645236738,\\\"name\\\":\\\"出勤\\\",\\\"num\\\": 20.0}$|{\\\"id\\\": 1663782552663613441,\\\"name\\\":\\\"调休（天）\\\",\\\"num\\\": 1.0}$|{\\\"id\\\":1663782654383874049,\\\"name\\\":\\\"公休\\\",\\\"num\\\":9.0}\",\n" +
                "        \"organization_cd\": \"116\",\n" +
                "        \"job_number\": \"31603\"\n" +
                "      },\n" +
                "{\n" +
                "        \"id\": \"1864339084353224711\",\n" +
                "        \"dt\": \"202411\",\n" +
                "        \"name\": \"唐嘉宜\",\n" +
                "        \"job_name\": \"\",\n" +
                "        \"title_name\": \"中药师\",\n" +
                "        \"work_nature_nm\": \"外包（明贝）\",\n" +
                "        \"post_name\": \"药学\",\n" +
                "        \"reward_rmk\": \"1\",\n" +
                "        \"reward_rate\": \"1.0\",\n" +
                "        \"no_reward_reason\": \"\",\n" +
                "        \"organization_name\": \"中药房\",\n" +
                "        \"group_name\": \"中药房（编外）\",\n" +
                "        \"attend_days\": \"30\",\n" +
                "        \"tab_infos\": \"{\\\"id\\\": 1663783100645236738,\\\"name\\\":\\\"出勤\\\",\\\"num\\\": 24.0}$|{\\\"id\\\":1663782654383874049,\\\"name\\\":\\\"公休\\\",\\\"num\\\":6.0}\",\n" +
                "        \"organization_cd\": \"191\",\n" +
                "        \"job_number\": \"41334\"\n" +
                "      },"+
                "      {\n" +
                "        \"id\": \"1864339083245928459\",\n" +
                "        \"dt\": \"202411\",\n" +
                "        \"name\": \"毛丹旦\",\n" +
                "        \"job_name\": \"护理部副主任兼护理教育科科护士长\",\n" +
                "        \"title_name\": \"主任护师\",\n" +
                "        \"work_nature_nm\": \"在编\",\n" +
                "        \"post_name\": \"行政（护理）\",\n" +
                "        \"reward_rmk\": \"1\",\n" +
                "        \"reward_rate\": \"1.0\",\n" +
                "        \"no_reward_reason\": \"\",\n" +
                "        \"organization_name\": \"护理部\",\n" +
                "        \"group_name\": \"护理部\",\n" +
                "        \"attend_days\": \"30\",\n" +
                "        \"tab_infos\": \"{\\\"id\\\":1663782654383874049,\\\"name\\\":\\\"公休\\\",\\\"num\\\":9.0}$|{\\\"id\\\": 1717358751507357698,\\\"name\\\":\\\"总值班\\\",\\\"num\\\": 1.0}$|{\\\"id\\\": 1663783100645236738,\\\"name\\\":\\\"出勤\\\",\\\"num\\\": 21.0}\",\n" +
                "        \"organization_cd\": \"116\",\n" +
                "        \"job_number\": \"30701\"\n" +
                "      }]";
        List<EmpAttendMonthDto> tempList = JSON.parseArray(a, EmpAttendMonthDto.class);
        List<UserCoreVo> data = remoteUserService.listMainDetails(SecurityConstants.FROM_IN).getData();
        List<KpiUserCalculationRule> kpiUserCalculationRules = kpiUserCalculationRuleMapper
                .selectList(new LambdaQueryWrapper<KpiUserCalculationRule>().eq(KpiUserCalculationRule::getStatus, "0")
                        .eq(KpiUserCalculationRule::getBusiType, "1"));
        List<KpiMemberCopy> kpiMemberCopies = kpiAccountTaskService.planMemberCopy(202411L);
        List<KpiUserAttendanceCopy> kpiUserAttendanceCopies = kpiAccountTaskService.planUserCopy2(202411L);
        List<KpiAccountUnitCopy> kpiAccountUnitCopies = kpiAccountTaskService.planUnitCopy2();
        List<KpiUserAttendance> rtnList = new ArrayList<>();
        Linq.of(tempList).groupBy(x -> new KpiKeyValueVO(x.getJobNumber(), x.getGroupName())).forEach(x -> {
            EmpAttendMonthDto empAttendMonth = dmoUtil.getNotNullEntity(x.toList());

            KpiUserAttendance costUserAttendance = new KpiUserAttendance();
            costUserAttendance.setPeriod(Long.valueOf(empAttendMonth.getDt()));
            costUserAttendance.setEmpId(empAttendMonth.getJobNumber());
            //UserInfo userInfo = remoteUserService.allInfoByJobNumber(empAttendMonth.getJobNumber()).getData();
            if (StringUtils.isNotEmpty(empAttendMonth.getJobNumber())) {
                UserCoreVo userCoreVo = Linq.of(data).firstOrDefault(t -> empAttendMonth.getJobNumber().equals(t.getJobNumber()));
                if (userCoreVo != null) {
                    costUserAttendance.setUserId(userCoreVo.getUserId());
                }
            }
            costUserAttendance.setEmpName(empAttendMonth.getName());
            costUserAttendance.setJobNature(empAttendMonth.getWorkNatureNm());
            costUserAttendance.setAttendanceGroup(empAttendMonth.getGroupName());
            costUserAttendance.setDutiesName(empAttendMonth.getJobName());
            costUserAttendance.setTitles(empAttendMonth.getTitleName());
            //科室单元封装
            List<AccountUnitDto> accountUnitIdAndNameDtoList = new ArrayList<>();
            AccountUnitDto accountUnitIdAndNameDto = new AccountUnitDto();
            if (StringUtils.isNotEmpty(empAttendMonth.getOrganizationCd())) {
                accountUnitIdAndNameDto.setId(empAttendMonth.getOrganizationCd());
                costUserAttendance.setAccountUnit(Long.valueOf(accountUnitIdAndNameDto.getId()));
            }
//            else
//            {
//                costUserAttendance.setAccountUnit(0L);
//            }
            //accountUnitIdAndNameDto.setId(StringUtil.isNullOrEmpty(empAttendMonth.getOrganizationCd()) ? empAttendMonth.getOrganizationCd() : "");
            accountUnitIdAndNameDto.setName(empAttendMonth.getOrganizationName());
            accountUnitIdAndNameDtoList.add(accountUnitIdAndNameDto);
            String accountUnitJsonString = JSON.toJSONString(accountUnitIdAndNameDtoList);
            // costUserAttendance.setAccountUnit(accountUnitJsonString);
            costUserAttendance.setAttendanceGroupDays(new BigDecimal(empAttendMonth.getAttendDays()));
            costUserAttendance.setPost(empAttendMonth.getPostName());
            costUserAttendance.setReward(empAttendMonth.getRewardRmk().equals("1") ? "1" : "0");
            costUserAttendance.setRewardIndex(new BigDecimal(empAttendMonth.getRewardRate()));
            costUserAttendance.setNoRewardReason(empAttendMonth.getNoRewardReason());
            costUserAttendance.setDeptCode(empAttendMonth.getOrganizationCd());
            costUserAttendance.setDeptName(empAttendMonth.getOrganizationName());
            //自定义字段
            String customFields = empAttendMonth.getTabInfos();
            costUserAttendance.setOriginCustomFields(customFields);
            //System.out.println("printing custom fields..." + customFields);
            costUserAttendance.setCustomFields(customFields.replace("$|", ","));
            rtnList.add(costUserAttendance);
        });
        if (!kpiUserCalculationRules.isEmpty()) {
            //将range转化成filter
            List<KpiUserCalculationRuleDto> rulelist=new ArrayList<>();
            for (KpiUserCalculationRule kpiUserCalculationRule : kpiUserCalculationRules) {
                KpiUserCalculationRuleDto kpiUserCalculationRuleDto = JSON.parseObject(kpiUserCalculationRule.getRule(), KpiUserCalculationRuleDto.class);
                if (kpiUserCalculationRuleDto.getRangeType().equals("range")) {
                    KpiFormulaDto2.FieldListDTO d = new KpiFormulaDto2.FieldListDTO();
                    BeanUtil.copyProperties(kpiUserCalculationRuleDto.getRange(), d);
                    List<Long> memberListComm = kpiAccountTaskService.getMemberListComm(d, kpiMemberCopies, kpiUserAttendanceCopies, kpiAccountUnitCopies);
                    List<KpiUserCalculationRuleFilterDto> in_list=new ArrayList<>();
                    KpiUserCalculationRuleFilterDto kpiUserCalculationRuleFilterDto = new KpiUserCalculationRuleFilterDto();
                    kpiUserCalculationRuleFilterDto.setKey("userId");
                    List<KpiFormulaDto2.MemberListDTO> list = Linq.of(memberListComm).select(t -> {
                        KpiFormulaDto2.MemberListDTO dtoin = new KpiFormulaDto2.MemberListDTO();
                        dtoin.setValue(t.toString());
                        return dtoin;
                    }).toList();
                    kpiUserCalculationRuleFilterDto.setValue(list);
                    in_list.add(kpiUserCalculationRuleFilterDto);
                    kpiUserCalculationRuleDto.setFilter(in_list);
                }
                rulelist.add(kpiUserCalculationRuleDto);
            }
            for (KpiUserAttendance costUserAttendance : rtnList)
            {
                for(KpiUserCalculationRuleDto rule :rulelist) {
                    if (dmoUtil.isMatch(costUserAttendance, rule)) {
                        for (KpiUserCalculationRuleValueDto type : rule.getMapValues()) {
                            if (type.getType().equals("custom")) {
                                //找到自定义字段的id 根据得出的user_id去赋值
                                // 自定义字段数值读取
                                String oldCustomFields = costUserAttendance.getCustomFields();
                                String oldStr = "[" + oldCustomFields.replaceAll("}(?=,)", "},") + "]";
                                List<CustomFieldVO> oldCustomFieldVOS = JSON.parseArray(oldStr, CustomFieldVO.class);
                                try {
                                    for (CustomFieldVO oldCustomFieldVO : oldCustomFieldVOS) {
                                        if (oldCustomFieldVO.getId().equals(type.getCode())) {
                                            String newValue = type.getValue();
                                            oldCustomFieldVO.setNum(newValue);
                                        }
                                    }
                                } catch (Exception e) {
                                    //log.info("生成自定义字段变更日志失败", e);
                                }
                                //改回原本的json格式
                                String jsonString = JSON.toJSONString(oldCustomFieldVOS);
                                jsonString = jsonString.replace("[", "");
                                jsonString = jsonString.replace("]", "");
                                costUserAttendance.setCustomFields(jsonString);
                            }
                            if (type.getType().equals("system")) {
                                //系统字段数值读取
                                //自定义字段名称
                                String code = type.getCode();
                                try {
                                    Class<? extends KpiUserAttendance> tClass = costUserAttendance.getClass();
                                    Field declaredField = tClass.getDeclaredField(code);
                                    String field_type = declaredField.getType().getSimpleName();
                                    declaredField.setAccessible(true);
                                    if ("String".equals(field_type)) {
                                        declaredField.set(costUserAttendance, type.getValue());
                                    }
                                    if ("Integer".equals(field_type)) {
                                        declaredField.set(costUserAttendance, Integer.parseInt(type.getValue()));
                                    }
                                    if ("Double".equals(field_type)) {
                                        declaredField.set(costUserAttendance, Double.parseDouble(type.getValue()));
                                    }
                                    if ("Long".equals(field_type)) {
                                        declaredField.set(costUserAttendance, Long.parseLong(type.getValue()));
                                    }
                                    if ("BigDecimal".equals(field_type)) {
                                        declaredField.set(costUserAttendance, new BigDecimal(type.getValue()));
                                    }
                                } catch (Exception e) {
                                    System.out.println("e" + e.getMessage());
                                }
                            }
                        }
                    }
                }
            }
        }
        System.out.println("1");

    }
}
