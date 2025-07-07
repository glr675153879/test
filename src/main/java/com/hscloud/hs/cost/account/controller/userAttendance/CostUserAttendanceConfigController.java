package com.hscloud.hs.cost.account.controller.userAttendance;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hscloud.hs.cost.account.model.entity.base.PageRequest;
import com.hscloud.hs.cost.account.model.entity.userAttendance.CostUserAttendanceConfig;
import com.hscloud.hs.cost.account.service.userAttendance.ICostUserAttendanceConfigService;
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
 * 人员考勤配置表
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/userAttendance/costUserAttendanceConfig")
@Tag(name = "costUserAttendanceConfig", description = "人员考勤配置表")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class CostUserAttendanceConfigController {

    private final ICostUserAttendanceConfigService costUserAttendanceConfigService;

    @SysLog("人员考勤配置表info")
    @GetMapping("/info/{id}")
    @Operation(summary = "人员考勤配置表info")
    public R<CostUserAttendanceConfig> info(@PathVariable Long id) {
        return R.ok(costUserAttendanceConfigService.getById(id));
    }

    @SysLog("人员考勤配置表page")
    @GetMapping("/page")
    @Operation(summary = "人员考勤配置表page")
    public R<IPage<CostUserAttendanceConfig>> page(PageRequest<CostUserAttendanceConfig> pr) {
        return R.ok(costUserAttendanceConfigService.page(pr.getPage(), pr.getWrapper().lambda().orderByDesc(CostUserAttendanceConfig::getDt)));
    }

    @SysLog("人员考勤配置表list")
    @GetMapping("/list")
    @Operation(summary = "人员考勤配置表list")
    public R<List<CostUserAttendanceConfig>> list(PageRequest<CostUserAttendanceConfig> pr) {
        return R.ok(costUserAttendanceConfigService.list(pr.getWrapper().lambda().orderByDesc(CostUserAttendanceConfig::getDt)));
    }

    @SysLog("人员考勤配置表add")
    @PostMapping("/add")
    @Operation(summary = "人员考勤配置表add")
    public R add(@RequestBody CostUserAttendanceConfig costUserAttendanceConfig) {
        return R.ok(costUserAttendanceConfigService.save(costUserAttendanceConfig));
    }

    @SysLog("人员考勤配置表edit")
    @PostMapping("/edit")
    @Operation(summary = "人员考勤配置表edit")
    public R edit(@RequestBody CostUserAttendanceConfig costUserAttendanceConfig) {
        return R.ok(costUserAttendanceConfigService.updateById(costUserAttendanceConfig));
    }

    @SysLog("人员考勤配置表del")
    @PostMapping("/del/{id}")
    @Operation(summary = "人员考勤配置表del")
    public R del(@PathVariable Long id) {
        return R.ok(costUserAttendanceConfigService.removeById(id));
    }

    @SysLog("人员考勤配置表delBatch")
    @PostMapping("/delBatch/{ids}")
    @Operation(summary = "人员考勤配置表delBatch 1,2,3")
    public R delBatch(@PathVariable String ids) {
        return R.ok(costUserAttendanceConfigService.removeBatchByIds(Arrays.asList(ids.split(","))));
    }
}