package com.hscloud.hs.cost.account.mapper.kpi;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiCalculate;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiSignLeft;
import com.hscloud.hs.cost.account.model.entity.second.UnitTaskCount;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
* 绩效签发 左侧固定 Mapper 接口
*
*/
@Mapper
public interface KpiSignLeftMapper extends BaseMapper<KpiSignLeft> {

    Integer insertBatchSomeColumn(List<KpiSignLeft> list);

    List<KpiCalculate> first(@Param("period") String period);

    List<UnitTaskCount> second(@Param("period") String period);

    @Update("update kpi_sign_left set del_flag = '1' ${ew.customSqlSegment}")
    void updateDelFlag(@Param("ew") QueryWrapper<KpiSignLeft> ew);
}

