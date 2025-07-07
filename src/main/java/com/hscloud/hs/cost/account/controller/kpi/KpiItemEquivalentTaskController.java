package com.hscloud.hs.cost.account.controller.kpi;

import com.hscloud.hs.cost.account.model.dto.kpi.KpiItemEquivalentTaskDTO;
import com.hscloud.hs.cost.account.model.vo.kpi.KpiItemEquivalentTaskVO;
import com.hscloud.hs.cost.account.service.kpi.IKpiItemEquivalentTaskService;
import com.pig4cloud.pigx.common.core.util.R;
import com.pig4cloud.pigx.common.log.annotation.SysLog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/kpi/kpiItemEquivalentTask")
@Tag(name = "k_当量核验任务", description = "当量核验任务")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class KpiItemEquivalentTaskController {
    @Autowired
    private IKpiItemEquivalentTaskService kpiItemEquivalentTaskService;

    @SysLog("列表（科室）")
    @GetMapping("/list/unit")
    @Operation(summary = "*列表（科室）")
    public R<List<KpiItemEquivalentTaskVO>> unitList(KpiItemEquivalentTaskDTO dto) {
        return R.ok(kpiItemEquivalentTaskService.getList(dto, false));
    }

    @SysLog("列表（绩效办）")
    @GetMapping("/list/admin")
    @Operation(summary = "*列表（绩效办）")
    public R<List<KpiItemEquivalentTaskVO>> adminList(KpiItemEquivalentTaskDTO dto) {
        return R.ok(kpiItemEquivalentTaskService.getList(dto, true));
    }

    @SysLog("下发")
    @PostMapping("/issue")
    @Operation(summary = "*下发")
    public R issueTask(@RequestBody KpiItemEquivalentTaskDTO dto) {
        kpiItemEquivalentTaskService.issueTask(dto);
        return R.ok();
    }

    @SysLog("提交")
    @PostMapping("/commit")
    @Operation(summary = "*提交")
    public R commit(@RequestParam Long accountUnitId, @RequestParam Long period) {
        kpiItemEquivalentTaskService.commitTask(accountUnitId, period);
        return R.ok();
    }

    @SysLog("审核")
    @PostMapping("/approve")
    @Operation(summary = "*审核")
    public R approve(@RequestBody KpiItemEquivalentTaskDTO dto) {
        kpiItemEquivalentTaskService.approveTask(dto);
        return R.ok();
    }

    @SysLog("审核状态统计")
    @GetMapping("/statusCount")
    @Operation(summary = "*审核状态统计")
    public R<Map<String, Long>> statusCount(@RequestParam(required = false) Long period) {
        return R.ok(kpiItemEquivalentTaskService.statusCount(period));
    }

    @SysLog("科室任务当量状态统计")
    @GetMapping("/unitTaskStatusCount")
    @Operation(summary = "*科室任务当量状态统计")
    public R<Map<String, Long>> unitTaskStatusCount(@RequestParam Long period, @RequestParam Long accountUnitId) {
        return R.ok(kpiItemEquivalentTaskService.unitTaskStatusCount(period, accountUnitId));
    }

}