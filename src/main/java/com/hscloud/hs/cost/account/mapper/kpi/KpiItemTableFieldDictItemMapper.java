package com.hscloud.hs.cost.account.mapper.kpi;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hscloud.hs.cost.account.model.dto.kpi.KpiItemTableFieldDictItemDto;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiItemTableFieldDictItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface KpiItemTableFieldDictItemMapper extends BaseMapper<KpiItemTableFieldDictItem> {
    IPage<KpiItemTableFieldDictItem> getPage(Page<Object> objectPage, @Param("input") KpiItemTableFieldDictItemDto input);
}
