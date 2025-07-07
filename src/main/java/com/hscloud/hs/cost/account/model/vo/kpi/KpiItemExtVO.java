package com.hscloud.hs.cost.account.model.vo.kpi;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author Administrator
 */
@Data
@Schema(description = "核算项计算数量")
public class KpiItemExtVO {
    @Schema(description = "0结果数量")
    private Integer extZeroNum;

    @Schema(description = "计算异常数量")
    private Integer extFailNum;

    @Schema(description = "未计算数量")
    private Integer notExtNum;

    @Schema(description = "总数量")
    private Integer allNum;

    @Schema(description = "当量核算项计算异常数量")
    private Integer extFailEqNum;

    @Schema(description = "当量核算项未计算数量")
    private Integer notExtEqNum;

    @Schema(description = "当量总数量")
    private Integer allExtNum;
}
