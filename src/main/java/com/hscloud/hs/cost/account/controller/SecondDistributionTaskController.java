package com.hscloud.hs.cost.account.controller;


import com.hscloud.hs.cost.account.model.dto.SecondDistributionManagementQueryDto;
import com.hscloud.hs.cost.account.model.dto.SecondDistributionTaskDto;
import com.hscloud.hs.cost.account.model.dto.SecondTaskDistributionDto;
import com.hscloud.hs.cost.account.model.dto.SecondTaskQueryDto;
import com.hscloud.hs.cost.account.service.ISecondDistributionTaskService;
import com.pig4cloud.pigx.common.core.util.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 * 二次分配任务表 前端控制器
 * </p>
 *
 * @author
 * @since 2023-11-17
 */
@RestController
@RequestMapping("/second/distribution/task")
@Tag(description = "second_distribution_task", name = "分配管理任务")
@RequiredArgsConstructor
public class SecondDistributionTaskController {

    private final ISecondDistributionTaskService taskService;

    @PostMapping("/save")
    @Operation(summary = "保存任务")
    public R saveTask(@RequestBody @Validated SecondDistributionTaskDto dto) {
        //TODO：实现保存逻辑
        return R.ok();
    }


    /**
     * 分配管理列表
     */
    @GetMapping("/list")
    @Operation(summary = "分配管理列表")
    public R getApproveLost(SecondDistributionManagementQueryDto queryDto) {
        return R.ok(taskService.getList(queryDto));
    }


    @GetMapping("/allocation/preview")
    @Operation(summary = "查询任务预览列表")
    public R getTaskAllocationPreview(SecondTaskQueryDto queryDto) {
        return R.ok(taskService.getTaskAllocationPreview(queryDto));
    }

    @PostMapping("/allocation/detail")
    @Operation(summary = "查询任务分配详情")
    public R getTaskAllocationDetail(@RequestBody @Validated SecondTaskDistributionDto secondTaskDistributionDto) {
        return R.ok(taskService.getTaskAllocationDetail(secondTaskDistributionDto));
    }


    @PostMapping("/export")
    @Operation(summary = "导出任务分配数据")
    public void export(HttpServletResponse response, @RequestBody @Validated SecondTaskDistributionDto secondTaskDistributionDto) {
        taskService.export(response, secondTaskDistributionDto);
    }


}
