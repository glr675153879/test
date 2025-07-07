package com.hscloud.hs.cost.account.model.dto.kpi;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class KpiCodeDetailDTO {
    @Schema(description = "id不为空就修改")
    private Long id;
    @Schema(description = "出参字段名")
    private String code;
    @Schema(description = "名称")
    private String name;
    @Schema(description = "口径")
    private String caliber;
    @Schema(description = "指标")
    private String indexCode;
}
