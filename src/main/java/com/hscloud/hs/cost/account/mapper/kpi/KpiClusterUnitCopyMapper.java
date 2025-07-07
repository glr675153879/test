package com.hscloud.hs.cost.account.mapper.kpi;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiClusterUnitCopy;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiUserAttendanceCopy;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface KpiClusterUnitCopyMapper extends BaseMapper<KpiClusterUnitCopy> {
    void insertBatchSomeColumn(@Param("list") List<KpiClusterUnitCopy> userStudyList);

    @Select("select * from cost_cluster_unit ${ew.customSqlSegment}")
    List<KpiClusterUnitCopy> getList(@Param("ew") QueryWrapper<KpiClusterUnitCopy> ew);
}
