package com.hscloud.hs.cost.account.model.pojo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class SecondDistributionTaskUnitDetail {


    @Schema(description = "任务id")
    private Long taskId;

    @Schema(description = "方案id")
    private Long planId;

    @Schema(description = "科室单元id")
    private Long unitId;

    @Schema(description = "总共可分配金额")
    private BigDecimal totalAmount;

    @Schema(description = "时间周期")
    private String period;

    @Schema(description = "提交时间")
    private LocalDateTime submitTime;

    @Schema(description = "租户id")
    private Long tenantId;
}
