package com.hscloud.hs.cost.account.model.dto.monitorCenter;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 监测值设置查询参数
 * @author  lian
 * @date  2023-09-20 16:47
 *
 */
@Data
@Schema(description = "监测值设置查询参数")
public class CostMonitorSetQueryDto {

    @Schema(description = "id")
    private Long id;

    @Schema(description = "科室单元id")
    private String unitId;

    @Schema(description = "科室单元名称")
    private String unitName;

    @Schema(description = "核算项id")
    private String itemId;

    @Schema(description = "核算项名称")
    private String itemName;

    @Schema(description = "监测值状态 启停用标记，0未设置，1已设置")
    private String status;
}
