package com.hscloud.hs.cost.account.mapper.kpi;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiCoefficient;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

/**
* 配置表 Mapper 接口
*
*/
@Mapper
public interface KpiCoefficientMapper extends BaseMapper<KpiCoefficient> {


}

