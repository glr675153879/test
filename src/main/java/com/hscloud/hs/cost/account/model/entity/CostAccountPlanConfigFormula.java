package com.hscloud.hs.cost.account.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * @author Admin
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "核算方案配置公式")
public class CostAccountPlanConfigFormula extends Model<CostAccountPlanConfigFormula> {


    @Schema(description = "主键id")
    @TableId(type = IdType.AUTO)
    private Long id;

    @Schema(description = "核算方案id")
    private Long planId;

    @Schema(description = "核算方案配置公式")
    private String planCostFormula;

    @Schema(description = "核算对象")
    private String accountObject;

    @Schema(description = "自定义科室单元id")
    private Long customUnitId;

    @Schema(description = "保留小数位数")
    private Integer reservedDecimal;

    @Schema(description = "配置")
    private String config;

    @Schema(description = "进位规则")
    private String carryRule;

    @Schema(description = "是否删除：0：未删除 1：删除")
    @TableLogic
    private String delFlag;

    @Schema(description = "租户id")
    private Long tenantId;

    /**
     * 创建人
     */
    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建人")
    private String createBy;

    /**
     * 修改人
     */
    @TableField(fill = FieldFill.UPDATE)
    @Schema(description = "修改人")
    private String updateBy;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /**
     * 修改时间
     */
    @TableField(fill = FieldFill.UPDATE)
    @Schema(description = "修改时间")
    private LocalDateTime updateTime;
}
