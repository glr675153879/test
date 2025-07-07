package com.hscloud.hs.cost.account.model.dto.monitorCenter;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 *
 * @author  lian
 * @date  2023-09-19 13:47
 *
 */
@Data
@Schema(description = "监测值设置参数")
public class CostMonitorSetDto {

    @Schema(description = "id")
    private Long id;

    @NotBlank(message = "科室单元id不能为空")
    @Schema(description = "科室单元id")
    private String unitId;

    @NotBlank(message = "核算项id不能为空")
    @Schema(description = "核算项id")
    private String itemId;

    @Schema(description = "目标值")
    private String targetValue;
}
