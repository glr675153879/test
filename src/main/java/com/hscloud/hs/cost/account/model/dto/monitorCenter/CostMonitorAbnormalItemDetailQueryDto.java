package com.hscloud.hs.cost.account.model.dto.monitorCenter;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 异常核算项查看参数
 * @author  lian
 * @date  2023-09-19 16:08
 * 
 */
@Data
@Schema(description = "异常核算项查看参数")
public class CostMonitorAbnormalItemDetailQueryDto {


    @Schema(description = "月份")
    private String month;

    @NotBlank(message = "科室单元id不能为空")
    @Schema(description = "科室单元Id")
    private String unitId;

    @Schema(description = "核算项名称")
    private String itemName;
}
