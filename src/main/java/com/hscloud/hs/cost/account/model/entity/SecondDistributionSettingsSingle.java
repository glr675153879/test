package com.hscloud.hs.cost.account.model.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.time.LocalDateTime;
import java.io.Serializable;

import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;

/**
 * <p>
 * 分配设置单项绩效
 * </p>
 *
 * @author
 * @since 2023-11-20
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("second_distribution_settings_single")
@Schema(description = "分配设置单项绩效")
public class SecondDistributionSettingsSingle extends Model<SecondDistributionSettingsSingle> {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键id")
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "名称")
    private String name;

    @Schema(description = "新性质")
    private String newProperty;

    @Schema(description = "说明")
    private String description;

    @NotNull
    @Schema(description = "科室单元id")
    private Long unitId;

    @Schema(description = "状态  0 启用  1 停用")
    private String status;

    @Schema(description = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @Schema(description = "修改时间")
    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;

    @Schema(description = "租户id")
    private Long tenantId;


}
