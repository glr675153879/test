package com.hscloud.hs.cost.account.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author banana
 * @create 2023-09-15 12:54
 */
@Data
@Schema(description = "核算比例状态入参")
public class CostAccountProportionStatusDto {

    @Schema(description = "核算比例项id")
    private Long id;

    @Schema(description = "状态：0：启用  1:停用")
    private String status;
}
