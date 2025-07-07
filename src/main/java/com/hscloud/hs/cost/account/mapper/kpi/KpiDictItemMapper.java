/*
 *    Copyright (c) 2018-2025, lengleng All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * Neither the name of the hscloud.com developer nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 * Author: lengleng (wangiegie@gmail.com)
 */
package com.hscloud.hs.cost.account.mapper.kpi;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hscloud.hs.cost.account.model.dto.kpi.KpiDicSearchDto;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiDict;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiDictItem;
import com.pig4cloud.pigx.admin.api.dto.DicPageDto;
import com.pig4cloud.pigx.admin.api.dto.DicPageOutDto;
import com.pig4cloud.pigx.admin.api.entity.SysDictItem;
import com.pig4cloud.pigx.common.data.datascope.PigxBaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 字典项
 *
 * @author lengleng
 * @date 2019/03/19
 */
@Mapper
public interface KpiDictItemMapper extends PigxBaseMapper<KpiDictItem> {

    List<DicPageOutDto> findDictype(@Param("input") DicPageDto input
            , @Param("tenantId") Long tenantId);

    SysDictItem selectByIdIgnoreDelFlag(@Param("id") Long id);


    IPage<KpiDict> dictPage(Page page, @Param("input") KpiDicSearchDto input);


    IPage<KpiDictItem> dictItemPage(Page page, @Param("input") KpiDicSearchDto input);
}
