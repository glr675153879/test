package com.hscloud.hs.cost.account.controller;


import com.hscloud.hs.cost.account.model.dto.EnableDto;
import com.hscloud.hs.cost.account.model.dto.SecondQueryDto;
import com.hscloud.hs.cost.account.model.entity.SecondDistributionSettingsRank;
import com.hscloud.hs.cost.account.service.ISecondDistributionSettingsRankService;
import com.pig4cloud.pigx.common.core.util.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 分配设置职称绩效 前端控制器
 * </p>
 *
 * @author 
 * @since 2023-11-20
 */
@RestController
@RequestMapping("/second/settings/rank")
@Tag(description = "second_distribution_settings_rank", name = "分配设置职称绩效")
@RequiredArgsConstructor
public class SecondDistributionSettingsRankController {

    private final ISecondDistributionSettingsRankService rankService;

    @PostMapping("/save")
    @Operation(summary = "新增职称绩效")
    public R saveRank(@RequestBody @Validated SecondDistributionSettingsRank settingsRank) {

        return R.ok( rankService.saveRank(settingsRank) );
    }

    @PutMapping("/update")
    @Operation(summary = "修改职称绩效")
    public R updateRank(@RequestBody @Validated SecondDistributionSettingsRank settingsRank) {

        return R.ok(rankService.updateById(settingsRank));
    }

    @PutMapping("/updateStatus")
    @Operation(summary = "启停用职称绩效")
    public R updateStatusRank(@RequestBody EnableDto dto) {
        return R.ok( rankService.updateStatus(dto));
    }

    @GetMapping("/list")
    @Operation(summary = "查询职称列表")
    public R getRankList(SecondQueryDto queryDto) {
        return R.ok(rankService.getList(queryDto));
    }

}
