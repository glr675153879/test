package com.hscloud.hs.cost.account.model.dto.second;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "批量ids")
public class CommonBatchIdsDTO {

    @Schema(description = "ids 1，2，3")
    private String ids;

}
