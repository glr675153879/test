package com.hscloud.hs.cost.account.controller.second;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hscloud.hs.cost.account.constant.enums.SecondDistributionTaskStatusEnum;
import com.hscloud.hs.cost.account.model.dto.SecondDistributionTaskApproveQueryDto;
import com.hscloud.hs.cost.account.model.dto.SecondDistributionTaskSubmitDto;
import com.hscloud.hs.cost.account.model.dto.SecondTaskApprovingRecordQueryDto;
import com.hscloud.hs.cost.account.model.dto.second.TaskApproveQueryDto;
import com.hscloud.hs.cost.account.model.dto.second.TaskSubmitDto;
import com.hscloud.hs.cost.account.model.entity.base.PageRequest;
import com.hscloud.hs.cost.account.model.entity.second.Attendance;
import com.hscloud.hs.cost.account.model.entity.second.UnitTask;
import com.hscloud.hs.cost.account.service.ISecondDistributionTaskUnitInfoService;
import com.hscloud.hs.cost.account.service.second.IOaService;
import com.hscloud.hs.cost.account.service.second.IUnitTaskService;
import com.hscloud.hs.oa.workflow.api.dto.ProcessInstanceApproveDto;
import com.hscloud.hs.oa.workflow.api.dto.ProcessInstanceRejectDto;
import com.hscloud.hs.oa.workflow.api.dto.process.ProcessFormChangeDto;
import com.pig4cloud.pigx.common.core.util.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.annotations.Param;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

/**
 * @author 小小w
 * @date 2024/3/9 11:42
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/kpi_second_approve")
@Tag(name = "approve", description = "二次分配流程")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class OaController {
    private final IOaService oaService;
    private final IUnitTaskService unitTaskService;


    /**
     * 分配审核列表
     */
    @GetMapping("/list")
    @Operation(summary = "分配审核列表")
    public R getApproveLost(SecondTaskApprovingRecordQueryDto dto) {
        return R.ok(oaService.getList(dto));
    }

    /**
     * 提交审核
     */
    @PostMapping("/create")
    @Operation(summary = "提交审核")
    public R create(@RequestBody TaskSubmitDto taskSubmitDto) {
        return R.ok(oaService.create(taskSubmitDto));
    }


    /**
     * 未提交列表
     */
    @GetMapping("/unCommit")
    @Operation(summary = "未提交列表")
    public R unCommit(PageRequest<UnitTask> pr) {
        QueryWrapper<UnitTask> wrapper = pr.getWrapper();
        wrapper.eq("status", SecondDistributionTaskStatusEnum.UNCOMMITTED.getCode());
        return R.ok(unitTaskService.page(pr.getPage(),wrapper));
    }




    /**
     * 待我审核列表
     */
    @GetMapping("/todo")
    @Operation(summary = "待审核列表")
    public R todo(PageRequest<UnitTask> pr) {
        QueryWrapper<UnitTask> wrapper = pr.getWrapper();
        wrapper.eq("status", SecondDistributionTaskStatusEnum.PENDING_APPROVAL.getCode());
        return R.ok(unitTaskService.page(pr.getPage(),wrapper));
    }

    /**
     * 审核中列表
     */
    @GetMapping("/approvingList")
    @Operation(summary = "审核中列表")
    public R listByApproving(PageRequest<UnitTask> pr) {
        QueryWrapper<UnitTask> wrapper = pr.getWrapper();
        wrapper.eq("status", SecondDistributionTaskStatusEnum.UNDERWAY.getCode());
        return R.ok(unitTaskService.page(pr.getPage(),wrapper));
    }

    /**
     * 审核通过列表
     */
    @GetMapping("/passedList")
    @Operation(summary = "审核通过列表")
    public R listByPassed(PageRequest<UnitTask> pr) {
        QueryWrapper<UnitTask> wrapper = pr.getWrapper();
        wrapper.eq("status", SecondDistributionTaskStatusEnum.APPROVAL_APPROVED.getCode());
        return R.ok(unitTaskService.page(pr.getPage(),wrapper));
    }

    /**
     * 审核驳回列表
     */
    @GetMapping("/rejectList")
    @Operation(summary = "审核驳回列表")
    public R listByReject(PageRequest<UnitTask> pr) {
        QueryWrapper<UnitTask> wrapper = pr.getWrapper();
        wrapper.eq("status", SecondDistributionTaskStatusEnum.APPROVAL_REJECTED.getCode());
        return R.ok(unitTaskService.page(pr.getPage(),wrapper));
    }


    /**
     * 审核通过
     */
    @PostMapping("/approve")
    @Operation(summary = "审核通过")
    public R approve(@RequestBody ProcessInstanceApproveDto processInstanceApproveDto) {
        return oaService.approve(processInstanceApproveDto);
    }

    @PostMapping("/revoke")
    @Operation(summary = "撤回")
    public R revoke(@RequestBody ProcessFormChangeDto processFormChangeDto) {
        return oaService.revoke(processFormChangeDto);
    }


    /**
     * 审核驳回
     */
    @PostMapping("/reject")
    @Operation(summary = "审核驳回")
    public R reject(@RequestBody ProcessInstanceRejectDto processInstanceRejectDto) {
        return oaService.reject(processInstanceRejectDto);
    }

    @PostMapping("/reject1")
    @Operation(summary = "已经通过的审批单 审核驳回")
    public R reject1(@RequestBody ProcessInstanceRejectDto processInstanceRejectDto) {
        return oaService.reject1(processInstanceRejectDto);
    }




    /**
     * 审核详情
     */
    @GetMapping("/detail")
    @Operation(summary = "审核详情")
    public R detail(@Param("id") String id) {
        return R.ok(oaService.processDetail(id));
    }


}
