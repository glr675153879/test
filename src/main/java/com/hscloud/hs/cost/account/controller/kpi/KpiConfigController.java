package com.hscloud.hs.cost.account.controller.kpi;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hscloud.hs.cost.account.model.dto.PageDto;
import com.hscloud.hs.cost.account.model.dto.kpi.KpiConfigSearchDto;
import com.hscloud.hs.cost.account.model.vo.kpi.KpiConfigVO;
import com.hscloud.hs.cost.account.service.kpi.IKpiConfigService;
import com.pig4cloud.pigx.common.core.util.R;
import com.pig4cloud.pigx.common.log.annotation.SysLog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * 配置表
 *
 * @author Administrator
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/kpi/kpiConfig")
@Tag(name = "k_配置表", description = "配置表")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class KpiConfigController {

    private final IKpiConfigService kpiConfigService;

    @GetMapping("/info")
    @Operation(summary = "*配置表info")
    public R<KpiConfigVO> info(KpiConfigSearchDto dto) {
        return R.ok(kpiConfigService.getConfig(dto));
    }

    @GetMapping("/page")
    @Operation(summary = "*配置表page")
    public R<IPage<KpiConfigVO>> page(PageDto dto, String allFlag) {
        return R.ok(kpiConfigService.getPage(dto, allFlag));
    }

    @GetMapping("/list")
    @Operation(summary = "*配置表list")
    public R<List<KpiConfigVO>> list() {
        return R.ok(kpiConfigService.getList());
    }

    @SysLog("配置表add")
    @PostMapping("/add/{period}")
    @Operation(summary = "*配置表add")
    public R<Long> add(@PathVariable Long period) {
        return R.ok(kpiConfigService.saveLastCycle(period, false, "1"));
    }

    @SysLog("配置表edit")
    @PostMapping("/edit")
    @Operation(summary = "*配置表edit")
    public R<Long> edit(@RequestBody KpiConfigSearchDto dto) {
        return R.ok(kpiConfigService.saveLastCycle(dto.getPeriod(), true, dto.getType()));
    }

    @SysLog("配置表del")
    @PostMapping("/del/{id}")
    @Operation(summary = "*配置表del")
    public R<Boolean> del(@PathVariable Long id) {
        return R.ok(kpiConfigService.removeById(id));
    }

    @GetMapping("/lastCycle")
    @Operation(summary = "*获取最新的配置周期")
    public R<String> getLastCycle() {
        return R.ok(kpiConfigService.getLastCycle(true));
    }

    @GetMapping("/lastCycle/info")
    @Operation(summary = "*获取最新的配置周期记录")
    public R<KpiConfigVO> getLastCycleInfo(KpiConfigSearchDto dto) {
        return R.ok(kpiConfigService.getLastCycleInfo(dto));
    }

    @Operation(summary = "*设置当量单价")
    @PostMapping("/edit/equivalentPrice")
    public R<Boolean> editEquivalentPrice(@RequestParam Long period, @RequestParam(required = false) BigDecimal equivalentPrice) {
        kpiConfigService.editEquivalentPrice(period, equivalentPrice);
        return R.ok();
    }

}