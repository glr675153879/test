package com.hscloud.hs.cost.account.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "核算指标状态dto")
public class CostAccountIndexStatusDto {

    @Schema(description = "核算指标id")
    private Long id;

    @Schema(description = "状态：0：启用  1:停用")
    private String status;

}
