package com.hscloud.hs.cost.account.mapper;

import com.hscloud.hs.cost.account.model.entity.CostAccountPlanConfigInfo;
import com.pig4cloud.pigx.common.data.datascope.PigxBaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface CostAccountPlanConfigInfoMapper extends PigxBaseMapper<CostAccountPlanConfigInfo> {
    Long selectIdByParentIndexId(@Param("parentIndexId") Long parentIndexId);

    void saveUnitId(@Param("unitId")Long unitId, @Param("plan_index_id")Long planIndexId);
}
