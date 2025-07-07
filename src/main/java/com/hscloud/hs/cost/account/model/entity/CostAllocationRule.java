package com.hscloud.hs.cost.account.model.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.time.LocalDateTime;
import java.io.Serializable;

import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.hscloud.hs.cost.account.constant.enums.StatisticalPeriodEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.apache.commons.math3.analysis.function.Cos;

/**
 * <p>
 * 分摊规则表
 * </p>
 *
 * @author 
 * @since 2023-09-11
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Schema(description = "分摊规则表")
public class CostAllocationRule extends Model<CostAllocationRule> {

    private static final long serialVersionUID = 1L;

    @Schema(description = "分摊规则id")
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "分摊规则名称")
    private String name;

    @Schema(description = "统计周期")
    private StatisticalPeriodEnum statisticalCycle;

    @Schema(description = "分摊规则公式")
    private String allocationRuleFormula;


    @Schema(description = "状态：0：启用  1:停用")
    private String status;


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
