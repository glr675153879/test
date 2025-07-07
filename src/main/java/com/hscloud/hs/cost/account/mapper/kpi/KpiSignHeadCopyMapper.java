package com.hscloud.hs.cost.account.mapper.kpi;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiSignHead;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiSignHeadCopy;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
* entity名称未取到 Mapper 接口
*
*/
@Mapper
public interface KpiSignHeadCopyMapper extends BaseMapper<KpiSignHeadCopy> {
    Integer insertBatchSomeColumn(List<KpiSignHeadCopy> list);

    @Select("select * from kpi_sign_head_copy ${ew.customSqlSegment}")
    List<KpiSignHead> getList(@Param("ew") QueryWrapper<KpiSignHeadCopy> ew);
}

