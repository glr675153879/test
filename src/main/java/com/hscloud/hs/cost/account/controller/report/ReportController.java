package com.hscloud.hs.cost.account.controller.report;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.hscloud.hs.cost.account.model.dto.report.*;
import com.hscloud.hs.cost.account.model.entity.base.PageRequest;
import com.hscloud.hs.cost.account.model.entity.report.Report;
import com.hscloud.hs.cost.account.model.entity.report.ReportDb;
import com.hscloud.hs.cost.account.model.entity.report.ReportFieldFormula;
import com.hscloud.hs.cost.account.model.vo.report.ReportCellDataVo;
import com.hscloud.hs.cost.account.model.vo.report.ReportTableDataVo;
import com.hscloud.hs.cost.account.model.vo.report.SonReportParamVo;
import com.hscloud.hs.cost.account.service.impl.report.ReportCheckService;
import com.hscloud.hs.cost.account.service.impl.report.ReportCopyService;
import com.hscloud.hs.cost.account.service.impl.report.ReportExportService;
import com.hscloud.hs.cost.account.service.report.IReportDbService;
import com.hscloud.hs.cost.account.service.report.IReportService;
import com.pig4cloud.pigx.common.core.util.R;
import com.pig4cloud.pigx.common.log.annotation.SysLog;
import com.pig4cloud.pigx.common.security.annotation.Inner;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 报表设计表
 */
@RestController("RpReportController")
@RequiredArgsConstructor
@RequestMapping("/report/report")
@Tag(name = "报表中心-报表", description = "报表设计表")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class ReportController {

    private final IReportService reportService;
    private final ReportCopyService reportCopyService;
    private final ReportCheckService reportCheckService;
    private final IReportDbService reportDbService;
    private final ReportExportService reportExportService;

    @SysLog("报表设计表info")
    @GetMapping("/info/{id}")
    @Operation(summary = "报表设计表info")
    public R<Report> info(@PathVariable Long id) {
        return R.ok(reportService.getById(id));
    }

    @GetMapping("/getByCode/{reportCode}")
    @Operation(summary = "根据code获取报表")
    public R<Report> info(@PathVariable String reportCode) {
        return R.ok(reportService.getByReportCode(reportCode));
    }

    @SysLog("报表设计表page")
    @GetMapping("/page")
    @Operation(summary = "报表设计表page")
    public R<IPage<Report>> page(PageRequest<Report> pr) {
        return R.ok(reportService.page(pr.getPage(), pr.getWrapper()));
    }

    @SysLog("报表设计表list")
    @GetMapping("/list")
    @Operation(summary = "报表设计表list")
    public R<List<Report>> list(PageRequest<Report> pr) {
        List<Report> list = reportService.list(pr.getWrapper());
        if (CollUtil.isNotEmpty(list)) {
            for (Report report : list) {
                ReportDb reportDb = reportDbService.getByReportId(report.getId());
                if (Objects.nonNull(reportDb)) {
                    report.setReportDbId(reportDb.getId());
                }
            }
        }
        return R.ok(list);
    }


    @SysLog("报表设计表listByGroupId")
    @GetMapping("/list/{groupId}")
    @Operation(summary = "报表设计表listByGroupId")
    public  R<List<Report>> listByGroupId(@PathVariable Long groupId) {
        List<Report> list = reportService.list(Wrappers.<Report>lambdaQuery()
                .eq(Report::getGroupId, groupId));
        if (CollUtil.isNotEmpty(list)) {
            for (Report report : list) {
                ReportDb reportDb = reportDbService.getByReportId(report.getId());
                if (Objects.nonNull(reportDb)) {
                    report.setReportDbId(reportDb.getId());
                }
            }
        }
        return R.ok(list);
    }

//
//    @SysLog("报表设计表add")
//    @PostMapping("/add")
//    @Operation(summary = "报表设计表add")
//    public R add(@RequestBody Report report) {
//        return R.ok(reportService.save(report));
//    }

    @SysLog("报表设计表edit")
    @PostMapping("/edit")
    @Operation(summary = "报表设计表edit")
    public R<Boolean> edit(@RequestBody Report report) {
        return R.ok(reportService.createOrEdit(report));
    }

    @SysLog("报表设计表del")
    @PostMapping("/del/{id}")
    @Operation(summary = "报表设计表del")
    public R del(@PathVariable Long id) {
        return R.ok(reportService.removeById(id));
    }

    @SysLog("报表设计表delBatch")
    @PostMapping("/delBatch/{ids}")
    @Operation(summary = "报表设计表delBatch 1,2,3")
    public R delBatch(@PathVariable String ids) {
        return R.ok(reportService.removeBatchByIds(Arrays.asList(ids.split(","))));
    }

    @PostMapping("/reportData")
    @Operation(summary = "报表数据")
    public R<ReportTableDataVo> reportData(@RequestBody ReportDataDto dto) {
        return R.ok(reportService.reportData(dto));
    }

    @PostMapping("/rowConvert2SonParams")
    @Operation(summary = "父子报表入参转换")
    public R<SonReportParamVo> rowConvert2SonParams(@RequestBody RowConvert2SonParamsDto dto) {
        return R.ok(reportService.rowConvert2SonParams(dto));
    }

    @PostMapping("/fieldUseFormula")
    @Operation(summary = "字段使用公式")
    public R<ReportFieldFormula> fieldUseFormula(@RequestBody FieldUseFormulaDto dto) {
        return R.ok(reportService.fieldUseFormula(dto));
    }

    @PostMapping("/copy")
    @Operation(summary = "复制报表")
    public R<?> copy(@RequestBody ReportCopyDto dto) {
        reportCopyService.copy(dto);
        return R.ok();
    }

    @GetMapping("/check/{id}")
    @Operation(summary = "校验报表")
    public R<?> check(@PathVariable Long id) {
        reportCheckService.check(id);
        return R.ok();
    }

    @PostMapping("/reportDataExport")
    @Operation(summary = "报表数据导出")
    public void reportDataExport(@RequestBody ReportDataDto dto,  HttpServletResponse response) {
        reportExportService.reportDataExport(dto, response);
    }

    @Inner(value = false)
    @PostMapping("/cellData")
    @Operation(summary = "单元格值")
    public R<ReportCellDataVo> cellData(@RequestBody ReportCellDataDto dto) {
        return R.ok(reportService.cellData(dto));
    }


    @Inner(value = false)
    @PostMapping("/cellDataList")
    @Operation(summary = "单元格值")
    public R<Map<String,ReportCellDataVo>> cellDataList(@RequestBody ReportCellDataDto dto) {
        return R.ok(reportService.cellDataList(dto));
    }
}