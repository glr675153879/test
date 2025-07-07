package com.hscloud.hs.cost.account.model.pojo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "指标公式对象")
public class IndexFormulaObject {

    @Schema(description = "指标公式")
    private String indexFormula;

    @Schema(description = "配置项key")
    private String configKey;

}
