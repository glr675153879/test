package com.hscloud.hs.cost.account.model.vo;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "方案预览Vo")
public class CostAccountPlanSnapshotVo {

    @Schema(description = "总成本")
    private List<PlanCostPreviewVo> costAccountPlanCost;

    @Schema(description = "方案配置")
    private Page<CostAccountPlanConfigVo> costAccountPlanConfigVo;
}
