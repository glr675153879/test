package com.hscloud.hs.cost.account.controller.dataReport;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hscloud.hs.cost.account.model.entity.base.PageRequest;
import com.hscloud.hs.cost.account.model.entity.dataReport.CostReportDetailInfo;
import com.hscloud.hs.cost.account.service.dataReport.ICostReportDetailInfoService;
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
* 上报详情基本信息表
*
*/
@RestController
@RequiredArgsConstructor
@RequestMapping("/dataReport/costReportDetailInfo")
@Tag(name = "costReportDetailInfo", description = "上报详情基本信息表")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class CostReportDetailInfoController {

    private final ICostReportDetailInfoService costReportDetailInfoService;

    @SysLog("上报详情基本信息表info")
    @GetMapping("/info/{id}")
    @Operation(summary = "上报详情基本信息表info")
    public R<CostReportDetailInfo> info(@PathVariable Long id) {
        return R.ok(costReportDetailInfoService.getById(id));
    }

    @SysLog("上报详情基本信息表page")
    @GetMapping("/page")
    @Operation(summary = "上报详情基本信息表page")
    public R<IPage<CostReportDetailInfo>> page(PageRequest<CostReportDetailInfo> pr) {
        return R.ok(costReportDetailInfoService.page(pr.getPage(),pr.getWrapper()));
    }

    @SysLog("上报详情基本信息表list")
    @GetMapping("/list")
    @Operation(summary = "上报详情基本信息表list")
    public R<List<CostReportDetailInfo>> list(PageRequest<CostReportDetailInfo> pr) {
        return R.ok(costReportDetailInfoService.list(pr.getWrapper()));
    }

    @SysLog("上报详情基本信息表add")
    @PostMapping("/add")
    @Operation(summary = "上报详情基本信息表add")
    public R add(@RequestBody CostReportDetailInfo costReportDetailInfo)  {
        return R.ok(costReportDetailInfoService.save(costReportDetailInfo));
    }

    @SysLog("上报详情基本信息表edit")
    @PostMapping("/edit")
    @Operation(summary = "上报详情基本信息表edit")
    public R edit(@RequestBody CostReportDetailInfo costReportDetailInfo)  {
        return R.ok(costReportDetailInfoService.updateById(costReportDetailInfo));
    }

    @SysLog("上报详情基本信息表del")
    @PostMapping("/del/{id}")
    @Operation(summary = "上报详情基本信息表del")
    public R del(@PathVariable Long id)  {
        return R.ok(costReportDetailInfoService.removeById(id));
    }

    @SysLog("上报详情基本信息表delBatch")
    @PostMapping("/delBatch/{ids}")
    @Operation(summary = "上报详情基本信息表delBatch 1,2,3")
    public R delBatch(@PathVariable String ids)  {
        return R.ok(costReportDetailInfoService.removeBatchByIds(Arrays.asList(ids.split(","))));
    }
}