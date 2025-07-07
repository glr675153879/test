package com.hscloud.hs.cost.account.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@Schema(description = "任务分配获取对象")
public class SecondTaskDistributionDto {


    @Schema(description = "核算单元id")
    @NotNull(message = "核算单元id不能为空")
    private Long unitId;


    @Schema(description = "关联任务id")
    @NotNull(message = "关联任务id不能为空")
    private Long relateTaskId;

    @Schema(description = "关联方案id")
    @NotNull(message = "关联方案id不能为空")
    private Long relatePlanId;


}
