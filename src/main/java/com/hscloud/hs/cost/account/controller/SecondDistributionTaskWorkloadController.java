package com.hscloud.hs.cost.account.controller;


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.hscloud.hs.cost.account.model.entity.SecondDistributionTaskWorkload;
import com.hscloud.hs.cost.account.service.ISecondDistributionTaskWorkloadService;
import com.pig4cloud.pigx.common.core.util.R;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

/**
 * <p>
 * 二次分配任务工作量绩效表 前端控制器
 * </p>
 *
 * @author
 * @since 2023-11-17
 */
@RestController
@RequestMapping("/second/distribution/task/workload")
public class SecondDistributionTaskWorkloadController {


    @Autowired
    private ISecondDistributionTaskWorkloadService secondDistributionTaskWorkloadService;


    @PostMapping
    @Operation(summary = "新增工作量绩效")
    public R saveWorkload(@RequestBody SecondDistributionTaskWorkload secondDistributionTaskWorkload) {
        return R.ok(secondDistributionTaskWorkloadService.saveWorkload(secondDistributionTaskWorkload));
    }

    @DeleteMapping
    @Operation(summary = "删除工作量绩效")
    public R deleteWorkload(@RequestParam Long id) {
        return R.ok(secondDistributionTaskWorkloadService.removeById(id));
    }

    @GetMapping
    @Operation(summary = "查询工作量绩效")
    public R getWorkload(SecondDistributionTaskWorkload secondDistributionTaskWorkload) {
        return R.ok(secondDistributionTaskWorkloadService.list(Wrappers.<SecondDistributionTaskWorkload>query()
                .lambda()
                .eq(Objects.nonNull(secondDistributionTaskWorkload.getPlanId()), SecondDistributionTaskWorkload::getPlanId, secondDistributionTaskWorkload.getPlanId())
                .eq(Objects.nonNull(secondDistributionTaskWorkload.getIndexId()), SecondDistributionTaskWorkload::getIndexId, secondDistributionTaskWorkload.getIndexId())
                .eq(Objects.nonNull(secondDistributionTaskWorkload.getTaskUnitRelateId()), SecondDistributionTaskWorkload::getTaskUnitRelateId, secondDistributionTaskWorkload.getTaskUnitRelateId())
                .eq(Objects.nonNull(secondDistributionTaskWorkload.getJobNumber()), SecondDistributionTaskWorkload::getJobNumber, secondDistributionTaskWorkload.getJobNumber())
                .like(StrUtil.isNotBlank(secondDistributionTaskWorkload.getName()), SecondDistributionTaskWorkload::getName, secondDistributionTaskWorkload.getName())));
    }


}
