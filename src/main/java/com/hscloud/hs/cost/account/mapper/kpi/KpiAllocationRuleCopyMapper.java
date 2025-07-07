package com.hscloud.hs.cost.account.mapper.kpi;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiAllocationRuleCopy;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* 分摊公式表备份 Mapper 接口
*
*/
@Mapper
public interface KpiAllocationRuleCopyMapper extends BaseMapper<KpiAllocationRuleCopy> {
    void insertBatchSomeColumn(@Param("list") List<KpiAllocationRuleCopy> list);
}

