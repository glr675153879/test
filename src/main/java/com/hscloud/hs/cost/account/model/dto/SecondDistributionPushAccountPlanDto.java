package com.hscloud.hs.cost.account.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author banana
 * @create 2023-11-26 9:28
 */
@Data
public class SecondDistributionPushAccountPlanDto {
    @Schema(description = "核算指标表id集合")
    private String accountIndexIds;

    @Schema(description = "总公式id集合")
    private String formulaIds;
}
