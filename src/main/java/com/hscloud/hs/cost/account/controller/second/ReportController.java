package com.hscloud.hs.cost.account.controller.second;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hscloud.hs.cost.account.model.dto.MingBeiExcel;
import com.hscloud.hs.cost.account.model.dto.second.RepotHulijxValueDTO;
import com.hscloud.hs.cost.account.model.dto.second.RepotZhigongjxValueDTO;
import com.hscloud.hs.cost.account.model.dto.second.RepotZhigongjxflValueDTO;
import com.hscloud.hs.cost.account.service.second.IReportService;
import com.hscloud.hs.cost.account.utils.DmoUtil;
import com.pig4cloud.pigx.common.core.exception.BizException;
import com.pig4cloud.pigx.common.core.util.R;
import com.pig4cloud.pigx.common.excel.annotation.ResponseExcel;
import com.pig4cloud.pigx.common.excel.annotation.Sheet;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;

/**
 * @author 小小w
 * @date 2024/3/9 11:42
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/second/report")
@Tag(name = "report", description = "报表")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class ReportController {
    private final IReportService reportService;
    private final DmoUtil dmoUtil;

    @GetMapping("/zhigongjx")
    @Operation(summary = "职工绩效总览报表")
    public R zhigongjx(@RequestParam String cycle, @RequestParam String endCycle, String userName, Page page) {
        return R.ok(reportService.zhigongjxList(cycle, endCycle, userName, null, page));
    }

    @GetMapping("/sumZhigongjx")
    @Operation(summary = "职工绩效总览报表汇总")
    public R sumZhigongjx(@RequestParam String cycle, @RequestParam String endCycle, String userName) {
        return R.ok(reportService.sumZhigongjx(cycle, endCycle, userName));
    }

    @GetMapping("/hulijx")
    @Operation(summary = "护理绩效总览报表")
    public R hulijx(String cycle) {
        return R.ok(reportService.hulijxList(cycle));
    }

    /**
     * 职工绩效分类表
     *
     * @param cycle     周期
     * @param isMingBei 是否明贝 0-否 1-是
     */
    @GetMapping("/zhigongjxfl")
    @Operation(summary = "职工绩效分类表")
    public R zhigongjxfl(@RequestParam String cycle, @RequestParam String endCycle, String userName, String isMingBei, Page page) {
        return R.ok(reportService.zhigongjxflList(cycle, endCycle, userName, isMingBei, page));
    }

    @GetMapping("/sumZhigongjxfl")
    @Operation(summary = "职工绩效分类表汇总")
    public R sumZhigongjxfl(@RequestParam String cycle, @RequestParam String endCycle, String userName, String isMingBei) {
        return R.ok(reportService.sumZhigongjxfl(cycle, endCycle, userName, isMingBei));
    }


    @ResponseExcel(sheets = @Sheet(sheetName = "护理绩效总览"))
    @GetMapping("/exportHulijx")
    @Operation(summary = "导出 护理绩效总览报表")
    public List<RepotHulijxValueDTO> exportHulijx(String cycle) {
        return dmoUtil.hulijxList(cycle);
    }

    @ResponseExcel(sheets = @Sheet(sheetName = "职工绩效总览"))
    @GetMapping("/exportZhigongjx")
    @Operation(summary = "导出 职工绩效总览报表")
    public List<RepotZhigongjxValueDTO> exportZhigongjx(@RequestParam String cycle, @RequestParam String endCycle, String userName) {
        return reportService.exportZhigongjx(cycle, endCycle, userName);
    }

    @ResponseExcel(sheets = @Sheet(sheetName = "职工绩效分类表"))
    @GetMapping("/exportZhigongjxfl")
    @Operation(summary = "导出 职工绩效分类表")
    public List<RepotZhigongjxflValueDTO> exportZhigongjxfl(@RequestParam String cycle, @RequestParam String endCycle, String userName) {
        return reportService.exportZhigongjxfl(cycle, endCycle, userName);
    }

    @ResponseExcel(sheets = @Sheet(sheetName = "职工绩效（明贝）"))
    @GetMapping("/exportMingBei")
    @Operation(summary = "职工绩效（明贝）")
    public List<MingBeiExcel> exportMingBei(@RequestParam String cycle, @RequestParam String endCycle, String userName) {
        return reportService.exportMingBei(cycle, endCycle, userName);
    }

    @GetMapping("/lastCycle")
    @Operation(summary = "获取最新周期")
    public R lastCycle() {
        return R.ok(reportService.lastCycle());
    }

}
