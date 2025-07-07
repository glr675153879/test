package com.hscloud.hs.cost.account.controller.userAttendance;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hscloud.hs.cost.account.model.dto.userAttendance.FirstDistributionAccountFormulaDto;
import com.hscloud.hs.cost.account.model.entity.base.PageRequest;
import com.hscloud.hs.cost.account.model.entity.userAttendance.FirstDistributionAttendanceFormula;
import com.hscloud.hs.cost.account.service.userAttendance.IFirstDistributionAttendanceFormulaService;
import com.pig4cloud.pigx.common.core.util.R;
import com.pig4cloud.pigx.common.log.annotation.SysLog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

/**
 * 一次分配考勤公式配置表
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/userAttendance/firstDistributionAttendanceFormula")
@Tag(name = "firstDistributionAttendanceFormula", description = "一次分配考勤公式配置表")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class FirstDistributionAttendanceFormulaController {

    private final IFirstDistributionAttendanceFormulaService firstDistributionAttendanceFormulaService;

    @SysLog("一次分配考勤公式配置表info")
    @GetMapping("/info/{id}")
    @Operation(summary = "一次分配考勤公式配置表info")
    public R<FirstDistributionAttendanceFormula> info(@PathVariable Long id) {
        return R.ok(firstDistributionAttendanceFormulaService.getById(id));
    }

    @SysLog("一次分配考勤公式配置表page")
    @GetMapping("/page")
    @Operation(summary = "一次分配考勤公式配置表page")
    public R<IPage<FirstDistributionAttendanceFormula>> page(PageRequest<FirstDistributionAttendanceFormula> pr) {
        return R.ok(firstDistributionAttendanceFormulaService.page(pr.getPage(), pr.getWrapper()));
    }

    @SysLog("一次分配考勤公式配置表list")
    @GetMapping("/list")
    @Operation(summary = "一次分配考勤公式配置表list")
    public R<List<FirstDistributionAttendanceFormula>> list(PageRequest<FirstDistributionAttendanceFormula> pr) {
        return R.ok(firstDistributionAttendanceFormulaService.listData(pr.getWrapper()));
    }

    @SysLog("一次分配考勤公式配置表add")
    @PostMapping("/add")
    @Operation(summary = "一次分配考勤公式配置表add")
    public R add(@RequestBody FirstDistributionAttendanceFormula firstDistributionAttendanceFormula) {
        return firstDistributionAttendanceFormulaService.saveData(firstDistributionAttendanceFormula);
    }

    @SysLog("一次分配考勤公式配置表edit")
    @PostMapping("/edit")
    @Operation(summary = "一次分配考勤公式配置表edit")
    public R edit(@RequestBody FirstDistributionAttendanceFormula firstDistributionAttendanceFormula) {
        return firstDistributionAttendanceFormulaService.updateData(firstDistributionAttendanceFormula);
    }

    @SysLog("一次分配考勤公式配置表del")
    @PostMapping("/del/{id}")
    @Operation(summary = "一次分配考勤公式配置表del")
    public R del(@PathVariable Long id) {
        return R.ok(firstDistributionAttendanceFormulaService.removeById(id));
    }

    @SysLog("一次分配考勤公式配置表delBatch")
    @PostMapping("/delBatch/{ids}")
    @Operation(summary = "一次分配考勤公式配置表delBatch 1,2,3")
    public R delBatch(@PathVariable String ids) {
        return R.ok(firstDistributionAttendanceFormulaService.removeBatchByIds(Arrays.asList(ids.split(","))));
    }

    @SysLog("一次分配考勤公式方案表")
    @GetMapping("/formulaList")
    @Operation(summary = "一次分配考勤公式方案表")
    public R<List<FirstDistributionAccountFormulaDto>> formulaList(PageRequest<FirstDistributionAttendanceFormula> pr) {
        return R.ok(firstDistributionAttendanceFormulaService.formulaList(pr.getWrapper()));
    }


    // 计算
    @SysLog("test")
    @PostMapping("/test")
    @Operation(summary = "test")
    public R calculate() {
        return R.ok(firstDistributionAttendanceFormulaService.calculateAttendDays(1L, null));
    }


}