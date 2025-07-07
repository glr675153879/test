package com.hscloud.hs.cost.account.controller.kpi;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.metadata.data.ReadCellData;
import com.alibaba.excel.util.ConverterUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hscloud.hs.cost.account.mapper.kpi.KpiAccountTaskMapper;
import com.hscloud.hs.cost.account.model.dto.kpi.*;
import com.hscloud.hs.cost.account.model.dto.userAttendance.DateDto;
import com.hscloud.hs.cost.account.model.dto.userAttendance.ExcelExportDTO;
import com.hscloud.hs.cost.account.model.dto.userAttendance.ExcelImportDTO;
import com.hscloud.hs.cost.account.model.entity.base.PageRequest;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiConfig;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiHsUserRule;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiUserAttendance;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiUserCalculationRule;
import com.hscloud.hs.cost.account.model.vo.userAttendance.ValidateJobNumberVO;
import com.hscloud.hs.cost.account.model.vo.userAttendance.export.ImportErrVo;
import com.hscloud.hs.cost.account.service.impl.kpi.KpiConfigService;
import com.hscloud.hs.cost.account.service.impl.kpi.KpiImputationService;
import com.hscloud.hs.cost.account.service.kpi.IKpiUserAttendanceService;
import com.hscloud.hs.cost.account.utils.ExcelUtil;
import com.pig4cloud.pigx.common.core.exception.BizException;
import com.pig4cloud.pigx.common.core.util.R;
import com.pig4cloud.pigx.common.log.annotation.SysLog;
import com.pig4cloud.pigx.common.security.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 人员考勤表(cost_user_attendance)
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/kpi/kpiUserAttendance")
@Tag(name = "kpiUserAttendance", description = "kpi_人员考勤表")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
@Slf4j
public class KpiUserAttendanceController {

    private final IKpiUserAttendanceService kpiUserAttendanceService;
    private final KpiImputationService kpiImputationService;
    private final KpiAccountTaskMapper kpiAccountTaskMapper;
    private final KpiConfigService kpiConfigService;

    @SysLog("人员考勤表info")
    @GetMapping("/info/{id}")
    @Operation(summary = "人员考勤表(cost_user_attendance)info")
    public R<KpiUserAttendance> info(@PathVariable Long id) {
        return R.ok(kpiUserAttendanceService.getById(id));
    }

    @SysLog("人员考勤表page")
    @GetMapping("/page")
    @Operation(summary = "人员考勤表page")
    public R<IPage<KpiUserAttendance>> page(PageRequest<KpiUserAttendance> pr, String busiType, String period) {
        return R.ok(kpiUserAttendanceService.pageData(pr.getPage(), pr.getWrapper(), busiType, period, pr.getQ()));
    }

    @SysLog("人员考勤表list")
    @GetMapping("/list")
    @Operation(summary = "人员考勤表list")
    public R<List<KpiUserAttendance>> list(PageRequest<KpiUserAttendance> pr) {
        return R.ok(kpiUserAttendanceService.list(pr.getWrapper()));
    }

    @SysLog("人员考勤表)add")
    @PostMapping("/add")
    @Operation(summary = "人员考勤表add")
    public R<Long> add(@RequestBody KpiUserAttendance kpiUserAttendance) {
        return R.ok(kpiUserAttendanceService.insertData(kpiUserAttendance));
    }

    @SysLog("人员考勤表edit")
    @PostMapping("/edit")
    @Operation(summary = "人员考勤表edit")
    public R edit(@RequestBody KpiUserAttendanceEditDto dto) {
        return R.ok(kpiUserAttendanceService.editWithCustomFields(dto));
    }

    @SysLog("人员考勤表(cost_user_attendance)del")
    @PostMapping("/del/{id}")
    @Operation(summary = "人员考勤表(cost_user_attendance)del")
    public R del(@PathVariable Long id) {
        return R.ok(kpiUserAttendanceService.removeById(id));
    }

    @SysLog("人员考勤表(cost_user_attendance)delBatch")
    @PostMapping("/delBatch/{ids}")
    @Operation(summary = "人员考勤表(cost_user_attendance)delBatch 1,2,3")
    public R delBatch(@PathVariable String ids) {
        return R.ok(kpiUserAttendanceService.removeBatchByIds(Arrays.asList(ids.split(","))));
    }

    @SysLog("模版下载")
    @GetMapping("/downloadTemplate")
    @Operation(summary = "模版下载")
    public void downloadTemplate(@RequestParam("dt") String dt, HttpServletResponse response) {
        kpiUserAttendanceService.copyCustomFields(dt);
        kpiUserAttendanceService.downloadTemplate(dt, response);
    }

