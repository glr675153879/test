package com.hscloud.hs.cost.account.controller.report;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hscloud.hs.cost.account.model.entity.base.PageRequest;
import com.hscloud.hs.cost.account.model.entity.report.ReportFieldFormula;
import com.hscloud.hs.cost.account.service.report.IReportFieldFormulaService;
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
 * 数据集字段自定义公式
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/report/reportFieldFormula")
@Tag(name = "报表中心-数据集字段自定义公式", description = "数据集字段自定义公式")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class ReportFieldFormulaController {

    private final IReportFieldFormulaService reportFieldFormulaService;

    @SysLog("数据集字段自定义公式info")
    @GetMapping("/info/{id}")
    @Operation(summary = "数据集字段自定义公式info")
    public R<ReportFieldFormula> info(@PathVariable Long id) {
        return R.ok(reportFieldFormulaService.getById(id));
    }

    @SysLog("数据集字段自定义公式page")
    @GetMapping("/page")
    @Operation(summary = "数据集字段自定义公式page")
    public R<IPage<ReportFieldFormula>> page(PageRequest<ReportFieldFormula> pr) {
        return R.ok(reportFieldFormulaService.page(pr.getPage(), pr.getWrapper()));
    }

    @SysLog("数据集字段自定义公式list")
    @GetMapping("/list")
    @Operation(summary = "数据集字段自定义公式list")
    public R<List<ReportFieldFormula>> list(PageRequest<ReportFieldFormula> pr) {
        return R.ok(reportFieldFormulaService.list(pr.getWrapper()));
    }

    @SysLog("数据集字段自定义公式add")
    @PostMapping("/add")
    @Operation(summary = "数据集字段自定义公式add")
    public R add(@RequestBody ReportFieldFormula reportFieldFormula) {
        return R.ok(reportFieldFormulaService.save(reportFieldFormula));
    }

    @SysLog("数据集字段自定义公式save")
    @PostMapping("/save")
    @Operation(summary = "数据集字段自定义公式save")
    public R save(@RequestBody ReportFieldFormula reportFieldFormula) {
        reportFieldFormulaService.createOrEdit(reportFieldFormula);
        return R.ok();
    }

    @SysLog("数据集字段自定义公式edit")
    @PostMapping("/edit")
    @Operation(summary = "数据集字段自定义公式edit")
    public R edit(@RequestBody ReportFieldFormula reportFieldFormula) {
        return R.ok(reportFieldFormulaService.updateById(reportFieldFormula));
    }

    @SysLog("数据集字段自定义公式del")
    @PostMapping("/del/{id}")
    @Operation(summary = "数据集字段自定义公式del")
    public R del(@PathVariable Long id) {
        return R.ok(reportFieldFormulaService.deleteById(id));
    }

    @SysLog("数据集字段自定义公式delBatch")
    @PostMapping("/delBatch/{ids}")
    @Operation(summary = "数据集字段自定义公式delBatch 1,2,3")
    public R delBatch(@PathVariable String ids) {
        return R.ok(reportFieldFormulaService.removeBatchByIds(Arrays.asList(ids.split(","))));
    }
}