package com.hscloud.hs.cost.account.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author banana
 * @create 2023-09-13 19:03
 */
@Data
@Schema(description = "编辑核算比例入参")
public class CostAccountProportionEditDto {

    @Schema(description = "核算比例项关联id")
    private Long id;

    @Schema(description = "核算比例关联信息")
    private Double proportion;

}
