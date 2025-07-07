package com.hscloud.hs.cost.account.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Admin
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "方案自定义科室单元")
public class CostAccountPlanConfigCustomUnit extends Model<CostAccountPlanConfigCustomUnit> {

    @Schema(description = "id")
    @TableId(type = IdType.AUTO)
    private Long id;

    @Schema(description = "核算方案id")
    private Long planId;

    @Schema(description = "核算方案配置id")
    private Long planConfigId;

    @Schema(description = "自定义科室单元id")
    private Long customUnitId;


    @Schema(description = "租户id")
    private Long tenantId;

    @Schema(description = "是否删除：0：未删除 1：删除")
    private String del_flag;
}
