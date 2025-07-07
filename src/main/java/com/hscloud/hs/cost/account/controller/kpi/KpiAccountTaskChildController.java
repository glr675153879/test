package com.hscloud.hs.cost.account.controller.kpi;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hscloud.hs.cost.account.model.entity.base.PageRequest;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiAccountTaskChild;
import com.hscloud.hs.cost.account.service.kpi.IKpiAccountTaskChildService;
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
* 核算任务表
*
*/
@RestController
@RequiredArgsConstructor
@RequestMapping("/kpi/kpiAccountTaskChild")
@Tag(name = "kpiAccountTaskChild", description = "核算任务表")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class KpiAccountTaskChildController {

    private final IKpiAccountTaskChildService kpiAccountTaskChildService;

    @SysLog("核算任务表info")
    @GetMapping("/info/{id}")
    @Operation(summary = "核算任务表info")
    public R<KpiAccountTaskChild> info(@PathVariable Long id) {
        return R.ok(kpiAccountTaskChildService.getById(id));
    }

    @SysLog("核算任务表page")
    @GetMapping("/page")
    @Operation(summary = "核算任务表page")
    public R<IPage<KpiAccountTaskChild>> page(PageRequest<KpiAccountTaskChild> pr) {
        return R.ok(kpiAccountTaskChildService.page(pr.getPage(),pr.getWrapper()));
    }

    @SysLog("核算任务表list")
    @GetMapping("/list")
    @Operation(summary = "核算任务表list")
    public R<List<KpiAccountTaskChild>> list(PageRequest<KpiAccountTaskChild> pr) {
        return R.ok(kpiAccountTaskChildService.list(pr.getWrapper()));
    }

    @SysLog("核算任务表add")
    @PostMapping("/add")
    @Operation(summary = "核算任务表add")
    public R add(@RequestBody KpiAccountTaskChild kpiAccountTaskChild)  {
        return R.ok(kpiAccountTaskChildService.save(kpiAccountTaskChild));
    }

    @SysLog("核算任务表edit")
    @PostMapping("/edit")
    @Operation(summary = "核算任务表edit")
    public R edit(@RequestBody KpiAccountTaskChild kpiAccountTaskChild)  {
        return R.ok(kpiAccountTaskChildService.updateById(kpiAccountTaskChild));
    }

    @SysLog("核算任务表del")
    @PostMapping("/del/{id}")
    @Operation(summary = "核算任务表del")
    public R del(@PathVariable Long id)  {
        return R.ok(kpiAccountTaskChildService.removeById(id));
    }

    @SysLog("核算任务表delBatch")
    @PostMapping("/delBatch/{ids}")
    @Operation(summary = "核算任务表delBatch 1,2,3")
    public R delBatch(@PathVariable String ids)  {
        return R.ok(kpiAccountTaskChildService.removeBatchByIds(Arrays.asList(ids.split(","))));
    }
}