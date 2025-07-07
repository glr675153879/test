package com.hscloud.hs.cost.account.model.dto.second;

import com.hscloud.hs.cost.account.model.entity.second.UnitTaskCount;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@Schema(description = "线下人员批量修改")
public class UnitTaskCountEditBatchDTO {

    @Schema(description = "unitTaskId")
    @NotNull(message = "任务id不能为空")
    private Long unitTaskId;

    @Schema(description = "unitTaskCountList")
    private List<UnitTaskCount> unitTaskCountList;

}
