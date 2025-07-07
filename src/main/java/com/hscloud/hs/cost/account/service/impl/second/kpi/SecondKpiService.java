package com.hscloud.hs.cost.account.service.impl.second.kpi;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.hscloud.hs.cost.account.constant.DmoProperties;
import com.hscloud.hs.cost.account.constant.enums.SecondDistributionTaskStatusEnum;
import com.hscloud.hs.cost.account.mapper.second.SecondTaskMapper;
import com.hscloud.hs.cost.account.model.dto.second.FirstTaskCountDTO;
import com.hscloud.hs.cost.account.model.dto.second.ItemValueDTO;
import com.hscloud.hs.cost.account.model.dto.second.RepotZhigongjxValueDTO;
import com.hscloud.hs.cost.account.model.dto.second.RepotZhigongjxflValueDTO;
import com.hscloud.hs.cost.account.model.entity.second.*;
import com.hscloud.hs.cost.account.model.vo.kpi.KpiReportCodeDTO;
import com.hscloud.hs.cost.account.model.vo.kpi.KpiReportCodeVO;
import com.hscloud.hs.cost.account.model.vo.second.UserTypeVo;
import com.hscloud.hs.cost.account.service.impl.second.async.SecRedisService;
import com.hscloud.hs.cost.account.service.kpi.IKpiReportService;
import com.hscloud.hs.cost.account.service.second.*;
import com.hscloud.hs.cost.account.service.userAttendance.ICostUserAttendanceService;
import com.hscloud.hs.cost.account.utils.DmoUtil;
import com.pig4cloud.pigx.common.core.exception.BizException;
import com.pig4cloud.pigx.common.core.util.R;
import com.pig4cloud.pigx.common.media.service.DingRobotService;
import com.pig4cloud.pigx.common.security.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
* 一次分配 3.0 提供二次分配数据接口，替换原dmoUtil里的接口
*
*/
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class SecondKpiService {

    @Lazy
    @Autowired
    private IAttendanceService attendanceService;

    private final IKpiReportService kpiReportService;
    private final DmoUtil dmoUtil;
    private final DmoProperties dmoProperties;
    private final SecRedisService secRedisService;

     public List<UnitTaskUser> deptLeaderList(String cycle, String deptIds) {
         String apiMonth = dmoProperties.getApiMonth();
         cycle = cycle.replace("-", "");
         apiMonth = apiMonth.replace("-", "");
         if (cycle.compareTo(apiMonth) <=0 ){
             log.info(cycle+" deptLeaderList 调用dmo");
             return dmoUtil.deptLeaderList(cycle, deptIds);
         }
         /**
          * {
                "cycle": "202408",
                "userType": "",
                "userId": "1702600883699445761",
                "userName": "龚文波",
                "deptId": "1703742341026361346",
                "deptName": "中医经典（内分泌二）科"
            }
          */
        if (cycle != null) {
            cycle = cycle.replace("-", "");
        }
        KpiReportCodeDTO dto = new KpiReportCodeDTO();
        dto.setCycle(Long.parseLong(cycle));
        dto.setCode("KSFZR");

        KpiReportCodeVO report = kpiReportService.report(dto);
        List<String> deptIdList;
        if (deptIds != null){
            deptIdList = Arrays.asList(deptIds.split(","));
        } else {
            deptIdList = null;
        }
         List<JSONObject> reportList = report.getList().stream()
                 .filter(o -> deptIds == null || deptIdList.contains(o.getStr("deptId"))).collect(Collectors.toList());

        List<UnitTaskUser> rtn = new ArrayList<>();
        for (JSONObject json:reportList){
            UnitTaskUser unitTaskUser = json.toBean(UnitTaskUser.class);
            unitTaskUser.setEmpName(json.getStr("userName"));
            rtn.add(unitTaskUser);
        }
         return rtn;
    }

    /**
     * 获取 核算项 和 指标的值
     *
     * 绩效接口实际不接收  deptIds   和  userIds 入参，要在返回结果里 过滤
     * @param cycle
     * @param deptIds
     * @param userIds
     * @param itemCode
     * @param itemType
     * @return
     */
    public List<ItemValueDTO> accountItemValue(String cycle, String deptIds, String userIds, String itemCode,String itemType) {
        String apiMonth = dmoProperties.getApiMonth();
        cycle = cycle.replace("-", "");
        apiMonth = apiMonth.replace("-", "");
        if (cycle.compareTo(apiMonth) <= 0 ){
            log.info(cycle+" accountItemValue 调用dmo");
            return dmoUtil.accountItemValue(cycle, deptIds,userIds,itemCode);
        }

        if (itemType == null){
            throw new BizException("accountItemValue.itemType参数不能为空");
        }
        /*

        [
          {
            "cycle": "202408",
            "userId": "1702601149177917442",
            "userName": "杨晨",
            "deptId": "1703742337599614978",
            "deptName": "妇科二病区",
            "itemValue": 10028.34,
            "itemName": "门诊妇科西医执行",
            "itemCode": "h_cly"
          },
          {
            "cycle": "202408",
            "userId": "1702601065434443777",
            "userName": "王春华",
            "deptId": "1703742337733832706",
            "deptName": "妇科一病区",
            "itemValue": 7894.04,
            "itemName": "门诊妇科西医执行",
            "itemCode": "h_cly"
          }
        ]
         */
        List<JSONObject> report = secRedisService.accountItemValueCache(cycle, itemType, itemCode);

        List<String> deptIdList;
        if (deptIds != null){
            deptIdList = Arrays.asList(deptIds.split(","));
        } else {
            deptIdList = null;
        }
        List<String> userIdList;
        if (userIds != null){
            userIdList = Arrays.asList(userIds.split(","));
        } else {
            userIdList = null;
        }

        return report.stream()
                .filter(o -> deptIds == null || deptIdList.contains(o.getStr("deptId")))
                .filter(o -> userIds == null || userIdList.contains(o.getStr("userId")))
                .map(o -> o.toBean(ItemValueDTO.class)).collect(Collectors.toList());
    }

    /**
     * 周期 科室  获取一次分配的结果金额
     *  KESHIA
     * @param cycle
     * @param deptIds
     * @return
     */
    public List<FirstTaskCountDTO> firstCount(String cycle, String deptIds) {
        String apiMonth = dmoProperties.getApiMonth();
        cycle = cycle.replace("-", "");
        apiMonth = apiMonth.replace("-", "");
        if (cycle.compareTo(apiMonth) <= 0 ){
            log.info(cycle+" firstCount 调用dmo");
            return dmoUtil.firstCount(cycle, deptIds);
        }
        /*
            [
              {
                "cycle": "202408",
                "deptId": "1703742337536700419",
                "deptName": "妇科二",
                "ksAmt": -1138.592
              },
              {
                "cycle": "202408",
                "deptId": "1703742337599614980",
                "deptName": "妇科一",
                "ksAmt": -1944.292
              }
            ]
         */
        if (cycle != null) {
            cycle = cycle.replace("-", "");
        }
        KpiReportCodeDTO dto = new KpiReportCodeDTO();
        dto.setCycle(Long.parseLong(cycle));
        dto.setDeptIds(deptIds);
        dto.setCode("KESHIA");

        KpiReportCodeVO report = kpiReportService.report(dto);
        return report.getList().stream().map(o -> o.toBean(FirstTaskCountDTO.class)).collect(Collectors.toList());
    }

    /**
     * 编外人员 一次分配金额 GERENA
     * @param cycle
     * @param userType
     * @param deptIds
     * @return
     */
    public List<UserTypeVo> queryUserWorkTypeList(String cycle, String userType, String deptIds) {
        String apiMonth = dmoProperties.getApiMonth();
        cycle = cycle.replace("-", "");
        apiMonth = apiMonth.replace("-", "");
        if (cycle.compareTo(apiMonth) <= 0 ){
            log.info(cycle+" queryUserWorkTypeList 调用dmo");
            return dmoUtil.queryUserWorkTypeList(cycle, userType,deptIds);
        }
        /*
        [{
                "cycle": "202408",
                "userType": "Y",
                "userId": "1702600824178077697",
                "userName": "陈成法",
                "deptId": "1703742338123902979",
                "deptName": "后勤保障部",
                "ksAmt": 5414.895501
            }]
         */
        if (cycle != null) {
            cycle = cycle.replace("-", "");
        }
        //先查科室下有哪些人，再查这些人的金额 （可以入参 是否编外）
        List<Attendance> attendanceList = attendanceService.getByCycleUserUnit(cycle,null, Arrays.asList(deptIds.split(",")));
        if (attendanceList.isEmpty()){
            return new ArrayList<>();
        }
        String userIds = attendanceList.stream().map(o -> o.getUserId() + "").collect(Collectors.joining(","));
        KpiReportCodeDTO dto = new KpiReportCodeDTO();
        dto.setCycle(Long.parseLong(cycle));
        dto.setUserIds(userIds);
        dto.setCode("GERENA");
        if (userType != null) {
            dto.setUserType(userType);
        }
        KpiReportCodeVO report = kpiReportService.report(dto);
        List<UserTypeVo> rtn = report.getList().stream().map(o -> o.toBean(UserTypeVo.class)).collect(Collectors.toList());
        for (UserTypeVo userTypeVo : rtn){
            userTypeVo.setAmt(new BigDecimal(userTypeVo.getAmt()).add(new BigDecimal(userTypeVo.getGlAmt()))+"");
        }
        return rtn;
    }
    /**
     * 职工绩效 一次分配
     *
     * @param cycle
     * @return
     */
    public List<RepotZhigongjxValueDTO> zhigongjxList(String cycle) {
        return this.zhigongjxList(cycle,null);
    }

    /**
     * 职工绩效 一次个人绩效
     * @param cycle
     * @param userIds
     * @return
     */
    public List<RepotZhigongjxValueDTO> zhigongjxList(String cycle,String userIds) {
        if (cycle == null){
            return new ArrayList<>();
        }

        String apiMonth = dmoProperties.getApiMonth();
        cycle = cycle.replace("-", "");
        apiMonth = apiMonth.replace("-", "");
        if (cycle.compareTo(apiMonth) <= 0 ){
            log.info(cycle+" zhigongjxList 调用dmo");
            return dmoUtil.zhigongjxList(cycle);
        }

                /*
        [{
            "cycle": "202411",
            "userType": "Y",
            "userId": "1702601003220332545",
            "userName": "毛志浩",
            "deptId": "1703742337209544706",
            "deptName": "党政办",
            "amt": 2200
        }]
         */
        //个人绩效 和 管理绩效数据源合并
        List<RepotZhigongjxValueDTO> genrenjxList = this.genrenjxList(cycle);
        log.info("genrenjxList.size():"+genrenjxList.size());
        List<RepotZhigongjxValueDTO> guanlijxList = this.guanlijxList(cycle);
        log.info("guanlijxList.size():"+guanlijxList.size());
        List<RepotZhigongjxValueDTO> rtn = new ArrayList<>();
        rtn.addAll(genrenjxList);
        rtn.addAll(guanlijxList);

        if (userIds != null){ //用于发放单元额外人员一次金额获取
            List<String> userIdList = Arrays.asList(userIds.split(","));
            rtn = rtn.stream().filter(o -> userIdList.contains(o.getUserId())).collect(Collectors.toList());
        }
        log.info("rtn.size():"+rtn.size());
        return rtn;

    }


    //一次管理绩效，用于职工绩效总览表
    public List<RepotZhigongjxValueDTO> guanlijxList(String cycle) {
                /*
        [{
            "cycle": "202411",
            "userType": "",
            "userId": "1702601069419032578",
            "userName": "王海珍",
            "deptId": "1703742339512217604",
            "deptName": "内镜中心",
            "amt": 4261
        }]
         */
        if (cycle == null){
            return new ArrayList<>();
        }
        cycle = cycle.replace("-", "");
        KpiReportCodeDTO dto = new KpiReportCodeDTO();
        dto.setCycle(Long.parseLong(cycle));
        dto.setCode("GLJX");
        KpiReportCodeVO report = kpiReportService.report(dto);
        return report.getList().stream().map(o -> o.toBean(RepotZhigongjxValueDTO.class)).collect(Collectors.toList());

    }

    //用于发放单元额外人员一次金额获取
    //用于职工绩效总览表
    public List<RepotZhigongjxValueDTO> genrenjxList(String cycle) {
                /*
        [{
            "cycle": "202411",
            "userType": "Y",
            "userId": "1702601003220332545",
            "userName": "毛志浩",
            "deptId": "1703742337209544706",
            "deptName": "党政办",
            "amt": 2200
        }]
         */
        if (cycle == null){
            return new ArrayList<>();
        }
        cycle = cycle.replace("-", "");
        KpiReportCodeDTO dto = new KpiReportCodeDTO();
        dto.setCycle(Long.parseLong(cycle));
        dto.setCode("YCGRJXHJ");
        KpiReportCodeVO report = kpiReportService.report(dto);
        return report.getList().stream().map(o -> o.toBean(RepotZhigongjxValueDTO.class)).collect(Collectors.toList());

    }

    /**
     * 职工绩效分类 一次分配
     * zgjxfl
     * @param cycle
     * @return
     */
    public List<RepotZhigongjxflValueDTO> zhigongjxflList(String cycle) {
        String apiMonth = dmoProperties.getApiMonth();
        cycle = cycle.replace("-", "");
        apiMonth = apiMonth.replace("-", "");
        if (cycle.compareTo(apiMonth) <= 0 ){
            log.info(cycle+" zhigongjxflList 调用dmo");
            return dmoUtil.zhigongjxflList(cycle);
        }
        /*[
         {
                "cycle": "202408",
                "userType": "",
                "userId": "1702601108929376258",
                "userName": "吴权",
                "deptId": "1703742338060988418",
                "deptName": "骨伤一科",
                "menzhenjx": 1296.250000,
                "guanlijx": 7424.750000
            }
            ]
         */
        if (cycle != null) {
            cycle = cycle.replace("-", "");
        }

        KpiReportCodeDTO dto = new KpiReportCodeDTO();
        dto.setCycle(Long.parseLong(cycle));
        dto.setCode("zgjxfl");

        KpiReportCodeVO report = kpiReportService.report(dto);
        return report.getList().stream().map(o -> o.toBean(RepotZhigongjxflValueDTO.class)).collect(Collectors.toList());
    }


}