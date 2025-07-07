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
package com.hscloud.hs.cost.account.service.kpi;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hscloud.hs.cost.account.model.dto.kpi.DicPageDto;
import com.hscloud.hs.cost.account.model.dto.kpi.DicPageOutDto;
import com.hscloud.hs.cost.account.model.dto.kpi.KpiDicSearchDto;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiDict;
import com.pig4cloud.pigx.common.core.util.R;

import java.util.List;

/**
 * 字典表
 *
 * @author lengleng
 * @date 2019/03/19
 */
public interface KpiDictService extends IService<KpiDict> {

    /**
     * 根据ID 删除字典
     *
     * @param ids ID列表
     * @return
     */
    R removeDictByIds(Long[] ids);

    /**
     * 更新字典
     *
     * @param sysDict 字典
     * @return
     */
    R updateDict(KpiDict sysDict);

    /**
     * 同步缓存 （清空缓存）
     *
     * @return R
     */
    R syncDictCache();


   // List<DicPageOutDto> protectDic(DicPageDto input);


    void saveDic(KpiDict sysDict);


    IPage<KpiDict> dictPage(KpiDicSearchDto input);

}
