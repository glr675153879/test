package com.hscloud.hs.cost.account.controller.dataReport;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hscloud.hs.cost.account.model.entity.base.PageRequest;
import com.hscloud.hs.cost.account.model.entity.dataReport.CostReportRecordFileInfo;
import com.hscloud.hs.cost.account.service.dataReport.ICostReportRecordFileInfoService;
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
* 我的上报附件表
*
*/
@RestController
@RequiredArgsConstructor
@RequestMapping("/dataReport/costReportRecordFileInfo")
@Tag(name = "costReportRecordFileInfo", description = "我的上报附件表")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class CostReportRecordFileInfoController {

    private final ICostReportRecordFileInfoService costReportRecordFileInfoService;

    @SysLog("我的上报附件表info")
    @GetMapping("/info/{id}")
    @Operation(summary = "我的上报附件表info")
    public R<CostReportRecordFileInfo> info(@PathVariable Long id) {
        return R.ok(costReportRecordFileInfoService.getById(id));
    }

    @SysLog("我的上报附件表page")
    @GetMapping("/page")
    @Operation(summary = "我的上报附件表page")
    public R<IPage<CostReportRecordFileInfo>> page(PageRequest<CostReportRecordFileInfo> pr) {
        return R.ok(costReportRecordFileInfoService.page(pr.getPage(),pr.getWrapper()));
    }

    @SysLog("我的上报附件表list")
    @GetMapping("/list")
    @Operation(summary = "我的上报附件表list")
    public R<List<CostReportRecordFileInfo>> list(Long recordId) {
        return R.ok(costReportRecordFileInfoService.listData(recordId));
    }

    @SysLog("我的上报附件表add")
    @PostMapping("/add")
    @Operation(summary = "我的上报附件表add")
    public R add(@RequestBody CostReportRecordFileInfo costReportRecordFileInfo)  {
        return R.ok(costReportRecordFileInfoService.save(costReportRecordFileInfo));
    }

    @SysLog("我的上报附件表edit")
    @PostMapping("/edit")
    @Operation(summary = "我的上报附件表edit")
    public R edit(@RequestBody CostReportRecordFileInfo costReportRecordFileInfo)  {
        return R.ok(costReportRecordFileInfoService.updateById(costReportRecordFileInfo));
    }

    @SysLog("我的上报附件表del")
    @PostMapping("/del/{id}")
    @Operation(summary = "我的上报附件表del")
    public R del(@PathVariable Long id)  {
        return R.ok(costReportRecordFileInfoService.removeById(id));
    }

    @SysLog("我的上报附件表delBatch")
    @PostMapping("/delBatch/{ids}")
    @Operation(summary = "我的上报附件表delBatch 1,2,3")
    public R delBatch(@PathVariable String ids)  {
        return R.ok(costReportRecordFileInfoService.removeBatchByIds(Arrays.asList(ids.split(","))));
    }
}