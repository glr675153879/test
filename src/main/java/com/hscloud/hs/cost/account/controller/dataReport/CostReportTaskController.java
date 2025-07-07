package com.hscloud.hs.cost.account.controller.dataReport;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hscloud.hs.cost.account.constant.enums.dataReport.OpsTypeEnum;
import com.hscloud.hs.cost.account.model.entity.base.PageRequest;
import com.hscloud.hs.cost.account.model.entity.dataReport.CostReportTask;
import com.hscloud.hs.cost.account.service.dataReport.ICostReportTaskLogService;
import com.hscloud.hs.cost.account.service.dataReport.ICostReportTaskService;
import com.pig4cloud.pigx.common.core.util.R;
import com.pig4cloud.pigx.common.log.annotation.SysLog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

/**
* 上报任务
*
*/
@RestController
@RequiredArgsConstructor
@RequestMapping("/dataReport/costReportTask")
@Tag(name = "costReportTask", description = "上报任务")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
@Transactional(readOnly = true)
public class CostReportTaskController {

    private final ICostReportTaskService costReportTaskService;

    private final ICostReportTaskLogService costReportTaskLogService;

    @SysLog("上报任务info")
    @GetMapping("/info/{id}")
    @Operation(summary = "上报任务info")
    public R<CostReportTask> info(@PathVariable Long id) {
        return R.ok(costReportTaskService.getById(id));
    }

    @SysLog("上报任务page")
    @GetMapping("/page")
    @Operation(summary = "上报任务page")
    public R<IPage<CostReportTask>> page(PageRequest<CostReportTask> pr) {
        // 按照创建时间倒序
        QueryWrapper<CostReportTask> wrapper = pr.getWrapper().orderByDesc("create_time");
        return R.ok(costReportTaskService.page(pr.getPage(), wrapper));
    }

    @SysLog("上报任务list")
    @GetMapping("/list")
    @Operation(summary = "上报任务list")
    public R<List<CostReportTask>> list(PageRequest<CostReportTask> pr) {
        return R.ok(costReportTaskService.list(pr.getWrapper()));
    }

    @SysLog("上报任务add")
    @PostMapping("/add")
    @Operation(summary = "上报任务add")
    @Transactional(rollbackFor = Exception.class)
    public R add(@RequestBody CostReportTask costReportTask)  {
        costReportTaskLogService.generateLog(OpsTypeEnum.ADD.getVal(), "", costReportTask);
        return R.ok(costReportTaskService.insert(costReportTask));
    }

    @SysLog("上报任务edit")
    @PostMapping("/edit")
    @Operation(summary = "上报任务edit")
    @Transactional(rollbackFor = Exception.class)
    public R edit(@RequestBody CostReportTask costReportTask)  {
        String originName = costReportTaskService.getById(costReportTask).getTaskName();
        costReportTaskLogService.generateLog(OpsTypeEnum.UPDATE.getVal(), originName, costReportTask);
        return R.ok(costReportTaskService.updateTask(costReportTask));
    }

    @SysLog("上报任务初始化")
    @PostMapping("/initiate")
    @Operation(summary = "上报任务初始化")
    public R initiate()  {
        return R.ok(costReportTaskService.initiate());
    }

    @SysLog("上报任务启停用")
    @PostMapping("/activate")
    @Operation(summary = "上报任务启停用")
    @Transactional(rollbackFor = Exception.class)
    public R activate(@RequestBody CostReportTask costReportTask)  {
        String originName = costReportTaskService.getById(costReportTask).getTaskName();
        costReportTaskLogService.generateLog(OpsTypeEnum.ENABLE.getVal(), originName, costReportTask);
        return R.ok(costReportTaskService.activate(costReportTask));
    }

    @SysLog("上报任务del")
    @PostMapping("/del/{id}")
    @Operation(summary = "上报任务del")
    @Transactional(rollbackFor = Exception.class)
    public R del(@PathVariable Long id)  {
        costReportTaskLogService.generateLog(OpsTypeEnum.DEL .getVal(), "",  costReportTaskService.getById(id));
        return R.ok(costReportTaskService.removeById(id));
    }

    @SysLog("上报任务delBatch")
    @PostMapping("/delBatch/{ids}")
    @Operation(summary = "上报任务delBatch 1,2,3")
    public R delBatch(@PathVariable String ids)  {
        return R.ok(costReportTaskService.removeBatchByIds(Arrays.asList(ids.split(","))));
    }
}