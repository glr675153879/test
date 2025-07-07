package com.hscloud.hs.cost.account.controller.kpi;

import com.hscloud.hs.cost.account.model.dto.kpi.KpiAccountUnitQueryDTO;
import com.hscloud.hs.cost.account.model.dto.kpi.KpiItemBatchCalculateDTO;
import com.hscloud.hs.cost.account.model.dto.kpi.KpiItemCoefficientDTO;
import com.hscloud.hs.cost.account.model.dto.kpi.KpiItemEquivalentDTO;
import com.hscloud.hs.cost.account.model.vo.kpi.KpiAccountUnitVO;
import com.hscloud.hs.cost.account.model.vo.kpi.KpiItemCoefficientVO;
import com.hscloud.hs.cost.account.model.vo.kpi.KpiItemEquivalentVO;
import com.hscloud.hs.cost.account.service.impl.kpi.KpiItemEquivalentCalculateService;
import com.hscloud.hs.cost.account.service.kpi.IKpiItemEquivalentService;
import com.pig4cloud.pigx.common.core.util.R;
import com.pig4cloud.pigx.common.log.annotation.SysLog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/kpi/kpiItemEquivalent")
@Tag(name = "k_核算项当量", description = "核算项当量")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class KpiItemEquivalentController {
    private final IKpiItemEquivalentService kpiItemEquivalentService;
    private final KpiItemEquivalentCalculateService kpiItemEquivalentCalculateService;

    @SysLog("列表（绩效办）")
    @GetMapping("/list")
    @Operation(summary = "*列表（绩效办）")
    public R<List<KpiItemEquivalentVO>> list(KpiItemEquivalentDTO dto) {
        return R.ok(kpiItemEquivalentService.getList(dto, true));
    }

    @SysLog("列表（科室核验）")
    @GetMapping("/list/unit")
    @Operation(summary = "*列表（科室核验）")
    public R<List<KpiItemEquivalentVO>> listByUnit(KpiItemEquivalentDTO dto) {
        return R.ok(kpiItemEquivalentService.getList(dto, false));
    }

    @SysLog("数据调取")
    @PostMapping("/acquire")
    @Operation(summary = "*数据调取")
    public R equivalentAcquire(@RequestBody KpiItemBatchCalculateDTO dto) {
        kpiItemEquivalentCalculateService.eqItemBatchCalculate(dto);
        return R.ok();
    }

    @SysLog("核算单元列表（当量数据统计）")
    @GetMapping("/unit/list")
    @Operation(summary = "*核算单元列表（当量数据统计）")
    public R<List<KpiAccountUnitVO>> getEquivalentUnitList(KpiAccountUnitQueryDTO dto, Long period) {
        return R.ok(kpiItemEquivalentService.getEquivalentUnitList(dto, period));
    }

    @SysLog("当量锁定/解锁")
    @GetMapping("/lock/{period}")
    @Operation(summary = "*当量锁定/解锁")
    public R lock(@PathVariable Long period) {
        kpiItemEquivalentService.lock(period);
        return R.ok();
    }

    @SysLog("获取上月系数")
    @GetMapping("/coefficient/list")
    @Operation(summary = "*按周期获取核算项系数")
    public R<List<KpiItemCoefficientVO>> getCoefficientList(@RequestParam Long accountUnitId,
                                                            @RequestParam Long period,
                                                            @RequestParam Long itemId) {
        return R.ok(kpiItemEquivalentService.getCoefficientList(accountUnitId, period, itemId));
    }
}