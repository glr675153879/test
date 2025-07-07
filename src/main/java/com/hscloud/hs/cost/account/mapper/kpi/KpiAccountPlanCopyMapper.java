package com.hscloud.hs.cost.account.mapper.kpi;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiAccountPlanCopy;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiCategory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* 核算方案表备份 Mapper 接口
*
*/
@Mapper
public interface KpiAccountPlanCopyMapper extends BaseMapper<KpiAccountPlanCopy> {
    void insertBatchSomeColumn(@Param("list") List<KpiAccountPlanCopy> list);
}

