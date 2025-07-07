package com.hscloud.hs.cost.account.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author 小小w
 * @date 2023/9/14 16:15
 */

@Data
@Schema(description = "分摊规则校验dto")
public class CostAllocationRuleVerificationDto {
    @Schema(description = "开始时间")
    private String startTime;

    @Schema(description = "结束时间")
    private String endTime;

    @Schema(description = "核算单元对象")
    private String accountObject;

    @Schema(description = "核算对象id")
    private String objectId;

    @Schema(description = "核算周期类型")
    private String type;

    @Schema(description = "被核算的值")
    private Double number;

    @Schema(description = "分摊公式")
    @NotNull(message = "分摊公式不能为空")
    private FormulaDto formulaDto;

}
