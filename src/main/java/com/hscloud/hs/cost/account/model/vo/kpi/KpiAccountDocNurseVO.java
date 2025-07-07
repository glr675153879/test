package com.hscloud.hs.cost.account.model.vo.kpi;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author Administrator
 */
@Data
@Schema(description = "护士组科室单元")
public class KpiAccountDocNurseVO {
    @Schema(description = "护士组科室单元ID")
    private Long nurseAccountId;

    @Schema(description = "护士组科室单元名称")
    private String nurseAccountName;

    private Long status;
}
