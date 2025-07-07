package com.hscloud.hs.cost.account.service.impl.second;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hscloud.hs.cost.account.config.ScheduledTask;
import com.hscloud.hs.cost.account.constant.Constant;
import com.hscloud.hs.cost.account.constant.enums.SecondDistributionTaskStatusEnum;
import com.hscloud.hs.cost.account.mapper.second.GrantUnitLogMapper;
import com.hscloud.hs.cost.account.mapper.second.SecondTaskMapper;
import com.hscloud.hs.cost.account.mapper.second.UnitTaskMapper;
import com.hscloud.hs.cost.account.model.dto.SecondDistributionTaskApproveQueryDto;
import com.hscloud.hs.cost.account.model.dto.SecondTaskApprovingRecordQueryDto;
import com.hscloud.hs.cost.account.model.dto.second.TaskApproveQueryDto;
import com.hscloud.hs.cost.account.model.dto.second.TaskSubmitDto;
import com.hscloud.hs.cost.account.model.entity.CostAccountUnit;
import com.hscloud.hs.cost.account.model.entity.SecondDistributionTask;
import com.hscloud.hs.cost.account.model.entity.SecondDistributionTaskUnitInfo;
import com.hscloud.hs.cost.account.model.entity.second.GrantUnitLog;
import com.hscloud.hs.cost.account.model.entity.second.SecondTask;
import com.hscloud.hs.cost.account.model.entity.second.UnitTask;
import com.hscloud.hs.cost.account.model.vo.SecondDistributionTaskApproveRecordVo;
import com.hscloud.hs.cost.account.model.vo.SecondDistributionTaskApproveVo;
import com.hscloud.hs.cost.account.service.second.*;
import com.hscloud.hs.oa.workflow.api.common.enums.EnumProcTempBizResult;
import com.hscloud.hs.oa.workflow.api.dto.*;
import com.hscloud.hs.oa.workflow.api.dto.process.FormComponentValueDto;
import com.hscloud.hs.oa.workflow.api.dto.process.ProcessFormChangeDto;
import com.hscloud.hs.oa.workflow.api.feign.RemoteOaFlwInstanceService;
import com.hscloud.hs.oa.workflow.api.vo.*;
import com.pig4cloud.pigx.admin.api.entity.SysDept;
import com.pig4cloud.pigx.admin.api.feign.RemoteUserService;
import com.pig4cloud.pigx.admin.api.vo.UserVO;
import com.pig4cloud.pigx.common.core.exception.BizException;
import com.pig4cloud.pigx.common.core.util.R;
import com.pig4cloud.pigx.common.security.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author 小小w
 * @date 2024/3/9 11:50
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OaServiceImpl extends ServiceImpl<UnitTaskMapper, UnitTask> implements IOaService {
    private final RemoteOaFlwInstanceService remoteOaFlwInstanceService;
    private final RemoteUserService remoteUserService;

    private final ISecondTaskService secondTaskService;
    private final IUnitTaskService unitTaskService;
    private final IUnitTaskCountService unitTaskCountService;
    private final ISecondTaskCountService secondTaskCountService;
    /**
     * * 提交
     * @param taskSubmitDto
     * @return
     */
    @Override
    public Object create(TaskSubmitDto taskSubmitDto) {
        ProcessInstanceCreateDto processInstanceCreateDto = new ProcessInstanceCreateDto();
        BeanUtils.copyProperties(taskSubmitDto, processInstanceCreateDto);
        UnitTask unitTask = new UnitTask();
        for (FormComponentValueDto formComponentValue : taskSubmitDto.getFormComponentValues()) {
            if ("taskUnitId".equals(formComponentValue.getKey())) {
                String taskUnitId = formComponentValue.getValue().toString();
                unitTask = new UnitTask().selectById(taskUnitId);
            }
        }
        //查询是否有待审核中的记录,有则不允许再次申请记录
//        SecondDistributionTaskUnitInfo secondDistributionTaskUnitInfo = new SecondDistributionTaskUnitInfo().selectById(taskSubmitDto.getTaskUnitId());
        if (unitTask == null) {
            throw new BizException("任务不存在，请刷新后重试");
        }
        if (!SecondDistributionTaskStatusEnum.UNCOMMITTED.getCode().equals(unitTask.getStatus()) && !SecondDistributionTaskStatusEnum.APPROVAL_REJECTED.getCode().equals(unitTask.getStatus())) {
            throw new BizException("该任务已提交过审核，请勿重复提交");
        }
        //检查taskCount 是否都大于等于0
        if(!unitTaskCountService.checkAmt(unitTask.getId())){
            throw new BizException("每位职工的绩效不可为负数，请调整后再提交");
        }



        try {
            String instanceId = remoteOaFlwInstanceService.create(processInstanceCreateDto).getData();
            //修改我的分配任务状态
            //unitTask.setCreateBy(SecurityUtils.getUser().getId()+"");
            //unitTask.setName(SecurityUtils.getUser().getName());
            unitTask.setInstanceId(Long.parseLong(instanceId));
            List<FormComponentValueDto> formComponentValues = processInstanceCreateDto.getFormComponentValues();
            for (FormComponentValueDto formComponentValue : formComponentValues) {
                if ("planId".equals(formComponentValue.getKey())) {
                    unitTask.setProgrammeId(Long.valueOf(formComponentValue.getValue().toString()));
                }
            }
            //unitTask.setProcessCode(processInstanceCreateDto.getProcessCode());
            unitTask.setStatus(SecondDistributionTaskStatusEnum.PENDING_APPROVAL.getCode());
            //unitTask.setCreateTime(LocalDateTime.now());
            unitTask.setSubmitTime(LocalDateTime.now());
            this.updateById(unitTask);

            //提交时候计算 secondCount
            secondTaskCountService.doCount(unitTask.getSecondTaskId(),unitTask.getId());

            return instanceId;
        } catch (Exception e) {
            log.error("审核失败", e);
            throw new BizException("提交审核失败");
        }
    }

    /**
     * 通过
     * @param processInstanceApproveDto
     * @return
     */
    @Override
    public R approve(ProcessInstanceApproveDto processInstanceApproveDto) {
        remoteOaFlwInstanceService.approve(processInstanceApproveDto);
        //修改审核状态
        ProcessInstanceVo secondDistributionTaskApproveDetailVo = this.processDetail(processInstanceApproveDto.getInstanceId());
        log.info("审核结果{}", secondDistributionTaskApproveDetailVo.getResult());
        if (EnumProcTempBizResult.agree.getCode().equals(secondDistributionTaskApproveDetailVo.getResult())) {
            UnitTask unitTask = new UnitTask().selectOne(new LambdaQueryWrapper<UnitTask>()
                    .eq(UnitTask::getInstanceId, processInstanceApproveDto.getInstanceId()));
            unitTask.setStatus(SecondDistributionTaskStatusEnum.APPROVAL_APPROVED.getCode());
            unitTask.setIfFinish("1");
            unitTask.setEndTime(LocalDateTime.now());
            this.updateById(unitTask);

            //所有unitTask完成，增secondTask完成
            secondTaskService.finishCheck(unitTask.getSecondTaskId());
        }
        return R.ok();
    }
    /**
     * 获取审核详情
     *
     * @param id
     * @return
     */
    @Override
    public ProcessInstanceVo processDetail(String id) {
        ProcessInstanceDto processInstanceDto = new ProcessInstanceDto();
        processInstanceDto.setId(id);
        ProcessInstanceVo data = remoteOaFlwInstanceService.get(processInstanceDto).getData();
        return data;
    }

    /**
     * 驳回
     * @param processInstanceRejectDto
     * @return
     */
    @Override
    public R reject(ProcessInstanceRejectDto processInstanceRejectDto) {
        remoteOaFlwInstanceService.reject(processInstanceRejectDto);
        //修改审核状态
        ProcessInstanceVo secondDistributionTaskApproveDetailVo = this.processDetail(processInstanceRejectDto.getInstanceId());
        log.info("审核驳回{}", secondDistributionTaskApproveDetailVo.getResult());
        if (EnumProcTempBizResult.refuse.getCode().equals(secondDistributionTaskApproveDetailVo.getResult())) {
            UnitTask unitTask = new UnitTask().selectOne(new LambdaQueryWrapper<UnitTask>()
                    .eq(UnitTask::getInstanceId, processInstanceRejectDto.getInstanceId()));
            unitTask.setStatus(SecondDistributionTaskStatusEnum.APPROVAL_REJECTED.getCode());
            this.updateById(unitTask);

            //提交时候计算 secondCount
            secondTaskCountService.doCount(unitTask.getSecondTaskId(),unitTask.getId());
        }
        return R.ok();
    }

    @Override
    public Page<SecondDistributionTaskApproveRecordVo> getList(SecondTaskApprovingRecordQueryDto dto) {
        //根据条件查询出所有的任务列表
        Page<SecondTask> page = new SecondTask().selectPage(new Page<>(dto.getCurrent(), dto.getSize()), new LambdaQueryWrapper<SecondTask>()
                .like(StrUtil.isNotBlank(dto.getTaskName()), SecondTask::getName, dto.getTaskName())
                .eq(StrUtil.isNotBlank(dto.getTaskPeriod()), SecondTask::getCycle, dto.getTaskPeriod())
                .eq(StrUtil.isNotBlank(dto.getStatus()), SecondTask::getStatus, dto.getStatus()).orderByDesc(SecondTask::getCycle));
        // 封装返回结果
        List<SecondTask> secondTaskList = page.getRecords();
        List<SecondDistributionTaskApproveRecordVo> recordVoList = new ArrayList<>();
        //根据任务列表获取任务的所有提交信息
        for (SecondTask secondTask : secondTaskList) {
            List<UnitTask> unitTaskList = new UnitTask().selectList(new LambdaQueryWrapper<UnitTask>()
                    .eq(UnitTask::getSecondTaskId, secondTask.getId()));
            //封装vo
            SecondDistributionTaskApproveRecordVo recordVo = new SecondDistributionTaskApproveRecordVo();
            recordVo.setTaskId(secondTask.getId());
            recordVo.setTaskName(secondTask.getName());
            recordVo.setTaskType("二次分配");
            recordVo.setTaskPeriod(secondTask.getCycle());
            if (CollUtil.isNotEmpty(unitTaskList)) {
                //取出所有的实例id
                List<Long> instanceIdList = unitTaskList.stream().map(UnitTask::getInstanceId).collect(Collectors.toList());

                //统计各类状态数量
                recordVo.setUncommittedCount(unitTaskList.stream()
                        .filter(info -> SecondDistributionTaskStatusEnum.UNCOMMITTED.getCode().equals(info.getStatus()))
                        .count());
                recordVo.setTodoCount(unitTaskList.stream()
                        .filter(info -> SecondDistributionTaskStatusEnum.PENDING_APPROVAL.getCode().equals(info.getStatus()))
                        .count());
                recordVo.setPendingApprovalCount(unitTaskList.stream()
                        .filter(info -> SecondDistributionTaskStatusEnum.PENDING_APPROVAL.getCode().equals(info.getStatus()))
                        .count());
                recordVo.setApprovalApprovedCount(unitTaskList.stream()
                        .filter(info -> SecondDistributionTaskStatusEnum.APPROVAL_APPROVED.getCode().equals(info.getStatus()))
                        .count());
                recordVo.setApprovalRejectedCount(unitTaskList.stream()
                        .filter(info -> SecondDistributionTaskStatusEnum.APPROVAL_REJECTED.getCode().equals(info.getStatus()))
                        .count());
            } else {
                // 当 secondDistributionTaskUnitInfoList 为空时，将统计数量设置为0
                recordVo.setUncommittedCount(0L);
                recordVo.setPendingApprovalCount(0L);
                recordVo.setApprovalApprovedCount(0L);
                recordVo.setApprovalRejectedCount(0L);
                recordVo.setTodoCount(0L);
            }
            recordVo.setStatus(secondTask.getStatus());
            recordVoList.add(recordVo);

        }
        Page<SecondDistributionTaskApproveRecordVo> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        voPage.setRecords(recordVoList);
        return voPage;
    }

    /**
     * 列表
     * @param
     * @return
     */
//    @Override
//    public Page<SecondDistributionTaskApproveRecordVo> getList(SecondTaskApprovingRecordQueryDto dto) {
//        //根据条件查询出所有的任务列表
//        Page<UnitTask> page = new UnitTask().selectPage(new Page<>(dto.getCurrent(), dto.getSize()), new LambdaQueryWrapper<UnitTask>()
//                .like(StrUtil.isNotBlank(dto.getTaskName()), UnitTask::getName, dto.getTaskName())
//                .eq(StrUtil.isNotBlank(dto.getTaskPeriod()), UnitTask::getCycle, dto.getTaskPeriod())
//                .eq(StrUtil.isNotBlank(dto.getStatus()), UnitTask::getStatus, dto.getStatus()));
//        // 封装返回结果
//        List<UnitTask> taskList = page.getRecords();
//        List<SecondDistributionTaskApproveRecordVo> recordVoList = new ArrayList<>();
//        //根据任务列表获取任务的所有提交信息
//        for (UnitTask unitTask : taskList) {
//            //封装vo
//            SecondDistributionTaskApproveRecordVo recordVo = new SecondDistributionTaskApproveRecordVo();
//            recordVo.setTaskId(unitTask.getId());
//            recordVo.setTaskName(unitTask.getName());
//            //todo 发放类型
//            //recordVo.setTaskType(unitTask.getType());
//            recordVo.setTaskPeriod(unitTask.getCycle());
//                //取出所有的实例id
//                List<Long> instanceIdList = secondDistributionTaskUnitInfoList.stream().map(SecondDistributionTaskUnitInfo::getProcessInstanceId).collect(Collectors.toList());
//
//                FlwInstanceTaskTodoDto flwInstanceTaskTodoDto = new FlwInstanceTaskTodoDto();
//                flwInstanceTaskTodoDto.setAppCode(Constant.SECOND_TASK_CODE);
//                flwInstanceTaskTodoDto.setInstanceIds(instanceIdList);
//                flwInstanceTaskTodoDto.setNeedButton(true);
//
//                //根据实例id取出待我审核的实例id
//                Page<FlwInstanceTaskVo> pageR = remoteOaFlwInstanceService.todo(flwInstanceTaskTodoDto).getData();
//
//                //统计各类状态数量
//                recordVo.setUncommittedCount(secondDistributionTaskUnitInfoList.stream()
//                        .filter(info -> SecondDistributionTaskStatusEnum.UNCOMMITTED.getCode().equals(info.getStatus()))
//                        .count());
//                recordVo.setPendingApprovalCount(secondDistributionTaskUnitInfoList.stream()
//                        .filter(info -> SecondDistributionTaskStatusEnum.PENDING_APPROVAL.getCode().equals(info.getStatus()))
//                        .count());
//                recordVo.setApprovalApprovedCount(secondDistributionTaskUnitInfoList.stream()
//                        .filter(info -> SecondDistributionTaskStatusEnum.APPROVAL_APPROVED.getCode().equals(info.getStatus()))
//                        .count());
//                recordVo.setApprovalRejectedCount(secondDistributionTaskUnitInfoList.stream()
//                        .filter(info -> SecondDistributionTaskStatusEnum.APPROVAL_REJECTED.getCode().equals(info.getStatus()))
//                        .count());
//                recordVo.setTodoCount(pageR == null ? 0L : (long) pageR.getRecords().size());
//            recordVo.setStatus(unitTask.getStatus());
//            recordVoList.add(recordVo);
//
//        }
//        Page<SecondDistributionTaskApproveRecordVo> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
//        voPage.setRecords(recordVoList);
//        return voPage;
//    }

    @Override
    public R unCommit(TaskApproveQueryDto approveQueryDto) {
        Long taskId = approveQueryDto.getTaskId();
        Page<UnitTask> page = unitTaskService.page(new Page<>(approveQueryDto.getCurrent(), approveQueryDto.getSize()),Wrappers.<UnitTask>lambdaQuery().eq(UnitTask::getSecondTaskId,taskId).eq(UnitTask::getStatus,SecondDistributionTaskStatusEnum.UNCOMMITTED.getCode()));
        return R.ok(page);
//
//        // 构造分页对象
//        Page<UnitTask> page = new Page<>(approveQueryDto.getCurrent(), approveQueryDto.getSize());
//
//        // 执行分页查询
//        List<UnitTask> secondDistributionTaskUnitInfoList = new UnitTask().selectPage(page, new LambdaQueryWrapper<UnitTask>()
//                .like(StrUtil.isNotEmpty(approveQueryDto.getUnitName()), UnitTask::getName, approveQueryDto.getUnitName())
//                .eq(UnitTask::getStatus, SecondDistributionTaskStatusEnum.UNCOMMITTED.getCode())
//                .eq(UnitTask::getProgrammeId, approveQueryDto.getTaskId())
//        ).getRecords();
//        SecondTask secondTask = new SecondTask().selectById(approveQueryDto.getTaskId());
//        List<SecondDistributionTaskApproveVo> voList = secondDistributionTaskUnitInfoList.stream().map(secondDistributionTaskDeptInfo -> {
//            //封装返回vo
//            SecondDistributionTaskApproveVo approveVo = new SecondDistributionTaskApproveVo();
//            approveVo.setTaskUnitId(secondDistributionTaskDeptInfo.getId());
//
//            approveVo.setTaskName(secondTask.getName());
//            //任务类型
//            //approveVo.setTaskType(secondTask.getType());
//            approveVo.setTaskPeriod(secondTask.getCycle());
//            // TODO 目前获取的是unitId，待确定是unitId还是deptId
//            approveVo.setDeptName(new CostAccountUnit().selectById(secondDistributionTaskDeptInfo.getGrantUnitId()).getName());
//            approveVo.setStatus(secondDistributionTaskDeptInfo.getStatus());
//            return approveVo;
//        }).collect(Collectors.toList());
//
//        Page<SecondDistributionTaskApproveVo> resultPage = new Page<>(approveQueryDto.getCurrent(), approveQueryDto.getSize());
//        resultPage.setRecords(voList); // 设置查询结果
//        resultPage.setTotal(page.getTotal()); // 设置总记录数
//        return R.ok(resultPage);
    }

    @Override
    public R todo(TaskApproveQueryDto approveQueryDto) {
        Long taskId = approveQueryDto.getTaskId();
        Page<UnitTask> page = unitTaskService.page(new Page<>(approveQueryDto.getCurrent(), approveQueryDto.getSize()),Wrappers.<UnitTask>lambdaQuery().eq(UnitTask::getSecondTaskId,taskId).eq(UnitTask::getStatus,SecondDistributionTaskStatusEnum.PENDING_APPROVAL.getCode()));
        return R.ok(page);
//
//        FlwInstanceTaskTodoDto flwInstanceTaskTodoDto = new FlwInstanceTaskTodoDto();
//        BeanUtils.copyProperties(approveQueryDto, flwInstanceTaskTodoDto);
//        flwInstanceTaskTodoDto.setAppCode(Constant.SECOND_TASK_CODE);
//        //根据条件查询出我的流程id
//        List<Long> instanceIdList = getInstanceIdList(approveQueryDto);
//        flwInstanceTaskTodoDto.setInstanceIds(instanceIdList);
//        flwInstanceTaskTodoDto.setNeedButton(true);
//        Page<SecondDistributionTaskApproveVo> page = new Page<>(approveQueryDto.getCurrent(), approveQueryDto.getSize());
//        List<SecondDistributionTaskApproveVo> voList = new ArrayList<>();
//        // 构造分页对象
//        //根据实例id取出待我审核的实例id
//        Page<FlwInstanceTaskVo> pageR = remoteOaFlwInstanceService.todo(flwInstanceTaskTodoDto).getData();
//        if (pageR != null && CollUtil.isNotEmpty(pageR.getRecords())) {
//            List<FlwInstanceTaskVo> records = pageR.getRecords();
//            for (FlwInstanceTaskVo flwInstanceTaskVo : records) {
//                SecondDistributionTaskApproveVo taskApproveVo = BeanUtil.copyProperties(flwInstanceTaskVo, SecondDistributionTaskApproveVo.class);
//                //填充提交人
//                if (taskApproveVo.getCreateBy() != null) {
//                    //Todo 暂时没有状态返回
////                    SecondDistributionTaskUnitInfo taskUnitInfo = new SecondDistributionTaskUnitInfo().selectOne(new LambdaQueryWrapper<SecondDistributionTaskUnitInfo>()
////                            .eq(SecondDistributionTaskUnitInfo::getProcessInstanceId, taskApproveVo.getId()));
////                    taskApproveVo.setStatus();
//                    UserVO userVO = remoteUserService.details(taskApproveVo.getCreateBy()).getData();
//                    taskApproveVo.setSubmitName(userVO.getName());
//                    //填充发放单元名称
//                    if (!CollectionUtils.isEmpty(userVO.getDeptList())) {
//                        SysDept sysDept = userVO.getDeptList().get(0);
//                        taskApproveVo.setDeptName(sysDept.getName());
//                    }
//                    voList.add(taskApproveVo);
//                }
//            }
//            page.setTotal(pageR.getTotal());
//        }
//        page.setRecords(voList);
//        return R.ok(page);
    }

    @Override
    public R listApproving(TaskApproveQueryDto approveQueryDto) {
        Long taskId = approveQueryDto.getTaskId();
        Page<UnitTask> page = unitTaskService.page(new Page<>(approveQueryDto.getCurrent(), approveQueryDto.getSize()),Wrappers.<UnitTask>lambdaQuery().eq(UnitTask::getSecondTaskId,taskId).eq(UnitTask::getStatus,SecondDistributionTaskStatusEnum.PENDING_APPROVAL.getCode()));
        return R.ok(page);
//        FlwInstanceTaskApprovingDto oaFlwInstanceDto = new FlwInstanceTaskApprovingDto();
//        BeanUtils.copyProperties(approveQueryDto, oaFlwInstanceDto);
//        oaFlwInstanceDto.setAppCode(Constant.SECOND_TASK_CODE);
//        //根据条件查询出我的流程id
//        List<Long> instanceIdList = getInstanceIdList(approveQueryDto);
//        oaFlwInstanceDto.setInstanceIds(instanceIdList);
//        List<SecondDistributionTaskApproveVo> voList = new ArrayList<>();
//        Page<SecondDistributionTaskApproveVo> page = new Page<>(approveQueryDto.getCurrent(), approveQueryDto.getSize());
//        //根据实例id取出待我审核的实例id
//        Page<FlwInstanceApprovingVo> pageR = remoteOaFlwInstanceService.listByApproving(oaFlwInstanceDto).getData();
//        if (pageR != null && CollUtil.isNotEmpty(pageR.getRecords())) {
//            List<FlwInstanceApprovingVo> records = pageR.getRecords();
//            for (FlwInstanceApprovingVo flwInstanceTaskVo : records) {
//
//                SecondDistributionTaskApproveVo taskApproveVo = BeanUtil.copyProperties(flwInstanceTaskVo, SecondDistributionTaskApproveVo.class);
//                //填充提交人
//                if (taskApproveVo.getCreateBy() != null) {
//                    UserVO userVO = remoteUserService.details(taskApproveVo.getCreateBy()).getData();
//                    taskApproveVo.setSubmitName(userVO.getName());
//                    //填充发放单元名称
//                    if (!CollectionUtils.isEmpty(userVO.getDeptList())) {
//                        SysDept sysDept = userVO.getDeptList().get(0);
//                        taskApproveVo.setDeptName(sysDept.getName());
//                    }
//                    voList.add(taskApproveVo);
//                }
//            }
//            page.setTotal(pageR.getTotal());
//        }
//
//        page.setRecords(voList);
//        return R.ok(page);
    }

    @Override
    public R listPassed(TaskApproveQueryDto approveQueryDto) {
        Long taskId = approveQueryDto.getTaskId();
        Page<UnitTask> page = unitTaskService.page(new Page<>(approveQueryDto.getCurrent(), approveQueryDto.getSize()),Wrappers.<UnitTask>lambdaQuery().eq(UnitTask::getSecondTaskId,taskId).eq(UnitTask::getStatus,SecondDistributionTaskStatusEnum.APPROVAL_APPROVED.getCode()));
        return R.ok(page);

//        FlwInstanceTaskPassedDto passedDto = new FlwInstanceTaskPassedDto();
//        BeanUtils.copyProperties(approveQueryDto, passedDto);
//        passedDto.setAppCode(Constant.SECOND_TASK_CODE);
//        //根据条件查询出我的流程id
//        List<Long> instanceIdList = getInstanceIdList(approveQueryDto);
//        passedDto.setInstanceIds(instanceIdList);
//        List<SecondDistributionTaskApproveVo> voList = new ArrayList<>();
//        Page<SecondDistributionTaskApproveVo> page = new Page<>(approveQueryDto.getCurrent(), approveQueryDto.getSize());
//        //根据实例id取出审核通过的实例id
//        Page<FlwInstancePassedVo> pageR = remoteOaFlwInstanceService.secondDistributionApprovedList(passedDto).getData();
//        if (pageR != null && CollUtil.isNotEmpty(pageR.getRecords())) {
//            List<FlwInstancePassedVo> records = pageR.getRecords();
//            for (FlwInstancePassedVo flwInstanceTaskVo : records) {
//
//                SecondDistributionTaskApproveVo taskApproveVo = BeanUtil.copyProperties(flwInstanceTaskVo, SecondDistributionTaskApproveVo.class);
//                //填充提交人
//                if (taskApproveVo.getCreateBy() != null) {
//                    UserVO userVO = remoteUserService.details(taskApproveVo.getCreateBy()).getData();
//                    taskApproveVo.setSubmitName(userVO.getName());
//                    //填充发放单元名称
//                    if (!CollectionUtils.isEmpty(userVO.getDeptList())) {
//                        SysDept sysDept = userVO.getDeptList().get(0);
//                        taskApproveVo.setDeptName(sysDept.getName());
//                    }
//                    voList.add(taskApproveVo);
//                }
//            }
//            page.setTotal(pageR.getTotal());
//        }
//
//        page.setRecords(voList);
//        return R.ok(page);
    }

    @Override
    public R listReject(TaskApproveQueryDto approveQueryDto) {
        Long taskId = approveQueryDto.getTaskId();
        Page<UnitTask> page = unitTaskService.page(new Page<>(approveQueryDto.getCurrent(), approveQueryDto.getSize()),Wrappers.<UnitTask>lambdaQuery().eq(UnitTask::getSecondTaskId,taskId).eq(UnitTask::getStatus,SecondDistributionTaskStatusEnum.APPROVAL_REJECTED.getCode()));
        return R.ok(page);
//        FlwInstanceTaskRejectDto rejectDto = new FlwInstanceTaskRejectDto();
//        BeanUtils.copyProperties(approveQueryDto, rejectDto);
//        rejectDto.setAppCode(Constant.SECOND_TASK_CODE);
//        //根据条件查询出我的流程id
//        List<Long> instanceIdList = getInstanceIdList(approveQueryDto);
//        rejectDto.setInstanceIds(instanceIdList);
//        List<SecondDistributionTaskApproveVo> voList = new ArrayList<>();
//        Page<SecondDistributionTaskApproveVo> page = new Page<>(approveQueryDto.getCurrent(), approveQueryDto.getSize());
//        //根据实例id取出待我审核的实例id
//        Page<FlwInstanceRejectVo> pageR = remoteOaFlwInstanceService.listByReject(rejectDto).getData();
//        if (pageR != null && CollUtil.isNotEmpty(pageR.getRecords())) {
//            List<FlwInstanceRejectVo> records = pageR.getRecords();
//            for (FlwInstanceRejectVo flwInstanceTaskVo : records) {
//
//                SecondDistributionTaskApproveVo taskApproveVo = BeanUtil.copyProperties(flwInstanceTaskVo, SecondDistributionTaskApproveVo.class);
//                //填充提交人
//                if (taskApproveVo.getCreateBy() != null) {
//                    UserVO userVO = remoteUserService.details(taskApproveVo.getCreateBy()).getData();
//                    taskApproveVo.setSubmitName(userVO.getName());
//                    //填充发放单元名称
//                    if (!CollectionUtils.isEmpty(userVO.getDeptList())) {
//                        SysDept sysDept = userVO.getDeptList().get(0);
//                        taskApproveVo.setDeptName(sysDept.getName());
//                    }
//                    voList.add(taskApproveVo);
//                }
//            }
//            page.setTotal(pageR.getTotal());
//        }
//
//        page.setRecords(voList);
//        return R.ok(page);
    }

    @Override
    public R revoke(ProcessFormChangeDto processFormChangeDto) {
        String instanceId = processFormChangeDto.getInstanceId();
        remoteOaFlwInstanceService.revoke(processFormChangeDto);
        //修改审核状态
        ProcessInstanceVo secondDistributionTaskApproveDetailVo = this.processDetail(instanceId);
        log.info("审核结果{}", secondDistributionTaskApproveDetailVo.getResult());
        if (EnumProcTempBizResult.agree.getCode().equals(secondDistributionTaskApproveDetailVo.getResult())) {
            UnitTask unitTask = new UnitTask().selectOne(new LambdaQueryWrapper<UnitTask>()
                    .eq(UnitTask::getInstanceId, instanceId));

            unitTask.setStatus(SecondDistributionTaskStatusEnum.UNCOMMITTED.getCode());
            this.updateById(unitTask);
            //提交时候计算 secondCount
            secondTaskCountService.doCount(unitTask.getSecondTaskId(),unitTask.getId());
        }

        return R.ok();
    }

    @Override
    public R reject1(ProcessInstanceRejectDto processInstanceRejectDto) {
        String instanceId = processInstanceRejectDto.getInstanceId();
        //流程实例 只能本人或者管理员删，这里不上流程实例
//        ProcessInstanceCommonDto processInstanceCommonDto = new ProcessInstanceCommonDto();
//        processInstanceCommonDto.setId(instanceId);
//        processInstanceCommonDto.setAppCode(processInstanceRejectDto.getAppCode());
//        remoteOaFlwInstanceService.delete(processInstanceCommonDto);

        UnitTask unitTask = new UnitTask().selectOne(new LambdaQueryWrapper<UnitTask>()
                .eq(UnitTask::getInstanceId, instanceId));
        unitTask.setStatus(SecondDistributionTaskStatusEnum.APPROVAL_REJECTED.getCode());
        unitTask.setInstanceId(-1L);
//        unitTask.setStartTime("");
//        unitTask.setEndTime("");
        this.updateById(unitTask);

        return R.ok();
    }

    /**
     * 圈出所有的流程实例id
     *
     * @param approveQueryDto
     * @return
     */
    @NotNull
    private List<Long> getInstanceIdList(TaskApproveQueryDto approveQueryDto) {
        List<SecondDistributionTaskUnitInfo> secondDistributionTaskUnitInfoList = new SecondDistributionTaskUnitInfo().selectList(
                new LambdaQueryWrapper<SecondDistributionTaskUnitInfo>()
                        .eq(SecondDistributionTaskUnitInfo::getTaskId, approveQueryDto.getTaskId())
                        .like(StrUtil.isNotEmpty(approveQueryDto.getUnitName()), SecondDistributionTaskUnitInfo::getUnitName, approveQueryDto.getUnitName())
        );

        if (CollUtil.isEmpty(secondDistributionTaskUnitInfoList)) {
            SecondDistributionTaskUnitInfo secondDistributionTaskUnitInfo = new SecondDistributionTaskUnitInfo();
            secondDistributionTaskUnitInfo.setProcessInstanceId(null);
            secondDistributionTaskUnitInfoList.add(secondDistributionTaskUnitInfo);
        }
        //取出所有的实例id
        List<Long> instanceIdList = secondDistributionTaskUnitInfoList.stream().map(SecondDistributionTaskUnitInfo::getProcessInstanceId).filter(Objects::nonNull).collect(Collectors.toList());
        return instanceIdList;
    }
}
