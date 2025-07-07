package com.hscloud.hs.cost.account.model.dto.kpi;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author Administrator
 */
@Data
@Schema(description = "核算项重新计算DTO")
public class KpiItemCalculateDTO {
    @Schema(description = "id")
    @NotNull(message = "ID不能为空")
    private Long id;

    @Schema(description = "周期")
    private Long period;

    @Schema(description = "业务类型，1，一次绩效，2，科室成本")
    private String busiType;
}
