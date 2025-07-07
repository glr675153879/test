package com.hscloud.hs.cost.account.utils;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bestvike.linq.IGrouping;
import com.bestvike.linq.Linq;
import com.hscloud.hs.cost.account.constant.DmoProperties;
import com.hscloud.hs.cost.account.constant.UserProperties;
import com.hscloud.hs.cost.account.mapper.kpi.KpiUserCalculationRuleMapper;
import com.hscloud.hs.cost.account.model.dto.bi.IncomePerformancePayDTO;
import com.hscloud.hs.cost.account.model.dto.kpi.*;
import com.hscloud.hs.cost.account.model.dto.second.*;
import com.hscloud.hs.cost.account.model.dto.userAttendance.AccountUnitDto;
import com.hscloud.hs.cost.account.model.dto.userAttendance.DateDto;
import com.hscloud.hs.cost.account.model.dto.userAttendance.EmpAttendMonthDto;
import com.hscloud.hs.cost.account.model.dto.userAttendance.GetAttendanceDTO;
import com.hscloud.hs.cost.account.model.entity.dataReport.OdsHisUnidrgswedDboDrgsInfo;
import com.hscloud.hs.cost.account.model.entity.kpi.*;
import com.hscloud.hs.cost.account.model.entity.second.UnitTaskUser;
import com.hscloud.hs.cost.account.model.entity.userAttendance.CostUserAttendance;
import com.hscloud.hs.cost.account.model.entity.userAttendance.CostUserAttendanceCustomFields;
import com.hscloud.hs.cost.account.model.entity.userAttendance.DimMonthEmpIncome;
import com.hscloud.hs.cost.account.model.vo.MaterialChargeVo;
import com.hscloud.hs.cost.account.model.vo.kpi.KpiKeyValueVO;
import com.hscloud.hs.cost.account.model.vo.second.UserTypeVo;
import com.hscloud.hs.cost.account.model.vo.userAttendance.CustomFieldVO;
import com.hscloud.hs.cost.account.service.impl.kpi.KpiAccountTaskService;
import com.hscloud.hs.cost.account.service.impl.userAttendance.CostUserAttendanceCustomFieldsService;
import com.hscloud.hs.cost.account.utils.kpi.InvokeUtil;
import com.hscloud.hs.cost.account.utils.kpi.StringChangeUtil;
import com.pig4cloud.pigx.admin.api.feign.RemoteParamService;
import com.pig4cloud.pigx.admin.api.feign.RemoteThirdAccountUnitService;
import com.pig4cloud.pigx.admin.api.feign.RemoteUserService;
import com.pig4cloud.pigx.admin.api.vo.UserCoreVo;
import com.pig4cloud.pigx.common.core.constant.SecurityConstants;
import com.pig4cloud.pigx.common.datacenter.config.DataCenterProperties;
import com.pig4cloud.pigx.common.datacenter.service.DataCenterService;
import com.pig4cloud.pigx.common.datacenter.util.BaseResponse;
import io.netty.util.internal.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;


/**
 * 数据中台API
 */
@Component
@Slf4j
public class DmoUtil {

    private final static String EMP_ATTEND_MONTH = "emp_attend_month";
    private final static String GET_ATTENDANCE = "GET_ATTENDANCE";
    @Resource
    private DataCenterService dataCenterService;
    @Resource
    private DataCenterProperties dataCenterProperties;
    @Resource
    private RemoteParamService remoteParamService;
    @Resource
    private RemoteThirdAccountUnitService remoteThirdAccountUnitService;
    @Autowired
    private DmoProperties dmoProperties;
    @Autowired
    private UserProperties userProperties;
    @Autowired
    private RemoteUserService remoteUserService;
    @Autowired
    private KpiUserCalculationRuleMapper kpiUserCalculationRuleMapper;
    @Autowired
    private KpiAccountTaskService kpiAccountTaskService;
    @Autowired
    private CostUserAttendanceCustomFieldsService costUserAttendanceCustomFieldsService;

