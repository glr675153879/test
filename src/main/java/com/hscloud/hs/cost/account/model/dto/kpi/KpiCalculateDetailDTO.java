package com.hscloud.hs.cost.account.model.dto.kpi;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class KpiCalculateDetailDTO {
    @Schema(description = "指标项 有_0的这种")
    private String code;
    @Schema(description = "核算项项目分类指标才有值")
    private String codeType;
    @Schema(description = "calculate的id")
    private Long id;
    @Schema(description = "calculate的imputation_type 为1的时候必须要传imputation_code")
    private int imputationType;
    @Schema(description = "imputationCode")
    private String imputationCode;
    @Schema(description = "列表上有 入参带进来")
    private Long taskChildId;
    @Schema(description = "是否过滤值为0 1是2否")
    private Long filterZero;
    @Schema(description = "是否过滤值误差大于0.000001 1是2否")
    private Long filterEro;
    @Schema(description = "公式id")
    private Long formulateId;
    private Long reportId;

}
