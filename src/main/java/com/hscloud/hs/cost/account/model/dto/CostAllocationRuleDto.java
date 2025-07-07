package com.hscloud.hs.cost.account.model.dto;



import com.hscloud.hs.cost.account.constant.enums.StatisticalPeriodEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@Schema(description = "分摊规则dto")
public class CostAllocationRuleDto {

    @Schema(description = "分摊规则id")
    private Long id;

    @NotBlank(message = "分摊规则名称不能为空")
    @Schema(description = "分摊规则名称")
    private String name;

    @Schema(description = "统计周期")
    private StatisticalPeriodEnum statisticalCycle;

    @Schema(description = "指标公式")
    @NotNull(message = "指标公式不能为空")
    @Valid
    private FormulaDto formulaDto;



}
