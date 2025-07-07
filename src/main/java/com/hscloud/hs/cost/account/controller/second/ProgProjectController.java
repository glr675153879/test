package com.hscloud.hs.cost.account.controller.second;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hscloud.hs.cost.account.model.entity.base.PageRequest;
import com.hscloud.hs.cost.account.model.entity.second.ProgProject;
import com.hscloud.hs.cost.account.model.entity.second.Programme;
import com.hscloud.hs.cost.account.service.second.IProgProjectService;
import com.hscloud.hs.cost.account.service.second.IProgrammeService;
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
* 方案核算指标
*
*/
@RestController
@RequiredArgsConstructor
@RequestMapping("/second/progProject")
@Tag(name = "progProject", description = "方案核算指标")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class ProgProjectController {

    private final IProgProjectService progProjectService;
    private final IProgrammeService programmeService;

    @SysLog("方案核算指标info")
    @GetMapping("/info/{id}")
    @Operation(summary = "方案核算指标info")
    public R<ProgProject> info(@PathVariable Long id) {
        return R.ok(progProjectService.getById(id));
    }

    @SysLog("方案核算指标page")
    @GetMapping("/page")
    @Operation(summary = "方案核算指标page")
    public R<IPage<ProgProject>> page(PageRequest<ProgProject> pr) {
        return R.ok(progProjectService.page(pr.getPage(),pr.getWrapper()));
    }

    @SysLog("方案核算指标list")
    @GetMapping("/list")
    @Operation(summary = "方案核算指标list")
    public R<List<ProgProject>> list(PageRequest<ProgProject> pr) {
        return R.ok(progProjectService.list(pr.getWrapper()));
    }

    @SysLog("方案核算指标list")
    @GetMapping("/listByGrantUnitId/{grantUnitId}")
    @Operation(summary = "方案核算指标list")
    public R<List<ProgProject>> list(@PathVariable Long grantUnitId,PageRequest<ProgProject> pr) {
        Programme programme = programmeService.getByUnitId(grantUnitId);
        return R.ok(progProjectService.list(pr.getWrapper().eq("programme_id",programme.getId())));
    }


    @SysLog("方案核算指标add")
    @PostMapping("/add")
    @Operation(summary = "方案核算指标add")
    public R add(@RequestBody ProgProject progProject)  {
        return R.ok(progProjectService.save(progProject));
    }

    @SysLog("方案核算指标edit")
    @PostMapping("/edit")
    @Operation(summary = "方案核算指标edit")
    public R edit(@RequestBody ProgProject progProject)  {
        return R.ok(progProjectService.updateById(progProject));
    }

    @SysLog("方案核算指标del")
    @PostMapping("/del/{id}")
    @Operation(summary = "方案核算指标del")
    public R del(@PathVariable Long id)  {
        return R.ok(progProjectService.removeById(id));
    }

    @SysLog("方案核算指标delBatch")
    @PostMapping("/delBatch/{ids}")
    @Operation(summary = "方案核算指标delBatch 1,2,3")
    public R delBatch(@PathVariable String ids)  {
        return R.ok(progProjectService.removeBatchByIds(Arrays.asList(ids.split(","))));
    }
}