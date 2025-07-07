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
 * 分摊规则配置项为核算指标
 * </p>
 *
 * @author 
 * @since 2023-09-11
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description="分摊规则配置项为核算指标")
public class CostAllocationRuleConfigIndex extends Model<CostAllocationRuleConfigIndex> {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键id")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "配置项key")
    private String configKey;

    @Schema(description = "分摊规则id")
    private Long allocationRuleId;

    @Schema(description = "配置项指标id")
    private Long configIndexId;

    @Schema(description = "配置项指标名称")
    private String configIndexName;

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
