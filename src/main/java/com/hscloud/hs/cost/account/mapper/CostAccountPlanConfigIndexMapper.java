package com.hscloud.hs.cost.account.mapper;

import com.hscloud.hs.cost.account.model.entity.CostAccountPlanConfigIndex;
import com.pig4cloud.pigx.common.data.datascope.PigxBaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CostAccountPlanConfigIndexMapper  extends PigxBaseMapper<CostAccountPlanConfigIndex> {
    List<CostAccountPlanConfigIndex> selectIndexByConfigId(@Param("configId") Long configId);
}
