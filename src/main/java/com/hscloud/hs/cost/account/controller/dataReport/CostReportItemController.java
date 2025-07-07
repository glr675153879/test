package com.hscloud.hs.cost.account.controller.dataReport;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hscloud.hs.cost.account.constant.enums.dataReport.OpsTypeEnum;
import com.hscloud.hs.cost.account.model.dto.dataReport.CostReportItemPageDto;
import com.hscloud.hs.cost.account.model.entity.base.PageRequest;
import com.hscloud.hs.cost.account.model.entity.dataReport.CostReportItem;
import com.hscloud.hs.cost.account.service.dataReport.ICostReportItemLogService;
import com.hscloud.hs.cost.account.service.dataReport.ICostReportItemService;
import com.pig4cloud.pigx.common.core.util.R;
import com.pig4cloud.pigx.common.log.annotation.SysLog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

/**
* 上报项
*
*/
@RestController
@RequiredArgsConstructor
@RequestMapping("/dataReport/costReportItem")
@Tag(name = "costReportItem", description = "上报项")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
@Transactional(readOnly = true)
public class CostReportItemController {

    private final ICostReportItemService costReportItemService;

    private final ICostReportItemLogService costReportItemLogService;

    @SysLog("上报项info")
    @GetMapping("/info/{id}")
    @Operation(summary = "上报项info")
    public R<CostReportItem> info(@PathVariable Long id) {
        return R.ok(costReportItemService.getById(id));
    }

    @SysLog("上报项page")
    @GetMapping("/page")
    @Operation(summary = "上报项page")
    public R<IPage<CostReportItemPageDto>> page(PageRequest<CostReportItem> pr) {
        // 绩效、科室成本需要区分，type类型必须指定，否则全量数据
        return R.ok(costReportItemService.pageData(pr.getPage(), pr.getWrapper()));
    }

    @SysLog("上报项list")
    @GetMapping("/list")
    @Operation(summary = "上报项list")
    public R<List<CostReportItem>> list(PageRequest<CostReportItem> pr) {
        return R.ok(costReportItemService.list(pr.getWrapper()));
    }

    @SysLog("上报项add")
    @PostMapping("/add")
    @Operation(summary = "上报项add")
    @Transactional(rollbackFor = Exception.class)
    public R add(@RequestBody CostReportItem costReportItem){
        costReportItemLogService.generateLog(OpsTypeEnum.ADD.getVal(), "", costReportItem);
        return R.ok(costReportItemService.save(costReportItem));
    }

    @SysLog("上报项edit")
    @PostMapping("/edit")
    @Operation(summary = "上报项edit")
    @Transactional(rollbackFor = Exception.class)
    public R edit(@RequestBody CostReportItem costReportItem)  {
        String originName = costReportItemService.getById(costReportItem).getName();
        costReportItemLogService.generateLog(OpsTypeEnum.UPDATE.getVal(), originName, costReportItem);
        return R.ok(costReportItemService.updateById(costReportItem));
    }

    @SysLog("上报项activate")
    @PostMapping("/activate")
    @Operation(summary = "上报项activate")
    @Transactional(rollbackFor = Exception.class)
    public R activate(@RequestBody CostReportItem costReportItem)  {
        String originName = costReportItemService.getById(costReportItem).getName();
        costReportItemLogService.generateLog(OpsTypeEnum.ENABLE.getVal(), originName, costReportItem);
        return R.ok(costReportItemService.activate(costReportItem));
    }

    @SysLog("上报项del")
    @PostMapping("/del/{id}")
    @Operation(summary = "上报项del")
    @Transactional(rollbackFor = Exception.class)
    public R del(@PathVariable Long id)  {
        costReportItemLogService.generateLog(OpsTypeEnum.DEL.getVal(), "", costReportItemService.getById(id));
        return R.ok(costReportItemService.removeById(id));
    }

    @SysLog("上报项delBatch")
    @PostMapping("/delBatch/{ids}")
    @Operation(summary = "上报项delBatch 1,2,3")
    public R delBatch(@PathVariable String ids)  {
        return R.ok(costReportItemService.removeBatchByIds(Arrays.asList(ids.split(","))));
    }

    @SysLog("上报项是否使用")
    @GetMapping("/isUsed")
    @Operation(summary = "上报项是否使用")
    public R<Boolean> isUsed(@RequestParam("id") Long id) {
        return R.ok(costReportItemService.isUsed(id));
    }
}