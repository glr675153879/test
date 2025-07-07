package com.hscloud.hs.cost.account.model.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 二次分配方案核算指标表
 * </p>
 *
 * @author 
 * @since 2023-11-24
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("second_distribution_account_index")
@Schema(description="二次分配方案核算指标表")
public class SecondDistributionAccountIndex extends Model<SecondDistributionAccountIndex> {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键id")
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "配置表id")
    private Long planId;

    @Schema(description = "核算指标")
    private String accountIndex;

    @Schema(description = "业务内容")
    private String bizContent;

    @Schema(description = "排序号")
    private Integer seq;

    @Schema(description = "进位规则")
    private String carryRule;

    @Schema(description = "指标保留小数")
    private Integer reservedDecimal;

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
