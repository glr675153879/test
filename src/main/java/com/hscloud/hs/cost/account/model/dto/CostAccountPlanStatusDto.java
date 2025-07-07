package com.hscloud.hs.cost.account.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "核算方案状态dto")
public class CostAccountPlanStatusDto {

    @Schema(description = "核算方案id")
    private Long id;

    @Schema(description = "状态：0：启用  1:停用  /  0：状态未确认 可编辑  1：状态确认 不可编辑")
    private String status;
}
