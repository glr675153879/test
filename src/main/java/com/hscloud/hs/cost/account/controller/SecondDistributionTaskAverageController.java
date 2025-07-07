package com.hscloud.hs.cost.account.controller;


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.hscloud.hs.cost.account.model.entity.SecondDistributionTaskAverage;
import com.hscloud.hs.cost.account.service.ISecondDistributionTaskAverageService;
import com.pig4cloud.pigx.common.core.util.R;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 二次分配任务平均绩效表 前端控制器
 * </p>
 *
 * @author
 * @since 2023-11-17
 */
@RestController
@RequestMapping("/second/distribution/task/average")
public class SecondDistributionTaskAverageController {


    @Autowired
    private ISecondDistributionTaskAverageService secondDistributionTaskAverageService;


    @PostMapping
    @Operation(summary = "新增平均绩效")
    public R saveAverage(@RequestBody SecondDistributionTaskAverage secondDistributionTaskAverage) {
        return R.ok(secondDistributionTaskAverageService.saveAverage(secondDistributionTaskAverage));
    }


    @DeleteMapping
    @Operation(summary = "删除平均绩效")
    public R deleteAverage(@RequestParam Long id) {
        return R.ok(secondDistributionTaskAverageService.removeById(id));
    }


    @GetMapping
    @Operation(summary = "查询平均绩效")
    public R getAverage(SecondDistributionTaskAverage secondDistributionTaskAverage) {
        return R.ok(secondDistributionTaskAverageService.list(Wrappers.<SecondDistributionTaskAverage>lambdaQuery()
                .eq(secondDistributionTaskAverage.getPlanId() != null, SecondDistributionTaskAverage::getPlanId, secondDistributionTaskAverage.getPlanId())
                .eq(secondDistributionTaskAverage.getIndexId() != null, SecondDistributionTaskAverage::getIndexId, secondDistributionTaskAverage.getIndexId())
                .eq(secondDistributionTaskAverage.getTaskUnitRelateId() != null, SecondDistributionTaskAverage::getTaskUnitRelateId, secondDistributionTaskAverage.getTaskUnitRelateId())
                .eq(secondDistributionTaskAverage.getJobNumber() != null, SecondDistributionTaskAverage::getJobNumber, secondDistributionTaskAverage.getJobNumber())
                .like(StrUtil.isNotBlank(secondDistributionTaskAverage.getName()), SecondDistributionTaskAverage::getName, secondDistributionTaskAverage.getName())));
    }

}
