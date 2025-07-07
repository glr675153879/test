package com.hscloud.hs.cost.account.model.dto.kpi;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "核算项基础表")
public class KpiItemTableDto {
    @Schema(description = "表id")
    private Long id;

    @Schema(description = "表名")
    private String tableName;

    @Schema(description = "表注释")
    private String tableComment;
}
