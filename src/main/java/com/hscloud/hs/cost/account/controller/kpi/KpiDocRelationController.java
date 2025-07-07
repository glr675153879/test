package com.hscloud.hs.cost.account.controller.kpi;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hscloud.hs.cost.account.model.entity.base.PageRequest;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiDocRelation;
import com.hscloud.hs.cost.account.service.kpi.IKpiDocRelationService;
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
* 医护关系
*
*/
@RestController
@RequiredArgsConstructor
@RequestMapping("/kpi/kpiDocRelation")
@Tag(name = "kpiDocRelation", description = "医护关系")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class KpiDocRelationController {

    private final IKpiDocRelationService kpiDocRelationService;

    @SysLog("医护关系info")
    @GetMapping("/info/{id}")
    @Operation(summary = "医护关系info")
    public R<KpiDocRelation> info(@PathVariable Long id) {
        return R.ok(kpiDocRelationService.getById(id));
    }

    @SysLog("医护关系page")
    @GetMapping("/page")
    @Operation(summary = "医护关系page")
    public R<IPage<KpiDocRelation>> page(PageRequest<KpiDocRelation> pr) {
        return R.ok(kpiDocRelationService.page(pr.getPage(),pr.getWrapper()));
    }

    @SysLog("医护关系list")
    @GetMapping("/list")
    @Operation(summary = "医护关系list")
    public R<List<KpiDocRelation>> list(PageRequest<KpiDocRelation> pr) {
        return R.ok(kpiDocRelationService.list(pr.getWrapper()));
    }

    @SysLog("医护关系add")
    @PostMapping("/add")
    @Operation(summary = "医护关系add")
    public R add(@RequestBody KpiDocRelation kpiDocRelation)  {
        return R.ok(kpiDocRelationService.save(kpiDocRelation));
    }

    @SysLog("医护关系edit")
    @PostMapping("/edit")
    @Operation(summary = "医护关系edit")
    public R edit(@RequestBody KpiDocRelation kpiDocRelation)  {
        return R.ok(kpiDocRelationService.updateById(kpiDocRelation));
    }

    @SysLog("医护关系del")
    @PostMapping("/del/{id}")
    @Operation(summary = "医护关系del")
    public R del(@PathVariable Long id)  {
        return R.ok(kpiDocRelationService.removeById(id));
    }

    @SysLog("医护关系delBatch")
    @PostMapping("/delBatch/{ids}")
    @Operation(summary = "医护关系delBatch 1,2,3")
    public R delBatch(@PathVariable String ids)  {
        return R.ok(kpiDocRelationService.removeBatchByIds(Arrays.asList(ids.split(","))));
    }
}