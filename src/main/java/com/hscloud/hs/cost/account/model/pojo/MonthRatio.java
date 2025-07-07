package com.hscloud.hs.cost.account.model.pojo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "环比")
@AllArgsConstructor
public class MonthRatio {

    @Schema(description = "环比核算值")
    private BigDecimal totalCountMonth;


    @Schema(description = "环比")
    private BigDecimal monthRatio;

    @Schema(description = "环比涨幅值")
    private BigDecimal monthIncrease;
}
