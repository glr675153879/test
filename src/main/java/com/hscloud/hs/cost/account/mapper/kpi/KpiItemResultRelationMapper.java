package com.hscloud.hs.cost.account.mapper.kpi;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiItemResultRelation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* 核算项结果匹配关系 Mapper 接口
*
*/
@Mapper
public interface KpiItemResultRelationMapper extends BaseMapper<KpiItemResultRelation> {

    Integer insertBatchSomeColumn(@Param("list") List<KpiItemResultRelation> list);
}

