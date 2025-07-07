package com.hscloud.hs.cost.account.controller;


import com.hscloud.hs.cost.account.model.dto.AccountStatementDetailQueryDto;
import com.hscloud.hs.cost.account.model.dto.CostAccountStatementQueryDto;
import com.hscloud.hs.cost.account.model.vo.*;
import com.hscloud.hs.cost.account.service.CostAccountStatementService;
import com.pig4cloud.pigx.common.core.util.R;
import com.pig4cloud.pigx.common.excel.annotation.ResponseExcel;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/account/statement")
@Tag(name = "报表中心", description = "statement")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
@RequiredArgsConstructor
public class CostAccountStatementController {

    @Autowired
    private CostAccountStatementService costAccountStatementService;


    @GetMapping("/result/list")
    @Operation(summary = "绩效结果报表")
    public R getResultPage(CostAccountStatementQueryDto queryDto) {
        return R.ok(costAccountStatementService.resultStatementPage(queryDto));
    }

    @GetMapping("/result/details")
    @Operation(summary = "绩效结果详情")
    public R getResultDetail(AccountStatementDetailQueryDto queryDto) {
        return R.ok(costAccountStatementService.resultStatementDetail(queryDto));
    }

    @ResponseExcel
    @GetMapping("/result/export")
    @Operation(summary = "绩效结果导出")
    public List<ResultExcelVo> exportResult() {
        return costAccountStatementService.exportResult();
    }

    @GetMapping("/group/list")
    @Operation(summary = "绩效分组报表")
    public R getGroupPage(CostAccountStatementQueryDto queryDto) {
        return R.ok(costAccountStatementService.groupStatementPage(queryDto));
    }

    @GetMapping("/group/details")
    @Operation(summary = "绩效分组详情")
    public R getGroupDetail(AccountStatementDetailQueryDto queryDto) {
        return R.ok(costAccountStatementService.groupStatementDetail(queryDto));
    }

    @ResponseExcel
    @GetMapping("/group/export")
    @Operation(summary = "绩效分组导出")
    public List<GroupExcelVo> exportGroup() {
        return costAccountStatementService.exportGroup();
    }

    @GetMapping("/unit/list")
    @Operation(summary = "核算单元报表")
    public R getUnitPage(CostAccountStatementQueryDto queryDto) {
        return R.ok(costAccountStatementService.unitStatementPage(queryDto));
    }

    @GetMapping("/unit/details")
    @Operation(summary = "核算单元详情")
    public R getUnitDetail(AccountStatementDetailQueryDto queryDto) {
        return R.ok(costAccountStatementService.unitStatementDetail(queryDto));
    }

    @ResponseExcel
    @GetMapping("/unit/export")
    @Operation(summary = "核算单元导出")
    public List<UnitExcelVo> exportUnit() {
        return costAccountStatementService.exportUnit();
    }

    @GetMapping("/index/list")
    @Operation(summary = "核算指标报表")
    public R getIndexPage(CostAccountStatementQueryDto queryDto) {
        return R.ok(costAccountStatementService.indexStatementPage(queryDto));
    }

    @GetMapping("/index/details")
    @Operation(summary = "核算指标详情")
    public R getIndexDetail(AccountStatementDetailQueryDto queryDto) {
        return R.ok(costAccountStatementService.indexStatementDetail(queryDto));
    }

    @ResponseExcel
    @GetMapping("/index/export")
    @Operation(summary = "核算指标导出")
    public List<IndexExcelVo> exportIndex() {
        return costAccountStatementService.exportIndex();
    }

    @GetMapping("/unit/item")
    @Operation(summary = "核算值分布")
    public R getItemDetail(AccountStatementDetailQueryDto queryDto) {
        return R.ok(costAccountStatementService.itemStatementDetail(queryDto));
    }

    @GetMapping("/test")
    @Operation(summary = "后端用")
    public R getResultPage(@RequestBody CostResultStatementVo queryDto) {
        return R.ok();
    }
}
