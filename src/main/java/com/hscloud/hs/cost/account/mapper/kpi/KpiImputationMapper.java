package com.hscloud.hs.cost.account.mapper.kpi;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hscloud.hs.cost.account.model.dto.kpi.KpiImputationDeptDto;
import com.hscloud.hs.cost.account.model.dto.kpi.KpiImputationListSearchDto;
import com.hscloud.hs.cost.account.model.dto.kpi.KpiImputationSearchDto;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiImputation;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiUserAttendance;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* 归集表 Mapper 接口
*
*/
@Mapper
public interface KpiImputationMapper extends BaseMapper<KpiImputation> {


    IPage<KpiImputationDeptDto> listByQueryDto(Page page, @Param("input") KpiImputationSearchDto queryDto);


    IPage<KpiImputationDeptDto> listByQueryDto_defalt(Page page, @Param("input") KpiImputationSearchDto queryDto);



    List<KpiImputationDeptDto> listByQueryDto_defalt2(@Param("busiType")String busiType,@Param("period")Long period,@Param("zhonzhishiId")Long zhonzhishiId);


    void insertBatchSomeColumn(@Param("list") List<KpiImputation> userStudyList);

}

