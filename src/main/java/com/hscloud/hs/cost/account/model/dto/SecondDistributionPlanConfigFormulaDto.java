package com.hscloud.hs.cost.account.model.dto;


import com.hscloud.hs.cost.account.model.pojo.SecondDistributionFormula;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.format.DecimalStyle;

@Data
@Schema(description = "核算方案总公式对象")
public class SecondDistributionPlanConfigFormulaDto {

    @Schema(description = "总公式id")
    private Long id;

    @Schema(description = "保留小数")
    @NotNull(message = "保留小数不能为空")
    private Integer reservedDecimal;

    @Schema(description = "进位规则")
    @NotBlank(message = "进位规则不能为空")
    private String carryRule;

    @Schema(description = "指标公式")
    @NotNull(message = "指标公式不能为空")
    private SecondDistributionFormula otherFormula = new SecondDistributionFormula();

}
