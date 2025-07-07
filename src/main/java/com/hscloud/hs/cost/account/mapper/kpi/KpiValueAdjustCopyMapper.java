package com.hscloud.hs.cost.account.mapper.kpi;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiValueAdjustCopy;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* 配置表 Mapper 接口
*
*/
@Mapper
public interface KpiValueAdjustCopyMapper extends BaseMapper<KpiValueAdjustCopy> {

    void insertBatchSomeColumn(@Param("list") List<KpiValueAdjustCopy> list);

}

