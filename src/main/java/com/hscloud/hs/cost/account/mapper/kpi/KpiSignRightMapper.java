package com.hscloud.hs.cost.account.mapper.kpi;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiSignHead;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiSignRight;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
* 绩效签发 右侧不固定 Mapper 接口
*
*/
@Mapper
public interface KpiSignRightMapper extends BaseMapper<KpiSignRight> {
    Integer insertBatchSomeColumn(List<KpiSignRight> list);

    @Update("update kpi_sign_right set del_flag = '1' ${ew.customSqlSegment}")
    void updateDelFlag(@Param("ew") QueryWrapper<KpiSignRight> ew);
}

