package com.hscloud.hs.cost.account.mapper.kpi;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hscloud.hs.cost.account.model.dto.kpi.KpiItemTableFieldDictThirdDto;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiItemTableFieldDictThird;
import com.hscloud.hs.cost.account.model.vo.kpi.KpiItemTableFieldDictThirdVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface KpiItemTableFieldDictThirdMapper extends BaseMapper<KpiItemTableFieldDictThird> {
    IPage<KpiItemTableFieldDictThirdVO> getPage(Page<Object> objectPage, @Param("input") KpiItemTableFieldDictThirdDto input);
}
