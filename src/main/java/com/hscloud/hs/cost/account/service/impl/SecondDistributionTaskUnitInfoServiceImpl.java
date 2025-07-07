package com.hscloud.hs.cost.account.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import com.hscloud.hs.cost.account.constant.CacheConstants;
import com.hscloud.hs.cost.account.constant.Constant;
import com.hscloud.hs.cost.account.constant.enums.SecondDistributionTaskStatusEnum;
import com.hscloud.hs.cost.account.mapper.SecondDistributionTaskUnitInfoMapper;
import com.hscloud.hs.cost.account.model.dto.SecondDistributionTaskApproveQueryDto;
import com.hscloud.hs.cost.account.model.dto.SecondDistributionTaskSubmitDto;
import com.hscloud.hs.cost.account.model.dto.SecondTaskApprovingRecordQueryDto;
import com.hscloud.hs.cost.account.model.entity.CostAccountUnit;
import com.hscloud.hs.cost.account.model.entity.SecondDistributionTask;
import com.hscloud.hs.cost.account.model.entity.SecondDistributionTaskUnitInfo;
import com.hscloud.hs.cost.account.model.pojo.SecondDistributionTaskUnitDetail;
import com.hscloud.hs.cost.account.model.vo.SecondDistributionTaskApproveRecordVo;
import com.hscloud.hs.cost.account.model.vo.SecondDistributionTaskApproveVo;
import com.hscloud.hs.cost.account.service.ISecondDistributionTaskService;
import com.hscloud.hs.cost.account.service.ISecondDistributionTaskUnitInfoService;
import com.hscloud.hs.cost.account.utils.RedisUtil;
import com.hscloud.hs.cost.account.utils.SqlUtil;
import com.hscloud.hs.oa.workflow.api.common.enums.EnumProcTempBizResult;
import com.hscloud.hs.oa.workflow.api.dto.*;
import com.hscloud.hs.oa.workflow.api.dto.process.FormComponentValueDto;
import com.hscloud.hs.oa.workflow.api.feign.RemoteOaFlwInstanceService;
import com.hscloud.hs.oa.workflow.api.vo.*;
import com.pig4cloud.pigx.admin.api.entity.SysDept;
import com.pig4cloud.pigx.admin.api.feign.RemoteDeptService;
import com.pig4cloud.pigx.admin.api.feign.RemoteUserService;
import com.pig4cloud.pigx.admin.api.vo.UserVO;
import com.pig4cloud.pigx.common.core.exception.BizException;
import com.pig4cloud.pigx.common.core.util.R;
import com.pig4cloud.pigx.common.security.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * <p>
 * 二次分配任务和科室单元关联表 服务实现类
 * </p>
 *
 * @author
 * @since 2023-11-23
 */
@Service
@RequiredArgsConstructor
public class SecondDistributionTaskUnitInfoServiceImpl extends ServiceImpl<SecondDistributionTaskUnitInfoMapper, SecondDistributionTaskUnitInfo> implements ISecondDistributionTaskUnitInfoService {


    private final RemoteOaFlwInstanceService remoteOaFlwInstanceService;

    private final RemoteUserService remoteUserService;

    private final ISecondDistributionTaskService distributionTaskService;

    private final RemoteDeptService remoteDeptService;

    private final SqlUtil sqlUtil;


    private final RedisUtil redisUtil;

    @Autowired
    private SecondDistributionTaskUnitInfoMapper secondDistributionTaskUnitInfoMapper;


