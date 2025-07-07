package com.hscloud.hs.cost.account.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * @author Admin
 */
@Data
@Schema(description = "核算指标配置dto")
public class CostIndexConfigItemDto {


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

    @Schema(description = "核算周期 current:当前周期 before:上一周期")
    private String accountPeriod;
}
