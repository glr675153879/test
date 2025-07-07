package com.hscloud.hs.cost.account.model.pojo;

import com.hscloud.hs.cost.account.model.vo.CostAccountPlanConfigItemsVo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * @author Admin
 */
@Data
@Schema(description = "核算方案配置的核算指标")
public class CostPlanConfigIndex {

    @Schema(description = "核算指标id")
    private Long id;

    @Schema(description = "核算指标的名称")
    private String name;

    @Schema(description = "路径")
    private String path;

    @Schema(description = "指标公式")
    private String indexFormula;

    @Schema(description = "配置项key 只针对子级核算指标")
    private String configKey;

    @Schema(description = "核算指标的子集 指标")
    private List<CostPlanConfigIndex> costIndexConfigIndexList;

    @Schema(description = "核算指标的子集 项")
    private List<CostAccountPlanConfigItemsVo> configItemsVos;
}
