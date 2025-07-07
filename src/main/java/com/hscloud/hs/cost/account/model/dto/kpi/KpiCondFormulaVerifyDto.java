package com.hscloud.hs.cost.account.model.dto.kpi;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class KpiCondFormulaVerifyDto {
    @Schema(description = "指标code")
    private String indexCode;
    @Schema(description = "公式")
    private List<String> formulas;
    @Schema(description = "业务周期")
    private String period;
    @Schema(description = "核算对象")
    private String member;
}
