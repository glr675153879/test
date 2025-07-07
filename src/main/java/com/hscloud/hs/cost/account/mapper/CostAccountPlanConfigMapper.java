package com.hscloud.hs.cost.account.mapper;

import com.hscloud.hs.cost.account.model.entity.CostAccountPlanConfig;
import com.pig4cloud.pigx.common.data.datascope.PigxBaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface CostAccountPlanConfigMapper extends PigxBaseMapper<CostAccountPlanConfig> {
    @Select("SELECT index_id FROM cost_account_plan_config WHERE id=#{id}")
    Long selectIndexIdById(@Param("id") Long id);

    @Select("select id FROM cost_account_plan_config_index WHERE plan_config_id=#{planConfigId} AND item_id=#{itemId}")
    Long selectIdByPlanConfigIdAndItemId(@Param("planConfigId") Long planConfigId, @Param("itemId") Long itemId);

    void deleteIndexbyPlanConfigId(@Param("planConfigId") Long planConfigId);

    @Select("SELECT account_proportion_object FROM cost_account_plan_config WHERE id=#{id}")
    String selectObjectById(@Param("id") Long id);
}
