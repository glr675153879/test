package com.hscloud.hs.cost.account.model.vo.report;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author tianbo
 * @version 1.0
 * @date 2024-12-10 16:31
 **/
@Data
public class SumZhigongjxVO {
    @Schema(description = "总核算值")
    private BigDecimal totalAmt;

    @Schema(description = "一次绩效值")
    private BigDecimal amt;

    @Schema(description = "二次绩效值")
    private BigDecimal secondAmt;
}
