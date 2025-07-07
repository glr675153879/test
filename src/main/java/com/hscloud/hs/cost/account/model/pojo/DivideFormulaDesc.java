package com.hscloud.hs.cost.account.model.pojo;

import com.hscloud.hs.cost.account.model.vo.CalculationAccountRuleConfigIndexVo;
import com.hscloud.hs.cost.account.model.vo.CalculationAccountRuleConfigItemVo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
@Data
@Schema(description = "分摊规则描述")
public class DivideFormulaDesc {

    @Schema(description = "分摊规则公式")
    private String allocationRuleFormula;

    @Schema(description = "配置项是核算指标的")
    private List<CalculationAccountRuleConfigIndexVo> calculationAccountRuleConfigIndex;

    @Schema(description = "配置项是核算项的")
    private List<CalculationAccountRuleConfigItemVo> calculationAccountRuleConfigItem;
}
