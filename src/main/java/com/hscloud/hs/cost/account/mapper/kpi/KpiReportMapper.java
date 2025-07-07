package com.hscloud.hs.cost.account.mapper.kpi;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hscloud.hs.cost.account.model.dto.kpi.KpiCodeDetailPageDTO;
import com.hscloud.hs.cost.account.model.dto.kpi.KpiCodePageDTO;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiCalculate;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiReport;
import com.hscloud.hs.cost.account.model.vo.kpi.KpiReportDetailVO;
import com.hscloud.hs.cost.account.model.vo.kpi.KpiReportVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
* 核算子方案备份 Mapper 接口
*
*/
@Mapper
public interface KpiReportMapper extends BaseMapper<KpiReport> {
    void insertBatchSomeColumn(@Param("list") List<KpiReport> list);

    IPage<KpiReportVO> page(Page page,@Param("input") KpiCodePageDTO input);

    IPage<KpiReportDetailVO> selectList(Page Page,@Param("input")  KpiCodeDetailPageDTO input);

    @Select("select * from kpi_report where code=#{code}")
    KpiReport getOne(String code);

    List<KpiCalculate> getItemCas(@Param("ew")QueryWrapper<KpiCalculate> ew);
}

