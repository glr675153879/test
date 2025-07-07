package com.hscloud.hs.cost.account.controller.second;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hscloud.hs.cost.account.model.entity.base.PageRequest;
import com.hscloud.hs.cost.account.model.entity.second.UnitTaskDetailCount;
import com.hscloud.hs.cost.account.service.second.IUnitTaskDetailCountService;
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
* 科室二次分配detail结果按人汇总
*
*/
@RestController
@RequiredArgsConstructor
@RequestMapping("/second/unitTaskDetailCount")
@Tag(name = "unitTaskDetailCount", description = "科室二次分配detail结果按人汇总")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class UnitTaskDetailCountController {

    private final IUnitTaskDetailCountService unitTaskDetailCountService;

    @SysLog("科室二次分配detail结果按人汇总info")
    @GetMapping("/info/{id}")
    @Operation(summary = "科室二次分配detail结果按人汇总info")
    public R<UnitTaskDetailCount> info(@PathVariable Long id) {
        return R.ok(unitTaskDetailCountService.getById(id));
    }

    @SysLog("科室二次分配detail结果按人汇总page")
    @GetMapping("/page")
    @Operation(summary = "科室二次分配detail结果按人汇总page")
    public R<IPage<UnitTaskDetailCount>> page(PageRequest<UnitTaskDetailCount> pr) {
        return R.ok(unitTaskDetailCountService.page(pr.getPage(),pr.getWrapper()));
    }

    @SysLog("科室二次分配detail结果按人汇总list")
    @GetMapping("/list")
    @Operation(summary = "科室二次分配detail结果按人汇总list")
    public R<List<UnitTaskDetailCount>> list(PageRequest<UnitTaskDetailCount> pr) {
        return R.ok(unitTaskDetailCountService.list(pr.getWrapper()));
    }

    @SysLog("科室二次分配detail结果按人汇总add")
    @PostMapping("/add")
    @Operation(summary = "科室二次分配detail结果按人汇总add")
    public R add(@RequestBody UnitTaskDetailCount unitTaskDetailCount)  {
        return R.ok(unitTaskDetailCountService.save(unitTaskDetailCount));
    }

    @SysLog("科室二次分配detail结果按人汇总edit")
    @PostMapping("/edit")
    @Operation(summary = "科室二次分配detail结果按人汇总edit")
    public R edit(@RequestBody UnitTaskDetailCount unitTaskDetailCount)  {
        return R.ok(unitTaskDetailCountService.updateById(unitTaskDetailCount));
    }

    @SysLog("科室二次分配detail结果按人汇总del")
    @PostMapping("/del/{id}")
    @Operation(summary = "科室二次分配detail结果按人汇总del")
    public R del(@PathVariable Long id)  {
        return R.ok(unitTaskDetailCountService.removeById(id));
    }

    @SysLog("科室二次分配detail结果按人汇总delBatch")
    @PostMapping("/delBatch/{ids}")
    @Operation(summary = "科室二次分配detail结果按人汇总delBatch 1,2,3")
    public R delBatch(@PathVariable String ids)  {
        return R.ok(unitTaskDetailCountService.removeBatchByIds(Arrays.asList(ids.split(","))));
    }
}