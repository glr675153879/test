package com.hscloud.hs.cost.account.model.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.time.LocalDateTime;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.hscloud.hs.cost.account.constant.enums.StatisticalPeriodEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 核算指标表
 * </p>
 *
 * @author 
 * @since 2023-09-04
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description = "核算指标表")
public class CostAccountIndex extends Model<CostAccountIndex> {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    @Schema(description = "核算指标id")
    private Long id;

    @Schema(description = "核算指标名称")
    private String name;

    @Schema(description = "指标单位")
    private String indexUnit;

    @Schema(description = "指标性质")
    private String indexProperty;

    @Schema(description = "统计周期")
    private StatisticalPeriodEnum statisticalCycle;

    @Schema(description = "指标分组id")
    private Long indexGroupId;

    @Schema(description = "状态：0：启用  1:停用")
    private String status;

    @Schema(description = "进位规则id")
    private String carryRule;

    @Schema(description = "指标保留小数")
    private Integer reservedDecimal;

    @Schema(description = "指标公式")
    private String indexFormula;

    @Schema(description = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @Schema(description = "修改时间")
    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;

    @Schema(description = "是否是系统指标  0 否  1 是")
    private String isSystemIndex;

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
     * 0-正常，1-删除
     */
    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "删除标记,1:已删除,0:正常")
    private String delFlag;

    /**
     * 租户ID
     */
    @Schema(description = "租户ID")
    private Long tenantId;

}
