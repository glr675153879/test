package com.hscloud.hs.cost.account.controller.report;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hscloud.hs.cost.account.model.entity.base.PageRequest;
import com.hscloud.hs.cost.account.model.entity.report.ReportDbParam;
import com.hscloud.hs.cost.account.service.report.IReportDbParamService;
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
 * 报表入参表
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/report/reportDbParam")
@Tag(name = "报表中心-报表入参", description = "报表入参表")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class ReportDbParamController {

    private final IReportDbParamService reportDbParamService;

    @SysLog("报表入参表info")
    @GetMapping("/info/{id}")
    @Operation(summary = "报表入参表info")
    public R<ReportDbParam> info(@PathVariable Long id) {
        return R.ok(reportDbParamService.getById(id));
    }

    @SysLog("报表入参表page")
    @GetMapping("/page")
    @Operation(summary = "报表入参表page")
    public R<IPage<ReportDbParam>> page(PageRequest<ReportDbParam> pr) {
        return R.ok(reportDbParamService.page(pr.getPage(), pr.getWrapper()));
    }

    @SysLog("报表入参表list")
    @GetMapping("/list")
    @Operation(summary = "报表入参表list")
    public R<List<ReportDbParam>> list(PageRequest<ReportDbParam> pr) {
        return R.ok(reportDbParamService.list(pr.getWrapper()));
    }

    @SysLog("报表入参表add")
    @PostMapping("/add")
    @Operation(summary = "报表入参表add")
    public R add(@RequestBody ReportDbParam reportDbParam) {
        return R.ok(reportDbParamService.save(reportDbParam));
    }

    @SysLog("报表入参表edit")
    @PostMapping("/edit")
    @Operation(summary = "报表入参表edit")
    public R edit(@RequestBody ReportDbParam reportDbParam) {
        return R.ok(reportDbParamService.updateById(reportDbParam));
    }

    @SysLog("报表入参表del")
    @PostMapping("/del/{id}")
    @Operation(summary = "报表入参表del")
    public R del(@PathVariable Long id) {
        return R.ok(reportDbParamService.removeById(id));
    }

    @SysLog("报表入参表delBatch")
    @PostMapping("/delBatch/{ids}")
    @Operation(summary = "报表入参表delBatch 1,2,3")
    public R delBatch(@PathVariable String ids) {
        return R.ok(reportDbParamService.removeBatchByIds(Arrays.asList(ids.split(","))));
    }
}