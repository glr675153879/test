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
package com.hscloud.hs.cost.account.service.impl.kpi;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hscloud.hs.cost.account.mapper.kpi.KpiDictItemMapper;
import com.hscloud.hs.cost.account.mapper.kpi.KpiDictMapper;
import com.hscloud.hs.cost.account.model.dto.kpi.KpiDicSearchDto;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiDict;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiDictItem;
import com.hscloud.hs.cost.account.service.kpi.KpiDictService;
import com.pig4cloud.pigx.common.core.constant.CacheConstants;
import com.pig4cloud.pigx.common.core.exception.BizException;
import com.pig4cloud.pigx.common.core.util.R;
import com.pig4cloud.pigx.common.security.util.SecurityUtils;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 字典表
 *
 * @author lengleng
 * @date 2019/03/19
 */
@Service
@AllArgsConstructor
public class KpiDictServiceImpl extends ServiceImpl<KpiDictMapper, KpiDict> implements KpiDictService {

    private final KpiDictItemMapper dictItemMapper;

    private final KpiDictMapper sysDictMapper;

    /**
     * 根据ID 删除字典
     *
     * @param ids 字典ID 列表
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = CacheConstants.DICT_DETAILS, allEntries = true)
    public R removeDictByIds(Long[] ids) {

        List<Long> dictIdList = baseMapper.selectBatchIds(CollUtil.toList(ids)).stream()
                .map(KpiDict::getId).collect(Collectors.toList());

        baseMapper.deleteBatchIds(dictIdList);

        dictItemMapper.delete(Wrappers.<KpiDictItem>lambdaQuery().in(KpiDictItem::getDictId, dictIdList));
        return R.ok();
    }

    /**
     * 更新字典
     *
     * @param dict 字典
     * @return
     */
    @Override
    //@CacheEvict(value = CacheConstants.DICT_DETAILS, key = "#dict.dictType")
    public R updateDict(KpiDict dict) {
        KpiDict sysDict = this.getById(dict.getId());
//        // 系统内置
//        if (DictTypeEnum.SYSTEM.getType().equals(sysDict.getSystemFlag())) {
//            return R.failed(MsgUtils.getMessage(ErrorCodes.SYS_DICT_UPDATE_SYSTEM));
//        }
        boolean notEmpty = StringUtils.isNotEmpty(sysDict.getDictType());
        if (!notEmpty) {
            throw new BizException("字典类型不可为空");
        }
        if (sysDict.getDictType().contains("_subsidy")||sysDict.getDictType().contains("_factor")){
            throw new BizException("_subsidy和_factor 为限定词");
        }
        Long num = sysDictMapper.selectCount(new LambdaQueryWrapper<KpiDict>().eq(KpiDict::getDictType, dict.getDictType()).
                eq(KpiDict::getTenantId, SecurityUtils.getUser().getTenantId()).
                ne(KpiDict::getId, dict.getId()));
        if (num < 1) {
            this.updateById(dict);
        } else {
            throw new BizException("字典类型已存在");
        }
        //更新明细表dictType字段
        dictItemMapper.update(Wrappers.<KpiDictItem>lambdaUpdate().eq(KpiDictItem::getDictId, dict.getId()).set(KpiDictItem::getDictType, dict.getDictType()));
        return R.ok(dict);
    }

    /**
     * 同步缓存 （清空缓存）
     *
     * @return R
     */
    @Override
    @CacheEvict(value = CacheConstants.DICT_DETAILS, allEntries = true)
    public R syncDictCache() {
        return R.ok();
    }

//    public List<DicPageOutDto> protectDic(DicPageDto input) {
//        //Page<Object> matchPage = new Page<>(input.getCurrent(), input.getSize());
//        Long tenantId = SecurityUtils.getUser().getTenantId();
//        return dictItemMapper.findDictype(input, tenantId);
//    }

    @Override
    public void saveDic(KpiDict sysDict) {
        boolean notEmpty = StringUtils.isNotEmpty(sysDict.getDictType());
        if (!notEmpty) {
            throw new BizException("字典类型不可为空");
        }
        if (sysDict.getDictType().contains("_subsidy")||sysDict.getDictType().contains("_factor")){
            throw new BizException("_subsidy和_factor 为限定词");
        }
        Long num = sysDictMapper.selectCount(new LambdaQueryWrapper<KpiDict>().eq(KpiDict::getDictType, sysDict.getDictType()).
                eq(KpiDict::getTenantId, SecurityUtils.getUser().getTenantId()));
        if (num < 1) {
            sysDictMapper.insert(sysDict);
        } else {
            throw new BizException("字典类型已存在");
        }
    }

    public IPage<KpiDict> dictPage(KpiDicSearchDto input) {
        Page matchPage = new Page<>(input.getCurrent(), input.getSize());
        return dictItemMapper.dictPage(matchPage, input);
    }


}
