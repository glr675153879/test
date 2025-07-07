package com.hscloud.hs.cost.account.mapper.kpi;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hscloud.hs.cost.account.model.dto.kpi.KpiTransferListDTO;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiItemResult;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiItemResultCopy;
import com.hscloud.hs.cost.account.model.vo.kpi.KpiTransferInfoVO2;
import com.hscloud.hs.cost.account.model.vo.kpi.KpiTransferListVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
* 核算项结果集 Mapper 接口
*
*/
@Mapper
public interface KpiItemResultMapper extends BaseMapper<KpiItemResult> {
    Integer insertBatchSomeColumn(@Param("list") List<KpiItemResult> list);

    IPage<KpiTransferListVO> getTransferList(Page<KpiTransferListVO> page,@Param("dto") KpiTransferListDTO dto);

    List<KpiTransferInfoVO2> getTransferList2(@Param("dto") KpiTransferListDTO dto);

    @Select("select * from kpi_item_result ${ew.customSqlSegment}")
    List<KpiItemResultCopy> getList(@Param("ew") QueryWrapper<KpiItemResult> ew);

}

