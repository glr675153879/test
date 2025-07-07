package com.hscloud.hs.cost.account.model.dto;

import com.hscloud.hs.cost.account.model.entity.CostAccountPlanConfigFormula;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;

/**
 * @author Admin
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "核算方案总公式对象")
public class CostAccountPlanConfigFormulaDto extends CostAccountPlanConfigFormula {


    @Schema(description = "指标公式")
    @NotNull(message = "指标公式不能为空")
    private FormulaDto formulaDto;

}
