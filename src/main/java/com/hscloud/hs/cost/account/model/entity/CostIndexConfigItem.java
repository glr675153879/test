package com.hscloud.hs.cost.account.model.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * @author Admin
 */
@Data
@Schema(description = "配置项")
@EqualsAndHashCode(callSuper = true)
public class CostIndexConfigItem extends Model<CostIndexConfigItem> {

    @TableId(type = IdType.AUTO)
    @Schema(description = "id")
    private Long id;

    @Schema(description = "指标项id")
    private Long indexId;

    @Schema(description = "配置项id")
    private Long configId;

    @Schema(description = "配置项key")
    private String configKey;

//    @Schema(description = "计算维度")
//    private String dimension;

    @Schema(description ="配置项名称")
    private String configName;

    @Schema(description ="配置项描述")
    private String configDesc;

    @Schema(description = "核算对象")
    private String accountObject;

    @Schema(description = "核算范围")
    private String accountRange;

    @Schema(description = "核算集")
    private String accounts;

    @Schema(description = "核算单元性质")
    private String accountDeptShip;

    @Schema(description = "核算周期 current:当前周期 before:上一周期")
    private String accountPeriod;


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