    /**
     * 提交审核
     *
     * @param taskSubmitDto
     * @return
     */
    @Override
    public Object create(SecondDistributionTaskSubmitDto taskSubmitDto) {

        ProcessInstanceCreateDto processInstanceCreateDto = new ProcessInstanceCreateDto();
        BeanUtils.copyProperties(taskSubmitDto, processInstanceCreateDto);
        SecondDistributionTaskUnitInfo secondDistributionTaskUnitInfo = new SecondDistributionTaskUnitInfo();
        for (FormComponentValueDto formComponentValue : taskSubmitDto.getFormComponentValues()) {
            if ("taskUnitId".equals(formComponentValue.getKey())) {
                String taskUnitId = formComponentValue.getValue().toString();
                secondDistributionTaskUnitInfo = new SecondDistributionTaskUnitInfo().selectById(taskUnitId);
            }
        }
        //查询是否有待审核中的记录,有则不允许再次申请记录
//        SecondDistributionTaskUnitInfo secondDistributionTaskUnitInfo = new SecondDistributionTaskUnitInfo().selectById(taskSubmitDto.getTaskUnitId());
        if (secondDistributionTaskUnitInfo == null) {
            throw new BizException("任务不存在，请刷新后重试");
        }
        if (secondDistributionTaskUnitInfo != null && !SecondDistributionTaskStatusEnum.UNCOMMITTED.getCode().equals(secondDistributionTaskUnitInfo.getStatus())) {
            throw new BizException("该任务已提交过审核，请勿重复提交");
        }
        try {
            String instanceId = remoteOaFlwInstanceService.create(processInstanceCreateDto).getData();
            //修改我的分配任务状态
            secondDistributionTaskUnitInfo.setUserId(SecurityUtils.getUser().getId());
            secondDistributionTaskUnitInfo.setName(SecurityUtils.getUser().getName());
            secondDistributionTaskUnitInfo.setProcessInstanceId(Long.parseLong(instanceId));
            List<FormComponentValueDto> formComponentValues = processInstanceCreateDto.getFormComponentValues();
            for (FormComponentValueDto formComponentValue : formComponentValues) {
                if ("planId".equals(formComponentValue.getKey())) {
                    secondDistributionTaskUnitInfo.setPlanId(Long.valueOf(formComponentValue.getValue().toString()));
                }
            }
            secondDistributionTaskUnitInfo.setProcessCode(processInstanceCreateDto.getProcessCode());
            secondDistributionTaskUnitInfo.setStatus(SecondDistributionTaskStatusEnum.PENDING_APPROVAL.getCode());
            secondDistributionTaskUnitInfo.setSubmitTime(LocalDateTime.now());
            this.updateById(secondDistributionTaskUnitInfo);
            return instanceId;
        } catch (Exception e) {
            log.error("审核失败{}", e);
            throw new BizException("提交审核失败");
        }
    }


    /**
     * 待我审核列表
     * @return
     */
    @Override
    public R todo(SecondDistributionTaskApproveQueryDto approveQueryDto) {
        FlwInstanceTaskTodoDto flwInstanceTaskTodoDto = new FlwInstanceTaskTodoDto();
        BeanUtils.copyProperties(approveQueryDto, flwInstanceTaskTodoDto);
        flwInstanceTaskTodoDto.setAppCode(Constant.SECOND_TASK_CODE);
        //根据条件查询出我的流程id
        List<Long> instanceIdList = getInstanceIdList(approveQueryDto);
        flwInstanceTaskTodoDto.setInstanceIds(instanceIdList);
        flwInstanceTaskTodoDto.setNeedButton(true);
        Page<SecondDistributionTaskApproveVo> page = new Page<>(approveQueryDto.getCurrent(), approveQueryDto.getSize());
        List<SecondDistributionTaskApproveVo> voList = new ArrayList<>();
        // 构造分页对象
        //根据实例id取出待我审核的实例id
        Page<FlwInstanceTaskVo> pageR = remoteOaFlwInstanceService.todo(flwInstanceTaskTodoDto).getData();
        if (pageR != null && CollUtil.isNotEmpty(pageR.getRecords())) {
            List<FlwInstanceTaskVo> records = pageR.getRecords();
            for (FlwInstanceTaskVo flwInstanceTaskVo : records) {
                SecondDistributionTaskApproveVo taskApproveVo = BeanUtil.copyProperties(flwInstanceTaskVo, SecondDistributionTaskApproveVo.class);
                //填充提交人
                if (taskApproveVo.getCreateBy() != null) {
                    //Todo 暂时没有状态返回
//                    SecondDistributionTaskUnitInfo taskUnitInfo = new SecondDistributionTaskUnitInfo().selectOne(new LambdaQueryWrapper<SecondDistributionTaskUnitInfo>()
//                            .eq(SecondDistributionTaskUnitInfo::getProcessInstanceId, taskApproveVo.getId()));
//                    taskApproveVo.setStatus();
                    UserVO userVO = remoteUserService.details(taskApproveVo.getCreateBy()).getData();
                    taskApproveVo.setSubmitName(userVO.getName());
                    //填充发放单元名称
                    if (!CollectionUtils.isEmpty(userVO.getDeptList())) {
                        SysDept sysDept = userVO.getDeptList().get(0);
                        taskApproveVo.setDeptName(sysDept.getName());
                    }
                    voList.add(taskApproveVo);
                }
            }
            page.setTotal(pageR.getTotal());
        }
        page.setRecords(voList);
        return R.ok(page);
    }

