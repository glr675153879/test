package com.hscloud.hs.cost.account.service.impl.second;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hscloud.hs.cost.account.constant.enums.SecondDistributionTaskStatusEnum;
import com.hscloud.hs.cost.account.constant.enums.second.UserType;
import com.hscloud.hs.cost.account.mapper.second.SecondTaskMapper;
import com.hscloud.hs.cost.account.mapper.second.UnitTaskMapper;
import com.hscloud.hs.cost.account.model.dto.second.FirstTaskCountDTO;
import com.hscloud.hs.cost.account.model.dto.second.FirstTaskExtraUserCountDTO;
import com.hscloud.hs.cost.account.model.dto.second.RepotZhigongjxValueDTO;
import com.hscloud.hs.cost.account.model.dto.second.SecStartDataDTO;
import com.hscloud.hs.cost.account.model.entity.second.*;
import com.hscloud.hs.cost.account.model.vo.second.UserTypeVo;
import com.hscloud.hs.cost.account.service.impl.second.async.SecRedisService;
import com.hscloud.hs.cost.account.service.impl.second.async.UnitTaskAsyncService;
import com.hscloud.hs.cost.account.service.impl.second.kpi.SecondKpiService;
import com.hscloud.hs.cost.account.service.second.*;
import com.hscloud.hs.cost.account.utils.DmoUtil;
import com.pig4cloud.pigx.common.core.exception.BizException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
* 发放单元任务 服务实现类
*
*/
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class UnitTaskService extends ServiceImpl<UnitTaskMapper, UnitTask> implements IUnitTaskService {

    private final IGrantUnitService grantUnitService;

    private final IProgrammeService programmeService;

    private final IUnitTaskProjectService unitTaskProjectService;

    private final IUnitTaskUserService unitTaskUserService;

    private final SecondTaskMapper secondTaskMapper;

    private final DmoUtil dmoUtil;
    private final SecondKpiService secondKpiService;
    private final SecRedisService secRedisService;
    @Lazy
    @Autowired
    private UnitTaskAsyncService unitTaskAsyncService;

//    private final ISecondTaskCountService secondTaskCountService;
//    private final IUnitTaskCountService unitTaskCountService;
//    private final IUnitTaskProjectCountService unitTaskProjectCountService;
//    private final IUnitTaskDetailCountService unitTaskDetailCountService;


    @Override
//    @Transactional(rollbackFor = Exception.class)
    public void createBySecondTaskId(SecondTask secondTask, String grantUnitIds) {
        List<String> listGrant = Arrays.asList(grantUnitIds.split(","));
        //每个发放单元都生成,如果有传入值则获取选择的发放单元信息
        List<GrantUnit> grantUnitList = grantUnitService.list(Wrappers.<GrantUnit>lambdaQuery()
                .eq(GrantUnit::getStatus,"0")
                .in(!CollectionUtils.isEmpty(listGrant),GrantUnit::getId,listGrant)
                );

        //删除已生成的相同发放单元的记录,放到 各科室 异步中，各自删自己的
        //this.removeBySecond(secondTask,listGrant);


        //提前准备各种数据 进redis
        this.getSecCacheData(secondTask);
        

        for (GrantUnit grantUnit : grantUnitList){
            //获取发放单元方案
            Programme programme = programmeService.getByUnitId(grantUnit.getId());
            if(programme == null){
                continue;
            }
            //异步执行
            unitTaskAsyncService.createByGrantUnit(grantUnit,secondTask,programme);

        }
    }

    private void getSecCacheData(SecondTask secondTask) {
        String cycle = secondTask.getCycle();
        secRedisService.clearCache(cycle);
        secRedisService.projectList(cycle);
        secRedisService.detailList(cycle);
        secRedisService.itemList(cycle);
        secRedisService.attendanceList(cycle);
    }

    @Override
    public void removeByGrantUnitId(SecondTask secondTask, GrantUnit grantUnit) {
        List<UnitTask> unitTaskList = this.list(Wrappers.<UnitTask>lambdaQuery()
                .eq(UnitTask::getCycle, secondTask.getCycle())
                .eq(UnitTask::getSecondTaskId, secondTask.getId())
                .eq( UnitTask::getGrantUnitId, grantUnit.getId()));
        List<Long> unitTaskIds = unitTaskList.stream().map(UnitTask::getId).collect(Collectors.toList());
        if(unitTaskIds.isEmpty()){
            return;
        }
        //删除 unitTask
        this.removeBatchByIds(unitTaskIds);
    }

    private void removeBySecond(SecondTask secondTask, List<String> listGrant) {
        List<UnitTask> unitTaskList = this.list(Wrappers.<UnitTask>lambdaQuery()
                .eq(UnitTask::getCycle, secondTask.getCycle())
                .eq(UnitTask::getSecondTaskId, secondTask.getId())
                .in(!CollectionUtils.isEmpty(listGrant), UnitTask::getGrantUnitId, listGrant));
        List<Long> unitTaskIds = unitTaskList.stream().map(UnitTask::getId).collect(Collectors.toList());
        if(unitTaskIds.isEmpty()){
            return;
        }
        //删除 unitTask
        this.removeBatchByIds(unitTaskIds);

        //删除count
        //secondTaskCountService.remove(Wrappers.<SecondTaskCount>lambdaQuery().in(SecondTaskCount::get))
    }

    @Override
    public BigDecimal getKsAmtNonStaff(GrantUnit grantUnit, String cycle) {
        //List<Long> ksUnitList = CommonUtils.longs2List(grantUnit.getKsUnitIds());
        //List<FirstTaskCountDTO> list = dmoUtil.firstCount(cycle,grantUnit.getKsUnitIdsNonStaff());
        List<FirstTaskCountDTO> list = secondKpiService.firstCount(cycle,grantUnit.getKsUnitIdsNonStaff());
        BigDecimal rtnAmt = BigDecimal.ZERO;
        for (FirstTaskCountDTO firstTaskCountDTO : list){
            String amt = firstTaskCountDTO.getKsAmt();
            if(amt != null){
                rtnAmt = rtnAmt.add(new BigDecimal(amt));
            }
        }
        log.info("getKsAmtNonStaff:{}",rtnAmt);
        return rtnAmt;
    }

    @Override
    public BigDecimal getKsAmtExtraUserIds(GrantUnit grantUnit, String cycle) {
        //List<Long> ksUnitList = CommonUtils.longs2List(grantUnit.getKsUnitIds());
        List<RepotZhigongjxValueDTO> list = secondKpiService.zhigongjxList(cycle, grantUnit.getExtraUserIds());
        BigDecimal rtnAmt = BigDecimal.ZERO;
        for (RepotZhigongjxValueDTO dto : list){
            String amt = dto.getAmt();
            if(amt != null){
                rtnAmt = rtnAmt.add(new BigDecimal(amt));
            }
        }
        log.info("getKsAmtExtraUserIds:{}",rtnAmt);
        return rtnAmt;
    }

    @Override
    public BigDecimal getKsAmt(GrantUnit grantUnit, String cycle) {
        //List<FirstTaskCountDTO> list = dmoUtil.firstCount(cycle,grantUnit.getKsUnitIds());
        List<FirstTaskCountDTO> list = secondKpiService.firstCount(cycle,grantUnit.getKsUnitIds());

        BigDecimal rtnAmt = BigDecimal.ZERO;
        for (FirstTaskCountDTO firstTaskCountDTO : list){
            String amt = firstTaskCountDTO.getKsAmt();
            if(amt != null){
                rtnAmt = rtnAmt.add(new BigDecimal(amt));
            }
        }
        log.info("getKsAmt1:{}",rtnAmt);
        //查询编外人员的金额信息,删除编外人员信息
        List<UserTypeVo> userInStaffVos = secondKpiService.queryUserWorkTypeList(cycle, UserType.Y.getCode(),grantUnit.getKsUnitIds());
        if(!CollectionUtils.isEmpty(userInStaffVos)){
            for (UserTypeVo userTypeVo : userInStaffVos){
                String amt = userTypeVo.getAmt();
                if(StrUtil.isNotBlank(amt)){
                    rtnAmt = rtnAmt.add(new BigDecimal(userTypeVo.getAmt()));
                }
            }
        }
        log.info("getKsAmt2:{}",rtnAmt);
        return rtnAmt;
    }

    @Override
    public UnitTask getLastUnitTask(Long unitTaskId, Long grantUnitId) {
        return this.getOne(Wrappers.<UnitTask>lambdaQuery()
                .eq(UnitTask::getIfFinish,"1")
                .eq(UnitTask::getGrantUnitId,grantUnitId)
                .ne(UnitTask::getId,unitTaskId)
                .notLikeLeft(UnitTask::getCycle,"13")
                .orderByDesc(UnitTask::getCreateTime)
                .last("limit 1"));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void syncByProgramme(Programme programme) {
        Long grantUnitId = programme.getGrantUnitId();
        //找到发放单元是否有进行中的任务
        if (grantUnitId != null) {
            List<UnitTask> unitTaskList = this.list(Wrappers.<UnitTask>lambdaQuery()
                    .eq(UnitTask::getGrantUnitId, grantUnitId)
                    .in(UnitTask::getStatus, Arrays.asList(SecondDistributionTaskStatusEnum.UNCOMMITTED, SecondDistributionTaskStatusEnum.APPROVAL_REJECTED)));
            if(!unitTaskList.isEmpty()) {
                //将这些任务的方案换成 新启用的方案
                unitTaskList.forEach(unitTask -> {
                    unitTask.setProgrammeId(programme.getId());
                });
                this.updateBatchById(unitTaskList);
                //同步任务
                unitTaskList.forEach(unitTaskProjectService::syncByUnitTask);
            }
        }

        //未提交 和 审批中的，使用了 该方案的任务
        //List<UnitTask> unitTaskList = this.list(Wrappers.<UnitTask>lambdaQuery().eq(UnitTask::getProgrammeId,programme.getId()).in(UnitTask::getStatus, Arrays.asList(SecondDistributionTaskStatusEnum.UNCOMMITTED, SecondDistributionTaskStatusEnum.APPROVAL_REJECTED)));
        //unitTaskList.forEach(unitTaskProjectService::syncByUnitTask);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void toOnline(Long unitTaskId) {
        if(unitTaskId == null){
            throw new BizException("id不能为空");
        }
        UnitTask unitTask = this.getById(unitTaskId);
        unitTask.setIfUpload("1");
        this.updateById(unitTask);

        GrantUnit grantUnit = grantUnitService.getById(unitTask.getGrantUnitId());
        grantUnit.setIfUpload("1");
        grantUnitService.updateById(grantUnit);
    }



    @Override
    @Transactional(rollbackFor = Exception.class)
    public void toOffline(Long unitTaskId) {
        if(unitTaskId == null){
            throw new BizException("id不能为空");
        }
        UnitTask unitTask = this.getById(unitTaskId);
        unitTask.setIfUpload("0");
        this.updateById(unitTask);

        GrantUnit grantUnit = grantUnitService.getById(unitTask.getGrantUnitId());
        grantUnit.setIfUpload("0");
        grantUnitService.updateById(grantUnit);
    }

}
