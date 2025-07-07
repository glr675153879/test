package com.hscloud.hs.cost.account.mapper.kpi;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiReportConfigCopy;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* 报表多选配置 Mapper 接口
*
*/
@Mapper
public interface KpiReportConfigCopyMapper extends BaseMapper<KpiReportConfigCopy> {
    void insertBatchSomeColumn(@Param("list") List<KpiReportConfigCopy> list);
}

