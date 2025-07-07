package com.hscloud.hs.cost.account.model.dto.second;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "采集指标项的值")
public class ItemValueDTO {

    @Schema(description = "工号")
    private String userId;

    @Schema(description = "核算单元id")
    private String deptId;

    @Schema(description = "指标项号")
    private String itemId;

    @Schema(description = "指标项code")
    private String itemCode;

    @Schema(description = "采集值")
    private String itemValue;

}
