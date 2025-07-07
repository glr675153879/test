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
 * 二次分配方案表
 * </p>
 *
 * @author 
 * @since 2023-11-20
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("second_distribution_account_plan")
@Schema(description="二次分配方案表")
public class SecondDistributionAccountPlan extends Model<SecondDistributionAccountPlan> {

    private static final long serialVersionUID = 1L;

     @Schema(description = "核算方案id")
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

     @Schema(description = "核算指标表id集合")
    private String accountIndexIds;

     @Schema(description = "总公式id集合")
    private String formulaIds;

    @Schema(description = "科室单元id")
    private Long unitId;

    @Schema(description = "状态 0未生效 1已生效")
    private Integer status;

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
