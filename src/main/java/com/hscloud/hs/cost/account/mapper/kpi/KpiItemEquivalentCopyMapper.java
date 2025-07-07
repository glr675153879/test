package com.hscloud.hs.cost.account.mapper.kpi;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hscloud.hs.cost.account.model.dto.kpi.KpiItemEquivalentCopyDTO;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiItemEquivalentCopy;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface KpiItemEquivalentCopyMapper extends BaseMapper<KpiItemEquivalentCopy>{
    void insertBatchSomeColumn(@Param("list") List<KpiItemEquivalentCopy> list);

    @Select("select * from kpi_item_equivalent_copy ${ew.customSqlSegment}")
    List<KpiItemEquivalentCopyDTO> getList(@Param("ew") QueryWrapper ew);
    @Select("select * from kpi_item_equivalent ${ew.customSqlSegment}")
    List<KpiItemEquivalentCopyDTO> getList2(@Param("ew") QueryWrapper ew);
}