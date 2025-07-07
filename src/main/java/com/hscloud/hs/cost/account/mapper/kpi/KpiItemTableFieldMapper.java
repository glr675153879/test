package com.hscloud.hs.cost.account.mapper.kpi;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hscloud.hs.cost.account.model.dto.kpi.KpiItemTableFieldDto;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiItemTableField;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface KpiItemTableFieldMapper extends BaseMapper<KpiItemTableField> {
    List<KpiItemTableField> getFields(@Param("schemaName") String schemaName, @Param("tableName") String tableName);

    IPage<KpiItemTableField> getPage(Page<Object> page, @Param("input") KpiItemTableFieldDto input);
}
