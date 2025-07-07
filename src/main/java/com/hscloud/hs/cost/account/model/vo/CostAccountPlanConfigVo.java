package com.hscloud.hs.cost.account.model.vo;

import com.hscloud.hs.cost.account.model.dto.CommonDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "核算方案配置")
public class CostAccountPlanConfigVo {

    @Schema(description = "方案配置id")
    private Long id;

    @Schema(description = "方案名称")
    private String planName;

    @Schema(description = "核算指标名称")
    private String name;

    @Schema(description = "是否是系统指标")
    private String isSystemIndex;

    @Schema(description = "是否是关联指标  0：否  1：是")
    private String isRelevance;

    @Schema(description = "核算对象")
    private String accountProportionObject;

    @Schema(description = "自定义核算单元列表")
    private List<CommonDTO> costCustomUnitList;

    @Schema(description = "关联指标核算对象")
    private String distributionAccountProportionObject;

    @Schema(description = "关联指标自定义核算单元列表")
    private List<CommonDTO> distributionCostCustomUnitList;

    @Schema(description = "关联指标公式描述")
    private String configDesc;

    @Schema(description = "核算指标公式")
    private String indexFormula;

    @Schema(description = "核算指标id")
    private Long indexId;

    @Schema(description = "核算单位")
    private String indexUnit;

    @Schema(description = "核算保留位数")
    private Integer reservedDecimal;

    @Schema(description = "进位规则")
    private String carryRule;

    @Schema(description = "核算指标")
    private List<CostAccountPlanConfigIndexList> costIndexConfigIndexList;

    @Schema(description = "核算指标的核算项")
    private List<CostAccountPlanConfigItemsVo> costIndexConfigItemList;
//
//    @Schema(description = "核算方案的顶层核算指标")
//    private CostPlanConfigIndex costPlanConfigIndex;

    @Schema(description = "排序")
    private Integer seq;
}
