package com.hscloud.hs.cost.account.model.vo;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 分配管理列表vo
 */
@Schema
@Data
public class SecondDistributionManagementVo {
    @Schema(description = "分配任务id")
    private Long taskUnitId;

    @Schema(description = "分配任务名称")
    private String taskName;

    @Schema(description = "任务类型")
    private String taskType;

    @Schema(description = "方案id")
    private Long planId;

    @Schema(description = "分配周期")
    private String taskPeriod;

    @Schema(description = "接收日期")
    private LocalDateTime receiptDate;

    @Schema(description = "状态")
    private String status;
}
