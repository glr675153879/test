package com.hscloud.hs.cost.account.mapper.kpi;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiAllocationRuleCopy;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiCategoryCopy;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* 分组表备份 Mapper 接口
*
*/
@Mapper
public interface KpiCategoryCopyMapper extends BaseMapper<KpiCategoryCopy> {
    void insertBatchSomeColumn(@Param("list") List<KpiCategoryCopy> list);
}

