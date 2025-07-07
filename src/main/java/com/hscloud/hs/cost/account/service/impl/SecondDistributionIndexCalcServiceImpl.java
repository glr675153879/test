package com.hscloud.hs.cost.account.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hscloud.hs.cost.account.constant.enums.SecondDistributionIndexEnum;
import com.hscloud.hs.cost.account.constant.enums.SecondDistributionPJJXRegularSelectIndexEnum;
import com.hscloud.hs.cost.account.constant.enums.UnitMapEnum;
import com.hscloud.hs.cost.account.model.entity.*;
import com.hscloud.hs.cost.account.model.vo.UserAttendanceVo;
import com.hscloud.hs.cost.account.service.*;
import com.hscloud.hs.cost.account.utils.SqlUtil;
import com.pig4cloud.pigx.admin.api.entity.SysUser;
import com.pig4cloud.pigx.common.core.exception.BizException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author banana
 * @create 2023-11-29 18:36
 */
@Slf4j
@Service
public class SecondDistributionIndexCalcServiceImpl implements ISecondDistributionIndexCalcService {

    @Autowired
    private ISecondDistributionAccountIndexService secondDistributionAccountIndexService;

    @Autowired
    private ISecondDistributionAccountFormulaParamService secondDistributionAccountFormulaParamService;

    @Autowired
    @Lazy
    private ISecondDistributionTaskUnitInfoService secondDistributionTaskUnitInfoService;

    @Autowired
    private IDistributionUserInfoService distributionUserInfoService;

    @Autowired
    private CostAccountUnitService costAccountUnitService;

    @Autowired
    private SqlUtil sqlUtil;


    /**
     * 个人岗位绩效出勤率
     * @param period 时间范围
     * @param userIds 用户id
     * @return
     */
    @Override
    public List<UserAttendanceVo> calAttendance(String period, List<Long> userIds){
        List<UserAttendanceVo> res = new ArrayList<>();
        CostEmpAttendance costEmpAttendance = new CostEmpAttendance();
        userIds.stream().forEach(r ->{
            CostEmpAttendance empAttendance = costEmpAttendance
                    .selectOne(new LambdaQueryWrapper<CostEmpAttendance>()
                    .eq(CostEmpAttendance::getEmpId, r)
                    .eq(CostEmpAttendance::getDt, period));

            UserAttendanceVo userAttendanceVo = new UserAttendanceVo();
            //当前user考勤信息，默认为0
            userAttendanceVo.setAttendance(BigDecimal.ZERO);

            if(empAttendance != null){
                userAttendanceVo.setAttendance(empAttendance.getAttendDaysArrange());
                userAttendanceVo.setUserId(empAttendance.getEmpId());
            }
            res.add(userAttendanceVo);
        });
        return res;
    }


    /**
     * 平均绩效参数
     * @param planId 分配方案id
     * @param accountIndexId 核算指标id
     * @param taskUnitId 科室单元任务id
     * @param period    时间范围
     * @return
     */
    @Override
    public Map<String, BigDecimal> analysisPJJX(Long planId, Long accountIndexId, Long taskUnitId, String period){

        //获取对应的平均绩效
        SecondDistributionAccountIndex pjjx =
                secondDistributionAccountIndexService
                        .getOne(new LambdaQueryWrapper<SecondDistributionAccountIndex>()
                        .eq(SecondDistributionAccountIndex::getId, accountIndexId)
                        .eq(SecondDistributionAccountIndex::getPlanId, planId));
        if(pjjx == null ||
                !SecondDistributionIndexEnum.PJFPJX.getItem().equals(pjjx.getAccountIndex())){
            throw new BizException("当前获取核算指标不合法");
        }
        //结果集
        Map<String, BigDecimal> res = new HashMap<>();
        //当前核算单元
        Long unitId = pjjx.getUnitId();
        //获取当前单元信息
        CostAccountUnit costAccountUnit = costAccountUnitService
                .getOne(new LambdaQueryWrapper<CostAccountUnit>()
                .eq(CostAccountUnit::getId, unitId));
        //科室code信息
        JSONObject accountGroupCode = JSON.parseObject(costAccountUnit.getAccountGroupCode());

        //获取公式（根据accountId 和 planId 获取对应的公式参数）
        List<SecondDistributionAccountFormulaParam> accountFormulaParams =
                secondDistributionAccountFormulaParamService
                .list(new LambdaQueryWrapper<SecondDistributionAccountFormulaParam>()
                        .eq(SecondDistributionAccountFormulaParam::getBizId, accountIndexId)
                        .eq(SecondDistributionAccountFormulaParam::getPlanId, planId));

        //处理公式参数，并存储其值
        accountFormulaParams.stream().forEach(r ->{
            String key = r.getFormulaKey();
            String value = r.getFormulaValue();
            BigDecimal calValue = BigDecimal.ZERO;

            if(SecondDistributionPJJXRegularSelectIndexEnum.YCFPJXJE.equals(value)){
                //一次分配绩效金额(统一处理)
                SecondDistributionTaskUnitInfo secondDistributionTaskUnitInfo =
                        secondDistributionTaskUnitInfoService
                        .getOne(new LambdaQueryWrapper<SecondDistributionTaskUnitInfo>()
                                .eq(SecondDistributionTaskUnitInfo::getId, taskUnitId));
                calValue = secondDistributionTaskUnitInfo.getTotalAmount();

            }else if(SecondDistributionPJJXRegularSelectIndexEnum.FFDWZRS.equals(value)){
                //发放单位总人数(=当前科室单元总人数，统一处理 )
                List<SysUser> sysUsers = costAccountUnitService.listUser(unitId);
                calValue = BigDecimal.valueOf(sysUsers.size());

            }else if(UnitMapEnum.DOCKER.getUnitGroup().equals(accountGroupCode.getString("value"))){
                //医生组
                calValue = sqlUtil.getDocPJJXInfo(value, period, unitId);

            }else if(UnitMapEnum.NURSE.getUnitGroup().equals(accountGroupCode.getString("value"))){
                //护理组
                calValue = sqlUtil.getNursePJJXInfo(value, period, unitId);

            }else if(UnitMapEnum.MEDICAL_SKILL.getUnitGroup().equals(accountGroupCode.getString("value"))){
                //医技组
                calValue = sqlUtil.getYJPJJXInfo(value, period, unitId);
            }else if(UnitMapEnum.ADMINISTRATION.getUnitGroup().equals(accountGroupCode.getString("value"))){
                //todo 行政组表格暂无

            }

            res.put(key, calValue);
        });
        return res;
    }

}
