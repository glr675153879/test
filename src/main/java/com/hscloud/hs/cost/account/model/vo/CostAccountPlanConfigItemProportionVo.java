package com.hscloud.hs.cost.account.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "方案配置项核算比例对象vo")
public class CostAccountPlanConfigItemProportionVo {

    @Schema(description = "医护分摊比例id")
    private Long allocateId;

    @Schema(description = "医护分摊比例")
    private Double medicalAllocationProportion;

    @Schema(description = "核算项id")
    private Long itemId;

    @Schema(description = "核算项名称")
    private String itemName;

    @Schema(description = "分组id")
    private Long groupId;

    @Schema(description = "分组名称")
    private String groupName;
}
