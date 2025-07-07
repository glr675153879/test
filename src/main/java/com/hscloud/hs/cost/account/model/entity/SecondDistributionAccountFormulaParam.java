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
 * 二次分配方案公式参数
 * </p>
 *
 * @author 
 * @since 2023-11-25
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("second_distribution_account_formula_param")
@Schema( description="二次分配方案公式参数")
public class SecondDistributionAccountFormulaParam extends Model<SecondDistributionAccountFormulaParam> {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键id")
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "方案id")
    private Long planId;

    @Schema(description = "核算指标/总公式配置id")
    private Long bizId;

    @Schema(description = "类型：0 核算指标  1总公式")
    private Integer type;

    @Schema(description = "公式参数key")
    private String formulaKey;

    @Schema(description = "公式参数名称")
    private String formulaName;

    @Schema(description = "参数类型 1分配设置 2指标 3数据小组")
    private String formulaType;

    @Schema(description = "参数值")
    private String formulaValue;

    @Schema(description = "科室单元id")
    private Long unitId;

    @Schema(description = "租户id")
    private Long tenantId;

    @Schema(description = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

     @Schema(description = "修改时间")
     @TableField(fill = FieldFill.UPDATE)
     private LocalDateTime updateTime;

     @Schema(description = "创建人")
     @TableField(fill = FieldFill.INSERT)
     private String createBy;

     @Schema(description = "修改人")
     @TableField(fill = FieldFill.UPDATE)
     private String updateBy;


}
