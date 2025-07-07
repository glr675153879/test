package com.hscloud.hs.cost.account.mapper.kpi;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiCalculateComp;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface KpiCalculateCompMapper extends BaseMapper<KpiCalculateComp> {

    void insertBatchSomeColumn(@Param("list") List<KpiCalculateComp> list);
}
