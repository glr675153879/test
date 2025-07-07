package com.hscloud.hs.cost.account.model.dto.userAttendance;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "人员考勤参数表")
public class FirstDistributionAccountFormulaDto {

    @Schema(description = "公式码")
    private String paramKey;

    @Schema(description = "公式名")
    private String paramName;

    @Schema(description = "id")
    private Long id;

    @Schema(description = "方案分配名")
    private String planName;

    @Schema(description = "方案分配表id")
    private Long planId;

    @Schema(description = "保留小数")
    private Integer reservedDecimal;

    @Schema(description = "进位规则")
    private String carryRule;
}
