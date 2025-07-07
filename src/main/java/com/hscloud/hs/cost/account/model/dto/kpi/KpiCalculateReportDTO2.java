package com.hscloud.hs.cost.account.model.dto.kpi;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class KpiCalculateReportDTO2 {
    @Schema(description = "指标项")
    private String indexCode;
    @Schema(description = "")
    private String planCode;
    @Schema(description = "核算单元id 人的userid或科室单元的id")
    private String id;
    @Schema(description = "类型 1人2科室")
    private int type;
    /*@Schema(description = "列表上有 入参带进来")
    private int period;*/
    @Schema(description = "列表上有 入参带进来")
    private Long taskChildId;
    @Schema(description = "是否过滤值为0 1是2否")
    private Long filterZero;
}
