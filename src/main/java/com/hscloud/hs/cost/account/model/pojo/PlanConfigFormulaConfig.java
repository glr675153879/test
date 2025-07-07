package com.hscloud.hs.cost.account.model.pojo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "核算指标公式")
public class PlanConfigFormulaConfig {


    @Schema(description = "核算指标id")
    private Long id;

    @Schema(description = "配置项key")
    private String key;

    @Schema(description = "核算指标的名称")
    private String name;

    @Schema(description = "类型")
    private String type;
}
