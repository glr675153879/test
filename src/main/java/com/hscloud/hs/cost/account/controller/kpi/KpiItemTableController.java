package com.hscloud.hs.cost.account.controller.kpi;

import com.hscloud.hs.cost.account.model.dto.kpi.KpiItemTableDto;
import com.hscloud.hs.cost.account.model.entity.base.PageRequest;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiItemTable;
import com.hscloud.hs.cost.account.service.kpi.IKpiItemTableService;
import com.pig4cloud.pigx.common.core.util.R;
import com.pig4cloud.pigx.common.log.annotation.SysLog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping("/kpi/kpiItemTable")
@Tag(name = "k_item_table", description = "核算项基础表")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class KpiItemTableController {
    private final IKpiItemTableService kpiItemTableService;

    @SysLog("核算项基础表add")
    @PostMapping("/saveOrUpdate")
    @Operation(summary = "*核算项基础表add")
    public R saveOrUpdate(@RequestBody KpiItemTableDto dto) {
        return R.ok(kpiItemTableService.saveOrUpdate(dto));
    }

    @SysLog("核算项基础表删除")
    @PostMapping("/del/{id}")
    @Operation(summary = "*核算项基础表删除")
    public R del(@PathVariable Long id) {
        return R.ok(kpiItemTableService.removeById(id));
    }

    @SysLog("核算项基础表列表")
    @PostMapping("/list")
    @Operation(summary = "*核算项基础表列表")
    public R<List<KpiItemTable>> list(PageRequest<KpiItemTable> pr) {
        return R.ok(kpiItemTableService.list(pr.getWrapper()));
    }
}
