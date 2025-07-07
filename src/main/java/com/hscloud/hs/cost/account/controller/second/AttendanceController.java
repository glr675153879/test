package com.hscloud.hs.cost.account.controller.second;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hscloud.hs.cost.account.model.entity.base.PageRequest;
import com.hscloud.hs.cost.account.model.entity.second.Attendance;
import com.hscloud.hs.cost.account.service.second.IAttendanceService;
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
 * 考勤表
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/second/attendance")
@Tag(name = "attendance", description = "考勤表")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class AttendanceController {

    private final IAttendanceService attendanceService;

    @SysLog("考勤表info")
    @GetMapping("/info/{id}")
    @Operation(summary = "考勤表info")
    public R<Attendance> info(@PathVariable Long id) {
        return R.ok(attendanceService.getById(id));
    }

    @SysLog("考勤表page")
    @GetMapping("/page")
    @Operation(summary = "考勤表page")
    public R<IPage<Attendance>> page(PageRequest<Attendance> pr) {
        return R.ok(attendanceService.page(pr.getPage(), pr.getWrapper()));
    }

    @SysLog("考勤表list")
    @GetMapping("/list")
    @Operation(summary = "考勤表list")
    public R<List<Attendance>> list(PageRequest<Attendance> pr) {
        return R.ok(attendanceService.list(pr.getWrapper()));
    }

    @SysLog("考勤表add")
    @PostMapping("/add")
    @Operation(summary = "考勤表add")
    public R add(@RequestBody Attendance attendance) {
        return R.ok(attendanceService.save(attendance));
    }

    @SysLog("考勤表edit")
    @PostMapping("/edit")
    @Operation(summary = "考勤表edit")
    public R edit(@RequestBody Attendance attendance) {
        return R.ok(attendanceService.updateById(attendance));
    }

    @SysLog("考勤表del")
    @PostMapping("/del/{id}")
    @Operation(summary = "考勤表del")
    public R del(@PathVariable Long id) {
        return R.ok(attendanceService.removeById(id));
    }

    @SysLog("考勤表delBatch")
    @PostMapping("/delBatch/{ids}")
    @Operation(summary = "考勤表delBatch 1,2,3")
    public R delBatch(@PathVariable String ids) {
        return R.ok(attendanceService.removeBatchByIds(Arrays.asList(ids.split(","))));
    }


    @SysLog("分页查询转科人员")
    @GetMapping("/pageMoreDept/{cycle}")
    @Operation(summary = "分页查询转科人员")
    public R<IPage<Attendance>> pageMoreDept(PageRequest<Attendance> pr, @PathVariable String cycle) {
        return R.ok(attendanceService.pageMoreDept(pr, cycle));
    }

    @SysLog("分页考勤表人员")
    @GetMapping("/pagePerson")
    @Operation(summary = "分页考勤表人员")
    public R<IPage<Attendance>> pagePerson(PageRequest<Attendance> pr) {
        return R.ok();
    }
}