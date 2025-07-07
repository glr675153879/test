package com.hscloud.hs.cost.account.model.pojo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "计算明细信息")
public class CalculateDetail {
    @Schema(description = "配置项key")
    private String configKey;

    @Schema(description = "配置项值")
    private BigDecimal configValue;
}
