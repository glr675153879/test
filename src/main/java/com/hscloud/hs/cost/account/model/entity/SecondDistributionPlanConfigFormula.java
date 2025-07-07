package com.hscloud.hs.cost.account.model.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.time.LocalDateTime;
import java.io.Serializable;

import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 二次分配方案公式配置表
 * </p>
 *
 * @author 
 * @since 2023-11-20
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("second_distribution_plan_config_formula")
@Schema( description="二次分配方案公式配置表")
public class SecondDistributionPlanConfigFormula extends Model<SecondDistributionPlanConfigFormula> {

    private static final long serialVersionUID = 1L;

     @Schema(description = "主键id")
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

     @Schema(description = "方案分配id")
     private Long planId;

     @Schema(description = "总成本公式")
    private String planCostFormula;

     @Schema(description = "保留小数")
    private Integer reservedDecimal;

     @Schema(description = "进位规则")
    private String carryRule;

    @Schema(description = "科室单元id")
    private Long unitId;

    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建人")
    private String createBy;

    @TableField(fill = FieldFill.UPDATE)
    @Schema(description = "修改人")
    private String updateBy;

    @Schema(description = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @Schema(description = "修改时间")
    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;

    @Schema(description = "租户id")
    private Long tenantId;

}
