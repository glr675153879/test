package com.hscloud.hs.cost.account.model.pojo;

import com.hscloud.hs.cost.account.model.dto.CommonDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * @author Admin
 */
@Data
@Schema(description = "核算方案配置的核算指标")
public class PlanItem {

    @Schema(description = "核算项id")
    private Long itemId;

    @Schema(description = "核算指标对应的核算项配置key")
    private String configKey;

    @Schema(description = "是否借床分摊 0-否 1-是")
    private String bedAllocation;

    @Schema(description = "是否医护分摊 0-否 1-是")
    private String docNurseAllocation;

    @Schema(description = "医护分摊比例id")
    private Long allocateId;

    @Schema(description = "是否门诊分摊 0-否 1-是")
    private String outpatientPublic;

    @Schema(description = "病区成本分摊标记 0-否 1-是")
    private String wardCosts;

    @Schema(description = "分摊规则公式id")
    private Long ruleFormulaId;

    @Schema(description = "核算指标的核算范围")
    private String accountRange;

    @Schema(description = "业务的id 科室传code,人员和单元传id")
    private List<String> bizList;


    @Schema(description = "核算项对应自定义人员 科室内容 回显用")
    private List<CommonDTO> commonDTOList;
}
