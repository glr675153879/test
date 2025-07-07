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
@Schema(description = "成本核算项")
public class CostAccountItem extends Model<CostAccountItem> {


    @Schema(description = "核算项id")
    @TableId(type = IdType.AUTO)
    private Long id;

    @Schema(description = "核算项名称")
    private String accountItemName;


    @Schema(description = "分组id")
    private String groupId;

    @Schema(description = "核算项类型 二次分配secondProg")
    private String typeGroup;

    @Schema(description = "计量方式")
    private String measureMethod;

    @Schema(description = "计费方式")
    private String billMethod;

    @Schema(description = "计量单位")
    private String measureUnit;

    @Schema(description = "采集方式")
    private String acqMethod;

    @Schema(description = "保留小数位数")
    private Integer retainDecimal;

    @Schema(description = "进位规则")
    private String carryRule;

    @Schema(description = "核算对象")
    private String dimension;

    @Schema(description = "指标项口径")
    private String caliber;

    @Schema(description = "统计方式")
    private String statisticMethod;

    @Schema(description = "配置")
    private String config;

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

    /**
     * 启停用标识 0-正常，1-停用
     */
    @Schema(description = "启停用标识 0-正常，1-停用")
    private String status;

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
