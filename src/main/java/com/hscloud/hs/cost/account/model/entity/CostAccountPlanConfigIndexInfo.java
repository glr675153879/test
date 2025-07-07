package com.hscloud.hs.cost.account.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "核算方案的核算指标配置项的被分摊核算对象的自定义科室表")
@TableName("cost_account_plan_config_index_info")
public class CostAccountPlanConfigIndexInfo extends Model<CostAccountPlanConfigIndexInfo> {
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    @Schema(description = "id")
    private 	  Long  id;
    /**
     * 核算方案配置的核算指标配置项的id
     */
    @Schema(description = "核算方案配置的核算指标配置项的id")
    private 	  Long  planIndexId;
    /**
     * 科室id
     */
    @Schema(description = "科室id")
    private 	  Long  unitId;
    /**
     * 租户id
     */
    @Schema(description = "租户id")
    private 	  Long  tenantId;

}
