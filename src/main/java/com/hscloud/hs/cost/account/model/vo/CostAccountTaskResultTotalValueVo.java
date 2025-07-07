package com.hscloud.hs.cost.account.model.vo;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Schema(description = "总核算值Vo")
public class CostAccountTaskResultTotalValueVo {


    @Schema(description = "总公式")
    private String overAllFormula;

    @Schema(description = "总值")
    private BigDecimal totalValue;

    @Schema(description = "核算指标")
    private List<CostAccountTaskIndexVo> configIndexList;

}
