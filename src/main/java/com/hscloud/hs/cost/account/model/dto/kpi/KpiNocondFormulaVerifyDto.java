package com.hscloud.hs.cost.account.model.dto.kpi;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class KpiNocondFormulaVerifyDto {
    @Schema(description = "指标code")
    private String indexCode;
    @Schema(description = "公式")
    private String formula;
    @Schema(description = "参数")
    private List<KpiFormulaParamsDto> params;
}
