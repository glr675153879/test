package com.hscloud.hs.cost.account.mapper;

import com.hscloud.hs.cost.account.model.entity.CostAccountPlanConfigIndexInfo;
import com.pig4cloud.pigx.common.data.datascope.PigxBaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CostAccountPlanConfigIndexInfoMapper extends PigxBaseMapper<CostAccountPlanConfigIndexInfo> {
    @Select("select unit_id from cost_account_plan_config_index_info where plan_index_id=#{planIndexId}")
    List<Long> selectUnitIdsByPlanIndexId(@Param("planIndexId") Long planIndexId);
}
