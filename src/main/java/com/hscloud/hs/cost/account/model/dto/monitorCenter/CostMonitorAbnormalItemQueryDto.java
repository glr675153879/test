package com.hscloud.hs.cost.account.model.dto.monitorCenter;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 异常核算项查看参数
 * @author  lian
 * @date  2023-09-19 16:08
 * 
 */
@Data
@Schema(description = "异常核算项查看参数")
public class CostMonitorAbnormalItemQueryDto {

    @Schema(description = "月份")
    private String month;

    @Schema(description = "科室单元名称")
    private String unitName;

    @Schema(description = "科室单元id")
    private String unitId;

    @Schema(description = "核算项名称")
    private String itemName;

}
