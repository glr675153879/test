package com.hscloud.hs.cost.account.controller.kpi;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hscloud.hs.cost.account.model.entity.base.PageRequest;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiDeptMap;
import com.hscloud.hs.cost.account.service.kpi.IKpiDeptMapService;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;

/**
* 科室映射
*
*/
@RestController
@RequiredArgsConstructor
@RequestMapping("/kpi/kpiDeptMap")
@Tag(name = "kpiDeptMap", description = "科室映射")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
@Slf4j
public class KpiDeptMapController {

    private final IKpiDeptMapService kpiDeptMapService;

    @SysLog("科室映射page")
    @GetMapping("/page")
    @Operation(summary = "科室映射page")
    public R<IPage<KpiDeptMap>> page(PageRequest<KpiDeptMap> pr) {
        return R.ok(kpiDeptMapService.pageList(pr.getPage(),pr.getWrapper()));
    }

    @SysLog("科室映射cu")
    @PostMapping("/cu")
    @Operation(summary = "科室映射cu")
    public R add(@RequestBody KpiDeptMap kpiDeptMap)  {
        kpiDeptMapService.cu(kpiDeptMap);
        return R.ok();
    }

    @SysLog("科室映射delBatch")
    @PostMapping("/delBatch/{ids}")
    @Operation(summary = "科室映射delBatch 1,2,3")
    public R delBatch(@PathVariable String ids)  {
        return R.ok(kpiDeptMapService.removeBatchByIds(Arrays.asList(ids.split(","))));
    }
}