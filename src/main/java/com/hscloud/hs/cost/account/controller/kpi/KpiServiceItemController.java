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

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hscloud.hs.cost.account.model.entity.base.PageRequest;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiServiceItem;
import com.hscloud.hs.cost.account.service.kpi.IKpiServiceItemService;
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
 * 浙江省基本医疗保险医疗服务项目目录 前端控制器
 * </p>
 *
 * @author lengleng
 * @since 2019-03-19
 */
@RestController
@AllArgsConstructor
@RequestMapping("/kpi/item")
@Tag(name = "item", description = "浙江省基本医疗保险医疗服务项目目录")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class KpiServiceItemController {

    private final IKpiServiceItemService kpiServiceItemService;

    @SysLog("浙江省基本医疗保险医疗服务项目目录add")
    @PostMapping("/add")
    @Operation(summary = "浙江省基本医疗保险医疗服务项目目录add")
    public R add(@RequestBody KpiServiceItem kpiServiceItem) {
        return R.ok(kpiServiceItemService.save(kpiServiceItem));
    }

    @SysLog("浙江省基本医疗保险医疗服务项目目录edit")
    @PostMapping("/edit")
    @Operation(summary = "浙江省基本医疗保险医疗服务项目目录edit")
    public R edit(@RequestBody KpiServiceItem kpiServiceItem) {
        return R.ok(kpiServiceItemService.updateById(kpiServiceItem));
    }

    @SysLog("浙江省基本医疗保险医疗服务项目目录删除")
    @PostMapping("/del/{id}")
    @Operation(summary = "*浙江省基本医疗保险医疗服务项目目录删除")
    public R<?> del(@PathVariable Long id) {
        return R.ok(kpiServiceItemService.removeById(id));
    }

    @GetMapping("/list")
    @Operation(summary = "*浙江省基本医疗保险医疗服务项目目录列表")
    public R<List<KpiServiceItem>> list(PageRequest<KpiServiceItem> pr) {
        return R.ok(kpiServiceItemService.list(pr.getWrapper()));
    }

    @GetMapping("/page")
    @Operation(summary = "*浙江省基本医疗保险医疗服务项目目录page")
    public R<IPage<KpiServiceItem>> getPage(PageRequest<KpiServiceItem> pr) {
        String searchItemCodes = StrUtil.toStringOrNull(pr.getQ().get("searchItemCodes"));
        pr.getQ().remove("searchItemCodes");
        LambdaQueryWrapper<KpiServiceItem> lambda = pr.getWrapper().lambda();
        if(StrUtil.isNotBlank(searchItemCodes)){
            List<String> split = StrUtil.split(searchItemCodes, ",");
            lambda.and(innerLambda->{
                for (String s : split) {
                    innerLambda.or().likeRight(KpiServiceItem::getItemCode, s);
                }
            });
        }
        return R.ok(kpiServiceItemService.page(pr.getPage(), lambda));
    }

    @GetMapping("/info/{id}")
    @Operation(summary = "*浙江省基本医疗保险医疗服务项目目录info")
    public R<KpiServiceItem> info(@PathVariable Long id) {
        return R.ok(kpiServiceItemService.getById(id));
    }

}
