package com.hscloud.hs.cost.account.model.dto.monitorCenter;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 监测值趋势详情查询参数
 * @author  lian
 * @date  2023-09-19 16:08
 * 
 */
@Data
@Schema(description = "监测值趋势详情查询参数")
public class CostMonitorValTrendQueryDto {

    @Schema(description = "科室单元id")
    private String unitId;

    @Schema(description = "核算项id")
    private String itemId;

    @Schema(description = "月份")
    private String month;
}
