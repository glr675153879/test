package com.hscloud.hs.cost.account.model.pojo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@Schema(description = "同比")
public class YearRatio {

    @Schema(description = "同比核算值")
    private BigDecimal totalCountYear;

    @Schema(description = "同比")
    private BigDecimal yearRatio;

    @Schema(description = "同比涨幅值")
    private BigDecimal yearIncrease;
}
