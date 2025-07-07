package com.hscloud.hs.cost.account.model.dto.second;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.util.List;

@Data
@Schema(description = "批量保存")
public class CommonBatchSaveDTO {
    @Schema(description = "新增list")
    private List<?> addList;

}
