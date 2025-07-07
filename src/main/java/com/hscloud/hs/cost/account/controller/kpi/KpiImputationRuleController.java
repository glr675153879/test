package com.hscloud.hs.cost.account.controller.kpi;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hscloud.hs.cost.account.model.entity.base.PageRequest;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiImputationRule;
import com.hscloud.hs.cost.account.service.kpi.IKpiImputationRuleService;
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
* 归集规则表
*
*/
@RestController
@RequiredArgsConstructor
@RequestMapping("/kpi/kpiImputationRule")
@Tag(name = "kpiImputationRule", description = "归集规则表")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class KpiImputationRuleController {

    private final IKpiImputationRuleService kpiImputationRuleService;

    @SysLog("归集规则表info")
    @GetMapping("/info/{id}")
    @Operation(summary = "归集规则表info")
    public R<KpiImputationRule> info(@PathVariable Long id) {
        return R.ok(kpiImputationRuleService.getById(id));
    }

    @SysLog("归集规则表page")
    @GetMapping("/page")
    @Operation(summary = "归集规则表page")
    public R<IPage<KpiImputationRule>> page(PageRequest<KpiImputationRule> pr) {
        return R.ok(kpiImputationRuleService.page(pr.getPage(),pr.getWrapper()));
    }

    @SysLog("归集规则表list")
    @GetMapping("/list")
    @Operation(summary = "归集规则表list")
    public R<List<KpiImputationRule>> list(PageRequest<KpiImputationRule> pr) {
        return R.ok(kpiImputationRuleService.list(pr.getWrapper()));
    }

    @SysLog("归集规则表add")
    @PostMapping("/add")
    @Operation(summary = "归集规则表add")
    public R add(@RequestBody KpiImputationRule kpiImputationRule)  {
        return R.ok(kpiImputationRuleService.save(kpiImputationRule));
    }

    @SysLog("归集规则表edit")
    @PostMapping("/edit")
    @Operation(summary = "归集规则表edit")
    public R edit(@RequestBody KpiImputationRule kpiImputationRule)  {
        return R.ok(kpiImputationRuleService.updateById(kpiImputationRule));
    }

    @SysLog("归集规则表del")
    @PostMapping("/del/{id}")
    @Operation(summary = "归集规则表del")
    public R del(@PathVariable Long id)  {
        return R.ok(kpiImputationRuleService.removeById(id));
    }

    @SysLog("归集规则表delBatch")
    @PostMapping("/delBatch/{ids}")
    @Operation(summary = "归集规则表delBatch 1,2,3")
    public R delBatch(@PathVariable String ids)  {
        return R.ok(kpiImputationRuleService.removeBatchByIds(Arrays.asList(ids.split(","))));
    }
}