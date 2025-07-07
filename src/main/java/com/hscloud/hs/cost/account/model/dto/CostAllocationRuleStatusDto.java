package com.hscloud.hs.cost.account.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "分摊规则状态dto")
public class CostAllocationRuleStatusDto {

    @Schema(description = "分摊规则id")
    private Long id;

    @Schema(description = "状态：0：启用  1:停用")
    private String status;
}
