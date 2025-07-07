package com.hscloud.hs.cost.account.model.dto.kpi;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class KpiItemEquivalentTaskDTO {
    private Long id;

    @Schema(description = "科室id")
    private Long accountUnitId;

    @Schema(description = "周期")
    private Long period;

    @Schema(description = "任务状态 -1:驳回 0:未提交 10:待审核 20:通过")
    private String status;

    @Schema(description = "科室id列表")
    private List<Long> accountUnitIds;

    @Schema(description = "驳回原因")
    private String reason;
}
