package com.hscloud.hs.cost.account.mapper.kpi;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiAccountPlanChildCopy;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiAccountUnitCopy;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiMemberCopy;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
* 核算单元名称备份 Mapper 接口
*
*/
@Mapper
public interface KpiAccountUnitCopyMapper extends BaseMapper<KpiAccountUnitCopy> {
    void insertBatchSomeColumn(@Param("list") List<KpiAccountUnitCopy> list);

    @Select("select * from kpi_account_unit ${ew.customSqlSegment}")
    List<KpiAccountUnitCopy> getList(@Param("ew") QueryWrapper<KpiAccountUnitCopy> ew);

}

