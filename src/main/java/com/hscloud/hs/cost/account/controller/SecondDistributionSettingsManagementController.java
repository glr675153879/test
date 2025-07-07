package com.hscloud.hs.cost.account.controller;


import com.hscloud.hs.cost.account.model.dto.EnableDto;
import com.hscloud.hs.cost.account.model.dto.SecondQueryDto;
import com.hscloud.hs.cost.account.model.entity.SecondDistributionSettingsManagement;
import com.hscloud.hs.cost.account.service.ISecondDistributionSettingsManagementService;
import com.pig4cloud.pigx.common.core.util.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 分配设置管理绩效 前端控制器
 * </p>
 *
 * @author 
 * @since 2023-11-15
 */
@RestController
@RequestMapping("/second/settings/management")
@Tag(description = "second_distribution_settings_management", name = "分配设置管理绩效")
@RequiredArgsConstructor
public class SecondDistributionSettingsManagementController {
    private final ISecondDistributionSettingsManagementService secondDistributionSettingsManagementService;

    @PostMapping("/save")
    @Operation(summary = "新增管理绩效")
    public R saveManagement(@RequestBody SecondDistributionSettingsManagement settingsManagement) {

        return R.ok(  secondDistributionSettingsManagementService.saveManagement(settingsManagement));
    }

    @PutMapping("/update")
    @Operation(summary = "修改管理绩效")
    // @PreAuthorize("@pms.hasPermission('[kpi_quota_add,kpi_quota_edit]')")
    public R updateManagement(@RequestBody @Validated SecondDistributionSettingsManagement settingsManagement) {


        secondDistributionSettingsManagementService.updateManagement(settingsManagement);

        return R.ok( );
    }

    @PutMapping("/updateStatus")
    @Operation(summary = "启停用管理绩效")
    // @PreAuthorize("@pms.hasPermission('kpi_quota_enable')")
    public R updateStatusManagement(@RequestBody EnableDto dto) {
        return R.ok( secondDistributionSettingsManagementService.updateStatus(dto));
    }

    @GetMapping("/list")
    @Operation(summary = "查询管理绩效列表")
    public R getManagementList(@Validated SecondQueryDto queryDto) {
        return R.ok(secondDistributionSettingsManagementService.getList(queryDto));
    }

}
