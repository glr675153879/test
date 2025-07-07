package com.hscloud.hs.cost.account.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * @author Administrator
 */
@Data
@Schema(description = "核算方案配置的核算指标的配置项")
public class CostAccountPlanConfigItemsDto {

    @Schema(description = "项id")
    private Long id;

    @Schema(description = "配置项id")
    private Long configId;

    @Schema(description = "医护分摊标记，0不选择医护分摊，1选择")
    private String medicalAllocation;

    @Schema(description = "医护分摊比例")
    private String medicalAllocationProportion;

    /**
     * 业务id 根据选的核算范围/核算比例详情类型区分不同的id（1.科室单元id 2.科室id 3.人员id）
     */
    @Schema(description = "医护分摊比例")
    private 	  String  bzid;

    @Schema(description = "借床分摊标记，0不选择借床分摊，1选择")
    private String bedAllocation;

    @Schema(description = "被分摊核算范围")
    private String accountRange;


    @Schema(description = "被分摊核算单元科室id")
    private List<AccountUnitIdAndNameDto> units;

    @Schema(description = "分摊规则公式id")
    private Long ruleFormulaId;

    @Schema(description = "分摊规则公式")
    private String ruleFormula;
}
