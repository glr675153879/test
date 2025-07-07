package com.hscloud.hs.cost.account.controller;


import com.hscloud.hs.cost.account.model.dto.EnableDto;
import com.hscloud.hs.cost.account.model.dto.SecondQueryDto;
import com.hscloud.hs.cost.account.model.entity.SecondDistributionSettingsDegree;
import com.hscloud.hs.cost.account.service.ISecondDistributionSettingsDegreeService;
import com.pig4cloud.pigx.common.core.util.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 分配设置学位系数 前端控制器
 * </p>
 *
 * @author 
 * @since 2023-11-20
 */
@RestController
@RequestMapping("/second/settings/degree")
@Tag(description = "second_distribution_settings_degree", name = "分配设置学位系数")
@RequiredArgsConstructor
public class SecondDistributionSettingsDegreeController {

    private final ISecondDistributionSettingsDegreeService degreeService;

    @PostMapping("/save")
    @Operation(summary = "新增学位系数")
    public R saveDegree(@RequestBody @Validated SecondDistributionSettingsDegree settingsDegree) {

        return R.ok( degreeService.saveRank(settingsDegree));
    }

    @PutMapping("/update")
    @Operation(summary = "修改学位系数")
    public R updateDegree(@RequestBody @Validated SecondDistributionSettingsDegree settingsDegree) {
        return R.ok( degreeService.updateById(settingsDegree));
    }

    @PutMapping("/updateStatus")
    @Operation(summary = "启停用学位系数")
    public R updateStatusDegree(@RequestBody EnableDto dto) {
        return R.ok( degreeService.updateStatus(dto));
    }

    @GetMapping("/list")
    @Operation(summary = "查询学位系数列表")
    public R getDegreeList(SecondQueryDto queryDto) {
        return R.ok(degreeService.getList(queryDto));
    }
}
