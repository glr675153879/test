package com.hscloud.hs.cost.account.controller.kpi;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hscloud.hs.cost.account.constant.enums.kpi.CodePrefixEnum;
import com.hscloud.hs.cost.account.mapper.kpi.KpiAccountTaskMapper;
import com.hscloud.hs.cost.account.model.dto.kpi.*;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiAccountTask;
import com.hscloud.hs.cost.account.model.vo.kpi.KpiAccountTaskListVO;
import com.hscloud.hs.cost.account.model.vo.kpi.KpiCalculateReportVO;
import com.hscloud.hs.cost.account.model.vo.kpi.KpiTaskReportLeftVO;
import com.hscloud.hs.cost.account.service.kpi.IKpiAccountTaskService;
import com.hscloud.hs.cost.account.service.kpi.task.TaskCaculateService;
import com.hscloud.hs.cost.account.service.kpi.task.TaskService;
import com.hscloud.hs.cost.account.utils.kpi.Convert;
import com.pig4cloud.pigx.common.core.exception.BizException;
import com.pig4cloud.pigx.common.core.util.R;
import com.pig4cloud.pigx.common.data.tenant.TenantContextHolder;
import com.pig4cloud.pigx.common.log.annotation.SysLog;
import io.netty.util.internal.StringUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Base64;
import java.util.List;

