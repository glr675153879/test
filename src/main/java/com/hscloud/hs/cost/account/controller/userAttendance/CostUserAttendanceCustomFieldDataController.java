package com.hscloud.hs.cost.account.controller.userAttendance;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hscloud.hs.cost.account.model.entity.base.PageRequest;
import com.hscloud.hs.cost.account.model.entity.userAttendance.CostUserAttendanceCustomFieldData;
import com.hscloud.hs.cost.account.service.impl.kpi.KpiUserAttendanceService;
import com.hscloud.hs.cost.account.service.userAttendance.ICostUserAttendanceCustomFieldDataService;
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
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/userAttendance/costUserAttendanceCustomFieldData")
@Tag(name = "costUserAttendanceCustomFieldData", description = "人员考勤自定义字段表")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class CostUserAttendanceCustomFieldDataController {

    private final ICostUserAttendanceCustomFieldDataService costUserAttendanceCustomFieldDataService;

    private final KpiUserAttendanceService kpiUserAttendanceService;

    @SysLog("人员考勤自定义字段表info")
    @GetMapping("/info/{id}")
    @Operation(summary = "人员考勤自定义字段表info")
    public R<CostUserAttendanceCustomFieldData> info(@PathVariable Long id) {
        return R.ok(costUserAttendanceCustomFieldDataService.getById(id));
    }

    @SysLog("人员考勤自定义字段表page")
    @GetMapping("/page")
    @Operation(summary = "人员考勤自定义字段表page")
    public R<IPage<CostUserAttendanceCustomFieldData>> page(PageRequest<CostUserAttendanceCustomFieldData> pr) {
        return R.ok(costUserAttendanceCustomFieldDataService.page(pr.getPage(), pr.getWrapper()));
    }

    @SysLog("人员考勤自定义字段表list")
    @GetMapping("/list")
    @Operation(summary = "人员考勤自定义字段表list")
    public R<List<CostUserAttendanceCustomFieldData>> list(PageRequest<CostUserAttendanceCustomFieldData> pr) {
        kpiUserAttendanceService.copyCustomFields((String) pr.getQ().get("dt"));
        return R.ok(costUserAttendanceCustomFieldDataService.list(pr.getWrapper()));
    }

    @SysLog("人员考勤自定义字段表add")
    @PostMapping("/add")
    @Operation(summary = "人员考勤自定义字段表add")
    public R add(@RequestBody CostUserAttendanceCustomFieldData costUserAttendanceCustomFieldData) {
        return R.ok(costUserAttendanceCustomFieldDataService.save(costUserAttendanceCustomFieldData));
    }

    @SysLog("人员考勤自定义字段表edit")
    @PostMapping("/edit")
    @Operation(summary = "人员考勤自定义字段表edit")
    public R edit(@RequestBody CostUserAttendanceCustomFieldData costUserAttendanceCustomFieldData) {
        return R.ok(costUserAttendanceCustomFieldDataService.updateById(costUserAttendanceCustomFieldData));
    }

    @SysLog("人员考勤自定义字段表del")
    @PostMapping("/del/{id}")
    @Operation(summary = "人员考勤自定义字段表del")
    public R del(@PathVariable Long id) {
        return R.ok(costUserAttendanceCustomFieldDataService.removeById(id));
    }

    @SysLog("人员考勤自定义字段表delBatch")
    @PostMapping("/delBatch/{ids}")
    @Operation(summary = "人员考勤自定义字段表delBatch 1,2,3")
    public R delBatch(@PathVariable String ids) {
        return R.ok(costUserAttendanceCustomFieldDataService.removeBatchByIds(Arrays.asList(ids.split(","))));
    }
}