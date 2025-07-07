package com.hscloud.hs.cost.account.model.pojo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author 小小w
 * @date 2023/9/26 9:29
 */
@Data
@Schema(description = "指标公式计算详情对象")
public class IndexFormulaCalculateDetail {

    @Schema(description = "计算结果")
    private String totalValue;

    @Schema(description = "指标公式")
    private String overAllFormula;

    @Schema(description = "计算配置详情")
    private List<ConfigObject> configIndexList;

    @Data
    @Schema(description = "配置详情")
    public static class ConfigObject {

        @Schema(description = "配置项类型(index/item)")
        private String type;

        @Schema(description = "配置项id")
        private Long id;

        @Schema(description = "配置项key")
        private String configKey;

        @Schema(description = "配置项名称")
        private String name;

        @Schema(description = "配置项公式(type类型为index时才有)")
        private String Formula;

        @Schema(description = "配置项的值")
        private BigDecimal totalValue;
    }
}
