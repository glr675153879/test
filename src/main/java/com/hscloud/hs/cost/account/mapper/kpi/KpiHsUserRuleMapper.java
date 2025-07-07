package com.hscloud.hs.cost.account.mapper.kpi;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiHsUserRule;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiUserCalculationRule;
import org.apache.ibatis.annotations.Mapper;

/**
* 核算单元 Mapper 接口
*
*/
@Mapper
public interface KpiHsUserRuleMapper extends BaseMapper<KpiHsUserRule> {

}

