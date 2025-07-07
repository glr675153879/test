package com.hscloud.hs.cost.account.model.dto.kpi;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * @author Administrator
 */

@Data
@Schema(description = "核算单元关系DTO")
public class KpiAccountRelationDTO {
    @Schema(description = "分组code")
    @NotEmpty(message = "分组code为空")
    private String categoryCode;

    @Schema(description = "医生组核算单元ID")
    @NotNull(message = "医生组核算单元ID为空")
    private Long docAccountId;

    @Schema(description = "护士组核算单元ID，逗号隔开")
    private String nurseAccountId;
}
