package com.hscloud.hs.cost.account.model.vo;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "核算结果公式核算指标Vo")
public class CostAccountTaskItemVo {

    @Schema(description = "核算项id")
    private Long itemId;

    @Schema(description = "配置项id")
    private Long configId;

    @Schema(description = "配置项key")
    private String configKey;

    @Schema(description = "配置项名称")
    private String configName;

    @Schema(description = "配置项描述")
    private String configDesc;

    @Schema(description = "核算项总值")
    private BigDecimal itemTotalValue;
}
