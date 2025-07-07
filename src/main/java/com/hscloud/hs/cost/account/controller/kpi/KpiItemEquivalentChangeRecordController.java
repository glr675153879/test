package com.hscloud.hs.cost.account.controller.kpi;

import com.hscloud.hs.cost.account.model.dto.kpi.KpiItemCoefficientDTO;
import com.hscloud.hs.cost.account.model.dto.kpi.KpiItemEquivalentChangeDTO;
import com.hscloud.hs.cost.account.model.dto.kpi.KpiItemEquivalentDTO;
import com.hscloud.hs.cost.account.model.dto.kpi.KpiItemEquivalentDistributeDTO;
import com.hscloud.hs.cost.account.model.vo.kpi.KpiItemEquivalentChangeRecordVO;
import com.hscloud.hs.cost.account.service.kpi.IKpiItemEquivalentChangeRecordService;
import com.pig4cloud.pigx.common.core.util.R;
import com.pig4cloud.pigx.common.log.annotation.SysLog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/kpi/kpiItemEquivalentChangeRecord")
@Tag(name = "k_当量调整", description = "当量调整")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class KpiItemEquivalentChangeRecordController {
    private final IKpiItemEquivalentChangeRecordService kpiItemEquivalentChangeRecordService;

    @SysLog("调整记录列表（按当量）")
    @GetMapping("/list")
    @Operation(summary = "*调整记录列表（按当量）")
    public R<List<KpiItemEquivalentChangeRecordVO>> list(KpiItemEquivalentChangeDTO dto) {
        return R.ok(kpiItemEquivalentChangeRecordService.getList(dto));
    }

    @SysLog("调整记录列表（按科室）")
    @GetMapping("/list/unit")
    @Operation(summary = "*调整记录列表（按科室）")
    public R<List<KpiItemEquivalentChangeRecordVO>> listByUnit(KpiItemEquivalentChangeDTO dto) {
        return R.ok(kpiItemEquivalentChangeRecordService.listByUnit(dto));
    }

    @SysLog("保存")
    @PostMapping("/save")
    @Operation(summary = "*保存")
    public R save(@Validated @RequestBody KpiItemEquivalentChangeDTO dto) {
        kpiItemEquivalentChangeRecordService.saveRecord(dto);
        return R.ok();
    }

    @SysLog("重置")
    @PostMapping("/reset")
    @Operation(summary = "*重置")
    public R reset(@RequestBody KpiItemEquivalentDTO dto) {
        kpiItemEquivalentChangeRecordService.reset(dto);
        return R.ok();
    }

    @SysLog("修改分配方式")
    @PostMapping("/update/distribute")
    @Operation(summary = "*修改分配方式")
    public R updateDistribute(@Validated @RequestBody KpiItemEquivalentDistributeDTO dto) {
        kpiItemEquivalentChangeRecordService.updateDistribute(dto);
        return R.ok();
    }

    @SysLog("系数批量设置")
    @PostMapping("/coefficient/batch/set")
    @Operation(summary = "*系数批量设置")
    public R coefficientBatchSet(@RequestBody @Validated KpiItemCoefficientDTO dto) {
        kpiItemEquivalentChangeRecordService.coefficientBatchSet(dto);
        return R.ok();
    }

}
