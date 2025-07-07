package com.hscloud.hs.cost.account.mapper.kpi;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hscloud.hs.cost.account.model.dto.kpi.KpiAccountRelationQueryDTO;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiAccountUnit;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiUserCalculationRule;
import com.hscloud.hs.cost.account.model.vo.kpi.KpiAccountRelationVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
* 核算单元 Mapper 接口
*
*/
@Mapper
public interface KpiUserCalculationRuleMapper extends BaseMapper<KpiUserCalculationRule> {

}

