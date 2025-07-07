package com.hscloud.hs.cost.account.mapper.kpi;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hscloud.hs.cost.account.model.dto.kpi.KpiItemTableFieldDictDto;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiItemTableFieldDict;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface KpiItemTableFieldDictMapper extends BaseMapper<KpiItemTableFieldDict> {
    IPage<KpiItemTableFieldDict> getPage(Page<Object> objectPage, @Param("input") KpiItemTableFieldDictDto input);
}
