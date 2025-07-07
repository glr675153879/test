package com.hscloud.hs.cost.account.model.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.pig4cloud.pigx.common.core.util.TenantTable;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 核算指标分组
 * </p>
 *
 * @author 
 * @since 2023-09-04
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TenantTable
@Schema(description = "核算指标分组")
public class CostBaseGroup extends Model<CostBaseGroup> {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    @Schema(description = "分组id")
    private Long id;

    @Schema(description = "分组名称")
    private String name;

    @Schema(description = "父级分组id")
    private Long parentId;

    @Schema(description = "启停用标识 0-正常，1-停用")
    private String status;

    @Schema(description = "分组类型")
    private String typeGroup;

    @Schema(description = "是否系统分组  0:非系统指标 1：系统指标")
    private String isSystem;

    @Schema(description = "是否为数据上报核算项")
    private Boolean isReport;

    @Schema(description = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @Schema(description = "修改时间")
    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;

    @Schema(description = "备用字段：1科室成本分摊项 存财务科目；")
    private String extra;

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

    @Schema(description = "租户id")
    private Long tenantId;

}
