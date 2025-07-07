package com.hscloud.hs.cost.account.model.vo;

import com.hscloud.hs.cost.account.model.dto.AccountUnitIdAndNameDto;
import com.hscloud.hs.cost.account.model.dto.CommonDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "核算方案配置的核算指标项")
public class CostAccountPlanConfigItemsVo {
    @Schema(description = "核算指标核算项的主键id")
    private Long id;

    @Schema(description = "指标项id")
    private Long indexId;

    @Schema(description = "配置项id")
    private Long configId;

    @Schema(description = "配置项key")
    private String configKey;

    @Schema(description = "配置项名称")
    private String configName;

    @Schema(description = "配置项描述")
    private String configDesc;

    @Schema(description = "核算单位")
    private String measureUnit;

    @Schema(description = "核算保留位数")
    private Integer retainDecimal;

    @Schema(description = "进位规则")
    private String carryRule;


    @Schema(description = "医护分摊标记，0不选择医护分摊，1选择")
    private String docNurseAllocation;


    @Schema(description = "被分摊核算范围")
    private String planAccountRange;


    @Schema(description = "借床分摊标记，0不选择借床分摊，1选择")
    private String bedAllocation;

    @Schema(description = "门诊公用分摊标记")
    private String outpatientPublic;

    @Schema(description = "病区成本分摊标记")
    private String wardCosts;

    @Schema(description = "被分摊核算范围")
    private String accountRange;

    @Schema(description = "分摊规则公式id")
    private Long ruleFormulaId;

    @Schema(description = "业务id集合")
    private List<String> bizIds;

    @Schema(description = "回显用")
    private List<CommonDTO> commonDTOList;

    @Schema(description = "分摊比例对象")
    private CostAccountPlanConfigItemProportionVo proportion;

}
