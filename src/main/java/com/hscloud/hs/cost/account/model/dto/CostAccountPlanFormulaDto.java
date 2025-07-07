package com.hscloud.hs.cost.account.model.dto;

import com.hscloud.hs.cost.account.constant.enums.StatisticalPeriodEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
@Data
@Schema(description = "添加核算方案总公式")
public class CostAccountPlanFormulaDto {
    @Schema(description = "总成本id")
    private Long id;

    @Schema(description = "总成本公式名称")
    private String name;

    @Schema(description = "总成本公式")
    @NotNull(message = "指标公式不能为空")
    @Valid
    private String planCostFormula;

    @Schema(description = "进位规则")
    @NotBlank(message = "进位规则不能为空")
    private String carryRule;

    @Schema(description = "进位保留小数")
    @NotNull(message = "进位保留小数不能为空")
    private Integer reservedDecimal;

    @Schema(description = "核算对象")
    private String accountProportionObject;


    @Schema(description = "指标性质")
    @NotBlank(message = "指标性质不能为空")
    private String indexProperty;

    @Schema(description = "方案id")
    private Long planId;



}
