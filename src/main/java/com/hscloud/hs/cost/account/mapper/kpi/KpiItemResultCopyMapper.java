package com.hscloud.hs.cost.account.mapper.kpi;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hscloud.hs.cost.account.model.dto.kpi.KpiItemResultCopyDTO;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiItemResultCopy;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiReportConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
* 核算项结果集备份 Mapper 接口
*
*/
@Mapper
public interface KpiItemResultCopyMapper extends BaseMapper<KpiItemResultCopy> {
    void insertBatchSomeColumn(@Param("list") List<KpiItemResultCopy> list);

    @Select("select * from kpi_item_result_copy ${ew.customSqlSegment}")
    List<KpiItemResultCopyDTO> getList(@Param("ew") QueryWrapper<KpiItemResultCopyDTO> ew);
    @Select("select * from kpi_item_result ${ew.customSqlSegment}")
    List<KpiItemResultCopyDTO> getList2(@Param("ew") QueryWrapper<KpiItemResultCopyDTO> ew);
}