    @SysLog("导入数据")
    @PostMapping("/handleFileUpload")
    @Operation(summary = "导入数据")
    @Deprecated
    public R handleFileUpload(@RequestParam("file") MultipartFile file) {
        return kpiUserAttendanceService.handleFileUpload(file);
    }

    @SysLog("导入文件")
    @PostMapping("/uploadFile")
    @Operation(summary = "导入文件 continueFlag 1继续，2终止；overwriteFlag 1覆盖，2增量导入")
    public R<ImportErrVo> uploadFile(@RequestParam("file") MultipartFile file, ExcelImportDTO dto, Long dt) throws Exception {
        String[][] xlsDataArr = ExcelUtil.doExcelH(file, 0);
        kpiUserAttendanceService.copyCustomFields(String.valueOf(dt));
        ImportErrVo importErrVo = kpiUserAttendanceService.uploadFile(xlsDataArr, dto, dt);
        return R.ok(importErrVo);
    }

    @GetMapping("/exportData")
    @Operation(summary = "导出数据")
    public void exportData(ExcelExportDTO period, HttpServletResponse response) {
        kpiUserAttendanceService.exportData(period, response);
    }

    @SysLog("本月待匹配变动人员list")
    @GetMapping("/toMatchList")
    @Operation(summary = "本月待匹配变动人员list")
    public R<IPage<KpiUserAttendanceDto>> toMatchList(KpiChangeUseSearchDto pr) {
        return R.ok(kpiUserAttendanceService.toMatchList(pr));
    }

    @SysLog("手动匹配规则科室单元人员list")
    @GetMapping("/manualMatchList")
    @Operation(summary = "手动匹配规则科室单元人员list")
    public R<IPage<KpiUserAttendanceDto>> manualMatchList(KpiChangeUseSearchDto pr) {
        return R.ok(kpiUserAttendanceService.manualMatchList(pr));
    }

    @SysLog("系统匹配规则科室单元人员list")
    @GetMapping("/sysMatchList")
    @Operation(summary = "系统匹配规则科室单元人员list")
    public R<IPage<KpiUserAttendanceDto>> sysMatchList(KpiChangeUseSearchDto pr) {
        return R.ok(kpiUserAttendanceService.sysMatchList(pr));
    }

    @SysLog("人员确认匹配")
    @PostMapping("editList")
    @Operation(summary = "人员确认匹配")
    public R editList(@RequestBody KpiUserAttendanceEditDto customParams) {
        return R.ok(kpiUserAttendanceService.updateData(customParams));
    }

    @SysLog("历史记录")
    @GetMapping("/historyList")
    @Operation(summary = "历史记录")
    public R<List<Long>> historyList(PageRequest<KpiUserAttendance> pr) {
        return R.ok(kpiUserAttendanceService.historyList(pr.getWrapper()));
    }

//    @PreAuthorize("@pms.hasPermission('kpi_member_lock')")
    @SysLog("数据锁定")
    @PostMapping("/lockData")
    @Operation(summary = "数据锁定")
    public R lockData(@RequestBody LockDataDto2 pr) {
        if (kpiAccountTaskMapper.getShedLog("('cost_refresh','cost_lockData')") > 0) {
            throw new BizException("数据正在锁定");
        }
        String dt = pr.getPeriod();
        Long tenantId = SecurityUtils.getUser().getTenantId();
        List<KpiConfig> list = kpiConfigService.list(new LambdaQueryWrapper<KpiConfig>()
                .eq(KpiConfig::getPeriod, Long.valueOf(dt)));
        if (pr.getBusiType().equals("1") && list.get(0).getIssuedFlag().equals("Y")) {
            throw new BizException("该月数据已发布，无法解锁");
        }
        new Thread(() -> {
            try {
                kpiImputationService.refresh(null, Long.valueOf(dt), pr.getBusiType(), tenantId, Long.valueOf(dt), false);
                kpiUserAttendanceService.lockData(pr.getPeriod(), pr.getBusiType(), tenantId, false);
            } catch (Exception exception) {
                log.error("数据锁定异常", exception);
            }
        }).start();
        //kpiUserAttendanceService.pullCustomFields(dt == null ? current : dt);
        return R.ok();
    }


    @PostMapping("/importData")
    @Operation(summary = "数据中台导入")
    public R<?> importData(@RequestBody DateDto dto) throws IOException {
        String dt = dto.getDt();
        kpiUserAttendanceService.importData(dto);
        return R.ok();
    }

    @SysLog("数据校验")
    @GetMapping("/validateData")
    @Operation(summary = "数据校验")
    public R validateData(@RequestParam(value = "period", required = false) String period, @RequestParam("busiType") String busiType) {
        String current = LocalDateTime.now().minusMonths(1).format(DateTimeFormatter.ofPattern("yyyyMM"));
        return kpiUserAttendanceService.validateData(period == null ? current : period, busiType);
    }