    public static void main(String[] args) {
        String str = "[{\n" +
                "      \"id\": \"1866362566872154134\",\n" +
                "      \"dt\": \"202411\",\n" +
                "      \"name\": \"徐长风\",\n" +
                "      \"work_nature_nm\": \"柔性引进\",\n" +
                "      \"post_name\": \"医生\",\n" +
                "      \"reward_rmk\": \"2\",\n" +
                "      \"reward_rate\": \"0.0\",\n" +
                "      \"organization_name\": \"肝病内科\",\n" +
                "      \"group_name\": \"肝病内科\",\n" +
                "      \"attend_days\": \"16\",\n" +
                "      \"tab_infos\": \"{\\\"id\\\": 1663783100645236738,\\\"name\\\":\\\"出勤\\\",\\\"num\\\": 1.0}$|{\\\"id\\\":1663782654383874049,\\\"name\\\":\\\"公休\\\",\\\"num\\\":15.0}\",\n" +
                "      \"organization_cd\": \"187\",\n" +
                "      \"job_number\": \"1473\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"id\": \"1866362566876348417\",\n" +
                "      \"dt\": \"202411\",\n" +
                "      \"name\": \"徐长风\",\n" +
                "      \"work_nature_nm\": \"柔性引进\",\n" +
                "      \"post_name\": \"医生\",\n" +
                "      \"reward_rmk\": \"2\",\n" +
                "      \"reward_rate\": \"0.0\",\n" +
                "      \"group_name\": \"肝病内科\",\n" +
                "      \"attend_days\": \"14\",\n" +
                "      \"tab_infos\": \"{\\\"id\\\":1663782654383874049,\\\"name\\\":\\\"公休\\\",\\\"num\\\":14.0}\",\n" +
                "      \"job_number\": \"1473\"\n" +
                "    }]";
        List<EmpAttendMonthDto> tempList = JSONObject.parseArray(str, EmpAttendMonthDto.class);

        List<KpiUserAttendance> rtnList = new ArrayList<>();
        Linq.of(tempList).groupBy(r -> new KpiKeyValueVO(r.getJobNumber(), r.getGroupName())).forEach(r -> {
            List<EmpAttendMonthDto> list = r.toList();

            EmpAttendMonthDto empAttendMonth = new EmpAttendMonthDto();
            list.forEach(x -> {
                if (StringUtil.isNullOrEmpty(empAttendMonth.getDt()) && !StringUtil.isNullOrEmpty(x.getDt())) {
                    empAttendMonth.setDt(x.getDt());
                }
                if (StringUtil.isNullOrEmpty(empAttendMonth.getJobNumber()) && !StringUtil.isNullOrEmpty(x.getJobNumber())) {
                    empAttendMonth.setJobNumber(x.getJobNumber());
                }
                if (StringUtil.isNullOrEmpty(empAttendMonth.getName()) && !StringUtil.isNullOrEmpty(x.getName())) {
                    empAttendMonth.setName(x.getName());
                }
                if (StringUtil.isNullOrEmpty(empAttendMonth.getJobName()) && !StringUtil.isNullOrEmpty(x.getJobName())) {
                    empAttendMonth.setJobName(x.getJobName());
                }
                if (StringUtil.isNullOrEmpty(empAttendMonth.getTitleName()) && !StringUtil.isNullOrEmpty(x.getTitleName())) {
                    empAttendMonth.setTitleName(x.getTitleName());
                }
                if (StringUtil.isNullOrEmpty(empAttendMonth.getWorkNatureNm()) && !StringUtil.isNullOrEmpty(x.getWorkNatureNm())) {
                    empAttendMonth.setWorkNatureNm(x.getWorkNatureNm());
                }
                if (StringUtil.isNullOrEmpty(empAttendMonth.getPostName()) && !StringUtil.isNullOrEmpty(x.getPostName())) {
                    empAttendMonth.setPostName(x.getPostName());
                }
                if (StringUtil.isNullOrEmpty(empAttendMonth.getRewardRmk()) && !StringUtil.isNullOrEmpty(x.getRewardRmk())) {
                    empAttendMonth.setRewardRmk(x.getRewardRmk());
                }
                if (StringUtil.isNullOrEmpty(empAttendMonth.getRewardRate()) && !StringUtil.isNullOrEmpty(x.getRewardRate())) {
                    empAttendMonth.setRewardRate(x.getRewardRate());
                }
                if (StringUtil.isNullOrEmpty(empAttendMonth.getNoRewardReason()) && !StringUtil.isNullOrEmpty(x.getNoRewardReason())) {
                    empAttendMonth.setNoRewardReason(x.getNoRewardReason());
                }
                if (StringUtil.isNullOrEmpty(empAttendMonth.getOrganizationName()) && !StringUtil.isNullOrEmpty(x.getOrganizationName())) {
                    empAttendMonth.setOrganizationName(x.getOrganizationName());
                }
                if (StringUtil.isNullOrEmpty(empAttendMonth.getOrganizationCd()) && !StringUtil.isNullOrEmpty(x.getOrganizationCd())) {
                    empAttendMonth.setOrganizationCd(x.getOrganizationCd());
                }
                if (StringUtil.isNullOrEmpty(empAttendMonth.getGroupName()) && !StringUtil.isNullOrEmpty(x.getGroupName())) {
                    empAttendMonth.setGroupName(x.getGroupName());
                }
                if (StringUtil.isNullOrEmpty(empAttendMonth.getAttendDays()) && !StringUtil.isNullOrEmpty(x.getAttendDays())) {
                    empAttendMonth.setAttendDays(Linq.of(list).select(q -> Long.parseLong(q.getAttendDays())).sumLong() + "");
                }
                if (StringUtil.isNullOrEmpty(empAttendMonth.getTabInfos()) && !StringUtil.isNullOrEmpty(x.getTabInfos())) {
                    empAttendMonth.setTabInfos(String.join("$|", Linq.of(list).select(q -> q.getTabInfos()).toList()));
                    if (!StringUtil.isNullOrEmpty(empAttendMonth.getTabInfos())) {
                        List<KpiAttendUserCustomFieldDto> fields = JSONObject.parseArray("[" + empAttendMonth.getTabInfos().replace("$|", ",") + "]", KpiAttendUserCustomFieldDto.class);
                        List<KpiAttendUserCustomFieldDto> li = Linq.of(fields).groupBy(f -> new KpiAttendUserCustomFieldDto(f.getName(), f.getId())).select(f -> {
                            KpiAttendUserCustomFieldDto dto = new KpiAttendUserCustomFieldDto(f.getKey().getName(), Linq.of(f.toList()).select(ff -> ff.getNum()).sumDecimal(), f.getKey().getId());
                            return dto;
                        }).toList();
                        String json = JSONObject.toJSONString(li);
                        empAttendMonth.setTabInfos(json.replace("[", "").replace("]", "").replace(",{\"id\"", "$|{\"id\""));
                    }
                }
            });

            KpiUserAttendance costUserAttendance = new KpiUserAttendance();


            costUserAttendance.setPeriod(Long.valueOf(empAttendMonth.getDt()));
            costUserAttendance.setEmpId(empAttendMonth.getJobNumber());
            //UserInfo userInfo = remoteUserService.allInfoByJobNumber(empAttendMonth.getJobNumber()).getData();
            costUserAttendance.setEmpName(empAttendMonth.getName());
            costUserAttendance.setJobNature(empAttendMonth.getWorkNatureNm());
            costUserAttendance.setAttendanceGroup(empAttendMonth.getGroupName());
            costUserAttendance.setDutiesName(empAttendMonth.getJobName());
            costUserAttendance.setTitles(empAttendMonth.getTitleName());
            //科室单元封装
            List<AccountUnitDto> accountUnitIdAndNameDtoList = new ArrayList<>();
            AccountUnitDto accountUnitIdAndNameDto = new AccountUnitDto();
            if (StringUtil.isNullOrEmpty(empAttendMonth.getOrganizationCd())) {
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
        System.out.println(JSON.toJSONString(rtnList));
    }

    /**
     * 科室单元下的管理人员人员列表
     * deptId 1，2，3
     *
     * @return
     */
    public List<UnitTaskUser> deptLeaderList(String cycle, String deptIds) {
        if (dataCenterProperties.getIfClosed()) {
            List<UnitTaskUser> rtnList = new ArrayList<>();
            UnitTaskUser user = new UnitTaskUser();
            user.setEmpCode("11");
            user.setEmpName("张三");
            user.setWorkdays(new BigDecimal("30"));
            user.setUserRate(BigDecimal.ONE);
            user.setIfGetAmt("0");
            user.setDeptName("208班");
            user.setPostName("班长");
            rtnList.add(user);

            UnitTaskUser user1 = new UnitTaskUser();
            user1.setEmpCode("22");
            user1.setEmpName("张四");
            user1.setWorkdays(new BigDecimal("20"));
            user1.setUserRate(new BigDecimal("1.2"));
            user1.setIfGetAmt("1");
            user1.setDeptName("208班");
            user1.setPostName("体育委员");
            rtnList.add(user1);

            return rtnList;
        }
        Map<String, Object> paramMap = new HashMap<>();
        if (deptIds != null) {
            paramMap.put("deptIds", deptIds);
        }
        paramMap.put("cycle", cycle.replace("-", ""));
        return dataCenterService.getApiData("manager.emp.list", paramMap, UnitTaskUser.class).getData();
    }

    /**
     * 指标项
     *
     * @param empCodes
     * @param itemCode
     * @return
     */
    public List<ItemValueDTO> accountItemValue(String cycle, String deptIds, String empCodes, String itemCode) {
        if (dataCenterProperties.getIfClosed()) {
            List<ItemValueDTO> rtnList = new ArrayList<>();
            ItemValueDTO item = new ItemValueDTO();
            item.setUserId("11");
            item.setItemId("竞赛");
            item.setItemValue("1000");
            rtnList.add(item);

            ItemValueDTO item1 = new ItemValueDTO();
            item1.setUserId("11");
            item1.setItemId("投稿");
            item1.setItemValue("5");
            rtnList.add(item1);
            return rtnList;
        }
        Map<String, Object> paramMap = new HashMap<>();
        if (StringUtils.isNotBlank(cycle)) {
            cycle = cycle.replace("-", "");
        }
        paramMap.put("cycle", cycle);
        paramMap.put("deptIds", deptIds);
        paramMap.put("userIds", empCodes);
        paramMap.put("itemIds", itemCode);
        log.info("accountItemValue paramMap：{}", JSON.toJSONString(paramMap));
        List<ItemValueDTO> data = dataCenterService.getApiData("indicator.items.value", paramMap, ItemValueDTO.class).getData();
        log.info("accountItemValue data：{}", JSON.toJSONString(data));
        return data;
    }

    /**
     * 周期 科室  获取一次分配的结果金额
     *
     * @param cycle
     * @param depts
     * @return
     */
    public List<FirstTaskCountDTO> firstCount(String cycle, String depts) {
        if (dataCenterProperties.getIfClosed()) {
            List<FirstTaskCountDTO> rtnList = new ArrayList<>();
            FirstTaskCountDTO item = new FirstTaskCountDTO();
            item.setCycle("202403");
            item.setDeptId("xx科室");
            item.setKsAmt("1000");
            rtnList.add(item);
            return rtnList;
        }
        if (cycle != null) {
            cycle = cycle.replace("-", "");
        }
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("cycle", cycle);
        paramMap.put("deptIds", depts);
        return dataCenterService.getApiData("dept.one.time.allocation", paramMap, FirstTaskCountDTO.class).getData();
    }

    /**
     * 职工绩效
     *
     * @param cycle
     * @return
     */
    public List<RepotZhigongjxValueDTO> zhigongjxList(String cycle) {
        if (dataCenterProperties.getIfClosed()) {
            List<RepotZhigongjxValueDTO> rtnList = new ArrayList<>();
            RepotZhigongjxValueDTO item = new RepotZhigongjxValueDTO();
            item.setCycle("202309");
            item.setDeptId("1703742337008218116");
            item.setUserId("1702600914540163074");
            item.setAmt("9317.000000");
            rtnList.add(item);
            return rtnList;
        }
        if (cycle != null) {
            cycle = cycle.replace("-", "");
        }
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("cycle", cycle);
        log.info("zhigongjxList paramMap:{}", paramMap);
        return dataCenterService.getApiData("emp.one_time.allocation", paramMap, RepotZhigongjxValueDTO.class).getData();
    }

    /**
     * 职工绩效分类
     *
     * @param cycle
     * @return
     */
    public List<RepotZhigongjxflValueDTO> zhigongjxflList(String cycle) {
        if (dataCenterProperties.getIfClosed()) {
            List<RepotZhigongjxflValueDTO> rtnList = new ArrayList<>();
            RepotZhigongjxflValueDTO item = new RepotZhigongjxflValueDTO();
            item.setCycle("202309");
            item.setUserId("2019");
            item.setGuanlijx("9317.000000");
            item.setMenzhenjx("1234.000000");
            rtnList.add(item);
            return rtnList;
        }
        if (cycle != null) {
            cycle = cycle.replace("-", "");
        }
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("cycle", cycle);
        return dataCenterService.getApiData("emp.performance.total", paramMap, RepotZhigongjxflValueDTO.class).getData();
    }

    public List<UserTypeVo> queryUserWorkTypeList(String cycle, String userType, String deptIds) {
        if (dataCenterProperties.getIfClosed()) {
            List<UserTypeVo> rtnList = new ArrayList<>();
            UserTypeVo item = new UserTypeVo();
            item.setCycle("202309");
            item.setDeptId("1703742337008218116");
            item.setUserId("2019");
            item.setAmt("9317.000000");
            rtnList.add(item);
            return rtnList;
        }
        if (cycle != null) {
            cycle = cycle.replace("-", "");
        }
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("cycle", cycle);
        paramMap.put("userType", userType);
        paramMap.put("deptIds", deptIds);
        return dataCenterService.getApiData("emp.one_time.allocation", paramMap, UserTypeVo.class).getData();
    }

    public List<MaterialChargeVo> queryMaterialChargeList(long currentPage, int pageSize) {
        if (dataCenterProperties.getIfClosed()) {
            List<MaterialChargeVo> rtnList = new ArrayList<>();
            MaterialChargeVo item = new MaterialChargeVo();
            item.setIsCharge("未设置");
            item.setResourceId("弹簧拉钩放置架");
            item.setResourceName("2019");
            item.setStoreId("1");
            item.setStoreName("设备仓库");
            rtnList.add(item);
            return rtnList;
        }
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("current_page", currentPage);
        paramMap.put("per_page", pageSize);
        return dataCenterService.getApiData("resource.is_charge", paramMap, MaterialChargeVo.class, true).getData();
    }

    /**
     * 护理绩效
     *
     * @param cycle
     * @return
     */
    public List<RepotHulijxValueDTO> hulijxList(String cycle) {
        if (dataCenterProperties.getIfClosed()) {
            List<RepotHulijxValueDTO> rtnList = new ArrayList<>();
            RepotHulijxValueDTO item = new RepotHulijxValueDTO();
            item.setCycle("202309");
            item.setDeptId("1703742337008218116");
            item.setDeptName("发放科");
            item.setGlAmt("9317.000000");
            item.setKsAmt("222.000000");
            item.setHszAmt("3333.000000");
            rtnList.add(item);
            return rtnList;
        }
        if (cycle != null) {
            cycle = cycle.replace("-", "");
        }
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("cycle", cycle);
        return dataCenterService.getApiData("nurse.performance.report", paramMap, RepotHulijxValueDTO.class).getData();
    }

    /**
     * 绩效总览
     *
     * @param cycle
     * @return
     */
    public List<IncomePerformancePayDTO> performance(String cycle) {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("cycle", cycle);
        return dataCenterService.getApiData("performance.overview.data", paramMap, IncomePerformancePayDTO.class).getData();
    }

    /**
     * rw数据
     */
    public List<OdsHisUnidrgswedDboDrgsInfo> rwList(String dt) {
        if (dataCenterProperties.getIfClosed()) {
            List<OdsHisUnidrgswedDboDrgsInfo> rtnList = new ArrayList<>();
            // OdsHisUnidrgswedDboDrgsInfo item = new OdsHisUnidrgswedDboDrgsInfo();
            // item.setRw(BigDecimal.valueOf(1.1));
            // item.setWardName("针推康复病区");
            // item.setOrdCode("2318531");
            // item.setPersonName("汤玉琴");
            // item.setOutDeptName("康复科");
            // item.setIsEditable(true);
            // rtnList.add(item);
            return rtnList;
        }
        if (StringUtils.isEmpty(dt)) {
            LocalDateTime now = LocalDateTime.now();
            dt = now.minusMonths(1).format(DateTimeFormatter.ofPattern("yyyyMM"));
        }
        JSONObject paramMap = new JSONObject();
        paramMap.put("dt", dt);//正式

        BaseResponse<OdsHisUnidrgswedDboDrgsInfo> apiDataList = dataCenterService.getApiDataList("customization.rw.value", paramMap, OdsHisUnidrgswedDboDrgsInfo.class);
        return apiDataList.getData();

    }

    /**
     * 人员考勤数据
     */
    public List<CostUserAttendance> userAttendanceList(String dt) throws IOException {
        if (dataCenterProperties.getIfClosed()) {
            List<CostUserAttendance> rtnList = new ArrayList<>();
            CostUserAttendance item = new CostUserAttendance();
            item.setEmpId("10005");
            item.setEmpName("王素英");
            item.setJobNature("在编");
            //add more data...
            rtnList.add(item);
            return rtnList;
        }
        JSONObject paramMap = new JSONObject();
        paramMap.put("dt", dt);
        //读取分页数据
        List<EmpAttendMonthDto> tempList = dataCenterService.getApiDataList(EMP_ATTEND_MONTH, paramMap, userProperties.getUrl(), userProperties.getAppkey(), userProperties.getAppsecret(), EmpAttendMonthDto.class).getData();

        //数据转化
        List<CostUserAttendance> rtnList = new ArrayList<>();
        for (EmpAttendMonthDto empAttendMonth : tempList) {
            CostUserAttendance costUserAttendance = new CostUserAttendance();
            costUserAttendance.setDt(empAttendMonth.getDt());
            costUserAttendance.setEmpId(empAttendMonth.getJobNumber());
            costUserAttendance.setEmpName(empAttendMonth.getName());
            costUserAttendance.setJobNature(empAttendMonth.getWorkNatureNm());
            costUserAttendance.setAttendanceGroup(empAttendMonth.getGroupName());
            costUserAttendance.setDutiesName(empAttendMonth.getJobName());
            costUserAttendance.setTitles(empAttendMonth.getTitleName());
            //科室单元封装
            List<AccountUnitDto> accountUnitIdAndNameDtoList = new ArrayList<>();
            AccountUnitDto accountUnitIdAndNameDto = new AccountUnitDto();
            accountUnitIdAndNameDto.setId(StringUtils.isNotEmpty(empAttendMonth.getOrganizationCd()) ? empAttendMonth.getOrganizationCd() : "");
            accountUnitIdAndNameDto.setName(empAttendMonth.getOrganizationName());
            accountUnitIdAndNameDtoList.add(accountUnitIdAndNameDto);
            String accountUnitJsonString = JSON.toJSONString(accountUnitIdAndNameDtoList);
            costUserAttendance.setAccountUnit(accountUnitJsonString);

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
            System.out.println("printing custom fields..." + customFields);
            costUserAttendance.setCustomFields(customFields.replace("$|", ","));
            rtnList.add(costUserAttendance);
        }
        return rtnList;
    }

    public EmpAttendMonthDto getNotNullEntity(List<EmpAttendMonthDto> list) {
        EmpAttendMonthDto rt = new EmpAttendMonthDto();
        list.forEach(x -> {
            if (StringUtil.isNullOrEmpty(rt.getDt()) && !StringUtil.isNullOrEmpty(x.getDt())) {
                rt.setDt(x.getDt());
            }
            if (StringUtil.isNullOrEmpty(rt.getJobNumber()) && !StringUtil.isNullOrEmpty(x.getJobNumber())) {
                rt.setJobNumber(x.getJobNumber());
            }
            if (StringUtil.isNullOrEmpty(rt.getName()) && !StringUtil.isNullOrEmpty(x.getName())) {
                rt.setName(x.getName());
            }
            if (StringUtil.isNullOrEmpty(rt.getJobName()) && !StringUtil.isNullOrEmpty(x.getJobName())) {
                rt.setJobName(x.getJobName());
            }
            if (StringUtil.isNullOrEmpty(rt.getTitleName()) && !StringUtil.isNullOrEmpty(x.getTitleName())) {
                rt.setTitleName(x.getTitleName());
            }
            if (StringUtil.isNullOrEmpty(rt.getWorkNatureNm()) && !StringUtil.isNullOrEmpty(x.getWorkNatureNm())) {
                rt.setWorkNatureNm(x.getWorkNatureNm());
            }
            if (StringUtil.isNullOrEmpty(rt.getPostName()) && !StringUtil.isNullOrEmpty(x.getPostName())) {
                rt.setPostName(x.getPostName());
            }
            if (StringUtil.isNullOrEmpty(rt.getRewardRmk()) && !StringUtil.isNullOrEmpty(x.getRewardRmk())) {
                rt.setRewardRmk(x.getRewardRmk());
            }
            if (StringUtil.isNullOrEmpty(rt.getRewardRate()) && !StringUtil.isNullOrEmpty(x.getRewardRate())) {
                rt.setRewardRate(x.getRewardRate());
            }
            if (StringUtil.isNullOrEmpty(rt.getNoRewardReason()) && !StringUtil.isNullOrEmpty(x.getNoRewardReason())) {
                rt.setNoRewardReason(x.getNoRewardReason());
            }
            if (StringUtil.isNullOrEmpty(rt.getOrganizationName()) && !StringUtil.isNullOrEmpty(x.getOrganizationName())) {
                rt.setOrganizationName(x.getOrganizationName());
            }
            if (StringUtil.isNullOrEmpty(rt.getOrganizationCd()) && !StringUtil.isNullOrEmpty(x.getOrganizationCd())) {
                rt.setOrganizationCd(x.getOrganizationCd());
            }
            if (StringUtil.isNullOrEmpty(rt.getGroupName()) && !StringUtil.isNullOrEmpty(x.getGroupName())) {
                rt.setGroupName(x.getGroupName());
            }
            if (StringUtil.isNullOrEmpty(rt.getAttendDays()) && !StringUtil.isNullOrEmpty(x.getAttendDays())) {
                rt.setAttendDays(Linq.of(list).select(q -> Long.parseLong(q.getAttendDays())).sumLong() + "");
            }
            if (StringUtil.isNullOrEmpty(rt.getTabInfos()) && !StringUtil.isNullOrEmpty(x.getTabInfos())) {
                rt.setTabInfos(String.join("$|", Linq.of(list).select(q -> q.getTabInfos()).toList()));
                if (!StringUtil.isNullOrEmpty(rt.getTabInfos())) {
                    List<KpiAttendUserCustomFieldDto> fields = JSONObject.parseArray("[" + rt.getTabInfos().replace("$|", ",") + "]", KpiAttendUserCustomFieldDto.class);
                    List<KpiAttendUserCustomFieldDto> li = Linq.of(fields).groupBy(f -> new KpiAttendUserCustomFieldDto(f.getName(), f.getId())).select(f -> {
                        KpiAttendUserCustomFieldDto dto = new KpiAttendUserCustomFieldDto(f.getKey().getName(), Linq.of(f.toList()).select(ff -> ff.getNum()).sumDecimal(), f.getKey().getId());
                        return dto;
                    }).toList();
                    String json = JSONObject.toJSONString(li);
                    rt.setTabInfos(json.replace("[", "").replace("]", "").replace(",{\"id\"", "$|{\"id\""));
                }
            }
        });
        return rt;
    }

    /**
     * 人员考勤数据2
     */
    public List<KpiUserAttendance> userAttendanceList2(String dt, DateDto dto) throws IOException {
        if (dataCenterProperties.getIfClosed()) {
            List<KpiUserAttendance> rtnList = new ArrayList<>();
            KpiUserAttendance item = new KpiUserAttendance();
            item.setEmpId("10005");
            item.setEmpName("王素英");
            item.setJobNature("在编");
            //add more data...
            rtnList.add(item);
            return rtnList;
        }
        JSONObject paramMap = new JSONObject();
        paramMap.put("dt", dt);
        //读取分页数据
        List<EmpAttendMonthDto> tempList = dataCenterService.getApiDataList(EMP_ATTEND_MONTH, paramMap, userProperties.getUrl(), userProperties.getAppkey(), userProperties.getAppsecret(), EmpAttendMonthDto.class).getData();
        List<UserCoreVo> data = remoteUserService.listMainDetails(SecurityConstants.FROM_IN).getData();
        List<KpiUserCalculationRule> kpiUserCalculationRules = kpiUserCalculationRuleMapper
                .selectList(new LambdaQueryWrapper<KpiUserCalculationRule>().eq(KpiUserCalculationRule::getStatus, "0")
                        .eq(KpiUserCalculationRule::getBusiType, dto.getBusiType()));
        List<KpiMemberCopy> kpiMemberCopies = kpiAccountTaskService.planMemberCopy(Long.valueOf(dt));
        List<KpiUserAttendanceCopy> kpiUserAttendanceCopies = kpiAccountTaskService.planUserCopy2(Long.valueOf(dt));
        List<KpiAccountUnitCopy> kpiAccountUnitCopies = kpiAccountTaskService.planUnitCopy2();
        List<KpiUserAttendance> rtnList = new ArrayList<>();
        List<IGrouping<KpiKeyValueVO, EmpAttendMonthDto>> list1 = Linq.of(tempList).groupBy(x -> new KpiKeyValueVO(x.getJobNumber(), x.getGroupName())).toList();
        for (IGrouping<KpiKeyValueVO, EmpAttendMonthDto> x : list1) {
            EmpAttendMonthDto empAttendMonth = getNotNullEntity(x.toList());

            KpiUserAttendance costUserAttendance = new KpiUserAttendance();
            costUserAttendance.setPeriod(Long.valueOf(empAttendMonth.getDt()));
            costUserAttendance.setEmpId(empAttendMonth.getJobNumber());
            //UserInfo userInfo = remoteUserService.allInfoByJobNumber(empAttendMonth.getJobNumber()).getData();
            if (StringUtils.isNotEmpty(empAttendMonth.getJobNumber())) {
                UserCoreVo userCoreVo = Linq.of(data).firstOrDefault(t -> empAttendMonth.getJobNumber().equals(t.getJobNumber()));
                if (userCoreVo != null) {
                    costUserAttendance.setUserId(userCoreVo.getUserId());
                } else {
                    continue;
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
        }
        if (!kpiUserCalculationRules.isEmpty()) {
            //将range转化成filter
            List<KpiUserCalculationRuleDto> rulelist = new ArrayList<>();
            for (KpiUserCalculationRule kpiUserCalculationRule : kpiUserCalculationRules) {
                KpiUserCalculationRuleDto kpiUserCalculationRuleDto = JSON.parseObject(kpiUserCalculationRule.getRule(), KpiUserCalculationRuleDto.class);
                if (kpiUserCalculationRuleDto.getRangeType().equals("range")) {
                    KpiFormulaDto2.FieldListDTO d = new KpiFormulaDto2.FieldListDTO();
                    BeanUtil.copyProperties(kpiUserCalculationRuleDto.getRange(), d);
                    List<Long> memberListComm = kpiAccountTaskService.getMemberListComm(d, kpiMemberCopies, kpiUserAttendanceCopies, kpiAccountUnitCopies);
                    List<KpiUserCalculationRuleFilterDto> in_list = new ArrayList<>();
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
            for (KpiUserAttendance costUserAttendance : rtnList) {
                for (KpiUserCalculationRuleDto rule : rulelist) {
                    if (isMatch(costUserAttendance, rule)) {
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
        return rtnList;
    }

    /**
     * 人员考勤数据3
     */
    public List<KpiUserAttendance> userAttendanceList3(String dt, DateDto dto) throws IOException {
        if (dataCenterProperties.getIfClosed()) {
            List<KpiUserAttendance> rtnList = new ArrayList<>();
            KpiUserAttendance item = new KpiUserAttendance();
            item.setEmpId("10005");
            item.setEmpName("王素英");
            item.setJobNature("在编");
            //add more data...
            rtnList.add(item);
            return rtnList;
        }
        //保证格式 202501
        String period = StringChangeUtil.periodChange(dt, DatePattern.SIMPLE_MONTH_PATTERN);
        JSONObject paramMap = new JSONObject();
        paramMap.put("year", period.substring(0,4));
        paramMap.put("month", period.substring(4,6));

        //读取分页数据
        List<GetAttendanceDTO> tempList2 = dataCenterService.getApiDataList(GET_ATTENDANCE, paramMap, userProperties.getUrl(), userProperties.getAppkey(), userProperties.getAppsecret(), GetAttendanceDTO.class).getData();
        List<EmpAttendMonthDto> tempList = convertAttendList(tempList2);

        List<UserCoreVo> data = remoteUserService.listMainDetails(SecurityConstants.FROM_IN).getData();
        List<KpiUserCalculationRule> kpiUserCalculationRules = kpiUserCalculationRuleMapper
                .selectList(new LambdaQueryWrapper<KpiUserCalculationRule>().eq(KpiUserCalculationRule::getStatus, "0")
                        .eq(KpiUserCalculationRule::getBusiType, dto.getBusiType()));
        List<KpiMemberCopy> kpiMemberCopies = kpiAccountTaskService.planMemberCopy(Long.valueOf(dt));
        List<KpiUserAttendanceCopy> kpiUserAttendanceCopies = kpiAccountTaskService.planUserCopy2(Long.valueOf(dt));
        List<KpiAccountUnitCopy> kpiAccountUnitCopies = kpiAccountTaskService.planUnitCopy2();
        List<KpiUserAttendance> rtnList = new ArrayList<>();
        List<IGrouping<KpiKeyValueVO, EmpAttendMonthDto>> list1 = Linq.of(tempList).groupBy(x -> new KpiKeyValueVO(x.getJobNumber(), x.getGroupName())).toList();
        for (IGrouping<KpiKeyValueVO, EmpAttendMonthDto> x : list1) {
            EmpAttendMonthDto empAttendMonth = getNotNullEntity(x.toList());

            KpiUserAttendance costUserAttendance = new KpiUserAttendance();
            costUserAttendance.setPeriod(Long.valueOf(empAttendMonth.getDt()));
            costUserAttendance.setEmpId(empAttendMonth.getJobNumber());
            //UserInfo userInfo = remoteUserService.allInfoByJobNumber(empAttendMonth.getJobNumber()).getData();
            if (StringUtils.isNotEmpty(empAttendMonth.getJobNumber())) {
                UserCoreVo userCoreVo = Linq.of(data).firstOrDefault(t -> empAttendMonth.getJobNumber().equals(t.getJobNumber()));
                if (userCoreVo != null) {
                    costUserAttendance.setUserId(userCoreVo.getUserId());
                } else {
                    continue;
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
        }
        if (!kpiUserCalculationRules.isEmpty()) {
            //将range转化成filter
            List<KpiUserCalculationRuleDto> rulelist = new ArrayList<>();
            for (KpiUserCalculationRule kpiUserCalculationRule : kpiUserCalculationRules) {
                KpiUserCalculationRuleDto kpiUserCalculationRuleDto = JSON.parseObject(kpiUserCalculationRule.getRule(), KpiUserCalculationRuleDto.class);
                if (kpiUserCalculationRuleDto.getRangeType().equals("range")) {
                    KpiFormulaDto2.FieldListDTO d = new KpiFormulaDto2.FieldListDTO();
                    BeanUtil.copyProperties(kpiUserCalculationRuleDto.getRange(), d);
                    List<Long> memberListComm = kpiAccountTaskService.getMemberListComm(d, kpiMemberCopies, kpiUserAttendanceCopies, kpiAccountUnitCopies);
                    List<KpiUserCalculationRuleFilterDto> in_list = new ArrayList<>();
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
            for (KpiUserAttendance costUserAttendance : rtnList) {
                for (KpiUserCalculationRuleDto rule : rulelist) {
                    if (isMatch(costUserAttendance, rule)) {
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
        return rtnList;
    }

    public List<EmpAttendMonthDto> convertAttendList(List<GetAttendanceDTO> tempList2) {
        List<EmpAttendMonthDto> list = new ArrayList<>();
        List<CostUserAttendanceCustomFields> dicts = costUserAttendanceCustomFieldsService.getBaseMapper().listGroup();

        for (GetAttendanceDTO r : tempList2) {
            EmpAttendMonthDto dto = new EmpAttendMonthDto();
            dto.setId(r.getId());
            if (!StringUtil.isNullOrEmpty(r.getAttendMonth()) && r.getAttendMonth().length() == 1){
                r.setAttendMonth("0"+r.getAttendMonth());
            }
            dto.setDt(r.getAttendYear()+r.getAttendMonth());
            dto.setName(r.getName());
            dto.setJobName(r.getJobName());
            dto.setTitleName(r.getTitleName());
            dto.setWorkNatureNm(r.getWorkNatureNm());
            dto.setPostName(r.getPostName());
            dto.setRewardRmk(r.getRewardRmk());
            dto.setRewardRate(r.getRewardRate());
            dto.setOrganizationName(r.getOrganizationName());
            dto.setGroupName(r.getGroupName());
            dto.setAttendDays(r.getDays());
            dto.setJobNumber(r.getJobNumber());
            if (!StringUtil.isNullOrEmpty(r.getTabInfo())){
                List<JSONObject> objs = JSONObject.parseArray(r.getTabInfo(), JSONObject.class);
                for (JSONObject obj : objs) {
                    String id = obj.getString("id");
                    CostUserAttendanceCustomFields first = Linq.of(dicts).firstOrDefault(x -> x.getColumnId().equals(id));
                    if (first != null){
                        obj.put("name",first.getName());
                    }
                }
                String tabinfo = JSONObject.toJSONString(objs);
                if (!StringUtil.isNullOrEmpty(tabinfo)){
                    tabinfo = tabinfo.replace("[","").replace("]","").replace("},","}$|");
                }
                dto.setTabInfos(tabinfo);
            }
            list.add(dto);
        }
        return list;
    }

    public boolean isMatch(KpiUserAttendance user, KpiUserCalculationRuleDto rule) {
        List<KpiUserCalculationRuleFilterDto> filter = rule.getFilter();
        boolean flag = false;
        //判断是否过滤了人员
        KpiUserCalculationRuleFilterDto kpiUserCalculationRuleFilterDto =
                Linq.of(filter).where(t -> t.getKey().equals("paramExcludes")).firstOrDefault();
        for (KpiUserCalculationRuleFilterDto filterDto : filter) {
            //字段名
            String key = filterDto.getKey();
            List<KpiFormulaDto2.MemberListDTO> value = filterDto.getValue();
            if (com.alibaba.nacos.common.utils.CollectionUtils.isNotEmpty(value)) {
                for (KpiFormulaDto2.MemberListDTO x : value) {
                    try {
                        Object getMethod = InvokeUtil.getGetMethod(user, key);
                        if (getMethod != null) {
                            boolean equals = Objects.equals(getMethod.toString(), x.getValue());
                            if (equals) {
                                flag = true;
                            }
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        if (!flag) {
            return false;
        }
        if (kpiUserCalculationRuleFilterDto != null) {
            //过滤人员
            try {
                Object getMethod = InvokeUtil.getGetMethod(user, "userId");
                List<KpiFormulaDto2.MemberListDTO> value = kpiUserCalculationRuleFilterDto.getValue();
                List<String> ExcludeUserIds = Linq.of(value).select(KpiFormulaDto2.MemberListDTO::getValue).toList();
                if (getMethod != null) {
                    boolean equals = ExcludeUserIds.contains(getMethod.toString());
                    flag = !equals;
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return flag;
    }


    public List<Long> InvertRule(String rule, List<KpiUserAttendance> rtnList, List<KpiMemberCopy> kpiMemberCopies
            , List<KpiUserAttendanceCopy> kpiUserAttendanceCopies, List<KpiAccountUnitCopy> kpiAccountUnitCopies) {
        KpiUserCalculationRuleDto kpiUserCalculationRuleDto = JSON.parseObject(rule, KpiUserCalculationRuleDto.class);
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
                if (com.alibaba.nacos.common.utils.CollectionUtils.isNotEmpty(value)) {
                    String Matching_values = value.get(0).getValue();
                    List<KpiUserAttendance> list = Linq.of(rtnList).where(t -> {
                        try {
                            return Objects.equals(InvokeUtil.getGetMethod(t, key), Matching_values);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }).toList();
                    if (com.alibaba.nacos.common.utils.CollectionUtils.isNotEmpty(list)) {
                        List<Long> list1 = Linq.of(list).select(KpiUserAttendance::getUserId).toList();
                        userIdsList.addAll(list1);
                    }
                }
            }
        }
        return userIdsList;
    }

    /**
     * 门诊费用明细数据 + 住院费用明细数据
     */
    public List<DimMonthEmpIncome> outpatientFeeList(String dt) {
        List<DimMonthEmpIncome> rtnList = new ArrayList<>();
        if (dataCenterProperties.getIfClosed()) {
            DimMonthEmpIncome item = new DimMonthEmpIncome();
            item.setEmpName("汤玉琴");
            rtnList.add(item);
            return rtnList;
        }
        JSONObject paramMap = new JSONObject();
        paramMap.put("cycle", dt);
        rtnList = dataCenterService.getApiDataList("emp.income.list", paramMap, userProperties.getUrl(), userProperties.getAppkey(), userProperties.getAppsecret(), DimMonthEmpIncome.class).getData();
        //process data
        System.out.println(rtnList);
        return rtnList;
    }

    /**
     * 连接数据中台API
     *
     * @param param 入参
     */
    private JSONArray getApiData(JSONObject param) {
        JSONArray rtnObj = new JSONArray();
        try {
            if (param == null) {
                param = new JSONObject();
            }
            String timestamp = DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss");
            String sign = this.getSign("customization.rw.value", timestamp);

            log.info("param.toJSONString()");
            log.info(param.toJSONString());
            String result = HttpUtil.createPost(dmoProperties.getUrl())
                    .header("appkey", dmoProperties.getAppkey())
                    .header("method", "customization.rw.value")
                    .header("sign", sign).header("timestamp", timestamp)
                    .contentType("application/json")
                    .body(param.toJSONString()).execute().body();

            JSONObject resultObj = JSONObject.parseObject(result);
            log.info("resultObj.toJSONString()");
            log.info(resultObj.toJSONString());

            Integer resultCode = resultObj.getInteger("code");
            //非空判断
            if (resultCode == 0) {
                JSONObject dataObj = null;
                try {
                    dataObj = resultObj.getJSONObject("data");
                } catch (Exception e) {
                    //对象间隙失败改用数组间隙
                    return resultObj.getJSONArray("data");
                }
                System.out.println(dataObj);
                rtnObj = dataObj.getJSONArray("list");
            } else {
                String errMsg = resultObj.getString("msg");
                throw new RuntimeException(errMsg);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        return rtnObj;

    }

    public String getSign(String method, String timestamp) {
        String s = dmoProperties.getAppkey() + dmoProperties.getAppsecret() + method + timestamp;
        return DigestUtils.md5Hex(s);
    }


}
