package com.hscloud.hs.cost.account.model.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@Schema(description = "核算任务计算总过程Dto")
public class CostAccountTaskCalculateTotalProcessDto {

    @Schema(description = "核算任务id")
    @NotNull(message = "核算任务id不能为空")
    private Long taskId;

    @Schema(description = "核算单元id")
    @NotNull(message = "核算单元id不能为空")
    private Long accountUnitId;

}
