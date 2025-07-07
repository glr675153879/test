package com.hscloud.hs.cost.account.controller;

import com.hscloud.hs.cost.account.model.dto.CostAccountStatementQueryNewDto;
import com.hscloud.hs.cost.account.model.entity.DistributionStatementTargetValue;
import com.hscloud.hs.cost.account.service.IDistributionAccountStatementService;
import com.pig4cloud.pigx.common.core.util.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

/**
 * @author 小小w
 * @date 2023/11/30 15:49
 */

@RestController
@RequestMapping("/distribution/statement")
@Tag(name = "绩效报表中心", description = "statement")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
@RequiredArgsConstructor
public class DistributionAccountStatementController {
    @Autowired
    private IDistributionAccountStatementService distributionAccountStatementService;

    /**
     * 新增目标值
     */
    @PostMapping("/add/targetValue")
    public R addTargetValue(@RequestBody DistributionStatementTargetValue distributionStatementTargetValue) {
        distributionAccountStatementService.addTargetValue(distributionStatementTargetValue);
        return R.ok();
    }

    /**
     * 医院结果绩效
     * @param queryDto
     * @return
     */
    @GetMapping("/hospital/list")
    @Operation(summary = "医院结果绩效报表")
    public R getHospitalPage(CostAccountStatementQueryNewDto queryDto) {
        return R.ok(distributionAccountStatementService.hospitalStatementPage(queryDto));
    }

    /**
     * 医院结果绩效报表趋势
     * @param queryDto
     * @return
     */
    @GetMapping("/hospital/tendency")
    @Operation(summary = "医院结果绩效报表趋势")
    public R getHospitalTendency(CostAccountStatementQueryNewDto queryDto) {
        return R.ok(distributionAccountStatementService.getHospitalTendency(queryDto));
    }

    /**
     * 医院结果绩效报表详情
     * @param
     * @return
     */
    @GetMapping("/hospital/details")
    @Operation(summary = "医院结果绩效报表详情")
    public R getHospitalDetail(String accountTime) {
        return R.ok(distributionAccountStatementService.getHospitalDetail(accountTime));
    }

    /**
     * * 医院结果绩效报表饼状图
     * @param accountTime
     * @return
     */
    @GetMapping("/hospital/figure")
    @Operation(summary = "医院结果绩效报表饼状图")
    public R getHospitalFigure(String accountTime) {
        return R.ok(distributionAccountStatementService.getHospitalFigure(accountTime));
    }
    /**
     * 科室结果绩效
     * @param queryDto
     * @return
     */
    @GetMapping("/unit/list")
    @Operation(summary = "科室结果绩效报表")
    public R getUnitPage(CostAccountStatementQueryNewDto queryDto) {
        return R.ok(distributionAccountStatementService.unitStatementPage(queryDto));
    }

    @GetMapping("/unit/total")
    @Operation(summary = "科室结果绩效报表总核算值汇总")
    public R getUnitTotalCount(CostAccountStatementQueryNewDto queryDto) {
        return R.ok(distributionAccountStatementService.getUnitTotalCount(queryDto));
    }

    /**
     * 科室结果绩效报表趋势
     * @param queryDto
     * @return
     */
    @GetMapping("/unit/tendency")
    @Operation(summary = "科室结果绩效报表趋势")
    public R getUnitTendency(CostAccountStatementQueryNewDto queryDto) {
        return R.ok(distributionAccountStatementService.getUnitTendency(queryDto));
    }
}
