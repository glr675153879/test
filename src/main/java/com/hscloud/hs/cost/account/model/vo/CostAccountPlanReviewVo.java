package com.hscloud.hs.cost.account.model.vo;

import cn.hutool.core.lang.tree.Tree;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hscloud.hs.cost.account.model.entity.CostAccountPlanCost;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "方案预览Vo")
public class CostAccountPlanReviewVo {

    @Schema(description = "方案id")
    private Long planId;

    @Schema(description = "总成本")
    private List<PlanCostPreviewVo> costAccountPlanCost;

    @Schema(description = "方案配置")
    private IPage costAccountPlanConfigVo;

    @Schema(description = "是否是成本   0:否  1:是")
    private String isCost;
}
