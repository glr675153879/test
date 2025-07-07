package com.hscloud.hs.cost.account.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.pig4cloud.pigx.common.core.util.TenantTable;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 物资收费管理
 * @author  lian
 * @date  2024/6/2 15:00
 *
 */

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TenantTable
@TableName("material_charge")
@Schema(description ="物资收费管理")
public class MaterialCharge extends Model<MaterialCharge> {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键id")
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;


    @Schema(description = "物资id")
    private String resourceId;

    @Schema(description = "物资名称")
    private String resourceName;

    @Schema(description = "仓库id")
    private String storeId;

    @Schema(description = "仓库名称")
    private String storeName;

    @Schema(description = "是否收费 N否 Y是")
    private String isCharge;

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
