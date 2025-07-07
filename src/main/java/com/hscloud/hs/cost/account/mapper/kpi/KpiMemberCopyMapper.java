package com.hscloud.hs.cost.account.mapper.kpi;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiMember;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiMemberCopy;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
*  Mapper 接口
*
*/
@Mapper
public interface KpiMemberCopyMapper extends BaseMapper<KpiMemberCopy> {
    void insertBatchSomeColumn(@Param("list") List<KpiMemberCopy> list);

    @Select("select * from kpi_member ${ew.customSqlSegment}")
    List<KpiMemberCopy> getList(@Param("ew") QueryWrapper<KpiMemberCopy> ew);
}

