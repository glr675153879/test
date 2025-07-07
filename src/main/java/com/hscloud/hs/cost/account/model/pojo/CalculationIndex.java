package com.hscloud.hs.cost.account.model.pojo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * @author Admin
 */
@Data
@Schema(description = "方案指标信息")
public class CalculationIndex implements CalculationComponent<Long>{


    @Schema(description = "指标id")
    private Long id;

    @Schema(description = "类型 index")
    private String type;

    @Schema(description = "指标名称")
    private String name;

    @Schema(description = "指标对应的配置key")
    private String configKey;

    @Schema(description = "子内容")
    private List<CalculationComponent> children;




    @Override
    public String getType() {
        return "index";
    }

    @Override
    public Long getCalculationComponent() {
        return id;
    }
}
