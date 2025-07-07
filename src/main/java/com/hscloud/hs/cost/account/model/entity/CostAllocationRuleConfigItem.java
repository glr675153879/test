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
 * 分摊规则配置项
 * </p>
 *
 * @author 
 * @since 2023-09-11
 */
@Data
@Schema(description="分摊规则配置项")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class CostAllocationRuleConfigItem extends Model<CostAllocationRuleConfigItem> {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    @TableId(type = IdType.AUTO)
    private Long id;

    @Schema(description = "分摊规则id")
    private Long allocationRuleId;

    @Schema(description = "配置项key")
    private String configKey;

    @Schema(description = "配置项id")
    private Long configId;

    @Schema(description = "配置项名称")
    private String configName;

    @Schema(description = "配置项描述")
    private String configDesc;

    @Schema(description = "计算维度")
    private String dimension;

    @Schema(description = "核算对象")
    private String accountObject;

    @Schema(description = "核算范围")
    private String accountRange;

    @Schema(description = "核算集")
    private String accounts;

    @Schema(description = "核算单元性质")
    private String accountDeptShip;

    @Schema(description = "核算周期")
    private String accountPeriod;

    @Schema(description = "核算比例")
    private String accountProportion;

    @Schema(description = "核算比例名称")
    private String accountProportionDesc;

    @Schema(description = "比例通用id")
    private Long proportionBaseId;

    @Schema(description = "核算比例id")
    private Long accountProportionId;

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

    @Schema(description = "租户id")
    private Long tenantId;

}
