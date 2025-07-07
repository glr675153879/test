package com.hscloud.hs.cost.account.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@Schema(description = "分配方案配置的核算指标")
public class SecondDistributionPlanConfigDto {

    @Schema(description = "方案配置id")
    @NotNull(message = "方案配置id不能为空")
    private Long planId;

    @Schema(description = "科室单元id")
    @NotNull(message = "科室id不能为空")
    private Long unitId;

    @Schema(description = "核算指标")
    private String accountIndex;

    @Schema(description = "进位规则")
    private String carryRule;

    @Schema(description = "指标保留小数")
    private Integer reservedDecimal;

    @Schema(description = "管理岗位ids")
    private List<Long> managementIdList;

    @Schema(description = "其他指标公式")
    private FormulaDto otherFormulaDto;

}
