package com.hscloud.hs.cost.account.model.dto.monitorCenter;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 
 * @author  lian
 * @date  2023-09-19 16:08
 * 
 */
@Data
@Schema(description = "监测值查询参数")
public class CostMonitorDataQueryDto{

    @Schema(description = "id")
    private Long id;

    @Schema(description = "月份")
    private String month;

    @Schema(description = "科室单元id")
    private String unitId;

    @Schema(description = "核算项id")
    private String itemId;

}
