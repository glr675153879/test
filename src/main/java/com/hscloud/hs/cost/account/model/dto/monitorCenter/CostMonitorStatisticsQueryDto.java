package com.hscloud.hs.cost.account.model.dto.monitorCenter;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "监测值设置查询参数")
public class CostMonitorStatisticsQueryDto {

    @Schema(description = "id")
    private Long id;

    @Schema(description = "科室单元id")
    private String unitId;

    @Schema(description = "核算项id")
    private String indexId;

    @Schema(description = "监测值状态 启停用标记，0未设置，1已设置")
    private String status;
}
