package com.hscloud.hs.cost.account.model.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.time.LocalDateTime;

import java.io.Serializable;

import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.hscloud.hs.cost.account.model.vo.CostAccountIndexVo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 核算指标配置项为核算指标
 * </p>
 *
 * @author
 * @since 2023-09-10
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "配置项为核算指标")
public class CostIndexConfigIndex extends Model<CostIndexConfigIndex> {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    @Schema(description = "id")
    private Long id;

    @Schema(description = "指标项id")
    private Long indexId;

    @Schema(description = "配置项指标id")
    private Long configIndexId;

    @Schema(description = "配置项指标名称")
    private String configIndexName;

    @Schema(description = "配置项key")
    private String configKey;


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


    @TableField(exist = false)
    @Schema(description = "里面包含的核算指标")
    private CostAccountIndexVo costAccountIndexVo;

}
