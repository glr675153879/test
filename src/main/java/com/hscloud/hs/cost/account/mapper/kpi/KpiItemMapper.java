package com.hscloud.hs.cost.account.mapper.kpi;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiItem;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiItemCopy;
import com.hscloud.hs.cost.account.model.vo.kpi.KpiItemVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

/**
* 核算项Mapper 接口
*
 * @author Administrator
 */
@Mapper
public interface KpiItemMapper extends BaseMapper<KpiItem> {

    List<LinkedHashMap<String, Object>> executeSql(HashMap<String, Object> param);

    IPage<KpiItemVO> pageData(Page page, @Param("ew") LambdaQueryWrapper<KpiItem> ew,@Param("period") Long period);

    IPage<KpiItemVO> pageDataOld(Page page, @Param("ew") LambdaQueryWrapper<KpiItem> ew);


    @Select("select * from kpi_item ${ew.customSqlSegment}")
    List<KpiItemCopy> getList( @Param("ew") QueryWrapper<KpiItem> eq);

    KpiItem getDeletedItem(@Param("code") String code);
}

