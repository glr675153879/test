package com.hscloud.hs.cost.account.mapper.kpi;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiIndexFormulaCopy;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* 指标公式表备份 Mapper 接口
*
*/
@Mapper
public interface KpiIndexFormulaCopyMapper extends BaseMapper<KpiIndexFormulaCopy> {
    void insertBatchSomeColumn(@Param("list") List<KpiIndexFormulaCopy> list);
}

