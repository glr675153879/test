package com.hscloud.hs.cost.account.controller.second;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hscloud.hs.cost.account.model.dto.second.SecondTaskCreateDto;
import com.hscloud.hs.cost.account.model.entity.base.PageRequest;
import com.hscloud.hs.cost.account.model.entity.second.SecondTask;
import com.hscloud.hs.cost.account.service.second.ISecondTaskService;
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
* 二次分配总任务
*
*/
@RestController
@RequiredArgsConstructor
@RequestMapping("/second/secondTask")
@Tag(name = "secondTask", description = "二次分配总任务")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class SecondTaskController {

    private final ISecondTaskService secondTaskService;

    @SysLog("二次分配总任务info")
    @GetMapping("/info/{id}")
    @Operation(summary = "二次分配总任务info")
    public R<SecondTask> info(@PathVariable Long id) {
        return R.ok(secondTaskService.getById(id));
    }

    @SysLog("二次分配总任务page")
    @GetMapping("/page")
    @Operation(summary = "二次分配总任务page")
    public R<IPage<SecondTask>> page(PageRequest<SecondTask> pr) {
        return R.ok(secondTaskService.taskPage(pr));
    }

    @SysLog("二次分配总任务list")
    @GetMapping("/list")
    @Operation(summary = "二次分配总任务list")
    public R<List<SecondTask>> list(PageRequest<SecondTask> pr) {
        return R.ok(secondTaskService.list(pr.getWrapper()));
    }

    @SysLog("二次分配总任务add")
    @PostMapping("/add")
    @Operation(summary = "二次分配总任务add")
    public R add(@RequestBody SecondTask secondTask)  {
        return R.ok(secondTaskService.save(secondTask));
    }

    /*@SysLog("二次分配总任务下发生成")
    @PostMapping("/create/{firstId}")
    @Operation(summary = "二次分配总任务下发生成")
    public R create(@PathVariable Long firstId)  {
        secondTaskService.create(firstId);
        return R.ok();
    }*/

    @SysLog("二次分配总任务下发生成")
    @PostMapping("/create")
    @Operation(summary = "二次分配总任务下发生成")
    public R create(@RequestBody SecondTaskCreateDto taskCreateDto)  {
        secondTaskService.create(taskCreateDto);
        return R.ok();
    }

    @SysLog("二次分配总任务 同周期是否已生成")
    @GetMapping("/ifPublished")
    @Operation(summary = "二次分配总任务 同周期是否已生成")
    public R<Boolean> ifPublished(Long firstId) {
        return R.ok(secondTaskService.ifPublished(firstId));
    }

    @SysLog("二次分配总任务edit")
    @PostMapping("/edit")
    @Operation(summary = "二次分配总任务edit")
    public R edit(@RequestBody SecondTask secondTask)  {
        return R.ok(secondTaskService.updateById(secondTask));
    }

    @SysLog("二次分配总任务del")
    @PostMapping("/del/{id}")
    @Operation(summary = "二次分配总任务del")
    public R del(@PathVariable Long id)  {
        return R.ok(secondTaskService.removeById(id));
    }

    @SysLog("二次分配总任务delBatch")
    @PostMapping("/delBatch/{ids}")
    @Operation(summary = "二次分配总任务delBatch 1,2,3")
    public R delBatch(@PathVariable String ids)  {
        return R.ok(secondTaskService.removeBatchByIds(Arrays.asList(ids.split(","))));
    }
}