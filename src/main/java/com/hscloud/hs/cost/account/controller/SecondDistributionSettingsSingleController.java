package com.hscloud.hs.cost.account.controller;


import com.hscloud.hs.cost.account.model.dto.EnableDto;
import com.hscloud.hs.cost.account.model.dto.SecondQueryDto;
import com.hscloud.hs.cost.account.model.entity.SecondDistributionSettingsSingle;
import com.hscloud.hs.cost.account.service.ISecondDistributionSettingsSingleService;
import com.pig4cloud.pigx.common.core.util.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 分配设置单项绩效 前端控制器
 * </p>
 *
 * @author 
 * @since 2023-11-16
 */
@RestController
@RequestMapping("/second/settings/single")
@Tag(description = "second_distribution_settings_singer", name = "分配设置单项绩效")
@RequiredArgsConstructor
public class SecondDistributionSettingsSingleController {
    private final ISecondDistributionSettingsSingleService secondDistributionSettingsSingleService;

    @PostMapping("/save")
    @Operation(summary = "新增单项绩效")
    public R saveSingle(@RequestBody @Validated SecondDistributionSettingsSingle settingsSingle) {

        return R.ok(secondDistributionSettingsSingleService.saveSingle(settingsSingle) );
    }

    @PutMapping("/update")
    @Operation(summary = "修改单项绩效")
    public R updateSingle(@RequestBody @Validated SecondDistributionSettingsSingle settingsSingle) {

        return R.ok(secondDistributionSettingsSingleService.updateById(settingsSingle));
    }

    @PutMapping("/updateStatus")
    @Operation(summary = "启停用单项绩效")
    public R updateStatusSingle(@RequestBody EnableDto dto) {
        return R.ok( secondDistributionSettingsSingleService.updateStatus(dto));
    }

    @GetMapping("/list")
    @Operation(summary = "查询单项绩效列表")
    public R getSingleList(SecondQueryDto queryDto) {
        return R.ok(secondDistributionSettingsSingleService.getList(queryDto));
    }

}