    /**
     * 审核中列表
     *
     * @param approveQueryDto 参数
     * @return 审核中列表
     */
    @Override
    public R listApproving(SecondDistributionTaskApproveQueryDto approveQueryDto) {
        FlwInstanceTaskApprovingDto oaFlwInstanceDto = new FlwInstanceTaskApprovingDto();
        BeanUtils.copyProperties(approveQueryDto, oaFlwInstanceDto);
        oaFlwInstanceDto.setAppCode(Constant.SECOND_TASK_CODE);
        //根据条件查询出我的流程id
        List<Long> instanceIdList = getInstanceIdList(approveQueryDto);
        oaFlwInstanceDto.setInstanceIds(instanceIdList);
        List<SecondDistributionTaskApproveVo> voList = new ArrayList<>();
        Page<SecondDistributionTaskApproveVo> page = new Page<>(approveQueryDto.getCurrent(), approveQueryDto.getSize());
        //根据实例id取出待我审核的实例id
        Page<FlwInstanceApprovingVo> pageR = remoteOaFlwInstanceService.listByApproving(oaFlwInstanceDto).getData();
        if (pageR != null && CollUtil.isNotEmpty(pageR.getRecords())) {
            List<FlwInstanceApprovingVo> records = pageR.getRecords();
            for (FlwInstanceApprovingVo flwInstanceTaskVo : records) {

                SecondDistributionTaskApproveVo taskApproveVo = BeanUtil.copyProperties(flwInstanceTaskVo, SecondDistributionTaskApproveVo.class);
                //填充提交人
                if (taskApproveVo.getCreateBy() != null) {
                    UserVO userVO = remoteUserService.details(taskApproveVo.getCreateBy()).getData();
                    taskApproveVo.setSubmitName(userVO.getName());
                    //填充发放单元名称
                    if (!CollectionUtils.isEmpty(userVO.getDeptList())) {
                        SysDept sysDept = userVO.getDeptList().get(0);
                        taskApproveVo.setDeptName(sysDept.getName());
                    }
                    voList.add(taskApproveVo);
                }
            }
            page.setTotal(pageR.getTotal());
        }

        page.setRecords(voList);
        return R.ok(page);
    }

    /**
     * 审核通过列表
     *
     * @param approveQueryDto
     * @return
     */
    @Override
    public R listPassed(SecondDistributionTaskApproveQueryDto approveQueryDto) {
        FlwInstanceTaskPassedDto passedDto = new FlwInstanceTaskPassedDto();
        BeanUtils.copyProperties(approveQueryDto, passedDto);
        passedDto.setAppCode(Constant.SECOND_TASK_CODE);
        //根据条件查询出我的流程id
        List<Long> instanceIdList = getInstanceIdList(approveQueryDto);
        passedDto.setInstanceIds(instanceIdList);
        List<SecondDistributionTaskApproveVo> voList = new ArrayList<>();
        Page<SecondDistributionTaskApproveVo> page = new Page<>(approveQueryDto.getCurrent(), approveQueryDto.getSize());
        //根据实例id取出审核通过的实例id
        Page<FlwInstancePassedVo> pageR = remoteOaFlwInstanceService.secondDistributionApprovedList(passedDto).getData();
        if (pageR != null && CollUtil.isNotEmpty(pageR.getRecords())) {
            List<FlwInstancePassedVo> records = pageR.getRecords();
            for (FlwInstancePassedVo flwInstanceTaskVo : records) {

                SecondDistributionTaskApproveVo taskApproveVo = BeanUtil.copyProperties(flwInstanceTaskVo, SecondDistributionTaskApproveVo.class);
                //填充提交人
                if (taskApproveVo.getCreateBy() != null) {
                    UserVO userVO = remoteUserService.details(taskApproveVo.getCreateBy()).getData();
                    taskApproveVo.setSubmitName(userVO.getName());
                    //填充发放单元名称
                    if (!CollectionUtils.isEmpty(userVO.getDeptList())) {
                        SysDept sysDept = userVO.getDeptList().get(0);
                        taskApproveVo.setDeptName(sysDept.getName());
                    }
                    voList.add(taskApproveVo);
                }
            }
            page.setTotal(pageR.getTotal());
        }

        page.setRecords(voList);
        return R.ok(page);
    }

