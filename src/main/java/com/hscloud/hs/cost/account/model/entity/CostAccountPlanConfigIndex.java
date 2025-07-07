package com.hscloud.hs.cost.account.model.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;


import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 核算方案配置的指标项
 *
 * @author zjd
 * @email zjd@gmail.com
 * @date 2023-09-12 13:55:03
 */
@Data
@Schema(description = "核算方案配置的指标项")

public class CostAccountPlanConfigIndex extends Model<CostAccountPlanConfigIndex> {
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    @Schema(description = "id")
    private 	  Long  id;
    /**
     * 核算方案配置id
     */
    @Schema(description = "核算方案配置id")
    private 	  Long  planConfigId;
    /**
     * 核算项id
     */
    @Schema(description = "核算项id")
    private 	  Long  itemId;
    /**
     * 医护分摊标记，0不选择医护分摊，1选择
     */
    @Schema(description = "医护分摊标记，0不选择医护分摊，1选择")
    private 	  String  medicalAllocation;
    /**
     * 医护分摊比例
     */
    @Schema(description = "医护分摊比例")
    private 	  String  medicalAllocationProportion;

    /**
     * 业务id 根据选的核算范围/核算比例详情类型区分不同的id（1.科室单元id 2.科室id 3.人员id）
     */
    @Schema(description = "医护分摊比例")
    private 	  String  bzid;

    /**
     * 借床分摊标记，0不选择借床分摊，1选择
     */
    @Schema(description = "借床分摊标记，0不选择借床分摊，1选择")
    private 	  String  bedAllocation;
    /**
     * 租户id
     */
    @Schema(description = "租户id")
    private 	  Long  tenantId;
    /**
     * 被分摊核算范围
     */
    @Schema(description = "被分摊核算范围")
    private 	  String  accountRange;
//    /**
//     * 分摊规则公式
//     */
//    @Schema(description = "分摊规则公式")
//    private 	  String  ruleFormula;
    /**
     * 分摊规则公式id
     */
    @Schema(description = "分摊规则公式id")
    private 	  Long  ruleFormulaId;

}
