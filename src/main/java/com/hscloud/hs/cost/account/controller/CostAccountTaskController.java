package com.hscloud.hs.cost.account.controller;


import com.hscloud.hs.cost.account.model.dto.*;
import com.hscloud.hs.cost.account.model.entity.CostAccountTask;
import com.hscloud.hs.cost.account.model.vo.*;
import com.hscloud.hs.cost.account.service.CostAccountTaskService;
import com.hscloud.hs.cost.account.service.CostTaskExecuteResultService;
import com.hscloud.hs.cost.account.service.ICostAccountTaskNewService;
import com.hscloud.hs.cost.account.utils.ExcelExporter;
import com.pig4cloud.pigx.common.core.util.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.text.ParseException;

@RestController
@RequestMapping("/account/task")
@Tag(description = "accountTask", name = "核算任务")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
@RequiredArgsConstructor
@Slf4j
public class CostAccountTaskController {

    private final CostAccountTaskService costAccountTaskService;
    private final ICostAccountTaskNewService costAccountTaskNewService;

    private final CostTaskExecuteResultService costTaskExecuteResultService;

    /**
     * 保存核算任务
     *
     * @param costAccountTask
     * @return
     */
    @Operation(description = "保存核算任务", summary = "保存核算任务")
    @PostMapping
    public R save(@RequestBody CostAccountTask costAccountTask) throws ParseException {
        return R.ok(costAccountTaskService.saveTask(costAccountTask));
    }

    /**
     * 保存核算任务(新)
     *
     * @param dto
     * @return
     */
    @PreAuthorize("@pms.hasPermission('kpi_task_add')")
    @Operation(description = "保存核算任务", summary = "保存核算任务")
    @PostMapping("/addNew")
    public R addNew(@RequestBody CostAccountTaskNewDto dto)  {
        return R.ok(costAccountTaskService.saveTaskNew(dto));
    }

    /**
     * 删除任务
     *
     * @param id
     * @return
     */
    @Operation(description = "删除任务", summary = "删除任务")
    @DeleteMapping("/delete/{id}")
    public R deleteTask(@PathVariable Long id) {
        return R.ok(costAccountTaskService.deleteTask(id));
    }
    /**
     * 查询回显任务
     *
     * @param id
     * @return
     */
    @Operation(description = "查询回显任务", summary = "查询回显任务")
    @PostMapping("/query/{id}")
    public R getById(@PathVariable Long id)  {
        return R.ok(costAccountTaskService.getTaskById(id));
    }

    /**
     * 修改核算任务
     *
     * @param dto
     * @return
     */
    @Operation(description = "修改核算任务", summary = "修改核算任务")
    @PostMapping("/update")
    public R update(@RequestBody CostAccountTaskNewDto dto)  {
        return R.ok(costAccountTaskService.updateTask(dto));
    }
    /**
     * 任务详情
     *
     * @param taskId
     * @return
     */
    @Operation(description = "任务详情", summary = "任务详情")
    @GetMapping("/detailsNew")
    public R getDetailNew(@RequestParam("taskId") Long taskId,@RequestParam("taskGroupId") Long taskGroupId) {
        return R.ok(costAccountTaskService.getDetailNew(taskId,taskGroupId));
    }
    /**
     * 核算任务分组列表
     *
     * @param id
     * @return
     */
    @Operation(description = "核算任务分组展示", summary = "核算任务分组展示")
    @PostMapping("/list/taskGroup/{id}")
    public R listTaskGroup(@PathVariable Long id)  {
        return R.ok(costAccountTaskService.listTaskGroup(id));
    }
    /**
     * 核算任务列表
     *
     * @param costAccountTaskQueryDto
     * @return
     */
    @Operation(description = "核算任务列表", summary = "核算任务列表")
    @GetMapping
    public R list(CostAccountTaskQueryDto costAccountTaskQueryDto) {
        return R.ok(costAccountTaskService.listAccountTask(costAccountTaskQueryDto));
    }
    /**
     * 核算任务列表
     *
     * @param dto
     * @return
     */
    @Operation(description = "核算任务列表", summary = "核算任务列表")
    @GetMapping("/list")
    public R listNew(CostAccountTaskQueryNewDto dto) {
        return R.ok(costAccountTaskNewService.listAccountTaskNew(dto));
    }


    /**
     * 获取任务的任务分组
     *
     * @param
     * @return
     */
    @Operation(description = "获取任务的任务分组", summary = "获取任务的任务分组")
    @GetMapping("/getTaskGroup/{id}")
    public R getTaskGroup(@PathVariable Long id) {
        return R.ok(costAccountTaskService.getTaskGroup(id));
    }

