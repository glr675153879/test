package com.hscloud.hs.cost.account.mapper.kpi;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hscloud.hs.cost.account.model.dto.kpi.KpiCodeDetailPageDTO;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiReportDetail;
import com.hscloud.hs.cost.account.model.vo.kpi.KpiReportDetailVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* 核算子方案备份 Mapper 接口
*
*/
@Mapper
public interface KpiReportDetailMapper extends BaseMapper<KpiReportDetail> {
    void insertBatchSomeColumn(@Param("list") List<KpiReportDetail> list);
}

