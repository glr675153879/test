package com.hscloud.hs.cost.account.controller.kpi;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.metadata.data.ReadCellData;
import com.alibaba.excel.util.ConverterUtils;
import com.baomidou.mybatisplus.core.conditions.AbstractWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bestvike.linq.Linq;
import com.hscloud.hs.cost.account.constant.enums.kpi.CategoryEnum;
import com.hscloud.hs.cost.account.mapper.kpi.KpiAccountTaskChildMapper;
import com.hscloud.hs.cost.account.mapper.kpi.KpiAccountTaskMapper;
import com.hscloud.hs.cost.account.mapper.kpi.KpiCategoryMapper;
import com.hscloud.hs.cost.account.mapper.kpi.KpiReportConfigMapper;
import com.hscloud.hs.cost.account.model.dto.kpi.*;
import com.hscloud.hs.cost.account.model.entity.kpi.*;
import com.hscloud.hs.cost.account.model.vo.kpi.KpiCalculateReportVO;
import com.hscloud.hs.cost.account.model.vo.kpi.KpiCalculateReportVO2;
import com.hscloud.hs.cost.account.service.kpi.IKpiReportConfigService;
import com.pig4cloud.pigx.common.core.exception.BizException;
import com.pig4cloud.pigx.common.core.util.R;
import com.pig4cloud.pigx.common.log.annotation.SysLog;
import com.pig4cloud.pigx.common.security.util.SecurityUtils;
import io.netty.util.internal.StringUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

/**
* 报表多选配置
*
*/
@RestController
@RequiredArgsConstructor
@RequestMapping("/kpi/kpiReportConfig")
@Tag(name = "kpiReportConfig", description = "报表多选配置")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
@Slf4j
public class KpiReportConfigController {

    private final IKpiReportConfigService kpiReportConfigService;

    @SysLog("报表多选配置list ")
    @GetMapping("/list")
    @Operation(summary = "报表多选配置list ")
    public R<List<KpiReportConfigListDTO>> page(String group, String type, String name, Long taskChildId ,String status) {
        return R.ok(kpiReportConfigService.getList(group,type,name,taskChildId,status));
    }

    @SysLog("报表多选配置add")
    @PostMapping("/add")
    @Operation(summary = "报表多选配置add")
    public R<Long> add(@RequestBody KpiReportConfig kpiReportConfig)  {
        kpiReportConfig.setTenantId(SecurityUtils.getUser().getTenantId());
        kpiReportConfigService.save(kpiReportConfig);
        return R.ok(kpiReportConfig.getId());
    }

    @SysLog("报表多选配置edit")
    @PostMapping("/edit")
    @Operation(summary = "报表多选配置edit")
    public R<Long> edit(@RequestBody KpiReportConfig kpiReportConfig)  {
        kpiReportConfigService.updateById(kpiReportConfig);
        return R.ok(kpiReportConfig.getId());
    }

    @SysLog("报表多选配置del")
    @PostMapping("/del/{id}")
    @Operation(summary = "报表多选配置del")
    public R del(@PathVariable Long id)  {
        return R.ok(kpiReportConfigService.removeById(id));
    }

    @SysLog("报表多选配置启用停用")
    @PostMapping("/enable")
    @Operation(summary = "报表多选配置启用停用")
    public R enable(@RequestBody KpiIndexEnableDto input)  {
        kpiReportConfigService.enable(input);
        return R.ok();
    }

    @SysLog("报表")
    @GetMapping("/report")
    @Operation(summary = "报表")
    public R<KpiCalculateReportVO2> report(KpiReportConfigDto input) {
        return R.ok(kpiReportConfigService.report(input));
    }

    @SysLog("年度报表")
    @GetMapping("/year/report")
    @Operation(summary = "年度报表")
    public R<List<KpiReportYearImport>> yearReport(KpiReportYearDto input) {
        return R.ok(kpiReportConfigService.yearReport(input));
    }

