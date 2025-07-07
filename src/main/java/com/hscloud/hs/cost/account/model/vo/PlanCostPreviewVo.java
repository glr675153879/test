package com.hscloud.hs.cost.account.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class PlanCostPreviewVo {

    /**
     * 方案总成本id
     */
    @Schema(description = "方案总成本id")
    private Long id;

    /**
     * 总成本公式
     */
    @Schema(description = "总成本公式")
    private String planCostFormula;


    /**
     * l
     * 保留小数
     */
    @Schema(description = "保留小数")
    private Long reservedDecimal;
    /**
     * 进位规则
     */
    @Schema(description = "进位规则")
    private String carryRule;

    @Schema(description = "核算对象")
    private String accountObject;

    @Schema(description = "核算对象名称")
    private String accountObjectName;

    @Schema(description = "item")
    private List<ConfigList> configLists;


}
