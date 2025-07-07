package com.hscloud.hs.cost.account.mapper.kpi;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiReportConfigImport;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface KpiReportConfigImportMapper extends BaseMapper<KpiReportConfigImport> {

    void insertBatchSomeColumn(@Param("list") List<KpiReportConfigImport> list);

}
