package com.hscloud.hs.cost.account.controller;

import com.hscloud.hs.cost.account.model.dto.CostAccountPlanConfigFormulaDto;
import com.hscloud.hs.cost.account.model.dto.CostAccountPlanQueryDto;
import com.hscloud.hs.cost.account.model.dto.CostAccountPlanStatusDto;
import com.hscloud.hs.cost.account.model.entity.CostAccountPlan;
import com.hscloud.hs.cost.account.service.CostAccountPlanConfigService;
import com.hscloud.hs.cost.account.service.CostAccountPlanService;
import com.pig4cloud.pigx.common.core.util.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/account/plan")
@Tag(name = "plan", description = "核算方案")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
@RequiredArgsConstructor
public class CostAccountPlanController {

    @Autowired
    private CostAccountPlanService costAccountPlanService;

    @Autowired
    private CostAccountPlanConfigService costAccountPlanConfigService;

    @PostMapping("/add")
    @PreAuthorize("@pms.hasPermission('kpi_case_add')")
    public R addPlan(@RequestBody CostAccountPlan costAccountPlan) {
        return R.ok(costAccountPlanService.save(costAccountPlan));
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("@pms.hasPermission('kpi_case_del')")
    public R deletePlan(@PathVariable Long id) {
        return R.ok(costAccountPlanService.removeById(id));
    }

    @PutMapping("/update")
    @PreAuthorize("@pms.hasPermission('kpi_case_edit')")
    public R updatePlan(@RequestBody CostAccountPlan costAccountPlan) {
        return R.ok(costAccountPlanService.updateById(costAccountPlan));
    }

    //列表详情
    @GetMapping("/list")
    public R listPlan(CostAccountPlanQueryDto costAccountPlanQueryDto) {
        return R.ok(costAccountPlanService.listPlan(costAccountPlanQueryDto));
    }

    @PutMapping("/switch")
    @PreAuthorize("@pms.hasPermission('kpi_case_enable')")
    @Operation(summary = "启用/停用")
    public R switchStatus(@RequestBody CostAccountPlanStatusDto costAccountPlanStatusDto) {
        costAccountPlanService.switchStatus(costAccountPlanStatusDto);
        return R.ok();
    }

    @PutMapping("/check")
    @Operation(summary = "确认配置")
    public R check(@RequestBody CostAccountPlanStatusDto costAccountPlanStatusDto) {
        costAccountPlanService.check(costAccountPlanStatusDto);
        return R.ok();
    }

    @GetMapping("/getplan")
    @Operation(summary = "根据主键或唯一标识返回一个")
    public R getOne(Long id) {
        return R.ok(costAccountPlanService.byId(id));
    }


    /**
     * 获取方案配置的核算对象列表
     */
    @GetMapping("/accountObject/list")
    public R listAccountObject(@RequestParam("planId") Long planId) {
        return R.ok(costAccountPlanService.listAccountObject(planId));
    }

    @GetMapping("/preview")
    @Operation(summary = "根据主键或唯一标识返回一个")
    public R getPreview(Long planId) {
        return R.ok(costAccountPlanConfigService.parsePlanConfig(planId));
    }

    /**
     * 保存方案的核算对象的公式
     */
    @PostMapping("/saveFormula")
    @Operation(summary = "保存方案的核算对象的公式")
    public R saveFormula(@RequestBody @Validated CostAccountPlanConfigFormulaDto configFormulaDto) {
        return R.ok(costAccountPlanConfigService.saveFormula(configFormulaDto));
    }

    @GetMapping("/copy/{id}")
    //@PreAuthorize("@pms.hasPermission('kpi_case_del')")
    @Operation(summary = "方案复制")
    public R copyPlan(@PathVariable Long id) {
        costAccountPlanService.copyById(id);
        return R.ok();
    }

}