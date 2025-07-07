package com.hscloud.hs.cost.account.controller.second;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hscloud.hs.cost.account.model.entity.base.PageRequest;
import com.hscloud.hs.cost.account.model.entity.second.GrantUnitLog;
import com.hscloud.hs.cost.account.service.second.IGrantUnitLogService;
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
* 发放单元操作日志
*
*/
@RestController
@RequiredArgsConstructor
@RequestMapping("/second/grantUnitLog")
@Tag(name = "grantUnitLog", description = "发放单元操作日志")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class GrantUnitLogController {

    private final IGrantUnitLogService grantUnitLogService;

    @SysLog("发放单元操作日志info")
    @GetMapping("/info/{id}")
    @Operation(summary = "发放单元操作日志info")
    public R<GrantUnitLog> info(@PathVariable Long id) {
        return R.ok(grantUnitLogService.getById(id));
    }

    @SysLog("发放单元操作日志page")
    @GetMapping("/page")
    @Operation(summary = "发放单元操作日志page")
    public R<IPage<GrantUnitLog>> page(PageRequest<GrantUnitLog> pr) {
        return R.ok(grantUnitLogService.page(pr.getPage(),pr.getWrapper()));
    }

    @SysLog("发放单元操作日志list")
    @GetMapping("/list")
    @Operation(summary = "发放单元操作日志list")
    public R<List<GrantUnitLog>> list(PageRequest<GrantUnitLog> pr) {
        return R.ok(grantUnitLogService.list(pr.getWrapper().orderByDesc("create_time")));
    }

    @SysLog("发放单元操作日志add")
    @PostMapping("/add")
    @Operation(summary = "发放单元操作日志add")
    public R add(@RequestBody GrantUnitLog grantUnitLog)  {
        return R.ok(grantUnitLogService.save(grantUnitLog));
    }

    @SysLog("发放单元操作日志edit")
    @PostMapping("/edit")
    @Operation(summary = "发放单元操作日志edit")
    public R edit(@RequestBody GrantUnitLog grantUnitLog)  {
        return R.ok(grantUnitLogService.updateById(grantUnitLog));
    }

    @SysLog("发放单元操作日志del")
    @PostMapping("/del/{id}")
    @Operation(summary = "发放单元操作日志del")
    public R del(@PathVariable Long id)  {
        return R.ok(grantUnitLogService.removeById(id));
    }

    @SysLog("发放单元操作日志delBatch")
    @PostMapping("/delBatch/{ids}")
    @Operation(summary = "发放单元操作日志delBatch 1,2,3")
    public R delBatch(@PathVariable String ids)  {
        return R.ok(grantUnitLogService.removeBatchByIds(Arrays.asList(ids.split(","))));
    }
}