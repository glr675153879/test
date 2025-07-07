package com.hscloud.hs.cost.account.controller;


import com.hscloud.hs.cost.account.model.dto.SecondDistributionTaskApproveQueryDto;
import com.hscloud.hs.cost.account.model.dto.SecondDistributionTaskSubmitDto;
import com.hscloud.hs.cost.account.model.dto.SecondTaskApprovingRecordQueryDto;
import com.hscloud.hs.cost.account.service.ISecondDistributionTaskUnitInfoService;
import com.hscloud.hs.oa.workflow.api.dto.ProcessInstanceApproveDto;
import com.hscloud.hs.oa.workflow.api.dto.ProcessInstanceRejectDto;
import com.pig4cloud.pigx.common.core.util.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.annotations.Param;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 二次分配任务和科室单元关联表 前端控制器
 * </p>
 *
 * @author 
 * @since 2023-11-23
 */
@RestController
@RequestMapping("/kpi_second_approve_bak")
@Tag(description = "second-distribution-task-unit-info", name = "分配审核")
@RequiredArgsConstructor
public class SecondDistributionTaskUnitInfoController {

    private final ISecondDistributionTaskUnitInfoService taskDeptInfoService;


    /**
     * 分配审核列表
     */
    @GetMapping("/list")
    @Operation(summary = "分配审核列表")
    public R getApproveLost(SecondTaskApprovingRecordQueryDto dto) {
        return R.ok(taskDeptInfoService.getList(dto));
    }

    /**
     * 提交审核
     */
    @PostMapping("/create")
    @Operation(summary = "提交审核")
    public R create(@RequestBody SecondDistributionTaskSubmitDto taskSubmitDto) {
        return R.ok(taskDeptInfoService. create(taskSubmitDto));
    }


    /**
     * 未提交列表
     */
    @PostMapping("/unCommit")
    @Operation(summary = "未提交列表")
    public R unCommit(@RequestBody SecondDistributionTaskApproveQueryDto approveQueryDto) {
        return taskDeptInfoService.unCommit(approveQueryDto);
    }




    /**
     * 待我审核列表
     */
    @PostMapping("/todo")
    @Operation(summary = "待审核列表")
    public R todo(@RequestBody SecondDistributionTaskApproveQueryDto approveQueryDto) {
        return taskDeptInfoService.todo(approveQueryDto);
    }

    /**
     * 审核中列表
     */
    @PostMapping("/approvingList")
    @Operation(summary = "审核中列表")
    public R listByApproving(@RequestBody SecondDistributionTaskApproveQueryDto approveQueryDto) {
        return taskDeptInfoService.listApproving(approveQueryDto);
    }

    /**
     * 审核通过列表
     */
    @PostMapping("/passedList")
    @Operation(summary = "审核通过列表")
    public R listByPassed(@RequestBody SecondDistributionTaskApproveQueryDto approveQueryDto) {
        return taskDeptInfoService.listPassed(approveQueryDto);
    }

    /**
     * 审核驳回列表
     */
    @PostMapping("/rejectList")
    @Operation(summary = "审核驳回列表")
    public R listByReject(@RequestBody SecondDistributionTaskApproveQueryDto approveQueryDto) {
        return taskDeptInfoService.listReject(approveQueryDto);
    }


    /**
     * 审核通过
     */
    @PostMapping("/approve")
    @Operation(summary = "审核通过")
    public R approve(@RequestBody ProcessInstanceApproveDto processInstanceApproveDto) {
        return taskDeptInfoService.approve(processInstanceApproveDto);
    }

    /**
     * 审核驳回
     */
    @PostMapping("/reject")
    @Operation(summary = "审核驳回")
    public R reject(@RequestBody ProcessInstanceRejectDto processInstanceRejectDto) {
        return taskDeptInfoService.reject(processInstanceRejectDto);
    }



    /**
     * 审核详情
     */
    @GetMapping("/detail")
    @Operation(summary = "审核详情")
    public R detail(@Param("id") String id) {
        return R.ok(taskDeptInfoService.processDetail(id));
    }





}
