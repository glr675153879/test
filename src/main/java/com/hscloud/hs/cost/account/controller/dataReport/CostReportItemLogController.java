package com.hscloud.hs.cost.account.controller.dataReport;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hscloud.hs.cost.account.constant.enums.dataReport.OpsTypeEnum;
import com.hscloud.hs.cost.account.model.entity.base.PageRequest;
import com.hscloud.hs.cost.account.model.entity.dataReport.CostReportItemLog;
import com.hscloud.hs.cost.account.model.vo.dataReport.CostReportItemLogExcelExportVO;
import com.hscloud.hs.cost.account.service.dataReport.ICostReportItemLogService;
import com.pig4cloud.pigx.common.core.util.R;
import com.pig4cloud.pigx.common.excel.annotation.ResponseExcel;
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
* 上报项变更日志
*
*/
@RestController
@RequiredArgsConstructor
@RequestMapping("/dataReport/costReportItemLog")
@Tag(name = "costReportItemLog", description = "上报项变更日志")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class CostReportItemLogController {

    private final ICostReportItemLogService costReportItemLogService;

    @SysLog("上报项变更日志info")
    @GetMapping("/info/{id}")
    @Operation(summary = "上报项变更日志info")
    public R<CostReportItemLog> info(@PathVariable Long id) {
        return R.ok(costReportItemLogService.getById(id));
    }

    @SysLog("上报项变更日志page")
    @GetMapping("/page")
    @Operation(summary = "上报项变更日志page")
    public R<IPage<CostReportItemLog>> page(PageRequest<CostReportItemLog> pr) {
        LambdaQueryWrapper<CostReportItemLog> wrapper = pr.getWrapper().lambda();
        wrapper.orderByDesc(CostReportItemLog::getOpsTime);
        return R.ok(costReportItemLogService.page(pr.getPage(),wrapper));
    }

    @SysLog("上报项变更日志list")
    @GetMapping("/list")
    @Operation(summary = "上报项变更日志list")
    public R<List<CostReportItemLog>> list(PageRequest<CostReportItemLog> pr) {
        return R.ok(costReportItemLogService.list(pr.getWrapper()));
    }

    @SysLog("上报项变更日志add")
    @PostMapping("/add")
    @Operation(summary = "上报项变更日志add")
    public R add(@RequestBody CostReportItemLog costReportItemLog)  {
        return R.ok(costReportItemLogService.save(costReportItemLog));
    }

    @SysLog("上报项变更日志edit")
    @PostMapping("/edit")
    @Operation(summary = "上报项变更日志edit")
    public R edit(@RequestBody CostReportItemLog costReportItemLog)  {
        return R.ok(costReportItemLogService.updateById(costReportItemLog));
    }

    @SysLog("上报项变更日志del")
    @PostMapping("/del/{id}")
    @Operation(summary = "上报项变更日志del")
    public R del(@PathVariable Long id)  {
        return R.ok(costReportItemLogService.removeById(id));
    }

    @SysLog("上报项变更日志delBatch")
    @PostMapping("/delBatch/{ids}")
    @Operation(summary = "上报项变更日志delBatch 1,2,3")
    public R delBatch(@PathVariable String ids)  {
        return R.ok(costReportItemLogService.removeBatchByIds(Arrays.asList(ids.split(","))));
    }

    @ResponseExcel(name = "上报项变更日志")
    @GetMapping("/export")
    @Operation(summary = "导出")
    public List<CostReportItemLogExcelExportVO> exportGroup() {
        LambdaQueryWrapper<CostReportItemLog> qr = new LambdaQueryWrapper<>();
        qr.orderByDesc(CostReportItemLog::getOpsTime);
        List<CostReportItemLog> logs = costReportItemLogService.list(qr);
        List<CostReportItemLogExcelExportVO> costReportItemLogExcelExportVOS = BeanUtil.copyToList(logs, CostReportItemLogExcelExportVO.class);
        if (CollUtil.isNotEmpty(costReportItemLogExcelExportVOS)) {
            costReportItemLogExcelExportVOS.forEach(item -> {
                OpsTypeEnum opsTypeEnum = OpsTypeEnum.getByVal(item.getOpsType());
                switch (opsTypeEnum) {
                    case ADD:
                        item.setOpsType("新增");
                        break;
                    case UPDATE:
                        item.setOpsType("变更");
                        break;
                    case DEL:
                        item.setOpsType("删除");
                        break;
                    case ENABLE:
                        item.setOpsType("启停用");
                        break;
                    default:
                        break;
                }
            });
        }
        return costReportItemLogExcelExportVOS;
    }
}