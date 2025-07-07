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

import com.alibaba.nacos.common.utils.CollectionUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bestvike.linq.Linq;
import com.hscloud.hs.cost.account.mapper.kpi.KpiDictItemMapper;
import com.hscloud.hs.cost.account.model.dto.kpi.KpiDicSearchDto;
import com.hscloud.hs.cost.account.model.dto.kpi.KpiDictItemList;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiDict;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiDictItem;
import com.hscloud.hs.cost.account.service.kpi.KpiDictItemService;
import com.hscloud.hs.cost.account.service.kpi.KpiDictService;
import com.pig4cloud.pigx.common.core.constant.CacheConstants;
import com.pig4cloud.pigx.common.core.exception.BizException;
import com.pig4cloud.pigx.common.core.util.R;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 字典项
 *
 * @author lengleng
 * @date 2019/03/19
 */
@Service
@AllArgsConstructor
public class KpiDictItemServiceImpl extends ServiceImpl<KpiDictItemMapper, KpiDictItem> implements KpiDictItemService {

    private final KpiDictService dictService;

    @NotNull
    private static KpiDictItemList getKpiDictItemList(KpiDictItem child) {
        KpiDictItemList dictItemChild = new KpiDictItemList();
        dictItemChild.setId(child.getId());
        dictItemChild.setDictType(child.getDictType());
        dictItemChild.setItemCode(child.getItemCode());
        dictItemChild.setItemValue(child.getItemValue());
        dictItemChild.setParentCode(child.getParentCode());
        dictItemChild.setLabel(child.getLabel());
        dictItemChild.setSortOrder(child.getSortOrder());
        dictItemChild.setPersonnelFactoryValue(child.getPersonnelFactorValue());
        dictItemChild.setPerformanceSubsidyValue(child.getPerformanceSubsidyValue());
        return dictItemChild;
    }

    /**
     * 删除字典项
     *
     * @param id 字典项ID
     * @return
     */
    @Override
    @CacheEvict(value = CacheConstants.DICT_DETAILS, allEntries = true)
    public R removeDictItem(Long id) {
        // 根据ID查询字典ID
        KpiDictItem dictItem = this.getById(id);
        KpiDict dict = dictService.getById(dictItem.getDictId());
        //是否存在下级菜单
        List<KpiDictItem> list = this.list(new LambdaQueryWrapper<KpiDictItem>().eq(KpiDictItem::getParentCode, dictItem.getItemCode()));
        if (CollectionUtils.isEmpty(list)) {
            return R.ok(this.removeById(id));
        } else {
            throw new BizException("存在下级字典 无法删除");
        }
    }

    /**
     * 更新字典项
     *
     * @param item 字典项
     * @return
     */
    @Override
    //@CacheEvict(value = CacheConstants.DICT_DETAILS, key = "#item.dictType")
    public R updateDictItem(KpiDictItem item) {
        // 查询字典
        if (item.getId() == null) {
            //新增
            Long l = this.getBaseMapper().selectCount(new LambdaQueryWrapper<KpiDictItem>()
                    .eq(KpiDictItem::getDictType, item.getDictType())
                    .eq(KpiDictItem::getItemCode, item.getItemCode()));
            if (l > 0) {
                return R.failed("字典项已存在");
            } else {
                return R.ok(this.save(item));
            }
        } else {
            //修改
            return R.ok(this.updateById(item));
        }


    }

    public List<KpiDictItemList> getDictItemList(String dictType) {
        List<KpiDictItem> listAll = this.list();
        List<KpiDictItem> list = Linq.of(listAll).where(x -> x.getDictType().equals(dictType))
                .orderBy(KpiDictItem::getSortOrder).toList();
        return getChildrenDic("-1", list);
    }

    @Override
    public IPage<KpiDictItem> dictPage(KpiDicSearchDto input) {
        Page matchPage = new Page<>(input.getCurrent(), input.getSize());
        return this.getBaseMapper().dictItemPage(matchPage, input);
    }

    private List<KpiDictItemList> getChildrenDic(String parentCode, List<KpiDictItem> allDics) {
        List<KpiDictItemList> finalList = new ArrayList<>();
        List<KpiDictItem> dicDto = allDics.stream()
                .filter(x -> x.getParentCode().equals(parentCode))
                .sorted(Comparator.comparing(KpiDictItem::getSortOrder)).collect(Collectors.toList());
        dicDto.forEach(r -> {
            List<KpiDictItemList> childMenu = getChildrenDic2(r.getItemCode(), allDics);
            KpiDictItemList kpiDictItemList = getKpiDictItemList(r);
            kpiDictItemList.setChildren(childMenu);
            finalList.add(kpiDictItemList);
        });
        return finalList;
    }

    private List<KpiDictItemList> getChildrenDic2(String parentCode, List<KpiDictItem> allDics) {
        List<KpiDictItemList> result = new ArrayList<>();
        List<KpiDictItem> dicDto = allDics.stream()
                .filter(x -> x.getParentCode().equals(parentCode))
                .sorted(Comparator.comparing(KpiDictItem::getSortOrder)).collect(Collectors.toList());
        dicDto.forEach(r -> {
            List<KpiDictItemList> childMenu = getChildrenDic2(r.getItemCode(), allDics);
            KpiDictItemList kpiDictItemList = getKpiDictItemList(r);
            kpiDictItemList.setChildren(childMenu);
            result.add(kpiDictItemList);
        });
        return result;
    }

}
