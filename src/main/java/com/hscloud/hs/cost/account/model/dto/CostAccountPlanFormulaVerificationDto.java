package com.hscloud.hs.cost.account.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author 小小w
 * @date 2023/9/16 14:17
 */
@Data
@Schema(description = "核算方案总公式校验dto")
public class CostAccountPlanFormulaVerificationDto {
    @Schema(description = "核算范围")
    private String accountProportionObject;

    @Schema(description = "开始时间")
    private String startTime;

    @Schema(description = "结束时间")
    private String endTime;

    @Schema(description = "核算对象id")
    private String objectId;

    @Schema(description = "进位规则")
    private String carryRule;

    @Schema(description = "保留小数")
    private Integer reservedDecimal;

    @Schema(description = "指标公式")
    @NotNull(message = "指标公式不能为空")
    private FormulaDto formulaDto;

}
