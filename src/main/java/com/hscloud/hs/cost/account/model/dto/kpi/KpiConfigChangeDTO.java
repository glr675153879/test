package com.hscloud.hs.cost.account.model.dto.kpi;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class KpiConfigChangeDTO {
    private Long period;
    @Schema(description = "新任务id 不是taskchildid")
    private Long taskId;
}
