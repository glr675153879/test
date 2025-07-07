package com.hscloud.hs.cost.account.controller.kpi;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.metadata.data.ReadCellData;
import com.alibaba.excel.util.ConverterUtils;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hscloud.hs.cost.account.model.dto.kpi.*;
import com.hscloud.hs.cost.account.model.dto.kpi.excel.ExcelKpiCalculateDTO;
import com.hscloud.hs.cost.account.model.vo.kpi.*;
import com.hscloud.hs.cost.account.service.kpi.IKpiReportService;
import com.hscloud.hs.cost.account.utils.kpi.ExcelUtil;
import com.pig4cloud.pigx.common.core.exception.BizException;
import com.pig4cloud.pigx.common.core.util.R;
import com.pig4cloud.pigx.common.log.annotation.SysLog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/kpi/kpiReport")
@Tag(name = "k_一次任务报表", description = "k_一次任务报表")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class KpiReportController {

    @Autowired
    private IKpiReportService kpiReportService;

    @SysLog("报表接口配置")
    @PostMapping("/cu")
    @Operation(summary = "报表接口配置")
    public R cu(@RequestBody KpiCodeDTO input) {
        kpiReportService.cu(input);
        return R.ok();
    }

    @SysLog("报表接口详情配置")
    @PostMapping("/detail/cu")
    @Operation(summary = "报表接口详情配置")
    public R detailCu(@RequestBody KpiReportDetailDTO input) {
        kpiReportService.detailCu(input);
        return R.ok();
    }

    @SysLog("接口配置 分页")
    @GetMapping("/page")
    @Operation(summary = "接口配置 分页")
    public R<IPage<KpiReportVO>> page(KpiCodePageDTO input) {
        return R.ok(kpiReportService.getPage(input));
    }

    @SysLog("接口详情")
    @GetMapping("/detail/list")
    @Operation(summary = "接口详情")
    public R<IPage<KpiReportDetailVO>> list(KpiCodeDetailPageDTO input) {
        return R.ok(kpiReportService.getlist(input));
    }

    @SysLog("用接口code换结果")
    @GetMapping("/report")
    @Operation(summary = "用接口code换结果")
    public R<KpiReportCodeVO> report(KpiReportCodeDTO input) {
        return R.ok(kpiReportService.report(input));
    }

    @SysLog("删除报表")
    @PostMapping("/del/{reportCode}")
    @Operation(summary = "删除报表")
    public R<KpiCalculateReportVO> reportDel(@PathVariable String reportCode) {
        kpiReportService.reportDel(reportCode);
        return R.ok();
    }

    @SysLog("导入模版")
    @Operation(summary = "导入模版")
    @GetMapping("/template")
    public void template(HttpServletResponse response){
        String fileName = "计算值导入模版";
        String sheetName = "sheet1";
        List<ExcelKpiCalculateDTO> list = new ArrayList<>();
        try {
            ExcelUtil.writeExcel(response, list, fileName, sheetName, ExcelKpiCalculateDTO.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SysLog("测试任务 导入")
    @Operation(summary = "测试任务 导入")
    @PostMapping("/import")
    public R<List<String>> importData(@RequestParam(value = "file") MultipartFile file,
                                      @RequestParam(value ="taskChildId") Long taskChildId,
                                      @RequestParam(value ="imputationCode",required = false) String imputationCode
                                      ) throws IOException {
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
            System.out.println(1);
            return R.ok(kpiReportService.importData(taskChildId,list,imputationCode));
        } catch (BizException e){
            return R.failed(e.getDefaultMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return R.failed(e.getMessage());
        }finally {
            file.getInputStream().close();
        }
    }
}