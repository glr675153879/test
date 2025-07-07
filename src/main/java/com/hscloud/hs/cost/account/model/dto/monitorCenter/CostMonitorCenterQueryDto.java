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
@Schema(description = "监测动态查询参数")
public class CostMonitorCenterQueryDto {

    @Schema(description = "id")
    private Long id;

    @Schema(description = "月份")
    private String month;

    @Schema(description = "科室单元id")
    private String unitId;

    @Schema(description = "科室单元id测试 无用")
    private Long testUnitId;

    @Schema(description = "科室单元名称")
    private String unitName;

    @Schema(description = "核算项id")
    private String itemId;

    @Schema(description = "核算项名称")
    private String itemName;

    @Schema(description = "月份异常个数")
    private Integer abnormalMonth;
}
