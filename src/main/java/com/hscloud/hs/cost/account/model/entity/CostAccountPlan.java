package com.hscloud.hs.cost.account.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = false)
@Schema(description = "核算方案配置")
public class CostAccountPlan extends Model<CostAccountPlan> {

    /**
     * 核算方案id
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    @Schema(description = "id")
    private Long id;
    /**
     * 分组id
     */
    @Schema(description = "分组id")
    private Long planGroupId;

    /**
     * 任务分组id
     */
    @Schema(description = "任务分组id")
    private Long taskGroupId;
    /**
     * 核算方案名称
     */
    @Schema(description = "核算方案名称")
    private String name;
    /**
     * 是否删除：0：未删除 1：删除
     */
    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "是否删除：0：未删除 1：删除")
    private String delFlag;
    /**
     * 状态：0：启用  1:停用
     */
    @Schema(description = "启停用标记，0启用，1停用")
    private String status;
    /**
     * 创建人
     */
    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建人")
    private String createBy;
    /**
     * 更新人
     */
    @TableField(fill = FieldFill.UPDATE)
    @Schema(description = "修改人")
    private String updateBy;

    @TableField(fill = FieldFill.UPDATE)
    @Schema(description = "修改人工号")
    private Integer updateByNumber;
    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;
    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    @TableField(fill = FieldFill.UPDATE)
    private Date updateTime;
    /**
     * 租户id
     */
    @Schema(description = "租户id")
    private Long tenantId;

    @Schema(description = "确认配置 0未确认 1确认")
    private String checkFlag;

}
