package com.hscloud.hs.cost.account.mapper.kpi;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hscloud.hs.cost.account.model.dto.kpi.KpiItemEquivalentConfigDTO;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiItemEquivalentConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface KpiItemEquivalentConfigMapper extends BaseMapper<KpiItemEquivalentConfig> {
    IPage<KpiItemEquivalentConfig> getPage(Page<Object> page , @Param("input") KpiItemEquivalentConfigDTO input);
}
