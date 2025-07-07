package com.hscloud.hs.cost.account.mapper.kpi;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiCoefficient;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiCoefficientCopy;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* 配置表 Mapper 接口
*
*/
@Mapper
public interface KpiCoefficientCopyMapper extends BaseMapper<KpiCoefficientCopy> {
    void insertBatchSomeColumn(@Param("list") List<KpiCoefficientCopy> list);
}


