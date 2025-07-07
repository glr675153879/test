package com.hscloud.hs.cost.account.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hscloud.hs.cost.account.model.dto.CostAccountPlanQueryDto;
import com.hscloud.hs.cost.account.model.entity.CostAccountPlan;
import com.pig4cloud.pigx.common.data.datascope.PigxBaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;


/**
 * @author Administrator
 */
@Mapper
public interface CostAccountPlanMapper extends PigxBaseMapper<CostAccountPlan> {

    IPage<CostAccountPlan> listByQueryDto(Page page, @Param("query") CostAccountPlanQueryDto queryDto);
}
