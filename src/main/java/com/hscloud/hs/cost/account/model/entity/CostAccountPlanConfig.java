package com.hscloud.hs.cost.account.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * @author Administrator
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Schema(description = "核算方案配置")
public class CostAccountPlanConfig extends Model<CostAccountPlanConfig> {
    /**
     * 方案指标配置的id
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    @Schema(description = "id")
    private Long id;
    /**
     * 核算方案id
     */
    @Schema(description = "核算方案id")
    private Long planId;
    /**
     * 核算对象
     */
    /**
     * 核算指标id
     */
    @Schema(description = "核算指标id")
    private Long indexId;

    @Schema(description = "核算指标key")
    private String configKey;

    @Schema(description = "配置指标名称")
    private String configIndexName;

    @Schema(description = "核算对象")
    private String accountProportionObject;

    @Schema(description = "是否是关联指标   0：否   1：是")
    private String isRelevance;

    @Schema(description = "关联指标核算范围")
    private String relevanceAccountProportionObject;

    @Schema(description = "关联系统指标公式描述")
    private String configDesc;


    /**
     * 是否删除：0：未删除 1：删除
     */
    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "是否删除：0：未删除 1：删除")
    private String delFlag;

    /**
     * 租户id
     */
    @Schema(description = "租户id")
    private Long tenantId;


    @Schema(description = "排序seq")
    private Integer seq;

}
