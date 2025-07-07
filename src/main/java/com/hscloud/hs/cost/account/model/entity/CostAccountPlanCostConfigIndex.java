package com.hscloud.hs.cost.account.model.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;

import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import lombok.Data;

/**
 * 核算方案成本公式配置表
 * 
 * @author zjd
 * @email zjd@gmail.com
 * @date 2023-09-17 11:32:01
 */
@Data
@Schema(description = "核算方案成本公式配置表")
@TableName("cost_account_plan_cost_config_index")
public class CostAccountPlanCostConfigIndex implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 核算方案成本公式配置id
	 */
	@TableId(value = "id", type = IdType.ASSIGN_ID)
	@Schema(description = "核算方案成本公式配置id")
	private 	  Long  id;
	/**
	 * 配置指标id
	 */
	@Schema(description = "配置指标id")
	private 	  String  configId;
	/**
	 * 配置指标名
	 */
	@Schema(description = "配置指标名")
	private 	  String  name;
	/**
	 * 指标类型
	 */
	@Schema(description = "指标类型")
	private 	  String  type;
	/**
	 * 配置指标key
	 */
	@Schema(description = "配置指标key")
	private 	  String  configKey;
	/**
	 * 创建时间
	 */
	@Schema(description = "创建时间")
	private 	  Date  createTime;
	/**
	 * 修改时间
	 */
	@Schema(description = "修改时间")
	private 	  Date  updateTime;
	/**
	 * 创建人
	 */
	@Schema(description = "创建人")
	private 	  String  createBy;
	/**
	 * 修改人
	 */
	@Schema(description = "修改人")
	private 	  Date  updateBy;
	/**
	 * 租户id
	 */
	@Schema(description = "租户id")
	private 	  Long  tenantId;

}
