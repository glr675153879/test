package com.hscloud.hs.cost.account.controller;


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.hscloud.hs.cost.account.model.entity.SecondDistributionTaskSingle;
import com.hscloud.hs.cost.account.service.ISecondDistributionTaskSingleService;
import com.pig4cloud.pigx.common.core.util.R;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

/**
 * <p>
 * 二次分配任务单项绩效表 前端控制器
 * </p>
 *
 * @author
 * @since 2023-11-17
 */
@RestController
@RequestMapping("/second/distribution/task/single")
public class SecondDistributionTaskSingleController {


    @Autowired
    private ISecondDistributionTaskSingleService secondDistributionTaskSingleService;


    @PostMapping
    @Operation(summary = "新增单项绩效")
    public R saveSingle(@RequestBody SecondDistributionTaskSingle secondDistributionTaskSingle) {
        return R.ok(secondDistributionTaskSingleService.saveSingle(secondDistributionTaskSingle));
    }


    @DeleteMapping
    @Operation(summary = "删除单项绩效")
    public R deleteSingle(@RequestParam Long id) {
        return R.ok(secondDistributionTaskSingleService.removeById(id));
    }


    @GetMapping
    @Operation(summary = "查询单项绩效")
    public R getSingle(SecondDistributionTaskSingle secondDistributionTaskSingle) {
        return R.ok(secondDistributionTaskSingleService.list(Wrappers.<SecondDistributionTaskSingle>query()
                .lambda()
                .eq(Objects.nonNull(secondDistributionTaskSingle.getPlanId()), SecondDistributionTaskSingle::getPlanId, secondDistributionTaskSingle.getPlanId())
                .eq(Objects.nonNull(secondDistributionTaskSingle.getIndexId()), SecondDistributionTaskSingle::getIndexId, secondDistributionTaskSingle.getIndexId())
                .eq(Objects.nonNull(secondDistributionTaskSingle.getTaskUnitRelateId()), SecondDistributionTaskSingle::getTaskUnitRelateId, secondDistributionTaskSingle.getTaskUnitRelateId())
                .eq(Objects.nonNull(secondDistributionTaskSingle.getJobNumber()), SecondDistributionTaskSingle::getJobNumber, secondDistributionTaskSingle.getJobNumber())
                .eq(Objects.nonNull(secondDistributionTaskSingle.getSingleId()), SecondDistributionTaskSingle::getSingleId, secondDistributionTaskSingle.getSingleId())
                .like(StrUtil.isNotBlank(secondDistributionTaskSingle.getName()), SecondDistributionTaskSingle::getName, secondDistributionTaskSingle.getName())));
    }


}
