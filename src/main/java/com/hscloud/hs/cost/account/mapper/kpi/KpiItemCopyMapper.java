package com.hscloud.hs.cost.account.mapper.kpi;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hscloud.hs.cost.account.model.dto.kpi.KpiCalculateConfigDto;
import com.hscloud.hs.cost.account.model.dto.kpi.KpiReportAlloValue;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiAccountUnitCopy;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiAllocationRuleCopy;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiItemCopy;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
* 核算项备份 Mapper 接口
*
*/
@Mapper
public interface KpiItemCopyMapper extends BaseMapper<KpiItemCopy> {
    void insertBatchSomeColumn(@Param("list") List<KpiItemCopy> list);

    List<KpiCalculateConfigDto> getList(@Param("ew") QueryWrapper<KpiCalculateConfigDto> ew);

    @Select("select * from kpi_item ${ew.customSqlSegment}")
    List<KpiItemCopy> getList2(@Param("ew") QueryWrapper<KpiItemCopy> ew);

    List<KpiReportAlloValue> getAlloValue(@Param("r") KpiAllocationRuleCopy r,@Param("period")Long period);

    List<KpiReportAlloValue> getAlloValue2(@Param("r") KpiAllocationRuleCopy r,@Param("period")Long period);
}

