package com.hscloud.hs.cost.account.controller.report;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hscloud.hs.cost.account.model.entity.base.PageRequest;
import com.hscloud.hs.cost.account.model.entity.report.ReportFieldFormulaDetails;
import com.hscloud.hs.cost.account.service.report.IReportFieldFormulaDetailsService;
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
* 数据集字段自定义公式包含的字段
*
*/
@RestController
@RequiredArgsConstructor
@RequestMapping("/report/reportFieldFormulaDetails")
@Tag(name = "报表中心-自定义公式包含的字段", description = "数据集字段自定义公式包含的字段")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class ReportFieldFormulaDetailsController {

    private final IReportFieldFormulaDetailsService reportFieldFormulaDetailsService;

    @SysLog("数据集字段自定义公式包含的字段info")
    @GetMapping("/info/{id}")
    @Operation(summary = "数据集字段自定义公式包含的字段info")
    public R<ReportFieldFormulaDetails> info(@PathVariable Long id) {
        return R.ok(reportFieldFormulaDetailsService.getById(id));
    }

    @SysLog("数据集字段自定义公式包含的字段page")
    @GetMapping("/page")
    @Operation(summary = "数据集字段自定义公式包含的字段page")
    public R<IPage<ReportFieldFormulaDetails>> page(PageRequest<ReportFieldFormulaDetails> pr) {
        return R.ok(reportFieldFormulaDetailsService.page(pr.getPage(),pr.getWrapper()));
    }

    @SysLog("数据集字段自定义公式包含的字段list")
    @GetMapping("/list")
    @Operation(summary = "数据集字段自定义公式包含的字段list")
    public R<List<ReportFieldFormulaDetails>> list(PageRequest<ReportFieldFormulaDetails> pr) {
        return R.ok(reportFieldFormulaDetailsService.list(pr.getWrapper()));
    }

    @SysLog("数据集字段自定义公式包含的字段add")
    @PostMapping("/add")
    @Operation(summary = "数据集字段自定义公式包含的字段add")
    public R add(@RequestBody ReportFieldFormulaDetails reportFieldFormulaDetails)  {
        return R.ok(reportFieldFormulaDetailsService.save(reportFieldFormulaDetails));
    }

    @SysLog("数据集字段自定义公式包含的字段edit")
    @PostMapping("/edit")
    @Operation(summary = "数据集字段自定义公式包含的字段edit")
    public R edit(@RequestBody ReportFieldFormulaDetails reportFieldFormulaDetails)  {
        return R.ok(reportFieldFormulaDetailsService.updateById(reportFieldFormulaDetails));
    }

    @SysLog("数据集字段自定义公式包含的字段del")
    @PostMapping("/del/{id}")
    @Operation(summary = "数据集字段自定义公式包含的字段del")
    public R del(@PathVariable Long id)  {
        return R.ok(reportFieldFormulaDetailsService.removeById(id));
    }

    @SysLog("数据集字段自定义公式包含的字段delBatch")
    @PostMapping("/delBatch/{ids}")
    @Operation(summary = "数据集字段自定义公式包含的字段delBatch 1,2,3")
    public R delBatch(@PathVariable String ids)  {
        return R.ok(reportFieldFormulaDetailsService.removeBatchByIds(Arrays.asList(ids.split(","))));
    }
}