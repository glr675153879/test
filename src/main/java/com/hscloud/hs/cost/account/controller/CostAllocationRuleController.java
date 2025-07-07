package com.hscloud.hs.cost.account.controller;


import com.hscloud.hs.cost.account.model.dto.*;
import com.hscloud.hs.cost.account.service.ICostAllocationRuleService;
import com.pig4cloud.pigx.common.core.util.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 分摊规则表 前端控制器
 * </p>
 *
 * @author
 * @since 2023-09-11
 */
@RestController
@RequestMapping("/allocationRule")
@Tag(name = "分摊规则", description = "allocationRule")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class CostAllocationRuleController {

    @Autowired
    private ICostAllocationRuleService costAllocationRuleService;

    @PreAuthorize("@pms.hasPermission('kpi_rule_rule_edit','kpi_rule_rule_add')")
    @PostMapping("/saveOrUpdate")
    @Operation(summary = "新增或修改分摊规则")
    public R saveOrUpdateAllocationRule(@RequestBody @Validated CostAllocationRuleDto dto) {
        costAllocationRuleService.saveOrUpdateAccountRule(dto);
        return R.ok();
    }

    @PreAuthorize("@pms.hasPermission('kpi_rule_rule_del')")
    @DeleteMapping("/delete/{id}")
    @Operation(summary = "删除分摊规则")
    public R deleteAllocationRule(@PathVariable Long id) {
        return R.ok(costAllocationRuleService.removeById(id));
    }

    @GetMapping("/getAllocationRuleById/{id}")
    @Operation(summary = "根据id询分摊规则")
    public R getAllocationRuleById(@PathVariable Long id) {
        return R.ok(costAllocationRuleService.getAllocationRuleById(id));
    }


    @PreAuthorize("@pms.hasPermission('kpi_rule_rule_enable')")
    @PutMapping("/updateStatus")
    @Operation(summary = "启停用分摊规则")
    public R updateStatusAllocationRule(@RequestBody CostAllocationRuleStatusDto dto) {
        return R.ok(costAllocationRuleService.updateStatusAllocationRule(dto));
    }

    @GetMapping("/list")
    @Operation(summary = "查询分摊规则")
    public R getAllocationRulePage(CostAllocationRuleQueryDto queryDto) {
        return R.ok(costAllocationRuleService.getAllocationRulePage(queryDto));
    }


    @PostMapping("/verification")
    @Operation(summary = "分摊规则校验")
    public R verificationAllocationRule(@RequestBody CostAllocationRuleVerificationDto dto) {
        return R.ok(costAllocationRuleService.verificationAllocationRule(dto));
    }
}
