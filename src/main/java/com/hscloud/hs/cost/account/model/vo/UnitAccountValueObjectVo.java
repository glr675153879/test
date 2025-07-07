package com.hscloud.hs.cost.account.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "本单元核算对象Vo")
public class UnitAccountValueObjectVo {

    @Schema(description = "核算周期")
    private String accountPeriod;

    @Schema(description = "本单元核算值")
    private BigDecimal unitValue;
}
