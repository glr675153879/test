package com.hscloud.hs.cost.account.model.dto.kpi;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author Administrator
 */
@Data
@Schema(description = "全部核算项重新计算DTO")
public class KpiItemBatchCalculateDTO {
    @Schema(description = "周期")
    private Long period;

    @Schema(description = "业务类型，1，一次绩效，2，科室成本")
    private String busiType;

    @Schema(description = "核算项id列表")
    private String itemIds;
}
