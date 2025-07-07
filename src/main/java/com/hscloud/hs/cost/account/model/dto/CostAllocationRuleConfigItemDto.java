package com.hscloud.hs.cost.account.model.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "分摊规则配置dto")
public class CostAllocationRuleConfigItemDto {

    @Schema(description = "配置项id")
    private Long configId;

    @Schema(description = "核算对象")
    private String accountObject;

    @Schema(description = "核算范围")
    private String accountRange;

    @Schema(description = "核算集 核算单元 自定义人员 自定义科室")
    private String accounts;

    @Schema(description = "核算单元性质")
    private String accountDeptShip;

    @Schema(description = "核算周期 current:当前周期 before:上一周期 ")
    private String accountPeriod;

    @Schema(description = "核算比例")
    private String accountProportion;

    @Schema(description = "比例通用id")
    private Long proportionBaseId;

    @Schema(description = "核算比例id")
    private Long accountProportionId;
}
