package com.hscloud.hs.cost.account.controller.report;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hscloud.hs.cost.account.model.entity.base.PageRequest;
import com.hscloud.hs.cost.account.model.entity.report.ReportFieldSonReport;
import com.hscloud.hs.cost.account.model.pojo.report.ParamMapping;
import com.hscloud.hs.cost.account.service.report.IReportFieldSonReportService;
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
 * 链接报表列表
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/report/reportFieldSonReport")
@Tag(name = "报表中心-链接报表", description = "链接报表")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class ReportFieldSonReportController {

    private final IReportFieldSonReportService reportFieldSonReportService;

    @SysLog("链接报表列表info")
    @GetMapping("/info/{id}")
    @Operation(summary = "链接报表列表info")
    public R<ReportFieldSonReport> info(@PathVariable Long id) {
        ReportFieldSonReport byId = reportFieldSonReportService.getById(id);
        if (StrUtil.isNotBlank(byId.getParamMappingJson())) {
            byId.setParamMappingList(JSON.parseArray(byId.getParamMappingJson(), ParamMapping.class));
        }
        return R.ok(byId);
    }

    @SysLog("链接报表列表page")
    @GetMapping("/page")
    @Operation(summary = "链接报表列表page")
    public R<IPage<ReportFieldSonReport>> page(PageRequest<ReportFieldSonReport> pr) {
        return R.ok(reportFieldSonReportService.page(pr.getPage(), pr.getWrapper()));
    }

    @SysLog("链接报表列表list")
    @GetMapping("/list")
    @Operation(summary = "链接报表列表list")
    public R<List<ReportFieldSonReport>> list(PageRequest<ReportFieldSonReport> pr) {
        return R.ok(reportFieldSonReportService.list(pr.getWrapper()));
    }

    @SysLog("链接报表列表add")
    @PostMapping("/add")
    @Operation(summary = "链接报表列表add")
    public R add(@RequestBody ReportFieldSonReport reportFieldSonReport) {
        return R.ok(reportFieldSonReportService.save(reportFieldSonReport));
    }

    @SysLog("数据集字段链接报表save")
    @PostMapping("/save")
    @Operation(summary = "数据集字段链接报表save")
    public R save(@RequestBody ReportFieldSonReport sonReport) {
        reportFieldSonReportService.createOrEdit(sonReport);
        return R.ok();
    }

    @SysLog("链接报表列表edit")
    @PostMapping("/edit")
    @Operation(summary = "链接报表列表edit")
    public R edit(@RequestBody ReportFieldSonReport reportFieldSonReport) {
        return R.ok(reportFieldSonReportService.updateById(reportFieldSonReport));
    }

    @SysLog("链接报表列表del")
    @PostMapping("/del/{id}")
    @Operation(summary = "链接报表列表del")
    public R del(@PathVariable Long id) {
        return R.ok(reportFieldSonReportService.removeById(id));
    }

    @SysLog("链接报表列表delBatch")
    @PostMapping("/delBatch/{ids}")
    @Operation(summary = "链接报表列表delBatch 1,2,3")
    public R delBatch(@PathVariable String ids) {
        return R.ok(reportFieldSonReportService.removeBatchByIds(Arrays.asList(ids.split(","))));
    }
}