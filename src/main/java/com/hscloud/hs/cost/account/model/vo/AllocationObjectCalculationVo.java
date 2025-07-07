package com.hscloud.hs.cost.account.model.vo;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Schema(description = "步骤一：被分摊对象核算值计算")
public class AllocationObjectCalculationVo {
    @Schema(description = "核算周期")
    private String accountPeriod;

    @Schema(description = "被分摊核算对象")
    private String accountObject;

    @Schema(description = "本单元核算值对象")
    private List<UnitAccountValueObjectVo> unitAccountValueObjectVo;

    @Schema(description = "被分摊对象总值")
    private BigDecimal unitCountValue;
}
