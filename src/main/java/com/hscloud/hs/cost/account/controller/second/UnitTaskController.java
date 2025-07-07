package com.hscloud.hs.cost.account.controller.second;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.hscloud.hs.cost.account.model.entity.base.PageRequest;
import com.hscloud.hs.cost.account.model.entity.second.GrantUnit;
import com.hscloud.hs.cost.account.model.entity.second.UnitTask;
import com.hscloud.hs.cost.account.model.vo.second.UnitTaskProjectCountVo;
import com.hscloud.hs.cost.account.service.second.IGrantUnitService;
import com.hscloud.hs.cost.account.service.second.IUnitTaskService;
import com.pig4cloud.pigx.common.core.util.R;
import com.pig4cloud.pigx.common.log.annotation.SysLog;
import com.pig4cloud.pigx.common.security.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;
import java.util.Arrays;
import java.util.List;

/**
* 发放单元任务
*
*/
@RestController
@RequiredArgsConstructor
@RequestMapping("/second/unitTask")
@Tag(name = "unitTask", description = "发放单元任务")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class UnitTaskController {

    private final IUnitTaskService unitTaskService;
    private final IGrantUnitService grantUnitService;

    @SysLog("发放单元任务info")
    @GetMapping("/info/{id}")
    @Operation(summary = "发放单元任务info")
    public R<UnitTask> info(@PathVariable Long id) {
        return R.ok(unitTaskService.getById(id));
    }

    @SysLog("发放单元任务page")
    @GetMapping("/page")
    @Operation(summary = "发放单元任务page")
    public R<IPage<UnitTask>> page(PageRequest<UnitTask> pr) {
        Long userId = SecurityUtils.getUser().getId();
        return R.ok(unitTaskService.page(pr.getPage(),pr.getWrapper().like("leader_ids",","+userId+",").orderByDesc("cycle")));
    }

    @SysLog("发放单元任务list")
    @GetMapping("/list")
    @Operation(summary = "发放单元任务list")
    public R<List<UnitTask>> list(PageRequest<UnitTask> pr) {
        return R.ok(unitTaskService.list(pr.getWrapper()));
    }


    @SysLog("发放单元任务add")
    @PostMapping("/add")
    @Operation(summary = "发放单元任务add")
    public R add(@RequestBody UnitTask unitTask)  {
        return R.ok(unitTaskService.save(unitTask));
    }

    @SysLog("发放单元任务edit")
    @PostMapping("/edit")
    @Operation(summary = "发放单元任务edit")
    public R edit(@RequestBody UnitTask unitTask)  {
        return R.ok(unitTaskService.updateById(unitTask));
    }

    @SysLog("发放单元任务 toOnline")
    @PostMapping("/toOnline")
    @Operation(summary = "发放单元任务toOnline")
    public R toOnline(@RequestBody UnitTask unitTask)  {
        unitTaskService.toOnline(unitTask.getId());
        return R.ok();
    }

    @SysLog("发放单元任务 toOffline")
    @PostMapping("/toOffline")
    @Operation(summary = "发放单元任务 toOffline")
    public R toOffline(@RequestBody UnitTask unitTask)  {
        unitTaskService.toOffline(unitTask.getId());
        return R.ok();
    }

    @SysLog("发放单元任务del")
    @PostMapping("/del/{id}")
    @Operation(summary = "发放单元任务del")
    public R del(@PathVariable Long id)  {
        return R.ok(unitTaskService.removeById(id));
    }

    @SysLog("发放单元任务delBatch")
    @PostMapping("/delBatch/{ids}")
    @Operation(summary = "发放单元任务delBatch 1,2,3")
    public R delBatch(@PathVariable String ids)  {
        return R.ok(unitTaskService.removeBatchByIds(Arrays.asList(ids.split(","))));
    }
}