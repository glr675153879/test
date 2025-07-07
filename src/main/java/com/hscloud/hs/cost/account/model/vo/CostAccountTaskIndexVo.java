package com.hscloud.hs.cost.account.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "配置项为核算指标")
public class CostAccountTaskIndexVo {

    @Schema(description = "指标项id")
    private Long indexId;

    @Schema(description = "配置项指标名称")
    private String configIndexName;

    @Schema(description = "配置项key")
    private String configKey;

    @Schema(description = "指标公式")
    private String indexFormula;

    @Schema(description = "核算指标总值")
    private BigDecimal indexTotalValue;

    @Schema(description = "里面包含的核算指标")
    private CostAccountTaskResultIndexProcessVo costAccountTaskResultIndexProcessVo;
}
