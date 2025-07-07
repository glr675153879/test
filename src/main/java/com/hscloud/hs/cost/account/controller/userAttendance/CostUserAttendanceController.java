package com.hscloud.hs.cost.account.controller.userAttendance;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hscloud.hs.cost.account.model.dto.userAttendance.*;
import com.hscloud.hs.cost.account.model.entity.base.PageRequest;
import com.hscloud.hs.cost.account.model.entity.userAttendance.CostUserAttendance;
import com.hscloud.hs.cost.account.model.vo.userAttendance.ValidateJobNumberVO;
import com.hscloud.hs.cost.account.model.vo.userAttendance.export.ImportErrVo;
import com.hscloud.hs.cost.account.service.impl.userAttendance.CostUserAttendanceConfigService;
import com.hscloud.hs.cost.account.service.impl.userAttendance.CostUserAttendanceCustomFieldsService;
import com.hscloud.hs.cost.account.service.userAttendance.ICostUserAttendanceService;
import com.hscloud.hs.cost.account.utils.ExcelUtil;
import com.pig4cloud.pigx.common.core.util.R;
import com.pig4cloud.pigx.common.log.annotation.SysLog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

/**
 * 人员考勤表
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/userAttendance/costUserAttendance")
@Tag(name = "costUserAttendance", description = "人员考勤表")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class CostUserAttendanceController {

    private final ICostUserAttendanceService costUserAttendanceService;
    private final CostUserAttendanceCustomFieldsService costUserAttendanceCustomFieldsService;
    private final CostUserAttendanceConfigService costUserAttendanceConfigService;

    @SysLog("人员考勤表info")
    @GetMapping("/info/{id}")
    @Operation(summary = "人员考勤表info")
    public R<CostUserAttendance> info(@PathVariable Long id) {
        return R.ok(costUserAttendanceService.getById(id));
    }

    @SysLog("人员考勤表page")
    @GetMapping("/page")
    @Operation(summary = "人员考勤表page")
    public R<IPage<CostUserAttendance>> page(PageRequest<CostUserAttendance> pr) {
        return R.ok(costUserAttendanceService.pageData(pr.getPage(), pr.getWrapper()));
    }

    @SysLog("人员考勤表list")
    @GetMapping("/list")
    @Operation(summary = "人员考勤表list")
    public R<List<CostUserAttendance>> list(PageRequest<CostUserAttendance> pr) {
        return R.ok(costUserAttendanceService.list(pr.getWrapper()));
    }

    @SysLog("人员考勤表add")
    @PostMapping("/add")
    @Operation(summary = "人员考勤表add")
    public R add(@RequestBody CostUserAttendance costUserAttendance) {
        return R.ok(costUserAttendanceService.save(costUserAttendance));
    }

    @SysLog("人员考勤表edit")
    @PostMapping("/edit")
    @Operation(summary = "人员考勤表edit")
    public R edit(@RequestBody CostUserAttendanceEditDto dto) {
        return R.ok(costUserAttendanceService.editWithCustomFields(dto));
    }

    @SysLog("人员考勤表del")
    @PostMapping("/del/{id}")
    @Operation(summary = "人员考勤表del")
    public R del(@PathVariable Long id) {
        return R.ok(costUserAttendanceService.removeById(id));
    }

    @SysLog("人员考勤表delBatch")
    @PostMapping("/delBatch/{ids}")
    @Operation(summary = "人员考勤表delBatch 1,2,3")
    public R delBatch(@PathVariable String ids) {
        return R.ok(costUserAttendanceService.removeBatchByIds(Arrays.asList(ids.split(","))));
    }

    @SysLog("模版下载")
    @GetMapping("/downloadTemplate")
    @Operation(summary = "模版下载")
    public void downloadTemplate(@RequestParam("dt") String dt, HttpServletResponse response) {
        costUserAttendanceService.downloadTemplate(dt, response);
    }

    @SysLog("导入数据")
    @PostMapping("/handleFileUpload")
    @Operation(summary = "导入数据")
    @Deprecated
    public R handleFileUpload(@RequestParam("file") MultipartFile file) {
        return costUserAttendanceService.handleFileUpload(file);
    }

    @SysLog("导入文件")
    @PostMapping("/uploadFile")
    @Operation(summary = "导入文件 continueFlag 1继续，2终止；overwriteFlag 1覆盖，2增量导入")
    public R<ImportErrVo> uploadFile(@RequestParam("file") MultipartFile file, ExcelImportDTO dto, String dt) throws Exception {
        String[][] xlsDataArr = ExcelUtil.doExcelH(file, 0);
        ImportErrVo importErrVo = costUserAttendanceService.uploadFile(xlsDataArr, dto, dt);
        return R.ok(importErrVo);
    }

    @SysLog("错误记录下载")
    @GetMapping("/downloadError")
    @Operation(summary = "downloadError")
    public void downloadError(@RequestParam("dt") String dt, HttpServletResponse response) {
        costUserAttendanceService.downloadError(dt, response);
    }

    @GetMapping("/exportData")
    @Operation(summary = "导出数据")
    public void exportData(PageRequest<CostUserAttendance> pr, HttpServletResponse response) {
        costUserAttendanceService.exportData(pr, response);
    }


    @SysLog("本月待匹配变动人员list")
    @GetMapping("/toMatchList")
    @Operation(summary = "本月待匹配变动人员list")
    public R<List<CostUserAttendanceDto>> toMatchList(PageRequest<CostUserAttendance> pr) {
        return R.ok(costUserAttendanceService.toMatchList(pr.getWrapper()));
    }

    @SysLog("手动匹配规则科室单元人员list")
    @GetMapping("/manualMatchList")
    @Operation(summary = "手动匹配规则科室单元人员list")
    public R<Page<CostUserAttendanceDto>> manualMatchList(PageRequest<CostUserAttendance> pr) {
        return R.ok(costUserAttendanceService.manualMatchList(pr));
    }

    @SysLog("系统匹配规则科室单元人员list")
    @GetMapping("/sysMatchList")
    @Operation(summary = "系统匹配规则科室单元人员list")
    public R<Page<CostUserAttendanceDto>> sysMatchList(PageRequest<CostUserAttendance> pr) {
        return R.ok(costUserAttendanceService.sysMatchList(pr));
    }

    @SysLog("人员确认匹配")
    @PostMapping("editList")
    @Operation(summary = "人员确认匹配")
    public R editList(@RequestBody CostUserAttendanceEditDto customParams) {
        return R.ok(costUserAttendanceService.updateData(customParams));
    }

    @SysLog("历史记录")
    @GetMapping("/historyList")
    @Operation(summary = "历史记录")
    public R<List<String>> historyList(PageRequest<CostUserAttendance> pr) {
        return R.ok(costUserAttendanceService.historyList(pr.getWrapper()));
    }

    @PreAuthorize("@pms.hasPermission('kpi_member_lock')")
    @SysLog("数据锁定")
    @PostMapping("/lockData")
    @Operation(summary = "数据锁定")
    public R lockData(@RequestBody LockDataDto pr) {
        String dt = pr.getDt();
        String current = LocalDateTime.now().minusMonths(1).format(DateTimeFormatter.ofPattern("yyyyMM"));
        costUserAttendanceService.lockData(pr.getDt());
        costUserAttendanceService.pullCustomFields(dt == null ? current : dt);
         return R.ok();
    }

    @PostMapping("/importData")
    @Operation(summary = "数据中台导入")
    public R<?> importData(@RequestBody DateDto dto) throws IOException {
        String dt = dto.getDt();
        costUserAttendanceService.importData(dt);
        return R.ok();
    }

    @SysLog("数据校验")
    @GetMapping("/validateData")
    @Operation(summary = "数据校验")
    public R validateData(@RequestParam(value = "dt", required = false) String dt) {
        String current = LocalDateTime.now().minusMonths(1).format(DateTimeFormatter.ofPattern("yyyyMM"));
        return costUserAttendanceService.validateData(dt == null ? current : dt);
    }

    @SysLog("校验工号")
    @GetMapping("/validateJobNumber")
    @Operation(summary = "校验工号")
    public R<List<ValidateJobNumberVO>> validateJobNumber(@RequestParam(value = "dt", required = false) String dt) {
        String current = LocalDateTime.now().minusMonths(1).format(DateTimeFormatter.ofPattern("yyyyMM"));
        return R.ok(costUserAttendanceService.validateJobNumber(dt == null ? current : dt));
    }

    @SysLog("一次性计算变量数据")
    @GetMapping("/calculateData")
    @Operation(summary = "一次性计算变量数据")
    public R calculateData(@RequestParam(value = "dt", required = false) String dt) {
        String current = LocalDateTime.now().minusMonths(1).format(DateTimeFormatter.ofPattern("yyyyMM"));
        costUserAttendanceService.calculateData(dt == null ? current : dt);
        costUserAttendanceService.pullCustomFields(dt == null ? current : dt);
        return R.ok();
    }


    @SysLog("将自定义字段导出到分表")
    @GetMapping("/pullCustomFields")
    @Operation(summary = "将自定义字段导出到分表")
    public R pullCustomFields(@RequestParam("dt") String dt) {
        costUserAttendanceService.pullCustomFields(dt);
        return R.ok();
    }

}