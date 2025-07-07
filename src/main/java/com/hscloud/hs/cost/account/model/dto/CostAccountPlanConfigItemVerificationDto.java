package com.hscloud.hs.cost.account.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * @author 小小w
 * @date 2023/9/20 19:45
 */
@Data
@Schema(description = "核算方案配置的核算项计算Dto")
public class CostAccountPlanConfigItemVerificationDto {

    @Schema(description = "核算对象")
    private String objectId;

    @Schema(description = "开始时间")
    private String startTime;

    @Schema(description = "结束时间")
    private String endTime;

    @Schema(description = "核算项id")
    private Long itemId;

    @Schema(description = "被分摊对象id")
    private List<Long> allocationIds;

    @Schema(description = "指标单位")
    private String indexUnit;

    @Schema(description = "指标保留小数")
    private Integer reservedDecimal;

    @Schema(description = "进位规则")
    private String carryRule;

    @Schema(description = "医护分摊标识")
    private String docNurseAllocation;

    @Schema(description = "医护分摊比例id")
    private Long allocateId;

    @Schema(description = "是否门诊分摊 0-否 1-是")
    private String outpatientPublic;

    @Schema(description = "病区成本分摊标记 0-否 1-是")
    private String wardCosts;

    @Schema(description = "床位分摊标识")
    private String bedAllocation;

    @Schema(description = "被分摊核算范围")
    private String accountRange;

    @Schema(description = "自定义分摊对象")
    private String customObject;

    @Schema(description = "分摊规则公式id")
    private Long ruleFormulaId;
}
