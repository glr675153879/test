package com.hscloud.hs.cost.account.controller.report;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hscloud.hs.cost.account.model.entity.base.PageRequest;
import com.hscloud.hs.cost.account.model.entity.report.ReportField;
import com.hscloud.hs.cost.account.service.report.IReportFieldService;
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
 * 报表字段
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/report/reportField")
@Tag(name = "报表中心-报表字段", description = "报表字段")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class ReportFieldController {

    private final IReportFieldService reportFieldService;

    @SysLog("报表字段info")
    @GetMapping("/info/{id}")
    @Operation(summary = "报表字段info")
    public R<ReportField> info(@PathVariable Long id) {
        return R.ok(reportFieldService.info(id));
    }

    @SysLog("报表字段page")
    @GetMapping("/page")
    @Operation(summary = "报表字段page")
    public R<IPage<ReportField>> page(PageRequest<ReportField> pr) {
        return R.ok(reportFieldService.page(pr.getPage(), pr.getWrapper()));
    }

    @SysLog("报表字段list")
    @GetMapping("/list")
    @Operation(summary = "报表字段list")
    public R<List<ReportField>> list(PageRequest<ReportField> pr) {
        return R.ok(reportFieldService.list(pr.getWrapper()));
    }

    @SysLog("报表字段add")
    @PostMapping("/add")
    @Operation(summary = "报表字段add")
    public R add(@RequestBody ReportField reportField) {
        return R.ok(reportFieldService.save(reportField));
    }

    @SysLog("报表字段save")
    @PostMapping("/save")
    @Operation(summary = "报表字段save")
    public R<Long> save(@RequestBody ReportField reportField) {
        return R.ok(reportFieldService.createOrEdit(reportField));
    }

    @SysLog("报表字段edit")
    @PostMapping("/edit")
    @Operation(summary = "报表字段edit")
    public R edit(@RequestBody ReportField reportField) {
        return R.ok(reportFieldService.updateById(reportField));
    }

    @SysLog("报表字段del")
    @PostMapping("/del/{id}")
    @Operation(summary = "报表字段del")
    public R del(@PathVariable Long id) {
        return R.ok(reportFieldService.checkAndDeleteById(id));
    }

    @SysLog("报表字段delBatch")
    @PostMapping("/delBatch/{ids}")
    @Operation(summary = "报表字段delBatch 1,2,3")
    public R delBatch(@PathVariable String ids) {
        return R.ok(reportFieldService.removeBatchByIds(Arrays.asList(ids.split(","))));
    }


}