package com.hscloud.hs.cost.account.controller;


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.hscloud.hs.cost.account.model.entity.SecondDistributionTaskManagement;
import com.hscloud.hs.cost.account.service.ISecondDistributionTaskManagementService;
import com.pig4cloud.pigx.common.core.util.R;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

/**
 * <p>
 * 二次分配任务管理绩效表 前端控制器
 * </p>
 * todo 缺权限控制
 *
 * @author
 * @since 2023-11-17
 */
@RestController
@RequestMapping("/second/distribution/task/management")
public class SecondDistributionTaskManagementController {


    @Autowired
    private ISecondDistributionTaskManagementService secondDistributionTaskManagementService;


    @PostMapping
    @Operation(summary = "新增管理绩效")
    public R saveManagement(@RequestBody SecondDistributionTaskManagement secondDistributionTaskManagement) {
        return R.ok(secondDistributionTaskManagementService.saveManagement(secondDistributionTaskManagement));
    }


    @DeleteMapping
    @Operation(summary = "删除管理绩效")
    public R deleteManagement(@RequestParam Long id) {
        return R.ok(secondDistributionTaskManagementService.removeById(id));
    }

    @GetMapping
    @Operation(summary = "查询管理绩效")
    public R getManagement(SecondDistributionTaskManagement secondDistributionTaskManagement) {
        return R.ok(secondDistributionTaskManagementService.list(Wrappers.<SecondDistributionTaskManagement>query()
                .lambda()
                        .eq(Objects.nonNull(secondDistributionTaskManagement.getPlanId()), SecondDistributionTaskManagement::getPlanId, secondDistributionTaskManagement.getPlanId())
                        .eq(Objects.nonNull(secondDistributionTaskManagement.getIndexId()), SecondDistributionTaskManagement::getIndexId, secondDistributionTaskManagement.getIndexId())
                .eq(Objects.nonNull(secondDistributionTaskManagement.getTaskUnitRelateId()),SecondDistributionTaskManagement::getTaskUnitRelateId, secondDistributionTaskManagement.getTaskUnitRelateId())
                .eq(Objects.nonNull(secondDistributionTaskManagement.getJobNumber()), SecondDistributionTaskManagement::getJobNumber, secondDistributionTaskManagement.getJobNumber())
                .eq(Objects.nonNull(secondDistributionTaskManagement.getPositionId()), SecondDistributionTaskManagement::getPositionId, secondDistributionTaskManagement.getPositionId())
                .like(StrUtil.isNotBlank(secondDistributionTaskManagement.getName()), SecondDistributionTaskManagement::getName, secondDistributionTaskManagement.getName())));
    }


}
