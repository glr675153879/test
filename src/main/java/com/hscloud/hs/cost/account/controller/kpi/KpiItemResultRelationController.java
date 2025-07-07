package com.hscloud.hs.cost.account.controller.kpi;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hscloud.hs.cost.account.model.entity.base.PageRequest;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiItemResultRelation;
import com.hscloud.hs.cost.account.service.kpi.IKpiItemResultRelationService;
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
* 核算项结果匹配关系
*
*/
@RestController
@RequiredArgsConstructor
@RequestMapping("/kpi/kpiItemResultRelation")
@Tag(name = "kpiItemResultRelation", description = "核算项结果匹配关系")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class KpiItemResultRelationController {

    private final IKpiItemResultRelationService kpiItemResultRelationService;

    @SysLog("核算项结果匹配关系info")
    @GetMapping("/info/{id}")
    @Operation(summary = "核算项结果匹配关系info")
    public R<KpiItemResultRelation> info(@PathVariable Long id) {
        return R.ok(kpiItemResultRelationService.getById(id));
    }

    @SysLog("核算项结果匹配关系page")
    @GetMapping("/page")
    @Operation(summary = "核算项结果匹配关系page")
    public R<IPage<KpiItemResultRelation>> page(PageRequest<KpiItemResultRelation> pr) {
        return R.ok(kpiItemResultRelationService.page(pr.getPage(),pr.getWrapper()));
    }

    @SysLog("核算项结果匹配关系list")
    @GetMapping("/list")
    @Operation(summary = "核算项结果匹配关系list")
    public R<List<KpiItemResultRelation>> list(PageRequest<KpiItemResultRelation> pr) {
        return R.ok(kpiItemResultRelationService.list(pr.getWrapper()));
    }

    @SysLog("核算项结果匹配关系add")
    @PostMapping("/add")
    @Operation(summary = "核算项结果匹配关系add")
    public R add(@RequestBody KpiItemResultRelation kpiItemResultRelation)  {
        return R.ok(kpiItemResultRelationService.save(kpiItemResultRelation));
    }

    @SysLog("核算项结果匹配关系edit")
    @PostMapping("/edit")
    @Operation(summary = "核算项结果匹配关系edit")
    public R edit(@RequestBody KpiItemResultRelation kpiItemResultRelation)  {
        return R.ok(kpiItemResultRelationService.updateById(kpiItemResultRelation));
    }

    @SysLog("核算项结果匹配关系del")
    @PostMapping("/del/{id}")
    @Operation(summary = "核算项结果匹配关系del")
    public R del(@PathVariable Long id)  {
        return R.ok(kpiItemResultRelationService.removeById(id));
    }

    @SysLog("核算项结果匹配关系delBatch")
    @PostMapping("/delBatch/{ids}")
    @Operation(summary = "核算项结果匹配关系delBatch 1,2,3")
    public R delBatch(@PathVariable String ids)  {
        return R.ok(kpiItemResultRelationService.removeBatchByIds(Arrays.asList(ids.split(","))));
    }
}