package com.hscloud.hs.cost.account.mapper.kpi;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiIndexFormulaObj;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiIndexFormulaObjCopy;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
* 指标公式对象表 Mapper 接口
*
*/
@Mapper
public interface KpiIndexFormulaObjCopyMapper extends BaseMapper<KpiIndexFormulaObjCopy> {


    Integer insertBatchSomeColumn(List<KpiIndexFormulaObjCopy> list);
}

