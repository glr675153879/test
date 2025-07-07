package com.hscloud.hs.cost.account.model.pojo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * @author Admin
 */
@Data
@Schema(description = "方案计算信息")
public class CostPlanCalculateInfo {

    @Schema(description = "核算单元id")
    private Long unitId;

    @Schema(description = "核算单元组")
    private String group;

    @Schema(description = "核算单元对应方案表达式")
    private String planExpressSion;

    @Schema(description = "方案计算顶层指标")
    private List<CostFormulaInfo> costFormulaInfos;

}
