package com.hscloud.hs.cost.account.model.vo;

import com.hscloud.hs.cost.account.model.pojo.CalculateDetail;
import com.hscloud.hs.cost.account.model.pojo.DivideFormulaDesc;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Schema(description = "步骤二：核算规则计算")
public class CalculationAccountRuleVo {

    @Schema(description = "分摊公式描述")
    private DivideFormulaDesc dividerFormula;

    @Schema(description = "核算周期")
    private String accountPeriod;

    @Schema(description = "被分摊核算对象")
    private String accountObject;

    @Schema(description = "核算比例")
    private BigDecimal accountProportion;

    @Schema(description = "核算规则值")
    private BigDecimal ruleCount;


    @Schema(description = "计算明细对象")
    private List<CalculateDetail> calculateDetail;
}
