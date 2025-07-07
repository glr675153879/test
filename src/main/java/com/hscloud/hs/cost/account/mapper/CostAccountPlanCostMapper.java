package com.hscloud.hs.cost.account.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hscloud.hs.cost.account.model.entity.CostAccountPlanCost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface CostAccountPlanCostMapper extends BaseMapper<CostAccountPlanCost> {
    /*@Select("select id from cost_account_plan_cost where")
    Long selectIdByPlanIdAndAccountProportionObject(@Param("planId") Long planId, @Param("accountProportionObject")String accountProportionObject);*/
}
