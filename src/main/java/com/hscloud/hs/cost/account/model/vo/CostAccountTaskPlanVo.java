package com.hscloud.hs.cost.account.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author 小小w
 * @date 2023/11/27 11:21
 */
@Data
@Schema(description = "核算任务分组")
public class CostAccountTaskPlanVo {

    @Schema(description = "方案配置id")
    private Long taskGroupId;

    @Schema(description = "核算指标名称")
    private String taskGroupName;

    @Schema(description = "是否是系统指标")
    private String isSystemIndex;

    @Schema(description = "是否是关联指标  0：否  1：是")
    private String isRelevance;
}
