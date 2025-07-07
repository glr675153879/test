package com.hscloud.hs.cost.account.controller;

import com.hscloud.hs.cost.account.model.dto.DistributionTaskGroupQueryDto;
import com.hscloud.hs.cost.account.model.dto.EnableDto;
import com.hscloud.hs.cost.account.model.entity.DistributionTaskGroup;
import com.hscloud.hs.cost.account.service.IDistributionTaskGroupService;
import com.pig4cloud.pigx.common.core.util.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 任务分组 前端控制器
 * </p>
 *
 * @author author
 * @since 2023-11-20
 */
@RestController
@RequestMapping("/distribution/task/group")
@Tag(description = "distribution_task_proup", name = "任务分组")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
@RequiredArgsConstructor
public class DistributionTaskGroupController {

    private final IDistributionTaskGroupService taskGroupService;


    /**
     * 新增任务分组
     */
    @PreAuthorize("@pms.hasPermission('kpi_task_grouping_add')")
    @PostMapping("/add")
    @Operation(summary = "新增")
    public R addTaskGroup(@RequestBody DistributionTaskGroup distributionTaskGroup) {
        taskGroupService.saveTaskGroup(distributionTaskGroup);
        return R.ok();

    }

    /**
     * 启停用任务分组
     */
    @PreAuthorize("@pms.hasPermission('kpi_task_grouping_enable')")
    @PutMapping("/enable")
    @Operation(summary = "启停用")
    public R enableItem(@RequestBody @Validated EnableDto enableDto) {
        return R.ok(taskGroupService.enableTaskGroup(Long.parseLong(enableDto.getId()), enableDto.getStatus()));
    }

    /**
     * 修改任务分组
     */
    @PreAuthorize("@pms.hasPermission('kpi_task_grouping_edit')")
    @PutMapping("/update")
    @Operation(summary = "修改")
    public R updateItem(@RequestBody DistributionTaskGroup distributionTaskGroup) {
        return R.ok(taskGroupService.updateTaskGroup(distributionTaskGroup));
    }

    /**
     * 根据id查询
     * @param id
     * @return
     */
    @GetMapping("/getTaskGroupById/{id}")
    @Operation(summary = "根据id查询任务分组")
    public R getTaskGroupById(@PathVariable Long id) {
        return R.ok(taskGroupService.getById(id));
    }

    /**
     * 分页模糊匹配查询
     */
    @GetMapping("/list")
    @Operation(summary = "分页模糊匹配查询")
    public R listItem(@Validated DistributionTaskGroupQueryDto dto) {
        return R.ok(taskGroupService.listTaskGroup(dto));
    }

}
