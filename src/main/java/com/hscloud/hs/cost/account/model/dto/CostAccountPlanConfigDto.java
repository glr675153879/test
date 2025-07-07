package com.hscloud.hs.cost.account.model.dto;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hscloud.hs.cost.account.model.vo.CostAccountPlanConfigIndexList;
import com.hscloud.hs.cost.account.model.vo.CostAccountPlanConfigItemsVo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * @author Administrator
 */
@Data
@Schema(description = "核算方案配置的核算指标")
public class CostAccountPlanConfigDto extends PageDto {

    @Schema(description = "核算方案配置id")
    private Long id;

    @Schema(description = "核算方案id")
    private Long planId;

    @Schema(description = "核算指标id")
    private Long indexId;

    @Schema(description = "是否是关联指标  0：否  1：是")
    private String isRelevance;

    @Schema(description = "配置指标key")
    private String configKey;

    @Schema(description = "配置指标名称")
    private String configIndexName;

    @Schema(description = "核算对象")
    private String accountProportionObject;

    @Schema(description = "核算方案配置的核算指标的配置项")
    private List<CostAccountPlanConfigItemsVo> costIndexConfigItemList;

    @Schema(description = "核算方案配置的核算指标")
    private List<CostAccountPlanConfigIndexList> costIndexConfigIndexList;
}
