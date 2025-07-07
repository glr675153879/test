package com.hscloud.hs.cost.account.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

@Data
public class CostAccountPlanCost extends Model<CostAccountPlanCost> {
    private static final long serialVersionUID = 1L;

    /**
     * 方案总成本id
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    @Schema(description = "方案总成本id")
    private Long id;
    /**
     * 所属方案id
     */
    @Schema(description = "所属方案id")
    private Long planId;
    /**
     * 总成本公式
     */
    @Schema(description = "总成本公式")
    private String planCostFormula;

    @Schema(description = "总成本公式")
    private String accountObject;

    /**
     * 总成本公式
     */
    @Schema(description = "公式描述")
    private String costDescription;
//    /**
//     * 方案总成本
//     */
//    @Schema(description = "方案总成本")
//    private 	  String  name;
    /**
     * 核算范围
     */
    @Schema(description = "核算范围")
    private String accountProportionObject;
    /**
     * 保留小数
     */
    @Schema(description = "保留小数")
    private Long reservedDecimal;
    /**
     * 进位规则
     */
    @Schema(description = "进位规则")
    private String carryRule;
    /**
     * 创建人
     */
    @Schema(description = "创建人")
    private String createBy;
    /**
     * 修改人
     */
    @Schema(description = "修改人")
    private String updateBy;
    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private Date createTime;
    /**
     * 修改时间
     */
    @Schema(description = "修改时间")
    private Date updateTime;
    /**
     * 租户id
     */
    @Schema(description = "租户id")
    private Long tenantId;
    /**
     * 是否删除：0：未删除 1：删除
     */
    @Schema(description = "是否删除：0：未删除 1：删除")
    private String delFlag;
}
