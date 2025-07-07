package com.hscloud.hs.cost.account.controller.kpi;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hscloud.hs.cost.account.model.entity.base.PageRequest;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiUserAttendanceCustom;
import com.hscloud.hs.cost.account.service.kpi.IKpiUserAttendanceCustomService;
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
* 人员考勤自定义字段表
*
*/
@RestController
@RequiredArgsConstructor
@RequestMapping("/kpi/kpiUserAttendanceCustom")
@Tag(name = "kpiUserAttendanceCustom", description = "人员考勤自定义字段表")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class KpiUserAttendanceCustomController {

    private final IKpiUserAttendanceCustomService kpiUserAttendanceCustomService;

    @SysLog("人员考勤自定义字段表info")
    @GetMapping("/info/{id}")
    @Operation(summary = "人员考勤自定义字段表info")
    public R<KpiUserAttendanceCustom> info(@PathVariable Long id) {
        return R.ok(kpiUserAttendanceCustomService.getById(id));
    }

    @SysLog("人员考勤自定义字段表page")
    @GetMapping("/page")
    @Operation(summary = "人员考勤自定义字段表page")
    public R<IPage<KpiUserAttendanceCustom>> page(PageRequest<KpiUserAttendanceCustom> pr) {
        return R.ok(kpiUserAttendanceCustomService.page(pr.getPage(),pr.getWrapper()));
    }

    @SysLog("人员考勤自定义字段表list")
    @GetMapping("/list")
    @Operation(summary = "人员考勤自定义字段表list")
    public R<List<KpiUserAttendanceCustom>> list(PageRequest<KpiUserAttendanceCustom> pr) {
        return R.ok(kpiUserAttendanceCustomService.list(pr.getWrapper()));
    }

    @SysLog("人员考勤自定义字段表add")
    @PostMapping("/add")
    @Operation(summary = "人员考勤自定义字段表add")
    public R add(@RequestBody KpiUserAttendanceCustom kpiUserAttendanceCustom)  {
        return R.ok(kpiUserAttendanceCustomService.save(kpiUserAttendanceCustom));
    }

    @SysLog("人员考勤自定义字段表edit")
    @PostMapping("/edit")
    @Operation(summary = "人员考勤自定义字段表edit")
    public R edit(@RequestBody KpiUserAttendanceCustom kpiUserAttendanceCustom)  {
        return R.ok(kpiUserAttendanceCustomService.updateById(kpiUserAttendanceCustom));
    }

    @SysLog("人员考勤自定义字段表del")
    @PostMapping("/del/{id}")
    @Operation(summary = "人员考勤自定义字段表del")
    public R del(@PathVariable Long id)  {
        return R.ok(kpiUserAttendanceCustomService.removeById(id));
    }

    @SysLog("人员考勤自定义字段表delBatch")
    @PostMapping("/delBatch/{ids}")
    @Operation(summary = "人员考勤自定义字段表delBatch 1,2,3")
    public R delBatch(@PathVariable String ids)  {
        return R.ok(kpiUserAttendanceCustomService.removeBatchByIds(Arrays.asList(ids.split(","))));
    }
}