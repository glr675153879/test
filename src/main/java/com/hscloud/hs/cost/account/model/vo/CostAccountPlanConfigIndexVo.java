package com.hscloud.hs.cost.account.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
@Data
public class CostAccountPlanConfigIndexVo {
    @Schema(description = "指标配置的指标")
    private Long id;

    private String name;

    @Schema(description = "父节点id")
    private Long indexId;

    @Schema(description = "指标公式")
    private String indexFormula;

    private List<CostAccountPlanConfigItemsVo> costIndexConfigItemList;

    private List<CostAccountPlanConfigIndexList> costIndexConfigIndexList;
}
