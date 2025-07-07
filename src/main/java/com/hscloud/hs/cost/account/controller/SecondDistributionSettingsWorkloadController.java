package com.hscloud.hs.cost.account.controller;


import com.hscloud.hs.cost.account.model.dto.EnableDto;
import com.hscloud.hs.cost.account.model.dto.SecondQueryDto;
import com.hscloud.hs.cost.account.model.entity.SecondDistributionSettingsWorkload;
import com.hscloud.hs.cost.account.service.ISecondDistributionSettingsWorkloadService;
import com.pig4cloud.pigx.common.core.util.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 分配设置工作量绩效 前端控制器
 * </p>
 *
 * @author 
 * @since 2023-11-16
 */
@RestController
@RequestMapping("/second/settings/workload")
@Tag(description = "second_distribution_settings_workload", name = "分配设置工作量绩效")
@RequiredArgsConstructor
public class SecondDistributionSettingsWorkloadController {


    private final ISecondDistributionSettingsWorkloadService settingsWorkloadService;

    @PostMapping("/save")
    @Operation(summary = "新增工作量绩效 ")
    public R saveWorkload(@RequestBody @Validated SecondDistributionSettingsWorkload settingsWorkload) {

        return R.ok(settingsWorkloadService.saveWorkload(settingsWorkload) );
    }

    @PutMapping("/update")
    @Operation(summary = "修改工作量绩效 ")
    public R updateWorkload(@RequestBody @Validated SecondDistributionSettingsWorkload settingsWorkload) {

        return R.ok( settingsWorkloadService.updateById(settingsWorkload));
    }

    @PutMapping("/updateStatus")
    @Operation(summary = "启停用工作量绩效 ")
    public R updateStatusWorkload(@RequestBody EnableDto dto) {
        return R.ok( settingsWorkloadService.updateStatus(dto));
    }


    @GetMapping("/list")
    @Operation(summary = "查询单项绩效列表")
    public R getWorkloadList(SecondQueryDto queryDto) {
        return R.ok(settingsWorkloadService.getList(queryDto));
    }

}
