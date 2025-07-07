package com.hscloud.hs.cost.account.model.pojo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "分摊成本对象")
public class SharedCost {
    @Schema(description = "医护分摊值")
    private BigDecimal divideCount;

    @Schema(description = "借床分摊值")
    private BigDecimal bedBorrowCount;

    @Schema(description = "病区分摊值")
    private BigDecimal endemicAreaCount;

    @Schema(description = "门诊共用分摊值")
    private BigDecimal outpatientShardCount;

}
