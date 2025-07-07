package com.hscloud.hs.cost.account.model.vo.monitorCenter;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 监测值设置vo
 * @author  lian
 * @date  2023-09-18 11:04
 * 
 */
@Data
@Schema(description = "监测值设置vo")
public class CostMonitorSetVo {
    @Schema(description = "监测值设置id")
    private Long id;

    @Schema(description = "核算单元id")
    private String unitId;

    @Schema(description = "核算单元名称")
    private String unitName;

    @Schema(description = "核算单元状态 启停用标记，0启用，1停用")
    private String unitStatus;

    @Schema(description = "核算项id")
    private String itemId;

    @Schema(description = "核算项名称")
    private String itemName;

    @Schema(description = "核算项状态 启停用标记，0启用，1停用")
    private String itemStatus;

    @Schema(description = "单位")
    private String measureUnit;

    @Schema(description = "目标值")
    private String targetValue;

    @Schema(description = "监测值状态 启停用标记，0未设置，1已设置")
    private String status;
}
