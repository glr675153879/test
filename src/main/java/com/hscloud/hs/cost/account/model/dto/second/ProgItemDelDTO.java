package com.hscloud.hs.cost.account.model.dto.second;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "删除发放单元方案item（系数分配）")
public class ProgItemDelDTO {
    @Schema(description = "任务核算指标明细detailid")
    private Long taskDetailId;

    @Schema(description = "progItem")
    private Long progItemId;

}
