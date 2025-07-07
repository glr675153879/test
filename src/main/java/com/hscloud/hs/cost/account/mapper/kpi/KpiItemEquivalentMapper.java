package com.hscloud.hs.cost.account.mapper.kpi;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hscloud.hs.cost.account.model.dto.kpi.KpiItemEquivalentDTO;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiItemEquivalent;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiItemEquivalentCopy;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface KpiItemEquivalentMapper extends BaseMapper<KpiItemEquivalent>{
    IPage<KpiItemEquivalent> getPage(Page<Object> page , @Param("input") KpiItemEquivalentDTO input);

    @Select("select * from kpi_item_equivalent ${ew.customSqlSegment}")
    List<KpiItemEquivalentCopy> getList(@Param("ew") QueryWrapper<KpiItemEquivalent> ew);

    void updateBatchById(@Param("list") List<KpiItemEquivalent> updateEquivalents);

    void updateStdEquivalent(@Param("period") Long period);

    void updateTotalEquivalent(@Param("period") Long period);
}