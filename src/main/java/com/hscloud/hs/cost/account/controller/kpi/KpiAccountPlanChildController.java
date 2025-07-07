package com.hscloud.hs.cost.account.controller.kpi;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hscloud.hs.cost.account.model.dto.kpi.KpiAccountPlanChildAddDto;
import com.hscloud.hs.cost.account.model.dto.kpi.KpiAccountPlanChildListDto;
import com.hscloud.hs.cost.account.model.dto.kpi.KpiIndexBatchEnableDto;
import com.hscloud.hs.cost.account.model.dto.kpi.KpiIndexEnableDto;
import com.hscloud.hs.cost.account.model.entity.base.PageRequest;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiAccountPlanChild;
import com.hscloud.hs.cost.account.model.vo.kpi.KpiAccountPlanChildInfoVO;
import com.hscloud.hs.cost.account.model.vo.kpi.KpiAccountPlanChildListVO;
import com.hscloud.hs.cost.account.model.vo.kpi.KpiPlanConfigVO;
import com.hscloud.hs.cost.account.service.impl.kpi.KpiAccountPlanChildService;
import com.hscloud.hs.cost.account.service.kpi.IKpiAccountPlanChildService;
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

/**
* 核算子方案表
*
*/
@RestController
@RequiredArgsConstructor
@RequestMapping("/kpi/kpiAccountPlanChild")
@Tag(name = "k_子方案", description = "核算子方案表")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class KpiAccountPlanChildController {

    @Autowired
    private final KpiAccountPlanChildService kpiAccountPlanChildService;

    @SysLog("核算子方案表list")
    @GetMapping("/list")
    @Operation(summary = "*核算子方案表list")
    public R<List<KpiAccountPlanChildListVO>> list(KpiAccountPlanChildListDto input) {
        return R.ok(kpiAccountPlanChildService.list(input));
    }

    @SysLog("核算子方案表page")
    @GetMapping("/page")
    @Operation(summary = "*核算子方案表page")
    public R<IPage<KpiAccountPlanChildListVO>> page(KpiAccountPlanChildListDto input) {
        return R.ok(kpiAccountPlanChildService.getPage(input));
    }

    @SysLog("核算子方案表info")
    @GetMapping("/info/{id}")
    @Operation(summary = "*核算子方案表info")
    public R<KpiAccountPlanChildInfoVO> info(@PathVariable Long id) {
        return R.ok(kpiAccountPlanChildService.getInfo(id));
    }

    @SysLog("核算子方案新增修改")
    @PostMapping("/saveOrUpdate")
    @Operation(summary = "*核算子方案表新增保存")
    public R saveOrUpdate(@RequestBody KpiAccountPlanChildAddDto dto)  {
        kpiAccountPlanChildService.saveOrUpdate(dto);
        return R.ok();
    }

    @SysLog("核算子方案表edit")
    @PostMapping("/edit")
    @Operation(summary = "核算子方案表edit")
    public R edit(@RequestBody KpiAccountPlanChild kpiAccountPlanChild)  {
        return R.ok(kpiAccountPlanChildService.updateById(kpiAccountPlanChild));
    }

    @SysLog("启用停用")
    @PostMapping("/enable")
    @Operation(summary = "*启用停用")
    public R enable(@RequestBody @Validated KpiIndexBatchEnableDto dto) {
        kpiAccountPlanChildService.enable(dto);
        return R.ok();
    }

    @SysLog("子方案del")
    @PostMapping("/del/{id}")
    @Operation(summary = "子方案del")
    public R del(@PathVariable Long id)  {
        kpiAccountPlanChildService.del(id);
        return R.ok();
    }

    /*********************方案配置*********************/

    @SysLog("核算子方案表listv2")
    @GetMapping("/listv2")
    @Operation(summary = "*核算子方案表listv2")
    public R<List<KpiAccountPlanChildListVO>> configListV2(KpiAccountPlanChildListDto input) {
        return R.ok(kpiAccountPlanChildService.configList(input));
    }


    @SysLog("指标配置详情v2")
    @GetMapping("/index/config/infov2")
    @Operation(summary = "*指标配置详情")
    public R<KpiPlanConfigVO> configinfo(String indexCode, String planCode, String memberCode) {
        KpiPlanConfigVO kpiPlanConfigVO = kpiAccountPlanChildService.indexConfigInfoV2(indexCode, planCode, memberCode);
        return R.ok(kpiPlanConfigVO);
    }

    @SysLog("核算子方案校验也就是配置按钮")
    @GetMapping("/verify")
    @Operation(summary = "*核算子方案校验也就是配置按钮")
    public R verify(Long id){
        return R.ok(kpiAccountPlanChildService.verify(id));
    }
}