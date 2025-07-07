package com.hscloud.hs.cost.account.model.dto;

import com.hscloud.hs.cost.account.model.vo.CostAccountPlanConfigIndexList;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "核算方案配置的核算指标")
public class  CostAccountPlanConfigIndexDto {

    @Schema(description = "核算指标id")
    private Long Id;

    @Schema(description = "核算指标name")
    private Long indexName;

    @Schema(description = "此核算指标的父节点id")
    private Long IndexId;

    @Schema(description = "核算方案配置的核算指标的配置项")
    private List<CostAccountPlanConfigItemsDto> costIndexConfigItemList;

    @Schema(description = "核算方案配置的核算指标")
    private List<CostAccountPlanConfigIndexList> costIndexConfigIndexList;
}
