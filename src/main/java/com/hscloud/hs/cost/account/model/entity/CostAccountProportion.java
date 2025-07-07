package com.hscloud.hs.cost.account.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/** 核算比例表
 * @author banana
 * @create 2023-09-13 14:53
 */

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "成本核算比例项")
public class CostAccountProportion extends Model<CostAccountProportion> {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    @Schema(description = "主键")
    private Long id;

    @Schema(description = "核算项id")
    private String costAccountItemId;

    @Schema(description = "核算项")
    private String costAccountItem;

    @Schema(description = "核算范围")
    private String accountObject;

    @Schema(description = "分组类型id")
    private String typeGroupId;

    @Schema(description = "分组类型")
    private String typeGroup;

    @Schema(description = "启停用标记，0启用，1停用")
    private String status;

    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "删除标记，0未删除，1已删除")
    private String delFlag;

    @Schema(description = "初始化状态: Y:已初始化  N:未初始化")
    private String initialized;

    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建人")
    private String createBy;

    @TableField(fill = FieldFill.UPDATE)
    @Schema(description = "修改人")
    private String updateBy;

    @Schema(description = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;

    @Schema(description = "租户id")
    private Long tenantId;
}