    @SysLog("校验工号")
    @GetMapping("/validateJobNumber")
    @Operation(summary = "校验工号")
    public R<List<ValidateJobNumberVO>> validateJobNumber(@RequestParam(value = "dt", required = false) String dt, @RequestParam("busiType") String busiType) {
        String current = LocalDateTime.now().minusMonths(1).format(DateTimeFormatter.ofPattern("yyyyMM"));
        return R.ok(kpiUserAttendanceService.validateJobNumber(dt == null ? current : dt, busiType));
    }

    @SysLog("一次性计算变量数据")
    @GetMapping("/calculateData")
    @Operation(summary = "一次性计算变量数据")
    public R calculateData(@RequestParam(value = "period", required = false) String period, @RequestParam("busiType") String busiType) {
        String current = LocalDateTime.now().minusMonths(1).format(DateTimeFormatter.ofPattern("yyyyMM"));
        kpiUserAttendanceService.calculateData(period == null ? current : period, busiType);
        kpiUserAttendanceService.pullCustomFields(period == null ? current : period, busiType, SecurityUtils.getUser().getTenantId());
        return R.ok();
    }

    @SysLog("将自定义字段导出到分表")
    @GetMapping("/pullCustomFields")
    @Operation(summary = "将自定义字段导出到分表")
    public R pullCustomFields(@RequestParam("dt") String dt, @RequestParam("busiType") String busiType) {
        kpiUserAttendanceService.pullCustomFields(dt, busiType, SecurityUtils.getUser().getTenantId());
        return R.ok();
    }

    @SysLog("新增核算人员")
    @PostMapping("/addEmpRole")
    @Operation(summary = "新增核算人员")
    public R edit(@RequestBody KpiAccountUserAddDto dto) {
        kpiUserAttendanceService.AddUser(dto);
        return R.ok();
    }

    @SysLog("核算人员page")
    @GetMapping("/hs_page")
    @Operation(summary = "核算人员page")
    public R<IPage<KpiAccountUserDto>> hs_page(KpiAccountUseSearchDto dto) {
        return R.ok(kpiUserAttendanceService.hs_page(dto));
    }

