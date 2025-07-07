package com.hscloud.hs.cost.account.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "被分摊核算范围")
public class CostAccountPlanConfigAccountRange {

    @Schema(description = "被分摊核算范围")
    private String accountRange;

    @Schema(description = "被分摊的核算单元id")
    private Long unitId;
}
