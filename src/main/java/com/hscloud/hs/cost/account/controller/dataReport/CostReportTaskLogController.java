package com.hscloud.hs.cost.account.controller.dataReport;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hscloud.hs.cost.account.constant.enums.dataReport.OpsTypeEnum;
import com.hscloud.hs.cost.account.model.entity.base.PageRequest;
import com.hscloud.hs.cost.account.model.entity.dataReport.CostReportTaskLog;
import com.hscloud.hs.cost.account.model.vo.dataReport.CostReportTaskLogExcelExportVO;
import com.hscloud.hs.cost.account.service.dataReport.ICostReportTaskLogService;
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
* 上报任务变更日志
*
*/
@RestController
@RequiredArgsConstructor
@RequestMapping("/dataReport/costReportTaskLog")
@Tag(name = "costReportTaskLog", description = "上报任务变更日志")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class CostReportTaskLogController {

    private final ICostReportTaskLogService costReportTaskLogService;

    @SysLog("上报任务变更日志info")
    @GetMapping("/info/{id}")
    @Operation(summary = "上报任务变更日志info")
    public R<CostReportTaskLog> info(@PathVariable Long id) {
        return R.ok(costReportTaskLogService.getById(id));
    }

    @SysLog("上报任务变更日志page")
    @GetMapping("/page")
    @Operation(summary = "上报任务变更日志page")
    public R<IPage<CostReportTaskLog>> page(PageRequest<CostReportTaskLog> pr) {
        LambdaQueryWrapper<CostReportTaskLog> wrapper = pr.getWrapper().lambda();
        wrapper.orderByDesc(CostReportTaskLog::getCreateTime);
        return R.ok(costReportTaskLogService.page(pr.getPage(),wrapper));
    }

    @SysLog("上报任务变更日志list")
    @GetMapping("/list")
    @Operation(summary = "上报任务变更日志list")
    public R<List<CostReportTaskLog>> list(PageRequest<CostReportTaskLog> pr) {
        return R.ok(costReportTaskLogService.list(pr.getWrapper()));
    }

    @SysLog("上报任务变更日志add")
    @PostMapping("/add")
    @Operation(summary = "上报任务变更日志add")
    public R add(@RequestBody CostReportTaskLog costReportTaskLog)  {
        return R.ok(costReportTaskLogService.save(costReportTaskLog));
    }

    @SysLog("上报任务变更日志edit")
    @PostMapping("/edit")
    @Operation(summary = "上报任务变更日志edit")
    public R edit(@RequestBody CostReportTaskLog costReportTaskLog)  {
        return R.ok(costReportTaskLogService.updateById(costReportTaskLog));
    }

    @SysLog("上报任务变更日志del")
    @PostMapping("/del/{id}")
    @Operation(summary = "上报任务变更日志del")
    public R del(@PathVariable Long id)  {
        return R.ok(costReportTaskLogService.removeById(id));
    }

    @SysLog("上报任务变更日志delBatch")
    @PostMapping("/delBatch/{ids}")
    @Operation(summary = "上报任务变更日志delBatch 1,2,3")
    public R delBatch(@PathVariable String ids)  {
        return R.ok(costReportTaskLogService.removeBatchByIds(Arrays.asList(ids.split(","))));
    }

    @ResponseExcel(name = "上报任务变更日志")
    @GetMapping("/export")
    @Operation(summary = "导出")
    public List<CostReportTaskLogExcelExportVO> exportGroup() {
        LambdaQueryWrapper<CostReportTaskLog> qr = new LambdaQueryWrapper<>();
        qr.orderByDesc(CostReportTaskLog::getOpsTime);
        List<CostReportTaskLog> logs = costReportTaskLogService.list(qr);
        List<CostReportTaskLogExcelExportVO> costReportTaskLogExcelExportVOS = BeanUtil.copyToList(logs, CostReportTaskLogExcelExportVO.class);
        if (CollUtil.isNotEmpty(costReportTaskLogExcelExportVOS)) {
            costReportTaskLogExcelExportVOS.forEach(item -> {
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
        return costReportTaskLogExcelExportVOS;
    }

}