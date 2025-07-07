package com.hscloud.hs.cost.account.controller.report;

import cn.hutool.core.lang.tree.Tree;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hscloud.hs.cost.account.model.dto.report.BatchSaveReportHeadDto;
import com.hscloud.hs.cost.account.model.entity.base.PageRequest;
import com.hscloud.hs.cost.account.model.entity.report.ReportHead;
import com.hscloud.hs.cost.account.service.report.IReportHeadService;
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
 * 报表表头
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/report/reportHead")
@Tag(name = "报表中心-报表表头", description = "报表表头")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class ReportHeadController {

    private final IReportHeadService reportHeadService;

    @SysLog("报表表头info")
    @GetMapping("/info/{id}")
    @Operation(summary = "报表表头info")
    public R<ReportHead> info(@PathVariable Long id) {
        return R.ok(reportHeadService.getById(id));
    }

    @SysLog("报表表头page")
    @GetMapping("/page")
    @Operation(summary = "报表表头page")
    public R<IPage<ReportHead>> page(PageRequest<ReportHead> pr) {
        return R.ok(reportHeadService.page(pr.getPage(), pr.getWrapper()));
    }

    @SysLog("报表表头list")
    @GetMapping("/list")
    @Operation(summary = "报表表头list")
    public R<List<ReportHead>> list(PageRequest<ReportHead> pr) {
        return R.ok(reportHeadService.list(pr.getWrapper()));
    }

    @SysLog("报表表头add")
    @PostMapping("/add")
    @Operation(summary = "报表表头add")
    public R add(@RequestBody ReportHead reportHead) {
        return R.ok(reportHeadService.save(reportHead));
    }

    @SysLog("保存或更新表头")
    @PostMapping("/save")
    @Operation(summary = "保存或更新表头")
    public R<Boolean> save(@RequestBody ReportHead reportHead) {
        return R.ok(reportHeadService.createOrEdit(reportHead));
    }

    @SysLog("报表表头edit")
    @PostMapping("/edit")
    @Operation(summary = "报表表头edit")
    public R edit(@RequestBody ReportHead reportHead) {
        return R.ok(reportHeadService.updateById(reportHead));
    }

    @SysLog("报表表头del")
    @PostMapping("/del/{id}")
    @Operation(summary = "报表表头del")
    public R<Boolean> del(@PathVariable Long id) {
        return R.ok(reportHeadService.deleteById(id));
    }

    @SysLog("报表表头delBatch")
    @PostMapping("/delBatch/{ids}")
    @Operation(summary = "报表表头delBatch 1,2,3")
    public R delBatch(@PathVariable String ids) {
        return R.ok(reportHeadService.removeBatchByIds(Arrays.asList(ids.split(","))));
    }

    @PostMapping("/batchSave")
    @Operation(summary = "批量保存表头")
    public R<Boolean> batchSave(@RequestBody BatchSaveReportHeadDto dto) {
        return R.ok(reportHeadService.batchSave(dto.getReportId(), dto.getHeads()));
    }

    @GetMapping("/tree4Edit/{id}")
    @Operation(summary = "报表表头")
    public R<List<Tree<Long>>> tree4Edit(@PathVariable Long id) {
        return R.ok(reportHeadService.tree(id, false));
    }

    @GetMapping("/tree4View/{id}")
    @Operation(summary = "报表表头")
    public R<List<Tree<Long>>> tree4View(@PathVariable Long id) {
        return R.ok(reportHeadService.tree(id, false));
    }

    @GetMapping("/treeOnly/{id}")
    @Operation(summary = "报表表头（仅表头信息）")
    public R<List<Tree<Long>>> treeOnly(@PathVariable Long id) {
        return R.ok(reportHeadService.tree(id, true));
    }

}