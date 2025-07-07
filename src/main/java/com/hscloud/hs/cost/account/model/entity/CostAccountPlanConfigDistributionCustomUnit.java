package com.hscloud.hs.cost.account.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;

import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 自定义科室单元关联表
 * </p>
 *
 * @author author
 * @since 2023-11-23
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("cost_account_plan_config_distribution_custom_unit")
@Schema(description ="自定义科室单元关联表")
public class CostAccountPlanConfigDistributionCustomUnit extends Model<CostAccountPlanConfigDistributionCustomUnit> {

    private static final long serialVersionUID = 1L;

    @Schema(description  = "id")
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description  = "方案id")
    private Long planId;

    @Schema(description  = "核算方案配置id")
    private Long planConfigId;

    @Schema(description  = "自定义科室单元id")
    private Long distributionCustomUnitId;

    @Schema(description  = "租户id")
    private Long tenantId;

    @Schema(description  = "是否删除：0：未删除 1：删除")
    private String delFlag;


}
