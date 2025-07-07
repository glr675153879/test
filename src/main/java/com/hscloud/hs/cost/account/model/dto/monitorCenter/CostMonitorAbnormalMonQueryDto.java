package com.hscloud.hs.cost.account.model.dto.monitorCenter;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 异常月份查看参数
 * @author  lian
 * @date  2023-09-19 16:08
 * 
 */
@Data
@Schema(description = "异常月份查看参数")
public class CostMonitorAbnormalMonQueryDto {

    @Schema(description = "年份 先不传")
    private Integer year;

    @Schema(description = "科室单元id")
    @NotBlank(message="科室单元id 不能为空")
    private String unitId;

    @NotBlank(message="核算项id 不能为空")
    @Schema(description = "核算项id")
    private String itemId;
}
