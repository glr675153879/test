package com.hscloud.hs.cost.account.controller.userAttendance;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hscloud.hs.cost.account.model.entity.base.PageRequest;
import com.hscloud.hs.cost.account.model.entity.userAttendance.UserAttendanceLog;
import com.hscloud.hs.cost.account.model.vo.userAttendance.UserAttendanceLogVO;
import com.hscloud.hs.cost.account.service.userAttendance.IUserAttendanceLogService;
import com.pig4cloud.pigx.common.core.util.R;
import com.pig4cloud.pigx.common.excel.annotation.ResponseExcel;
import com.pig4cloud.pigx.common.log.annotation.SysLog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 人员考勤表变更日志
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/userAttendance/userAttendanceLog")
@Tag(name = "userAttendanceLog", description = "人员考勤表变更日志")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class UserAttendanceLogController {

    private final IUserAttendanceLogService userAttendanceLogService;

    @SysLog("人员考勤表变更日志info")
    @GetMapping("/info/{id}")
    @Operation(summary = "人员考勤表变更日志info")
    public R<UserAttendanceLog> info(@PathVariable Long id) {
        return R.ok(userAttendanceLogService.getById(id));
    }

    @SysLog("人员考勤表变更日志page")
    @GetMapping("/page")
    @Operation(summary = "人员考勤表变更日志page")
    public R<IPage<UserAttendanceLog>> page(PageRequest<UserAttendanceLog> pr) {
        LambdaQueryWrapper<UserAttendanceLog> wrapper = pr.getWrapper().lambda();
        wrapper.orderByDesc(UserAttendanceLog::getCreateTime);
        return R.ok(userAttendanceLogService.page(pr.getPage(), wrapper));
    }

    @SysLog("人员考勤表变更日志list")
    @GetMapping("/list")
    @Operation(summary = "人员考勤表变更日志list")
    public R<List<UserAttendanceLog>> list(PageRequest<UserAttendanceLog> pr) {
        return R.ok(userAttendanceLogService.list(pr.getWrapper()));
    }

    @SysLog("人员考勤表变更日志add")
    @PostMapping("/add")
    @Operation(summary = "人员考勤表变更日志add")
    public R add(@RequestBody UserAttendanceLog userAttendanceLog) {
        return R.ok(userAttendanceLogService.save(userAttendanceLog));
    }

    @SysLog("人员考勤表变更日志edit")
    @PostMapping("/edit")
    @Operation(summary = "人员考勤表变更日志edit")
    public R edit(@RequestBody UserAttendanceLog userAttendanceLog) {
        return R.ok(userAttendanceLogService.updateById(userAttendanceLog));
    }

    @SysLog("人员考勤表变更日志del")
    @PostMapping("/del/{id}")
    @Operation(summary = "人员考勤表变更日志del")
    public R del(@PathVariable Long id) {
        return R.ok(userAttendanceLogService.removeById(id));
    }

    @SysLog("人员考勤表变更日志delBatch")
    @PostMapping("/delBatch/{ids}")
    @Operation(summary = "人员考勤表变更日志delBatch 1,2,3")
    public R delBatch(@PathVariable String ids) {
        return R.ok(userAttendanceLogService.removeBatchByIds(Arrays.asList(ids.split(","))));
    }

    @ResponseExcel(name = "人员考勤表变更日志导出")
    @GetMapping("/export")
    @Operation(summary = "人员考勤表变更日志导出")
    public List<UserAttendanceLogVO> exportGroup() {
        List<UserAttendanceLogVO> rtnList = new ArrayList<>();
        LambdaQueryWrapper<UserAttendanceLog> qr = new LambdaQueryWrapper<>();
        qr.orderByDesc(UserAttendanceLog::getOpsTime);
        List<UserAttendanceLog> list = userAttendanceLogService.list(qr);
        list.forEach(item -> {
            UserAttendanceLogVO vo = new UserAttendanceLogVO();
            BeanUtils.copyProperties(item, vo);
            rtnList.add(vo);
        });
        return rtnList;
    }
}