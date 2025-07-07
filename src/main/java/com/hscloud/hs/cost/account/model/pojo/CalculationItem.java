package com.hscloud.hs.cost.account.model.pojo;

import com.hscloud.hs.cost.account.model.dto.CommonDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author Admin
 */
@Data
@Schema(description = "方案项信息")
public class CalculationItem implements CalculationComponent<PlanItem>{

    @Schema(description = "方案配置项")
    private PlanItem planItem;

    @Schema(description = "类型 item")
    private String type;

    @Schema(description = "核算项名称")
    private String name;

    @Schema(description = "核算项对应的配置key")
    private String configKey;




    @Override
    public String getType() {
        return "item";
    }

    @Override
    public PlanItem getCalculationComponent() {
        return planItem;
    }
}
