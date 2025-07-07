package com.hscloud.hs.cost.account.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Admin
 */
@Data
@Schema(description = "方案的核算指标配置")
@EqualsAndHashCode(callSuper = true)
public class CostAccountPlanConfigIndexNew extends Model<CostAccountPlanConfigIndexNew> {

    @Schema(description = "id")
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "方案配置id")
    private Long planConfigId;


    @Schema(description = "核算指标id")
    private Long indexId;

    @Schema(description = "核算项路径")
    private String path;

    @Schema(description = "核算项对应的配置key")
    private String configKey;

    @Schema(description = "核算项id")
    private Long itemId;

    @Schema(description = "医护分摊标识")
    private String docNurseAllocation;

    @Schema(description = "医护分摊比例id")
    private Long allocateId;

    @Schema(description = "床位分摊标识")
    private String bedAllocation;

    @Schema(description = "是否门诊分摊 0-否 1-是")
    private String outpatientPublic;

    @Schema(description = "病区成本分摊标记 0-否 1-是")
    private String wardCosts;

    @Schema(description = "被分摊核算范围")
    private String accountRange;

    @Schema(description = "自定义分摊对象")
    private String customObject;

    @Schema(description = "分摊规则公式id")
    private Long ruleFormulaId;

    @Schema(description = "自定义信息 用于前端回显")
    private String customInfo;

    @Schema(description = "关联指标自定义信息 用于前端回显")
    private String distributionCustomInfo;

    @Schema(description = "租户id")
    private Long tenantId;

    @Schema(description = "是否删除：0：未删除 1：删除")
    private String delFlag;
}
