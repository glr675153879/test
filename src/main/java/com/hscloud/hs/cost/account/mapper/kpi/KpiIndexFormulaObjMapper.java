package com.hscloud.hs.cost.account.mapper.kpi;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiIndexFormula;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiIndexFormulaObj;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
* 指标公式对象表 Mapper 接口
*
*/
@Mapper
public interface KpiIndexFormulaObjMapper extends BaseMapper<KpiIndexFormulaObj> {


    Integer insertBatchSomeColumn(List<KpiIndexFormulaObj> list);
}

