package com.hscloud.hs.cost.account.controller.kpi;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hscloud.hs.cost.account.model.dto.kpi.*;
import com.hscloud.hs.cost.account.model.entity.base.PageRequest;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiImputation;
import com.hscloud.hs.cost.account.service.kpi.IKpiImputationService;
import com.pig4cloud.pigx.common.core.util.R;
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
 * 归集表
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/kpi/kpiImputation")
@Tag(name = "kpiImputation", description = "*归集管理")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class KpiImputationController {

    private final IKpiImputationService kpiImputationService;

    @SysLog("归集表info")
    @GetMapping("/info/{id}")
    @Operation(summary = "归集表info")
    public R<KpiImputation> info(@PathVariable Long id) {
        return R.ok(kpiImputationService.getById(id));
    }

    @SysLog("归集表page")
    @GetMapping("/page")
    @Operation(summary = "归集表page")
    public R<IPage<KpiImputation>> page(PageRequest<KpiImputation> pr) {
        return R.ok(kpiImputationService.page(pr.getPage(), pr.getWrapper()));
    }

    @SysLog("归集科室单元page")
    @GetMapping("/page_list")
    @Operation(summary = "*归集科室单元page")
    public R<IPage<KpiImputationDeptDto>> page(KpiImputationSearchDto dto) {
        return R.ok(kpiImputationService.pageImputationDeptUnit(dto));
    }

    @SysLog("归集表list")
    @GetMapping("/list")
    @Operation(summary = "归集表list")
    public R<List<KpiImputation>> list(PageRequest<KpiImputation> pr) {
        return R.ok(kpiImputationService.list(pr.getWrapper()));
    }

    @SysLog("归集表add")
    @PostMapping("/add")
    @Operation(summary = "归集表add")
    public R add(@RequestBody KpiImputation kpiImputation) {
        return R.ok(kpiImputationService.save(kpiImputation));
    }


    @SysLog("归集新增特殊规则")
    @PostMapping("/addRule")
    @Operation(summary = "*归集新增特殊规则")
    public R addRule(@RequestBody KpiImputationAddDto kpiImputation) {
        return R.ok(kpiImputationService.addRule(kpiImputation));
    }

    @SysLog("归集表list")
    @GetMapping("/rule_list")
    @Operation(summary = "*归集规则表list")
    public R<List<KpiImputationListDto>> Rulelist(KpiImputationListSearchDto dto) {
        return R.ok(kpiImputationService.listImputation(dto));
    }

    @SysLog("归集规则表del")
    @PostMapping("/delRule/{id}")
    @Operation(summary = "*归集规则表del")
    public R delRule(@PathVariable Long id) {
        kpiImputationService.removeRule(id);
        return R.ok();
    }

    @SysLog("归集刷新")
    @PostMapping("/refresh")
    @Operation(summary = "*归集刷新")
    public R refresh(@RequestBody KpiImputationListSearchDto dto) {
        kpiImputationService.refresh(dto.getCategoryCode(), dto.getPeriod(), dto.getBusiType());
        return R.ok();
    }


    @SysLog("归集表edit")
    @PostMapping("/edit")
    @Operation(summary = "归集表edit")
    public R edit(@RequestBody KpiImputation kpiImputation) {
        return R.ok(kpiImputationService.updateById(kpiImputation));
    }

    @SysLog("归集表del")
    @PostMapping("/del/{id}")
    @Operation(summary = "归集表del")
    public R del(@PathVariable Long id) {
        return R.ok(kpiImputationService.removeById(id));
    }

    @SysLog("归集表delBatch")
    @PostMapping("/delBatch/{ids}")
    @Operation(summary = "归集表delBatch 1,2,3")
    public R delBatch(@PathVariable String ids) {
        return R.ok(kpiImputationService.removeBatchByIds(Arrays.asList(ids.split(","))));
    }
}