package com.hscloud.hs.cost.account.mapper.kpi;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hscloud.hs.cost.account.model.dto.kpi.KpiIndexListDto;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiIndex;
import com.hscloud.hs.cost.account.model.vo.kpi.KpiIndexListVO;
import com.pig4cloud.pigx.admin.api.entity.SysDictItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* 指标表 Mapper 接口
*
*/
@Mapper
public interface KpiIndexMapper extends BaseMapper<KpiIndex> {


    List<KpiIndexListVO> getList( @Param("input")KpiIndexListDto input);


    IPage<KpiIndexListVO> getPate(Page<Object> page, @Param("input") KpiIndexListDto input);


    List<SysDictItem> getSysDict(@Param("list") List<String> objAccountTypes,@Param("dicType") String kpiCalculateGrouping,@Param("tenantId") Long tenantId);
}

