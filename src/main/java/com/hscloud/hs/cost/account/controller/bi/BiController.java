package com.hscloud.hs.cost.account.controller.bi;

import com.hscloud.hs.cost.account.model.dto.bi.IncomePerformancePayDTO;
import com.hscloud.hs.cost.account.model.dto.bi.MultiDeptSingleMonthDataDTO;
import com.hscloud.hs.cost.account.model.dto.bi.SimpleDataDTO;
import com.hscloud.hs.cost.account.model.dto.bi.SingleDeptMultiMonthDataDTO;
import com.hscloud.hs.cost.account.model.vo.bi.HospitalFigureVo;
import com.hscloud.hs.cost.account.model.vo.bi.MultiDeptSingleMonthDataDataVo;
import com.hscloud.hs.cost.account.model.vo.bi.SimpleDataVo;
import com.hscloud.hs.cost.account.model.vo.bi.SingleDeptMultiMonthDataVo;
import com.hscloud.hs.cost.account.service.impl.bi.BiService;
import com.pig4cloud.pigx.common.core.util.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

/**
 * 大屏接口
 */
@RestController("BiDataController")
@RequiredArgsConstructor
@RequestMapping("/bi/data")
@Tag(name = "大屏接口", description = "大屏接口")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class BiController {

    private final BiService biService;

    @PostMapping("/simpleData")
    @Operation(summary = "简单报表数据")
    public R<SimpleDataVo> simpleData(@RequestBody SimpleDataDTO dto) {
        return R.ok(biService.simpleData(dto));
    }

    @PostMapping("/multiDeptSingleMonthData")
    @Operation(summary = "多科室单月环比报表")
    public R<MultiDeptSingleMonthDataDataVo> multiDeptSingleMonthData(@RequestBody MultiDeptSingleMonthDataDTO dto) {
        return R.ok(biService.multiDeptSingleMonthData(dto));
    }

    @PostMapping("/singleDeptMultiMonthData")
    @Operation(summary = "单科室多月环比报表")
    public R<SingleDeptMultiMonthDataVo> simpleData(@RequestBody SingleDeptMultiMonthDataDTO dto) {
        return R.ok(biService.singleDeptMultiMonthData(dto));
    }

    @GetMapping("/hospital/figure")
    @Operation(summary = "医院结果绩效报表饼状图")
    public R<HospitalFigureVo> getHospitalFigure(String accountTime) {
        return R.ok(biService.getHospitalFigureQoq(accountTime));
    }

    @GetMapping("/performView")
    @Operation(summary = "绩效总览")
    public R<IncomePerformancePayDTO> performView(String accountTime) {
        return R.ok(biService.performView(accountTime));
    }

}