/**
 * 核算任务表(cost_account_task)
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/kpi/kpiAccountTask")
@Tag(name = "k_核算任务", description = "核算任务表(cost_account_task)")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class KpiAccountTaskController {

    private final IKpiAccountTaskService kpiAccountTaskService;

    private final ApplicationEventPublisher publisher;
    @Autowired
    private KpiAccountTaskMapper kpiAccountTaskMapper;
    @Autowired
    private TaskService taskService;
    @Autowired
    private TaskCaculateService taskCaculateService;

    @SysLog("核算任务表(cost_account_task)info")
    @GetMapping("/info/{id}")
    @Operation(summary = "核算任务表(cost_account_task)info")
    public R<KpiAccountTask> info(@PathVariable Long id) {
        return R.ok(kpiAccountTaskService.getById(id));
    }

    @SysLog("核算任务表(cost_account_task)page")
    @GetMapping("/page")
    @Operation(summary = "核算任务表(cost_account_task)page")
    public R<IPage<KpiAccountTaskListVO>> page(KpiAccountTaskListDto input) {
        return R.ok(kpiAccountTaskService.getPage(input));
    }

    @SysLog("核算任务表(cost_account_task)list")
    @GetMapping("/list")
    @Operation(summary = "核算任务表(cost_account_task)list")
    public R<List<KpiAccountTaskListVO>> list(KpiAccountTaskListDto input) {
        return R.ok(kpiAccountTaskService.list(input));
    }

    @SysLog("下发错误日志")
    @GetMapping("/send_log")
    @Operation(summary = "下发错误日志")
    public R<String> send_log(Long task_id) {
        return R.ok(kpiAccountTaskService.send_log(task_id));
    }

    @SysLog("查看日志")
    @GetMapping("/log")
    @Operation(summary = "查看日志")
    public R<String> log(Long task_child_id) throws IOException {
        String log = kpiAccountTaskService.log(task_child_id);
        //通过Base64转成字符串
        byte[] compress = Convert.compress(log.getBytes());
        String longStringEncoded = Base64.getEncoder().encodeToString(compress);
        return R.ok(longStringEncoded);
    }

    @SysLog("查看错误日志")
    @GetMapping("/log/erro")
    @Operation(summary = "查看错误日志")
    public R<String> logErro(Long task_child_id) {
        return R.ok(kpiAccountTaskService.logErro(task_child_id));
    }

    @SysLog("核算任务新增修改")
    @PostMapping("/saveOrUpdate")
    @Operation(summary = "*核算任务新增修改")
    public R saveOrUpdate(@RequestBody KpiAccountTaskAddDto dto) {
        kpiAccountTaskService.saveOrUpdate(dto);
        return R.ok();
    }

    @SysLog("核算任务表(cost_account_task)del")
    @PostMapping("/del/{id}")
    @Operation(summary = "核算任务表(cost_account_task)del")
    public R del(@PathVariable Long id) {
        kpiAccountTaskService.del(id);
        return R.ok();
    }

    @SysLog("核算任务下发")
    @PostMapping("/issued")
    @Operation(summary = "核算任务下发")
    public R issued(@RequestBody IssuedDTO input) {
        kpiAccountTaskService.issued(input);
        return R.ok();
    }

    @SysLog("核算任务解锁")
    @PostMapping("/unlock/{id}")
    @Operation(summary = "核算任务解锁")
    public R unlock(@PathVariable Long id) {
        kpiAccountTaskService.unlock(id);
        return R.ok();
    }

    /*@SysLog("核算任务表(cost_account_task)delBatch")
    @PostMapping("/delBatch/{ids}")
    @Operation(summary = "核算任务表(cost_account_task)delBatch 1,2,3")
    public R delBatch(@PathVariable String ids)  {
        return R.ok(kpiAccountTaskService.removeBatchByIds(Arrays.asList(ids.split(","))));
    }*/

    @SysLog("报表左边列表")
    @GetMapping("/report/left")
    @Operation(summary = "报表左边列表")
    public R<KpiTaskReportLeftVO> reportLeft(Long taskChildId) {
        return R.ok(kpiAccountTaskService.reportLeft(taskChildId));
    }

    @SysLog("报表计算结果 右边")
    @GetMapping("/report/calculate")
    @Operation(summary = "报表计算结果 右边")
    public R<KpiCalculateReportVO> reportCalculate(KpiCalculateReportDTO input) {
        if (StringUtil.isNullOrEmpty(input.getIndexCode())) {
            throw new BizException("指标项代码为空");
        }else if (input.getIndexCode().startsWith(CodePrefixEnum.ITEM.getPrefix())){
            return R.ok(kpiAccountTaskService.reportCalculate2(input));
        } else if (input.getIndexCode().startsWith(CodePrefixEnum.INDEX.getPrefix()) || input.getIndexCode().startsWith(CodePrefixEnum.ALLOCATION.getPrefix())){
            return R.ok(kpiAccountTaskService.reportCalculate(input));
        }else{
            throw new BizException("指标项代码有误");
        }
    }

    @SysLog("报表计算结果 下转")
    @GetMapping("/report/detail")
    @Operation(summary = "报表计算结果 下转")
    public R<KpiCalculateReportVO> reportDetail(KpiCalculateDetailDTO input) {
        if (StringUtil.isNullOrEmpty(input.getCode())) {
            throw new BizException("指标项代码为空");
        }else if (input.getCode().startsWith(CodePrefixEnum.ITEM.getPrefix()) || !StringUtil.isNullOrEmpty(input.getCodeType())){
            return R.ok(kpiAccountTaskService.reportDetail2(input));
        } else if (!StringUtil.isNullOrEmpty(input.getImputationCode())||input.getCode().startsWith(CodePrefixEnum.INDEX.getPrefix()) || input.getCode().startsWith(CodePrefixEnum.ALLOCATION.getPrefix())){
            return R.ok(kpiAccountTaskService.reportDetail(input));
        }else{
            throw new BizException("指标项代码有误");
        }
    }

    @SysLog("报表计算结果 摊出明细")
    @GetMapping("/report/alloValue")
    @Operation(summary = "报表计算结果 摊出明细")
    public R<List<KpiReportAlloValue>> reportAlloValue(KpiCalculateDetailDTO input) {
        return R.ok(kpiAccountTaskService.reportAlloValue(input));
    }

    @SysLog("核算测试任务新增")
    @PostMapping("/test/save")
    @Operation(summary = "*核算测试任务新增")
    public R saveTest(@RequestBody KpiAccountTaskTestAddDto dto) {
        kpiAccountTaskService.saveTest(dto);
        return R.ok();
    }

    @SysLog("执行测试任务 入参task_id")
    @PostMapping("/test/run")
    @Operation(summary = "*执行测试任务 入参task_id")
    public R runTest(@RequestBody KpiRunTestDTO input) {
        if (kpiAccountTaskMapper.getShedLog("('cost_KpiCalculateMain')") >0){
            throw new BizException("有其他测试任务进行中，请稍后重试");
        }
        new Thread(() -> {
            try {
                KpiAccountTask task = kpiAccountTaskMapper.selectById(input.getId());
                if (task.getReportId() != null || "Y".equals(task.getIssuedFlag())){
                    return;
                }
                TenantContextHolder.setTenantId(task.getTenantId());
                if ("Y".equals(task.getTestFlag())){
                    taskService.calculateTest(input.getId());
                }else {
                    taskService.calculate(input.getId(),input.isItemRefresh(),input.isEmpRefresh(),input.isEquivalent());
                }
            } catch (Exception exception) {
            }
        }).start();

        //publisher.publishEvent(new KpiTaskCalculateEvent(input));
        return R.ok();
    }

    @SysLog("公式校验")
    @PostMapping("/test/formula")
    @Operation(summary = "*公式校验")
    public R formulaTest(@RequestBody KpiRunTestDTO input) {
        taskService.formulaTest(input.getId());
        return R.ok();
    }

    @SysLog("公式校验")
    @PostMapping("/test/formula2")
    @Operation(summary = "*公式校验")
    public R<String> formulaTest2(@RequestBody KpiTestFormulaDTO input) {

        return R.ok(taskService.formulaTest2(input));
    }

    @SysLog("备份reportConfig")
    @PostMapping("/copy/reportConfig")
    @Operation(summary = "备份reportConfig")
    public R reportConfig(@RequestBody ReportConfigCopyDTO input) {
        kpiAccountTaskService.reportConfig(input);
        return R.ok();
    }

    @SysLog("更换同周期任务")
    @PostMapping("/change")
    @Operation(summary = "*更换同周期任务")
    public R change(@RequestBody KpiConfigChangeDTO dto)  {
        kpiAccountTaskService.change(dto);
        return R.ok();
    }
}