    /**
     * 审核驳回列表
     *
     * @param approveQueryDto
     * @return
     */
    @Override
    public R listReject(SecondDistributionTaskApproveQueryDto approveQueryDto) {
        FlwInstanceTaskRejectDto rejectDto = new FlwInstanceTaskRejectDto();
        BeanUtils.copyProperties(approveQueryDto, rejectDto);
        rejectDto.setAppCode(Constant.SECOND_TASK_CODE);
        //根据条件查询出我的流程id
        List<Long> instanceIdList = getInstanceIdList(approveQueryDto);
        rejectDto.setInstanceIds(instanceIdList);
        List<SecondDistributionTaskApproveVo> voList = new ArrayList<>();
        Page<SecondDistributionTaskApproveVo> page = new Page<>(approveQueryDto.getCurrent(), approveQueryDto.getSize());
        //根据实例id取出待我审核的实例id
        Page<FlwInstanceRejectVo> pageR = remoteOaFlwInstanceService.listByReject(rejectDto).getData();
        if (pageR != null && CollUtil.isNotEmpty(pageR.getRecords())) {
            List<FlwInstanceRejectVo> records = pageR.getRecords();
            for (FlwInstanceRejectVo flwInstanceTaskVo : records) {

                SecondDistributionTaskApproveVo taskApproveVo = BeanUtil.copyProperties(flwInstanceTaskVo, SecondDistributionTaskApproveVo.class);
                //填充提交人
                if (taskApproveVo.getCreateBy() != null) {
                    UserVO userVO = remoteUserService.details(taskApproveVo.getCreateBy()).getData();
                    taskApproveVo.setSubmitName(userVO.getName());
                    //填充发放单元名称
                    if (!CollectionUtils.isEmpty(userVO.getDeptList())) {
                        SysDept sysDept = userVO.getDeptList().get(0);
                        taskApproveVo.setDeptName(sysDept.getName());
                    }
                    voList.add(taskApproveVo);
                }
            }
            page.setTotal(pageR.getTotal());
        }

        page.setRecords(voList);
        return R.ok(page);
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
     * 未提交审核列表
     *
     * @param approveQueryDto
     * @return
     */
    @Override
    public R unCommit(SecondDistributionTaskApproveQueryDto approveQueryDto) {
        // 构造分页对象
        Page<SecondDistributionTaskUnitInfo> page = new Page<>(approveQueryDto.getCurrent(), approveQueryDto.getSize());

        // 执行分页查询
        List<SecondDistributionTaskUnitInfo> secondDistributionTaskUnitInfoList = new SecondDistributionTaskUnitInfo().selectPage(page, new LambdaQueryWrapper<SecondDistributionTaskUnitInfo>()
                .like(StrUtil.isNotEmpty(approveQueryDto.getUnitName()), SecondDistributionTaskUnitInfo::getUnitName, approveQueryDto.getUnitName())
                .eq(SecondDistributionTaskUnitInfo::getStatus, SecondDistributionTaskStatusEnum.UNCOMMITTED.getCode())
                .eq(SecondDistributionTaskUnitInfo::getTaskId, approveQueryDto.getTaskId())
        ).getRecords();
        SecondDistributionTask secondDistributionTask = new SecondDistributionTask().selectById(approveQueryDto.getTaskId());
        List<SecondDistributionTaskApproveVo> voList = secondDistributionTaskUnitInfoList.stream().map(secondDistributionTaskDeptInfo -> {
            //封装返回vo
            SecondDistributionTaskApproveVo approveVo = new SecondDistributionTaskApproveVo();
            approveVo.setTaskUnitId(secondDistributionTaskDeptInfo.getId());

            approveVo.setTaskName(secondDistributionTask.getName());
            approveVo.setTaskType(secondDistributionTask.getType());
            approveVo.setTaskPeriod(secondDistributionTask.getTaskPeriod());
            // TODO 目前获取的是unitId，待确定是unitId还是deptId
            approveVo.setDeptName(new CostAccountUnit().selectById(secondDistributionTaskDeptInfo.getUnitId()).getName());
            approveVo.setStatus(secondDistributionTaskDeptInfo.getStatus());
            return approveVo;
        }).collect(Collectors.toList());

        Page<SecondDistributionTaskApproveVo> resultPage = new Page<>(approveQueryDto.getCurrent(), approveQueryDto.getSize());
        resultPage.setRecords(voList); // 设置查询结果
        resultPage.setTotal(page.getTotal()); // 设置总记录数
        return R.ok(resultPage);
    }

    /**
     * 获取二次审核列表
     *
     * @param dto
     * @return
     */
    @Override
    public Page<SecondDistributionTaskApproveRecordVo> getList(SecondTaskApprovingRecordQueryDto dto) {
        //根据条件查询出所有的任务列表
        Page<SecondDistributionTask> page = new SecondDistributionTask().selectPage(new Page<>(dto.getCurrent(), dto.getSize()), new LambdaQueryWrapper<SecondDistributionTask>()
                .like(StrUtil.isNotBlank(dto.getTaskName()), SecondDistributionTask::getName, dto.getTaskName())
                .eq(StrUtil.isNotBlank(dto.getTaskPeriod()), SecondDistributionTask::getTaskPeriod, dto.getTaskPeriod())
                .eq(StrUtil.isNotBlank(dto.getStatus()), SecondDistributionTask::getStatus, dto.getStatus()));
        // 封装返回结果
        List<SecondDistributionTask> taskList = page.getRecords();
        List<SecondDistributionTaskApproveRecordVo> recordVoList = new ArrayList<>();
        //根据任务列表获取任务的所有提交信息
        for (SecondDistributionTask secondDistributionTask : taskList) {
            List<SecondDistributionTaskUnitInfo> secondDistributionTaskUnitInfoList = new SecondDistributionTaskUnitInfo().selectList(new LambdaQueryWrapper<SecondDistributionTaskUnitInfo>()
                    .eq(SecondDistributionTaskUnitInfo::getTaskId, secondDistributionTask.getId()));
            //封装vo
            SecondDistributionTaskApproveRecordVo recordVo = new SecondDistributionTaskApproveRecordVo();
            recordVo.setTaskId(secondDistributionTask.getId());
            recordVo.setTaskName(secondDistributionTask.getName());
            recordVo.setTaskType(secondDistributionTask.getType());
            recordVo.setTaskPeriod(secondDistributionTask.getTaskPeriod());
            if (CollUtil.isNotEmpty(secondDistributionTaskUnitInfoList)) {
                //取出所有的实例id
                List<Long> instanceIdList = secondDistributionTaskUnitInfoList.stream().map(SecondDistributionTaskUnitInfo::getProcessInstanceId).collect(Collectors.toList());

                FlwInstanceTaskTodoDto flwInstanceTaskTodoDto = new FlwInstanceTaskTodoDto();
                flwInstanceTaskTodoDto.setAppCode(Constant.SECOND_TASK_CODE);
                flwInstanceTaskTodoDto.setInstanceIds(instanceIdList);
                flwInstanceTaskTodoDto.setNeedButton(true);

                //根据实例id取出待我审核的实例id
                Page<FlwInstanceTaskVo> pageR = remoteOaFlwInstanceService.todo(flwInstanceTaskTodoDto).getData();

                //统计各类状态数量
                recordVo.setUncommittedCount(secondDistributionTaskUnitInfoList.stream()
                        .filter(info -> SecondDistributionTaskStatusEnum.UNCOMMITTED.getCode().equals(info.getStatus()))
                        .count());
                recordVo.setPendingApprovalCount(secondDistributionTaskUnitInfoList.stream()
                        .filter(info -> SecondDistributionTaskStatusEnum.PENDING_APPROVAL.getCode().equals(info.getStatus()))
                        .count());
                recordVo.setApprovalApprovedCount(secondDistributionTaskUnitInfoList.stream()
                        .filter(info -> SecondDistributionTaskStatusEnum.APPROVAL_APPROVED.getCode().equals(info.getStatus()))
                        .count());
                recordVo.setApprovalRejectedCount(secondDistributionTaskUnitInfoList.stream()
                        .filter(info -> SecondDistributionTaskStatusEnum.APPROVAL_REJECTED.getCode().equals(info.getStatus()))
                        .count());
                recordVo.setTodoCount(pageR == null ? 0L : (long) pageR.getRecords().size());
            } else {
                // 当 secondDistributionTaskUnitInfoList 为空时，将统计数量设置为0
                recordVo.setUncommittedCount(0L);
                recordVo.setPendingApprovalCount(0L);
                recordVo.setApprovalApprovedCount(0L);
                recordVo.setApprovalRejectedCount(0L);
                recordVo.setTodoCount(0L);
            }
            recordVo.setStatus(secondDistributionTask.getStatus());
            recordVoList.add(recordVo);

        }
        Page<SecondDistributionTaskApproveRecordVo> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        voPage.setRecords(recordVoList);
        return voPage;
    }

    /**
     * 审核通过列表
     *
     * @param processInstanceApproveDto
     * @return
     */
    @Override
    public R approve(ProcessInstanceApproveDto processInstanceApproveDto) {
        remoteOaFlwInstanceService.approve(processInstanceApproveDto);
        //修改审核状态
        ProcessInstanceVo secondDistributionTaskApproveDetailVo = this.processDetail(processInstanceApproveDto.getInstanceId());
        if (EnumProcTempBizResult.agree.getCode().equals(secondDistributionTaskApproveDetailVo.getResult())) {
            SecondDistributionTaskUnitInfo taskUnitInfo = new SecondDistributionTaskUnitInfo().selectOne(new LambdaQueryWrapper<SecondDistributionTaskUnitInfo>()
                    .eq(SecondDistributionTaskUnitInfo::getProcessInstanceId, processInstanceApproveDto.getInstanceId()));
            taskUnitInfo.setStatus(SecondDistributionTaskStatusEnum.APPROVAL_APPROVED.getCode());
            this.updateById(taskUnitInfo);
        }
        return R.ok();
    }

    /**
     * 审核驳回列表
     *
     * @param processInstanceRejectDto
     * @return
     */
    @Override
    public R reject(ProcessInstanceRejectDto processInstanceRejectDto) {
        remoteOaFlwInstanceService.reject(processInstanceRejectDto);
        //修改审核状态
        ProcessInstanceVo secondDistributionTaskApproveDetailVo = this.processDetail(processInstanceRejectDto.getInstanceId());
        if (EnumProcTempBizResult.refuse.getCode().equals(secondDistributionTaskApproveDetailVo.getResult())) {
            SecondDistributionTaskUnitInfo taskUnitInfo = new SecondDistributionTaskUnitInfo().selectOne(new LambdaQueryWrapper<SecondDistributionTaskUnitInfo>()
                    .eq(SecondDistributionTaskUnitInfo::getProcessInstanceId, processInstanceRejectDto.getInstanceId()));
            taskUnitInfo.setStatus(SecondDistributionTaskStatusEnum.APPROVAL_REJECTED.getCode());
            this.updateById(taskUnitInfo);
        }
        return R.ok();
    }

    /**
     * 圈出所有的流程实例id
     *
     * @param approveQueryDto
     * @return
     */
    @NotNull
    private List<Long> getInstanceIdList(SecondDistributionTaskApproveQueryDto approveQueryDto) {
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

    @Override
    public SecondDistributionTaskUnitDetail getTaskUnitInfoById(Long id) {
        SecondDistributionTaskUnitDetail secondDistributionTaskUnitDetail = redisUtil.getObject(String.format(CacheConstants.SECOND_DISTRIBUTION_TASK_UNIT_DETAIL, id), SecondDistributionTaskUnitDetail.class);
        if (secondDistributionTaskUnitDetail != null) {
            return secondDistributionTaskUnitDetail;
        }
        MPJLambdaWrapper<SecondDistributionTaskUnitInfo> wrapper = new MPJLambdaWrapper<>();
        wrapper.select(SecondDistributionTaskUnitInfo::getId, SecondDistributionTaskUnitInfo::getUnitId, SecondDistributionTaskUnitInfo::getTotalAmount, SecondDistributionTaskUnitInfo::getTaskId,
                        SecondDistributionTaskUnitInfo::getSubmitTime, SecondDistributionTaskUnitInfo::getPlanId, SecondDistributionTaskUnitInfo::getTenantId)
                .leftJoin(SecondDistributionTask.class, SecondDistributionTask::getId, SecondDistributionTaskUnitInfo::getTaskId)
                .selectAs(SecondDistributionTask::getTaskPeriod, SecondDistributionTaskUnitDetail::getPeriod)
                .eq(SecondDistributionTaskUnitInfo::getId, id);
        secondDistributionTaskUnitDetail = secondDistributionTaskUnitInfoMapper.selectJoinOne(SecondDistributionTaskUnitDetail.class, wrapper);
        if (secondDistributionTaskUnitDetail != null) {
            Map<String, Object> map = BeanUtil.beanToMap(secondDistributionTaskUnitDetail);
            redisUtil.putObject(String.format(CacheConstants.SECOND_DISTRIBUTION_TASK_UNIT_DETAIL, id), map, CacheConstants.duration, TimeUnit.SECONDS);
            return secondDistributionTaskUnitDetail;
        }
        return null;
    }
}
