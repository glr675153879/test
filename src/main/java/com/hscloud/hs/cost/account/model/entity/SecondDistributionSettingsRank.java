package com.hscloud.hs.cost.account.model.entity;

import java.math.BigDecimal;

import com.baomidou.mybatisplus.annotation.*;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;

/**
 * <p>
 * 分配设置职称绩效
 * </p>
 *
 * @author
 * @since 2023-11-20
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("second_distribution_settings_rank")
@Schema(description = "分配设置职称绩效")
public class SecondDistributionSettingsRank extends Model<SecondDistributionSettingsRank> {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键id")
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "职称级别")
    private String name;

    @Schema(description = "系数")
    private BigDecimal coefficient;

    @Schema(description = "状态  0 启用  1 停用")
    private String status;

    @NotNull
    @Schema(description = "科室单元id")
    private Long unitId;

    @Schema(description = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @Schema(description = "修改时间")
    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;

    @Schema(description = "租户id")
    private Long tenantId;


}
