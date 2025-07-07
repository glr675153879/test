package com.hscloud.hs.cost.account.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "核算方案配置的核算指标")
public class CostAccountPlanConfigIndexList {
    @Schema(description = "id")
    private Long id;

    @Schema(description = "父指标id")
    private Long indexId;

    @Schema(description = "核算指标id")
    private Long configIndexId;

    @Schema(description = "name")
    private String configIndexName;

    @Schema(description = "配置项key")
    private String configKey;

    @Schema(description = "核算指标")
    private CostAccountPlanConfigIndexVo costAccountIndexVo;

//    @Schema(description = "核算指标的核算项")
//    private List<CostAccountPlanConfigItemsVo> costIndexConfigItemList;
}
