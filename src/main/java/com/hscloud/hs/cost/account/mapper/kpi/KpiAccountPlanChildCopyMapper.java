package com.hscloud.hs.cost.account.mapper.kpi;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hscloud.hs.cost.account.model.dto.kpi.KpiReportLeftDto;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiAccountPlanChildCopy;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiIndexCopy;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* 核算子方案备份 Mapper 接口
*
*/
@Mapper
public interface KpiAccountPlanChildCopyMapper extends BaseMapper<KpiAccountPlanChildCopy> {
    void insertBatchSomeColumn(@Param("list") List<KpiAccountPlanChildCopy> list);

    List<KpiReportLeftDto> getIndexs(@Param("task_child_id") Long task_child_id);
}

