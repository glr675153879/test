package com.hscloud.hs.cost.account.model.dto.kpi;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class KpiReportConfigDto {
    private Long id;
    @Schema(description = "报表id")
    private Long reportId;
    @Schema(description = "列表上有 入参带进来")
    private Long taskChildId;
    @Schema(description = "是否过滤值为0 1是2否")
    private int filterZero;
    @Schema(description = "周期 逗号隔开")
    private Long periodsBegin;
    @Schema(description = "周期 逗号隔开")
    private Long periodsEnd;
    @Schema(description = "",hidden = true)
    private List<Long> taskChildIds;
}
