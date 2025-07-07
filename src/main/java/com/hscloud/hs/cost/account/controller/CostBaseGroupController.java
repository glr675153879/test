package com.hscloud.hs.cost.account.controller;


import com.hscloud.hs.cost.account.model.dto.CostCommentGroupDto;
import com.hscloud.hs.cost.account.service.ICostBaseGroupService;
import com.pig4cloud.pigx.common.core.util.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 核算指标分组 前端控制器
 * </p>
 *
 * @author
 * @since 2023-09-04
 */
@RestController
@RequestMapping("/baseGroup")
@Tag(name = "通用分组", description = "group")
public class CostBaseGroupController {

    @Autowired
    private ICostBaseGroupService costBaseGroupService;

    @PreAuthorize("@pms.hasPermission('kpi_accounting_item_group_edit','kpi_quota_group_edit','kpi_case_group_edit','kpi_rule_rate_group_edit')")
    @PostMapping("/saveOrUpdate")
    @Operation(summary = "新增分组")
    public R saveOrUpdateBaseGroup(@RequestBody @Validated CostCommentGroupDto dto) {
        return R.ok(costBaseGroupService.saveOrUpdateBaseGroup(dto));
    }

    @DeleteMapping("/delete/{id}")
    @Operation(summary = "删除分组")
    public R deleteBaseGroup(@PathVariable Long id) {
        return R.ok(costBaseGroupService.deleteGroup(id));
    }

    @GetMapping("/listAccountItemGroup")
    @Operation(summary = "查询核算项分组列表")
    public R listAccountItemGroup(String status, String typeGroup) {
        return R.ok(costBaseGroupService.listAccountItemGroup(status, typeGroup));
    }

    @GetMapping("/listAccountIndexGroup")
    @Operation(summary = "根据核算指标查询分组列表")
    public R listAccountIndexGroup(String status) {
        return R.ok(costBaseGroupService.listAccountIndexGroup(status));
    }

    @GetMapping("/listBaseGroup")
    @Operation(summary = "获取通用分组")
    public R listBaseGroup(@RequestParam("typeGroup") String typeGroup, String isSystem) {
        return R.ok(costBaseGroupService.listBaseGroup(typeGroup, isSystem));
    }

    @GetMapping("/listGroupTree")
    @Operation(summary = "获取树形分组列表")
    public R listGroupTree(@RequestParam("typeGroup") String typeGroup, String status) {
        return R.ok(costBaseGroupService.listGroupTree(typeGroup, status));
    }
}
