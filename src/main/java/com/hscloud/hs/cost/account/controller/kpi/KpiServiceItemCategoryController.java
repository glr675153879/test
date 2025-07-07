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
import com.hscloud.hs.cost.account.model.entity.base.PageRequest;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiServiceItemCategory;
import com.hscloud.hs.cost.account.service.kpi.IKpiServiceItemCategoryService;
import com.pig4cloud.pigx.common.core.util.R;
import com.pig4cloud.pigx.common.log.annotation.SysLog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 医疗服务目录 前端控制器
 * </p>
 *
 * @author lengleng
 * @since 2019-03-19
 */
@RestController
@AllArgsConstructor
@RequestMapping("/kpi/itemCategory")
@Tag(name = "itemCategory", description = "医疗服务目录")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class KpiServiceItemCategoryController {

    private final IKpiServiceItemCategoryService kpiServiceItemCategoryService;

    @SysLog("医疗服务目录add")
    @PostMapping("/add")
    @Operation(summary = "医疗服务目录add")
    public R<Boolean> add(@RequestBody KpiServiceItemCategory kpiServiceItemCategory) {
        return R.ok(kpiServiceItemCategoryService.checkAndAdd(kpiServiceItemCategory));
    }

    @SysLog("医疗服务目录edit")
    @PostMapping("/edit")
    @Operation(summary = "医疗服务目录edit")
    public R<Boolean> edit(@RequestBody KpiServiceItemCategory kpiServiceItemCategory) {
        return R.ok(kpiServiceItemCategoryService.checkAndEdit(kpiServiceItemCategory));
    }

    @SysLog("医疗服务目录删除")
    @PostMapping("/del/{id}")
    @Operation(summary = "*医疗服务目录删除")
    public R<?> del(@PathVariable Long id) {
        kpiServiceItemCategoryService.checkAndDelete(id);
        return R.ok();
    }

    @GetMapping("/tree")
    @Operation(summary = "*医疗服务目录树")
    public R<List<KpiServiceItemCategory>> tree() {
        return R.ok(kpiServiceItemCategoryService.tree());
    }


    @GetMapping("/cutTree")
    @Operation(summary = "*医疗服务目录树（仅保留所需传入子节点及关联父节点）")
    public R<List<KpiServiceItemCategory>> cutTree(@RequestParam("selectedItemCodes") String selectedItemCodes) {
        return R.ok(kpiServiceItemCategoryService.cutTree(selectedItemCodes));
    }

    @GetMapping("/list")
    @Operation(summary = "*医疗服务目录列表")
    public R<List<KpiServiceItemCategory>> list(PageRequest<KpiServiceItemCategory> pr) {
        return R.ok(kpiServiceItemCategoryService.list(pr.getWrapper()));
    }

    @GetMapping("/page")
    @Operation(summary = "*医疗服务目录page")
    public R<IPage<KpiServiceItemCategory>> getPage(PageRequest<KpiServiceItemCategory> pr) {
        return R.ok(kpiServiceItemCategoryService.page(pr.getPage(), pr.getWrapper()));
    }

    @GetMapping("/info/{id}")
    @Operation(summary = "*医疗服务目录info")
    public R<KpiServiceItemCategory> info(@PathVariable Long id) {
        return R.ok(kpiServiceItemCategoryService.getById(id));
    }

}
