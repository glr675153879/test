/*
 *
 *      Copyright (c) 2018-2025, lengleng All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice,
 *  this list of conditions and the following disclaimer.
 *  Redistributions in binary form must reproduce the above copyright
 *  notice, this list of conditions and the following disclaimer in the
 *  documentation and/or other materials provided with the distribution.
 *  Neither the name of the hscloud.com developer nor the names of its
 *  contributors may be used to endorse or promote products derived from
 *  this software without specific prior written permission.
 *  Author: lengleng (wangiegie@gmail.com)
 *
 */

package com.hscloud.hs.cost.account.controller.kpi;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hscloud.hs.cost.account.model.dto.kpi.KpiDicSearchDto;
import com.hscloud.hs.cost.account.model.dto.kpi.KpiDictItemList;
import com.hscloud.hs.cost.account.model.dto.kpi.KpiGroupDelDto;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiDict;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiDictItem;
import com.hscloud.hs.cost.account.service.kpi.KpiDictItemService;
import com.hscloud.hs.cost.account.service.kpi.KpiDictService;
import com.pig4cloud.pigx.common.core.constant.CacheConstants;
import com.pig4cloud.pigx.common.core.util.R;
import com.pig4cloud.pigx.common.log.annotation.SysLog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * <p>
 * 字典表 前端控制器
 * </p>
 *
 * @author lengleng
 * @since 2019-03-19
 */
@RestController
@AllArgsConstructor
@RequestMapping("/kpi/dict")
@Tag(name = "dict", description = "字典管理模块")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class KpiDictController {

    private final KpiDictService sysDictService;

    private final KpiDictItemService sysDictItemService;

    /**
     * 通过ID查询字典信息
     *
     * @param id ID
     * @return 字典信息
     */
    @GetMapping("/details/{id}")
    @Operation(summary = "查大字典")
    public R<KpiDict> getById(@PathVariable Long id) {
        return R.ok(sysDictService.getById(id));
    }

//    /**
//     * 查询字典信息
//     *
//     * @param query 查询信息
//     * @return 字典信息
//     */
//    @GetMapping("/details")
//    public R getDetails(@ParameterObject KpiDict query) {
//        return R.ok(sysDictService.getOne(Wrappers.query(query), false));
//    }

    /**
     * 分页查询字典信息
     *
     * @param
     * @return 分页对象
     */
    @GetMapping("/page")
    @Operation(summary = "大字典列表")
    public R<IPage<KpiDict>> getDictPage(KpiDicSearchDto input) {
        return R.ok(sysDictService.dictPage(input));
    }

//    /**
//     * 通过字典类型查找字典
//     *
//     * @param type 类型
//     * @return 同类型字典
//     */
//    @GetMapping("/type/{type}")
//    @Cacheable(value = CacheConstants.DICT_DETAILS, key = "#type", unless = "#result.data.isEmpty()")
//    public R<List<KpiDictItem>> getDictByType(@PathVariable String type) {
//        return R.ok(sysDictItemService.list(Wrappers.<KpiDictItem>query().lambda().eq(KpiDictItem::getDictType, type)
//                .orderByDesc(KpiDictItem::getSortOrder)));
//    }


    /**
     * 添加字典
     *
     * @param sysDict 字典信息
     * @return success、false
     */
    @SysLog("添加字典")
    @PostMapping("/addDic")
    @Operation(summary = "添加字典")
    public R save(@Valid @RequestBody KpiDict sysDict) {
        sysDictService.saveDic(sysDict);
        return R.ok(sysDict);
    }

    /**
     * 删除字典，并且清除字典缓存
     *
     * @param ids ID
     * @return R
     */
    @SysLog("删除字典")
    @PostMapping("/del_dic")
    @Operation(summary = "删除字典")
    @CacheEvict(value = CacheConstants.DICT_DETAILS, allEntries = true)
    public R removeById(@RequestBody Long[] ids) {
        return R.ok(sysDictService.removeDictByIds(ids));
    }

    /**
     * 修改字典
     *
     * @param sysDict 字典信息
     * @return success/false
     */
    @PostMapping("/update_dic")
    @SysLog("修改字典")
    @Operation(summary = "修改字典")
    public R updateById(@Valid @RequestBody KpiDict sysDict) {
        return sysDictService.updateDict(sysDict);
    }


    /**
     * 分页查询
     *
     * @param
     * @param
     * @return
     */
    @SysLog("可用于系数及补贴配置")
    @Operation(summary = "可用于系数及补贴配置")
    @GetMapping("/item_page")
    public R<IPage<KpiDictItem>> getSysDictItemPage(KpiDicSearchDto input) {
        return R.ok(sysDictItemService.dictPage(input));
    }

    @SysLog("根据dicType获取字典详情 上下级")
    @Operation(summary = "根据dicType获取字典详情 上下级")
    @GetMapping("/item/listByDicType")
    public R<List<KpiDictItemList>> getSysDictItemPage(String dictType) {
        return R.ok(sysDictItemService.getDictItemList(dictType));
    }

    /**
     * 通过id查询字典项
     *
     * @param id id
     * @return R
     */
    @GetMapping("/item/details/{id}")
    @Operation(summary = "通过id查询字典项")
    public R<KpiDictItem> getDictItemById(@PathVariable("id") Long id) {
        return R.ok(sysDictItemService.getById(id));
    }


    /**
     * 修改字典项
     *
     * @param sysDictItem 字典项
     * @return R
     */
    @SysLog("修改字典项 新增字典项 默认顶级parentCode='-1'")
    @PostMapping("/update_item")
    @CacheEvict(value = CacheConstants.DICT_DETAILS, allEntries = true)
    public R updateById(@RequestBody KpiDictItem sysDictItem) {
        return sysDictItemService.updateDictItem(sysDictItem);
    }

    /**
     * 通过id删除字典项
     *
     * @param dto id
     * @return R
     */
    @SysLog("删除字典项")
    @PostMapping("/del_item")
    @CacheEvict(value = CacheConstants.DICT_DETAILS, allEntries = true)
    public R removeDictItemById(@RequestBody KpiGroupDelDto dto) {
        return sysDictItemService.removeDictItem(dto.getId());
    }


}
