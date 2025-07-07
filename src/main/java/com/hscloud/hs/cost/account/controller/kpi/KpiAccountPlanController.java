package com.hscloud.hs.cost.account.controller.kpi;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hscloud.hs.cost.account.model.dto.kpi.KpiAccountPlanAddDto;
import com.hscloud.hs.cost.account.model.dto.kpi.KpiAccountPlanChildListDto;
import com.hscloud.hs.cost.account.model.dto.kpi.KpiAccountPlanListDto;
import com.hscloud.hs.cost.account.model.dto.kpi.KpiIndexEnableDto;
import com.hscloud.hs.cost.account.model.entity.base.PageRequest;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiAccountPlan;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiIndex;
import com.hscloud.hs.cost.account.model.vo.kpi.KpiAccountPlanChildListVO;
import com.hscloud.hs.cost.account.model.vo.kpi.KpiAccountPlanListVO;
import com.hscloud.hs.cost.account.service.impl.kpi.KpiAccountPlanChildService;
import com.hscloud.hs.cost.account.service.impl.kpi.KpiIndexService;
import com.hscloud.hs.cost.account.service.kpi.IKpiAccountPlanService;
import com.pig4cloud.pigx.common.core.util.R;
import com.pig4cloud.pigx.common.log.annotation.SysLog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
* 核算方案表(COST_ACCOUNT_PLAN)
*
*/
@RestController
@RequiredArgsConstructor
@RequestMapping("/kpi/kpiAccountPlan")
@Tag(name = "k_核算总方案", description = "核算方案表(COST_ACCOUNT_PLAN)")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class KpiAccountPlanController {

    private final IKpiAccountPlanService kpiAccountPlanService;
    private final KpiIndexService kpiIndexService;

    @SysLog("核算方案list")
    @GetMapping("/list")
    @Operation(summary = "*核算方案表list")
    public R<List<KpiAccountPlanListVO>> list(KpiAccountPlanListDto input) {
        return R.ok(kpiAccountPlanService.list(input));
    }

    @SysLog("核算方案page")
    @GetMapping("/page")
    @Operation(summary = "*核算方案page")
    public R<IPage<KpiAccountPlanListVO>> page(KpiAccountPlanListDto input) {
        return R.ok(kpiAccountPlanService.getPage(input));
    }

    @SysLog("核算方案info")
    @GetMapping("/info/{id}")
    @Operation(summary = "*核算方案info")
    public R<KpiAccountPlan> info(@PathVariable Long id) {
        KpiAccountPlan byId = kpiAccountPlanService.getById(id);
        KpiIndex kpiIndex = kpiIndexService.getOne(new LambdaQueryWrapper<KpiIndex>().eq(KpiIndex::getCode, byId.getIndexCode()));
        if (kpiIndex != null){
            byId.setCaliber(kpiIndex.getCaliber());
            byId.setIndexName(kpiIndex.getName());
        }
        return R.ok(byId);
    }

    @SysLog("核算方案新增修改")
    @PostMapping("/saveOrUpdate")
    @Operation(summary = "*核算方案saveOrUpdate")
    public R saveOrUpdate(@RequestBody KpiAccountPlanAddDto dto)  {
        kpiAccountPlanService.saveOrUpdate(dto);
        return R.ok();
    }

    @SysLog("启用停用")
    @PostMapping("/enable")
    @Operation(summary = "*启用停用方案")
    public R enable(@RequestBody @Validated KpiIndexEnableDto dto) {
        kpiAccountPlanService.enable(dto);
        return R.ok();
    }

    @SysLog("核算方案表(COST_ACCOUNT_PLAN)del")
    @PostMapping("/del/{id}")
    @Operation(summary = "*删除核算方案")
    public R del(@PathVariable Long id)  {
        kpiAccountPlanService.del(id);
        return R.ok();
    }

//    @SysLog("核算方案校验")
//    @GetMapping("/verify")
//    @Operation(summary = "*核算方案校验")
//    public R verify(String ids){
//        return R.ok(kpiAccountPlanService.verify(Arrays.stream(ids.split(",")).map(Long::valueOf).collect(Collectors.toList())));
//    }
}