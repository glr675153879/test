package com.hscloud.hs.cost.account.controller.second;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hscloud.hs.cost.account.model.entity.base.PageRequest;
import com.hscloud.hs.cost.account.model.entity.second.UnitTaskProject;
import com.hscloud.hs.cost.account.service.second.IUnitTaskProjectService;
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
* 任务核算指标
*
*/
@RestController
@RequiredArgsConstructor
@RequestMapping("/second/unitTaskProject")
@Tag(name = "unitTaskProject", description = "任务核算指标")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class UnitTaskProjectController {

    private final IUnitTaskProjectService unitTaskProjectService;

    @SysLog("任务核算指标info")
    @GetMapping("/info/{id}")
    @Operation(summary = "任务核算指标info")
    public R<UnitTaskProject> info(@PathVariable Long id) {
        return R.ok(unitTaskProjectService.getById(id));
    }

    @SysLog("任务核算指标page")
    @GetMapping("/page")
    @Operation(summary = "任务核算指标page")
    public R<IPage<UnitTaskProject>> page(PageRequest<UnitTaskProject> pr) {
        return R.ok(unitTaskProjectService.page(pr.getPage(),pr.getWrapper()));
    }

    @SysLog("任务核算指标list")
    @GetMapping("/list")
    @Operation(summary = "任务核算指标list")
    public R<List<UnitTaskProject>> list(PageRequest<UnitTaskProject> pr) {
        return R.ok(unitTaskProjectService.list(pr.getWrapper().orderByAsc("sort_num")));
    }

    @SysLog("任务核算指标add")
    @PostMapping("/add")
    @Operation(summary = "任务核算指标add")
    public R add(@RequestBody UnitTaskProject unitTaskProject)  {
        return R.ok(unitTaskProjectService.save(unitTaskProject));
    }

    @SysLog("任务核算指标edit")
    @PostMapping("/edit")
    @Operation(summary = "任务核算指标edit")
    public R edit(@RequestBody UnitTaskProject unitTaskProject)  {
        return R.ok(unitTaskProjectService.updateById(unitTaskProject));
    }

    @SysLog("任务核算指标del")
    @PostMapping("/del/{id}")
    @Operation(summary = "任务核算指标del")
    public R del(@PathVariable Long id)  {
        return R.ok(unitTaskProjectService.removeById(id));
    }

    @SysLog("任务核算指标delBatch")
    @PostMapping("/delBatch/{ids}")
    @Operation(summary = "任务核算指标delBatch 1,2,3")
    public R delBatch(@PathVariable String ids)  {
        return R.ok(unitTaskProjectService.removeBatchByIds(Arrays.asList(ids.split(","))));
    }
}