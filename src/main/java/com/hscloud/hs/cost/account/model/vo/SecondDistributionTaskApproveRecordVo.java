package com.hscloud.hs.cost.account.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 返回二次审核列表vo
 */
@Data
@Schema(description = "二次审核列表vo")
public class SecondDistributionTaskApproveRecordVo {
    @Schema(description = "分配任务id")
    private Long taskId;

    @Schema(description = "分配任务名称")
    private String taskName;

    @Schema(description = "任务类型")
    private String taskType;

    @Schema(description = "分配周期")
    private String taskPeriod;

    @Schema(description = "未提交数量")
    private Long uncommittedCount;

    @Schema(description = "待我审批数量")
    private Long todoCount;

    @Schema(description = "审批中数量")
    private Long pendingApprovalCount;

    @Schema(description = "审批通过数量")
    private Long approvalApprovedCount;

    @Schema(description = "审批驳回数量")
    private Long approvalRejectedCount;

    @Schema(description = "状态")
    private String status;

}