    /**
     * 任务详情
     *
     * @param id
     * @return
     */
    @Operation(description = "任务详情", summary = "任务详情")
    @GetMapping("/details/{id}")
    public R getDetail(@PathVariable Long id) {
        return R.ok(costAccountTaskService.getDetail(id));
    }

    /**
     * 任务结果
     *
     * @param taskResultQueryDto
     * @return
     */
    @Operation(description = "任务结果", summary = "任务结果")
    @GetMapping("/result/list")
    public R<CostAccountTaskResultDetailVo> getResult(TaskResultQueryDto taskResultQueryDto) {
        return R.ok(costTaskExecuteResultService.listResult(taskResultQueryDto));
    }


    /**
     * 计算过程总核算值
     *
     * @param dto
     * @return
     */
    @Operation(description = "计算过程总核算值", summary = "计算过程总核算值")
    @PostMapping("/totalProcess")
    public R<CostAccountTaskResultTotalValueVo> getTotalProcess(@RequestBody @Validated CostAccountTaskCalculateTotalProcessDto dto) {
        return R.ok(costAccountTaskService.getTotalProcess(dto));
    }

    /**
     * 计算过程详情页
     *
     * @param costAccountTaskCalculateProcessDto
     * @return
     */
    @Operation(description = "计算过程详情页", summary = "计算过程详情页")
    @PostMapping("/calculationProcess")
    public R getCalculationProcess(@RequestBody @Validated CostAccountTaskCalculateProcessDto costAccountTaskCalculateProcessDto) {
        return R.ok(costAccountTaskService.getCalculationProcess(costAccountTaskCalculateProcessDto));
    }

    /**
     * 任务结果导出
     */
    @Operation(description = "任务结果导出", summary = "任务结果导出")
    @GetMapping("/export")
    public void export(HttpServletResponse response,TaskResultQueryDto taskResultQueryDto) {
        CostAccountTaskResultDetailVo data = costTaskExecuteResultService.listResult(taskResultQueryDto);

        String fileName = "任务结果.xlsx";
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; filename=" + fileName);

        try {
            ExcelExporter.export(response.getOutputStream(), data);
        } catch (Exception e) {
            log.error("任务结果导出失败", e);
        }
    }


    /**
     * 任务结果数据小组数据
     *
     * @param taskResultQueryDto
     * @return
     */
    @Operation(description = "任务结果", summary = "任务结果")
    @GetMapping("/result/newList")
    public R<CostAccountTaskResultDetailVo> getResultList(TaskResultQueryDto taskResultQueryDto) {
        return R.ok(costTaskExecuteResultService.newListResult(taskResultQueryDto));
    }

    //计算过程数据小组数据
    @Operation(description = "计算过程总核算值", summary = "计算过程总核算值")
    @PostMapping("/newTotalProcess")
    public R<CostAccountTaskResultTotalValueVo> getNewTotalProcess(@RequestBody @Validated CostAccountTaskCalculateTotalProcessDto dto) {
        return R.ok(costAccountTaskService.getNewTotalProcess(dto));
    }
    /**
     * 计算过程详情页 数据小组数据
     *
     * @param costAccountTaskCalculateProcessDto
     * @return
     */
    @Operation(description = "计算过程详情页", summary = "计算过程详情页")
    @PostMapping("/newCalculationProcess")
    public R getNewCalculationProcess(@RequestBody @Validated CostAccountTaskCalculateProcessDto costAccountTaskCalculateProcessDto) {
        return R.ok(costAccountTaskService.getNewCalculationProcess(costAccountTaskCalculateProcessDto));
    }


    /**
     * 任务结果数据小组数据
     *
     * @param dto
     * @return
     */
    @Operation(description = "任务结果", summary = "任务结果")
    @GetMapping("/result/distributionList")
    public R<CostAccountTaskResultDetailNewVo> getDistributionList(TaskResultQueryNewDto dto) {
        return R.ok(costTaskExecuteResultService.getDistributionList(dto));
    }

    /**
     * 任务计算过程详情页 数据小组数据
     *
     * @param dto
     * @return
     */
    @Operation(description = "绩效计算过程详情页", summary = "绩效计算过程详情页")
    @GetMapping("/distribution/calculationProcess")
    public R<CostAccountTaskResultIndexProcessNewVo> getDistributionNewList(CostAccountTaskCalculateProcessNewDto dto) {
        return R.ok(costTaskExecuteResultService.getDistributionNewList(dto));
    }
}
