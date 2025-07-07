package com.hscloud.hs.cost.account.mapper.kpi;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiReportYearImport;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface KpiReportYearImportMapper extends BaseMapper<KpiReportYearImport> {
    void insertBatchSomeColumn(@Param("list") List<KpiReportYearImport> list);
}
