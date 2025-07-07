package com.hscloud.hs.cost.account.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author 小小w
 * @date 2023/9/12 15:46
 */

@Data
@Schema(description = "核算指标校验dto")
public class   CostAccountIndexVerificationDto{

    @Schema(description = "指标id")
    private String indexId;

    @Schema(description = "开始时间")
    private String startTime;

    @Schema(description = "结束时间")
    private String endTime;

    @Schema(description = "核算单元对象")
    private String accountObject;//类型

    @Schema(description = "核算对象id")
    private String objectId;

    @Schema(description = "核算类型    同比,环比")
    private String type;

    @Schema(description = "进位规则")
    private String carryRule;

    @Schema(description = "指标保留小数")
    private Integer reservedDecimal;

    @Schema(description = "指标单位")
    private String indexUnit;

    @Schema(description = "指标公式")
    @NotNull(message = "指标公式不能为空")
    private FormulaDto formulaDto;

}
