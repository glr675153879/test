package com.hscloud.hs.cost.account.mapper.kpi;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiIndexCopy;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* 指标表备份 Mapper 接口
*
*/
@Mapper
public interface KpiIndexCopyMapper extends BaseMapper<KpiIndexCopy> {
    void insertBatchSomeColumn(@Param("list") List<KpiIndexCopy> list);
}