    @SysLog("年度报表 过滤权限")
    @GetMapping("/year/power/report")
    @Operation(summary = "年度报表 过滤权限")
    public R<List<KpiReportYearImport>> yearPowerReport(KpiReportYearDto input) {
        return R.ok(kpiReportConfigService.yearPowerReport(input));
    }

    @SysLog("报表第一层下转 /report的id入参")
    @GetMapping("/report/id")
    @Operation(summary = "报表第一层下转 /report的id入参")
    public R<KpiCalculateReportVO> reportId(KpiCalculateReportDTO2 input) {
        return R.ok(kpiReportConfigService.reportId(input));
    }

    @SysLog("二次报表 权限配置")
    @PostMapping("/power/ed")
    @Operation(summary = "二次报表 权限配置")
    public R powerEd(@RequestBody KpiReportConfigPowerDto input)  {
        kpiReportConfigService.powerEd(input);
        return R.ok();
    }

    @SysLog("二次报表 权限配置详情")
    @GetMapping("/power/detail")
    @Operation(summary = "二次报表 权限配置")
    public R<List<KpiReportConfigPowerListDTO>> powerDetail(Long reportId)  {
        return R.ok(kpiReportConfigService.powerDetail(reportId));
    }

    @SysLog("二次报表 左侧权限")
    @GetMapping("/second/power")
    @Operation(summary = "二次报表 左侧权限")
    public R<List<KpiReportConfig>> powerLeft()  {
        return R.ok(kpiReportConfigService.powerLeft());
    }

    @SysLog("二次报表")
    @GetMapping("/second/report")
    @Operation(summary = "二次报表")
    public R<KpiCalculateReportVO2> reportSecond(KpiReportConfigDto input) {
        return R.ok(kpiReportConfigService.reportSecond(input));
    }

    @SysLog("下发列表 入参1 锁定， 2 下发")
    @GetMapping("/send/list")
    @Operation(summary = "下发列表 入参1 锁定， 2 下发")
    public R<List<KpiConfig>> sendList(Long input) {
        return R.ok(kpiReportConfigService.sendList(input));
    }

    @SysLog("导入")
    @Operation(summary = "导入")
    @PostMapping("/import")
    public R importData(@RequestParam(value = "file") MultipartFile file,
                        @RequestParam(value ="taskChildId") Long taskChildId,
                        @RequestParam(value ="reportId") Long reportId) throws IOException {
        List<Map<Integer,String>> list=new ArrayList<>();
        try {
            EasyExcel.read(file.getInputStream(), new AnalysisEventListener<Map<Integer,String>>() {
                @Override
                public void invoke(Map<Integer,String> data, AnalysisContext context) {
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
            if (list.isEmpty()){
                throw new BizException("未识别到数据");
            }
            kpiReportConfigService.importData(taskChildId,reportId,list,false,null);
            System.out.println(1);
        } catch (Exception e) {
            e.printStackTrace();
            throw new BizException(e.getMessage());
        }finally {
            file.getInputStream().close();
        }
        return R.ok();
    }

    @SysLog("年度报表导入")
    @Operation(summary = "年度报表导入")
    @PostMapping("/year/import")
    public R yearImportData(@RequestParam(value = "file") MultipartFile file,
                        @RequestParam(value ="taskChildId") Long taskChildId,
                        @RequestParam(value ="reportId") Long reportId) throws IOException {
        List<Map<Integer,String>> list=new ArrayList<>();
        try {
            EasyExcel.read(file.getInputStream(), new AnalysisEventListener<Map<Integer,String>>() {
                @Override
                public void invoke(Map<Integer,String> data, AnalysisContext context) {
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
            if (list.isEmpty()){
                throw new BizException("未识别到数据");
            }
            kpiReportConfigService.yearImportData(taskChildId,reportId,list);
            System.out.println(1);
        } catch (BizException e) {
            throw new BizException(e.getDefaultMessage());
        }catch (Exception e) {
            e.printStackTrace();
        }finally {
            file.getInputStream().close();
        }
        return R.ok();
    }
}