    @SysLog("核算人员 导入")
    @Operation(summary = "核算人员 导入")
    @PostMapping("/import")
    public R<List<String>> importData(@RequestParam(value = "file") MultipartFile file,
                                      @RequestParam(value = "categoryCode") String categoryCode,
                                      @RequestParam(value = "overwriteFlag") String overwriteFlag) throws IOException {
        List<Map<Integer, String>> list = new ArrayList<>();
        try {
            EasyExcel.read(file.getInputStream(), new AnalysisEventListener<Map<Integer, String>>() {
                @Override
                public void invoke(Map<Integer, String> data, AnalysisContext context) {
                    Map<Integer, String> in = new HashMap<>(data);
                    list.add(in);
                }

                @Override
                public void doAfterAllAnalysed(AnalysisContext context) {
                    // 解析完成后的操作
                }

                @Override
                public void invokeHead(Map<Integer, ReadCellData<?>> headMap, AnalysisContext context) {
                    Map<Integer, String> integerStringMap = ConverterUtils.convertToStringMap(headMap, context);
                    list.add(integerStringMap);
                }
            }).sheet(0).doRead();
            if (list.isEmpty()) {
                throw new BizException("未识别到数据");
            }
            System.out.println(1);
            return R.ok(kpiUserAttendanceService.importData2(categoryCode, list, overwriteFlag));
        } catch (BizException e) {
            return R.failed(e.getDefaultMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return R.failed(e.getMessage());
        } finally {
            file.getInputStream().close();
        }
    }

    @SysLog("核算人员edit")
    @PostMapping("/hs_edit")
    @Operation(summary = "核算人员edit")
    public R edit(@RequestBody KpiHsUserEditDto dto) {
        kpiUserAttendanceService.editUser(dto, "1");
        return R.ok();
    }

    @SysLog("核算人员del")
    @PostMapping("/hs_del/{id}")
    @Operation(summary = "核算人员del")
    public R hs_del(@PathVariable Long id) {
        kpiUserAttendanceService.delUser(id);
        return R.ok();
    }

    @SysLog("字典系数表edit")
    @PostMapping("/coefficient_edit")
    @Operation(summary = "字典系数表edit")
    public R coefficient_edit(@RequestBody KpiCoefficientDto2 dto) {
        kpiUserAttendanceService.addDicCoefficient(dto);
        return R.ok();
    }

    @SysLog("字典系数page")
    @GetMapping("/coefficient_page")
    @Operation(summary = "字典系数page")
    public R<List<KpiCoefficientPageDto>> coefficient_page(KpiCoefficientDto pr) {
        return R.ok(kpiUserAttendanceService.pageCoefficient(pr));
    }

    @SysLog("调整表page")
    @GetMapping("/value_adjust_page")
    @Operation(summary = "调整表page")
    public R<IPage<KpiValueAdjustPageDto>> pageValueAdjust(KpiValueAdjustSearchDto dto) {
        return R.ok(kpiUserAttendanceService.pageValueAdjust(dto));
    }

    @SysLog("调整表edit")
    @PostMapping("/value_adjust_edit")
    @Operation(summary = "调整表edit")
    public R value_adjust_edit(@RequestBody KpiValueAdjustDto dto) {
        kpiUserAttendanceService.addValueAdjust(dto);
        return R.ok();
    }

    @SysLog("调整表复制")
    @PostMapping("/value_adjust_copy")
    @Operation(summary = "调整表复制")
    public R value_adjust_copy(@RequestBody KpiValueAdjustCopyDto dto) {
        kpiUserAttendanceService.copyValueAdjust(dto);
        return R.ok();
    }

    @SysLog("调整表del")
    @PostMapping("/value_adjust_del/{id}")
    @Operation(summary = "调整表del")
    public R value_adjust_del(@PathVariable Long id) {
        kpiUserAttendanceService.delValueAdjust(id);
        return R.ok();
    }

    @SysLog("导入更新规则列表")
    @GetMapping("/calculationRule_page")
    @Operation(summary = "导入更新规则列表")
    public R<List<KpiUserCalculationRule>> calculationRule_page(String busiType) {
        return R.ok(kpiUserAttendanceService.pageCalculationRule(busiType));
    }

    @SysLog("导入更新规则表edit")
    @PostMapping("/calculationRule_edit")
    @Operation(summary = "导入更新规则表edit 启用停用也在")
    public R calculationRule_edit(@RequestBody CalculationRuleInsertDto dto) {
        kpiUserAttendanceService.addCalculationRule(dto);
        return R.ok();
    }

    @SysLog("考勤校验")
    @GetMapping("/attendance/check")
    @Operation(summary = "考勤校验")
    public R<List<AttendanceCheckDTO>> attendanceCheck(String busiType, Long period) {
        return R.ok(kpiUserAttendanceService.attendanceCheck(busiType, period));
    }

    @SysLog("核算人员规则表edit")
    @PostMapping("/hsUserRule_edit")
    @Operation(summary = "核算人员规则表edit 启用停用也在")
    public R hsUserRule_edit(@RequestBody HsUserRuleInsertDto dto) {
        kpiUserAttendanceService.addHsUserRule(dto);
        return R.ok();
    }

    @SysLog("核算人员规则表")
    @GetMapping("/hsUserRule_page")
    @Operation(summary = "核算人员规则表")
    public R<List<KpiHsUserRule>> hsUserRule_page(HsUserRuleInsertDto dto) {
        return R.ok(kpiUserAttendanceService.pageHsUserRule(dto));
    }

    @SysLog("核算人员规则表del")
    @PostMapping("/hsUserRule_del/{id}")
    @Operation(summary = "核算人员规则表del")
    public R hsUserRule_del(@PathVariable Long id) {
        kpiUserAttendanceService.hsUserRule_del(id);
        return R.ok();
    }

    @SysLog("核算人员刷新")
    @PostMapping("/hsUser_refresh")
    @Operation(summary = "核算人员刷新")
    public R<String> refresh(@RequestBody HsUserRuleInsertDto dto) {
        return R.ok(kpiUserAttendanceService.getNewHsUser(dto));
    }

    @SysLog("字典搜索")
    @GetMapping("/dicSearch")
    @Operation(summary = "字典搜索")
    public R<IPage<DicPageOutDto>> dicSearch(DicPageDto dto) {
        return R.ok(kpiUserAttendanceService.protectDic(dto));
    }


    @SysLog("配置默认考勤天数")
    @PostMapping("/editMonthDays")
    @Operation(summary = "*配置默认考勤天数")
    public R editMonthDays(@RequestBody KpiAttendanceMonthDaysListDto dto) {
        kpiUserAttendanceService.editMonthDays(dto);
        return R.ok();
    }

    @SysLog("默认天数列表")
    @GetMapping("/MonthDays")
    @Operation(summary = "默认天数列表")
    public R<List<KpiAttendanceMonthDaysDto>> attendanceCheck(Long year) {
        return R.ok(kpiUserAttendanceService.monthDays(year));
    }

    @SysLog("引入上月考勤数据")
    @PostMapping("/copyAttendance")
    @Operation(summary = "引入上月考勤数据 周期传上个月")
    public R copyAttendance(@RequestBody DateDto dto) {
        kpiUserAttendanceService.copyAttendance(dto);
        return R.ok();
    }
}