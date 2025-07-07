package com.hscloud.hs.cost.account.controller.report;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hscloud.hs.cost.account.model.entity.base.PageRequest;
import com.hscloud.hs.cost.account.model.entity.report.ReportGroup;
import com.hscloud.hs.cost.account.service.report.IReportGroupService;
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
* 报表分组
*
*/
@RestController
@RequiredArgsConstructor
@RequestMapping("/report/reportGroup")
@Tag(name = "报表中心-分组", description = "报表分组")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class ReportGroupController {

    private final IReportGroupService reportGroupService;

    @SysLog("报表分组info")
    @GetMapping("/info/{id}")
    @Operation(summary = "报表分组info")
    public R<ReportGroup> info(@PathVariable Long id) {
        return R.ok(reportGroupService.getById(id));
    }

    @SysLog("报表分组page")
    @GetMapping("/page")
    @Operation(summary = "报表分组page")
    public R<IPage<ReportGroup>> page(PageRequest<ReportGroup> pr) {
        return R.ok(reportGroupService.page(pr.getPage(),pr.getWrapper()));
    }

    @SysLog("报表分组list")
    @GetMapping("/list")
    @Operation(summary = "报表分组list")
    public R<List<ReportGroup>> list(PageRequest<ReportGroup> pr) {
        return R.ok(reportGroupService.list(pr.getWrapper()));
    }

    @SysLog("报表分组add")
    @PostMapping("/add")
    @Operation(summary = "报表分组add")
    public R add(@RequestBody ReportGroup reportGroup)  {
        return R.ok(reportGroupService.save(reportGroup));
    }

    @SysLog("报表分组edit")
    @PostMapping("/edit")
    @Operation(summary = "报表分组edit")
    public R edit(@RequestBody ReportGroup reportGroup)  {
        return R.ok(reportGroupService.updateById(reportGroup));
    }

    @SysLog("报表分组del")
    @PostMapping("/del/{id}")
    @Operation(summary = "报表分组del")
    public R del(@PathVariable Long id)  {
        return R.ok(reportGroupService.removeById(id));
    }

    @SysLog("报表分组delBatch")
    @PostMapping("/delBatch/{ids}")
    @Operation(summary = "报表分组delBatch 1,2,3")
    public R delBatch(@PathVariable String ids)  {
        return R.ok(reportGroupService.removeBatchByIds(Arrays.asList(ids.split(","))));
    }
}