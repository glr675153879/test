package com.hscloud.hs.cost.account.mapper.kpi;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiItemEquivalentTask;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface KpiItemEquivalentTaskMapper extends BaseMapper<KpiItemEquivalentTask> {
    List<String> statusCount(Long period);
}
