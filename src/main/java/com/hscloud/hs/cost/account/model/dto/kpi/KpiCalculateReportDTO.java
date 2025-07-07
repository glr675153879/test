package com.hscloud.hs.cost.account.model.dto.kpi;

import com.pig4cloud.pigx.common.excel.annotation.Sheet;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

@Data
public class KpiCalculateReportDTO {
    @Schema(description = "指标项")
    private String indexCode;
    @Schema(description = "")
    private String planCode;
    @Schema(description = "任务id")
    private Long id;
//    @Schema(description = "类型 1人2科室")
//    private int type;
    /*@Schema(description = "列表上有 入参带进来")
    private int period;*/
    @Schema(description = "列表上有 入参带进来")
    private Long taskChildId;
    @Schema(description = "是否过滤值为0 1是2否")
    private Long filterZero;
    @Schema(description = "是否过滤值误差大于0.000001 1是2否")
    private Long filterEro;

    private String ids;
}
