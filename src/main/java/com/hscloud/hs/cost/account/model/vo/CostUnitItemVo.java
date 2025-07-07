package com.hscloud.hs.cost.account.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "指标下的核算项vo")
public class CostUnitItemVo {


    @Schema(description = "核算单元id")
    private Long itemId;

    @Schema(description = "核算指标名")
    private String itemName;

    @Schema(description = "指标总核算值")
    private BigDecimal calculateCount=BigDecimal.ZERO;

    @Schema(description = "所占百分比")
    private BigDecimal percentage=BigDecimal.ZERO;




}
