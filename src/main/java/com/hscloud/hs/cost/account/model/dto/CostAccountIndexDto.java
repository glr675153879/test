package com.hscloud.hs.cost.account.model.dto;

import com.hscloud.hs.cost.account.constant.enums.StatisticalPeriodEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@Schema(description = "核算指标dto")
public class CostAccountIndexDto {


    @Schema(description = "核算指标id")
    private Long id;

    @Schema(description = "核算指标名称")
    //@NotBlank(message = "核算指标名称不能为空")
    private String name;


    @Schema(description = "指标性质")
    //@NotBlank(message = "指标性质不能为空")
    private String indexProperty;

    @Schema(description = "统计周期")
    private StatisticalPeriodEnum statisticalCycle;

    @Schema(description = "指标分组id")
    //@NotNull(message = "指标分组id不能为空")
    private Long indexGroupId;

    @Schema(description = "指标单位")
    //@NotBlank(message = "指标单位不能为空")
    private String indexUnit;

    @Schema(description = "进位规则")
    //@NotBlank(message = "进位规则不能为空")
    private String carryRule;

    @Schema(description = "指标保留小数")
    //@NotNull(message = "指标保留小数不能为空")
    private Integer reservedDecimal;

    @Schema(description = "指标公式")
    //@NotNull(message = "指标公式不能为空")
    @Valid
    private FormulaDto formulaDto;



}
