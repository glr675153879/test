package com.hscloud.hs.cost.account.model.vo;

import com.hscloud.hs.cost.account.constant.enums.StatisticalPeriodEnum;
import com.hscloud.hs.cost.account.model.entity.CostAllocationRuleConfigIndex;
import com.hscloud.hs.cost.account.model.entity.CostAllocationRuleConfigItem;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "分摊规则Vo")
public class CostAllocationRuleVo {

    @Schema(description = "分摊规则id")
    private Long id;

    @Schema(description = "分摊规则名称")
    private String name;

    @Schema(description = "统计周期")
    private StatisticalPeriodEnum statisticalCycle;

    @Schema(description = "分摊规则公式")
    private String allocationRuleFormula;

    @Schema(description = "状态：0：启用  1:停用")
    private String status;


    @Schema(description = "配置项是核算指标的")
    private List<CostAllocationRuleConfigIndex> costAllocationRuleConfigIndex;

    @Schema(description = "配置项是核算项的")
    private List<CostAllocationRuleConfigItem> costAllocationRuleConfigItem;



}
