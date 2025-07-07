package com.hscloud.hs.cost.account.model.dto.second;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;


import javax.validation.constraints.NotNull;
import java.util.List;

/**
 *
 * @author  lian
 * @date  2024/5/16 16:59
 *
 */

@Data
@Schema(description = "二次任务下发dto")
public class SecondTaskCreateDto {

    @Schema(description = "任务id")
    @NotNull(message = "任务id不能为空")
    private Long firstId;

    @Schema(description = "发放单元id")
    @NotNull(message = "发放单元id不能为空")
    private String grantUnitIds;

    @Schema(description = "发放单元名称")
    @NotNull(message = "发放单元名称不能为空")
    private String grantUnitNames;
}
