package com.hscloud.hs.cost.account.controller.dataReport;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hscloud.hs.cost.account.model.entity.base.PageRequest;
import com.hscloud.hs.cost.account.model.entity.dataReport.CostReportDetailCost;
import com.hscloud.hs.cost.account.service.dataReport.ICostReportDetailCostService;
import com.pig4cloud.pigx.common.core.util.R;
import com.pig4cloud.pigx.common.log.annotation.SysLog;
import com.pig4cloud.pigx.common.security.annotation.Inner;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;
import java.util.Arrays;
import java.util.List;

/**
* 上报详情-费用表
*
*/
@RestController
@RequiredArgsConstructor
@RequestMapping("/dataReport/costReportDetailCost")
@Tag(name = "costReportDetailCost", description = "上报详情-费用表")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class CostReportDetailCostController {

    private final ICostReportDetailCostService costReportDetailCostService;

    @SysLog("上报详情-费用表info")
    @GetMapping("/info/{id}")
    @Operation(summary = "上报详情-费用表info")
    public R<CostReportDetailCost> info(@PathVariable Long id) {
        return R.ok(costReportDetailCostService.getById(id));
    }

    @SysLog("上报详情-费用表page")
    @GetMapping("/page")
    @Operation(summary = "上报详情-费用表page")
    public R<IPage<CostReportDetailCost>> page(PageRequest<CostReportDetailCost> pr) {
        return R.ok(costReportDetailCostService.page(pr.getPage(),pr.getWrapper()));
    }

    @SysLog("上报详情-费用表list")
    @GetMapping("/list")
    @Operation(summary = "上报详情-费用表list")
    public R<List<CostReportDetailCost>> list(PageRequest<CostReportDetailCost> pr) {
        return R.ok(costReportDetailCostService.list(pr.getWrapper()));
    }

    @SysLog("上报详情-费用表add")
    @PostMapping("/add")
    @Operation(summary = "上报详情-费用表add")
    public R add(@RequestBody CostReportDetailCost costReportDetailCost)  {
        return R.ok(costReportDetailCostService.save(costReportDetailCost));
    }

    @SysLog("上报详情-费用表edit")
    @PostMapping("/edit")
    @Operation(summary = "上报详情-费用表edit")
    public R edit(@RequestBody CostReportDetailCost costReportDetailCost)  {
        return R.ok(costReportDetailCostService.updateById(costReportDetailCost));
    }

    @SysLog("上报详情-费用表del")
    @PostMapping("/del/{id}")
    @Operation(summary = "上报详情-费用表del")
    public R del(@PathVariable Long id)  {
        return R.ok(costReportDetailCostService.removeById(id));
    }

    @SysLog("上报详情-费用表delBatch")
    @PostMapping("/delBatch/{ids}")
    @Operation(summary = "上报详情-费用表delBatch 1,2,3")
    public R delBatch(@PathVariable String ids)  {
        return R.ok(costReportDetailCostService.removeBatchByIds(Arrays.asList(ids.split(","))));
    }
}