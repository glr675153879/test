package com.hscloud.hs.cost.account.controller.kpi;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hscloud.hs.cost.account.model.dto.kpi.KpiAllocationRuleDto;
import com.hscloud.hs.cost.account.model.dto.kpi.KpiAllocationRuleListDto;
import com.hscloud.hs.cost.account.model.dto.kpi.KpiIndexDto;
import com.hscloud.hs.cost.account.model.entity.base.PageRequest;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiAllocationRule;
import com.hscloud.hs.cost.account.model.vo.kpi.KpiAllocationRuleListVO;
import com.hscloud.hs.cost.account.model.vo.kpi.KpiAllocationRuleVO;
import com.hscloud.hs.cost.account.service.kpi.IKpiAllocationRuleService;
import com.pig4cloud.pigx.common.core.util.R;
import com.pig4cloud.pigx.common.log.annotation.SysLog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.Arrays;
import java.util.List;

/**
* 分摊公式表
*
*/
@RestController
@RequiredArgsConstructor
@RequestMapping("/kpi/kpiAllocationRule")
@Tag(name = "k_分摊公式", description = "分摊公式表")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class KpiAllocationRuleController {

    private final IKpiAllocationRuleService kpiAllocationRuleService;

    @SysLog("分摊公式表page")
    @GetMapping("/page")
    @Operation(summary = "*分摊公式表page")
    public R<IPage<KpiAllocationRuleListVO>> getRulePage(KpiAllocationRuleListDto dto) {
        return R.ok(kpiAllocationRuleService.getRulePage(dto));
    }

    @SysLog("分摊公式新增修改")
    @PostMapping("/saveOrUpdate")
    @Operation(summary = "*分摊公式新增修改")
    public R saveOrUpdate(@RequestBody @Validated KpiAllocationRuleDto dto) {
        kpiAllocationRuleService.saveOrUpdate(dto);
        return R.ok();
    }

    @SysLog("分摊公式表info")
    @GetMapping("/info/{id}")
    @Operation(summary = "*分摊公式表info")
    public R<KpiAllocationRuleVO> info(@PathVariable Long id) {
        return R.ok(kpiAllocationRuleService.info(id));
    }


    @SysLog("分摊公式表del")
    @PostMapping("/del/{id}")
    @Operation(summary = "*分摊公式表del")
    public R del(@PathVariable Long id)  {
        kpiAllocationRuleService.del(id);
        return R.ok();
    }
}