package com.hscloud.hs.cost.account.mapper.kpi;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hscloud.hs.cost.account.model.dto.kpi.KpiAccountPlanListDto;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiAccountPlan;
import com.hscloud.hs.cost.account.model.vo.kpi.KpiAccountPlanListVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

/**
* 核算方案表(COST_ACCOUNT_PLAN) Mapper 接口
*
*/
@Mapper
public interface KpiAccountPlanMapper extends BaseMapper<KpiAccountPlan> {


    IPage<KpiAccountPlanListVO> getPage(Page<Object> objectPage,@Param("input") KpiAccountPlanListDto input);

    List<KpiAccountPlanListVO> getList(@Param("input") KpiAccountPlanListDto input);

    Integer insertBatchSomeColumn(Collection<KpiAccountPlan> entityList);
}

