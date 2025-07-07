package com.hscloud.hs.cost.account.model.dto.kpi;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class KpiReportYearDto {
    @Schema(description = "报表id")
    private Long reportId;
    @Schema(description = "列表上有 入参带进来")
    private Long taskChildId;
    @Schema(description = "科室跳转入参")
    private Long deptId;
}
