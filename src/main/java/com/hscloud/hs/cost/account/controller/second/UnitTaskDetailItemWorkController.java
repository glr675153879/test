package com.hscloud.hs.cost.account.controller.second;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hscloud.hs.cost.account.model.entity.base.PageRequest;
import com.hscloud.hs.cost.account.model.entity.second.UnitTaskDetailItemWork;
import com.hscloud.hs.cost.account.service.second.IUnitTaskDetailItemWorkService;
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
* 任务科室二次分配工作量系数
*
*/
@RestController
@RequiredArgsConstructor
@RequestMapping("/second/unitTaskDetailItemWork")
@Tag(name = "unitTaskDetailItemWork", description = "任务科室二次分配工作量系数")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class UnitTaskDetailItemWorkController {

    private final IUnitTaskDetailItemWorkService unitTaskDetailItemWorkService;

    @SysLog("任务科室二次分配工作量系数info")
    @GetMapping("/info/{id}")
    @Operation(summary = "任务科室二次分配工作量系数info")
    public R<UnitTaskDetailItemWork> info(@PathVariable Long id) {
        return R.ok(unitTaskDetailItemWorkService.getById(id));
    }

    @SysLog("任务科室二次分配工作量系数page")
    @GetMapping("/page")
    @Operation(summary = "任务科室二次分配工作量系数page")
    public R<IPage<UnitTaskDetailItemWork>> page(PageRequest<UnitTaskDetailItemWork> pr) {
        return R.ok(unitTaskDetailItemWorkService.page(pr.getPage(),pr.getWrapper()));
    }

    @SysLog("任务科室二次分配工作量系数list")
    @GetMapping("/list")
    @Operation(summary = "任务科室二次分配工作量系数list")
    public R<List<UnitTaskDetailItemWork>> list(PageRequest<UnitTaskDetailItemWork> pr) {
        return R.ok(unitTaskDetailItemWorkService.list(pr.getWrapper()));
    }

    @SysLog("任务科室二次分配工作量系数add")
    @PostMapping("/add")
    @Operation(summary = "任务科室二次分配工作量系数add")
    public R add(@RequestBody UnitTaskDetailItemWork unitTaskDetailItemWork)  {
        return R.ok(unitTaskDetailItemWorkService.save(unitTaskDetailItemWork));
    }

    @SysLog("任务科室二次分配工作量系数edit")
    @PostMapping("/edit")
    @Operation(summary = "任务科室二次分配工作量系数edit")
    public R edit(@RequestBody UnitTaskDetailItemWork unitTaskDetailItemWork)  {
        return R.ok(unitTaskDetailItemWorkService.updateById(unitTaskDetailItemWork));
    }

    @SysLog("任务科室二次分配工作量系数del")
    @PostMapping("/del/{id}")
    @Operation(summary = "任务科室二次分配工作量系数del")
    public R del(@PathVariable Long id)  {
        return R.ok(unitTaskDetailItemWorkService.removeById(id));
    }

    @SysLog("任务科室二次分配工作量系数delBatch")
    @PostMapping("/delBatch/{ids}")
    @Operation(summary = "任务科室二次分配工作量系数delBatch 1,2,3")
    public R delBatch(@PathVariable String ids)  {
        return R.ok(unitTaskDetailItemWorkService.removeBatchByIds(Arrays.asList(ids.split(","))));
    }
}