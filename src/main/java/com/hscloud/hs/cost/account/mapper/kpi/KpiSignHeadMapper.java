package com.hscloud.hs.cost.account.mapper.kpi;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiSignHead;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiSignHeadCopy;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiSignLeft;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
* entity名称未取到 Mapper 接口
*
*/
@Mapper
public interface KpiSignHeadMapper extends BaseMapper<KpiSignHead> {
    Integer insertBatchSomeColumn(List<KpiSignHead> list);

    @Update("update kpi_sign_head set del_flag = '1' ${ew.customSqlSegment}")
    void updateDelFlag(@Param("ew") QueryWrapper<KpiSignHead> ew);

    @Select("select * from kpi_sign_head ${ew.customSqlSegment}")
    List<KpiSignHeadCopy> getList(@Param("ew") QueryWrapper<KpiSignHead> ew);

    int getCopyById(@Param("ids") List<String> ids);
}

