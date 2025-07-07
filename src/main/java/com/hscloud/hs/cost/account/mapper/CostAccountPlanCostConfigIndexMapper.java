package com.hscloud.hs.cost.account.mapper;

import com.hscloud.hs.cost.account.model.entity.CostAccountPlanConfig;
import com.hscloud.hs.cost.account.model.entity.CostAccountPlanCostConfigIndex;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 核算方案成本公式配置表
 * 
 * @author zjd
 * @email zjd@gmail.com
 * @date 2023-09-17 11:32:01
 */
@Mapper
public interface CostAccountPlanCostConfigIndexMapper extends BaseMapper<CostAccountPlanCostConfigIndex> {
	void deleteByConfigKey(@Param("configKey") String configKey);

	@Select("select * from cost_account_plan_config where account_proportion_object=#{accountProportionObject}")
	List<CostAccountPlanConfig> getCostAccountPlanConfigList(String accountProportionObject);
}
