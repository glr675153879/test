package com.hscloud.hs.cost.account.controller.kpi;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.metadata.data.ReadCellData;
import com.alibaba.excel.util.ConverterUtils;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hscloud.hs.cost.account.model.dto.kpi.*;
import com.hscloud.hs.cost.account.model.vo.kpi.KpiAccountRelationVO;
import com.hscloud.hs.cost.account.model.vo.kpi.KpiAccountUnitVO;
import com.hscloud.hs.cost.account.service.kpi.KpiAccountUnitService;
import com.pig4cloud.pigx.common.core.exception.BizException;
import com.pig4cloud.pigx.common.core.util.R;
import com.pig4cloud.pigx.common.log.annotation.SysLog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
* 核算单元
*
 * @author Administrator
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/kpi/unit")
@Tag(name = "k_核算单元", description = "核算单元")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class KpiAccountUnitController {

    private final KpiAccountUnitService kpiAccountUnitService;

    @SysLog("核算单元info")
    @GetMapping("/info/{id}")
    @Operation(summary = "*核算单元明细")
    public R<KpiAccountUnitVO> info(@PathVariable Long id) {
        return R.ok(kpiAccountUnitService.getUnit(id));
    }

    @SysLog("核算单元add")
    @PostMapping("/saveOrUpdate")
    @Operation(summary = "*新增/编辑核算单元")
    public R<Long> addUnit(@RequestBody @Validated KpiAccountUnitDTO dto) {
        return R.ok(kpiAccountUnitService.saveOrUpdate(dto));
    }

    @SysLog("核算单元switch")
    @PostMapping("/switch")
    @Operation(summary = "*状态停/启用")
    public R switchStatus(@RequestBody BaseIdStatusDTO dto) {
        kpiAccountUnitService.switchStatus(dto);
        return R.ok();
    }

    @SysLog("核算单元delete")
    @PostMapping("/delete/{id}")
    @Operation(summary = "*根据id删除核算单元")
    public R deleteUnit(@PathVariable Long id) {
        kpiAccountUnitService.deleteUnit(id);
        return R.ok();
    }

    @SysLog("核算单元列表（分页）")
    @GetMapping("/page")
    @Operation(summary = "*核算单元列表（分页）")
    public R<IPage<KpiAccountUnitVO>> getUnitPage(KpiAccountUnitQueryDTO dto) {
        return R.ok(kpiAccountUnitService.getUnitPageList(dto));
    }

    @SysLog("核算单元列表（全部）")
    @GetMapping("/list")
    @Operation(summary = "*核算单元列表（全部）")
    public R<List<KpiAccountUnitVO>> getUnitList(KpiAccountUnitQueryDTO dto) {
        return R.ok(kpiAccountUnitService.getUnitList(dto));
    }

    @SysLog("核算单元操作检测")
    @GetMapping("/check/{id}")
    @Operation(summary = "核算单元操作检测 0-未应用 1-已应用")
    public R<Integer> unitCheck(@PathVariable Long id) {
        return R.ok(kpiAccountUnitService.unitCheck(id));
    }

    @GetMapping("/relation/page")
    @Operation(summary = "*核算单元关系列表（分页）")
    public R<IPage<KpiAccountRelationVO>> getRelationPage(@Validated KpiAccountRelationQueryDTO dto) {
        return R.ok(kpiAccountUnitService.getAccountRelationPageList(dto));
    }

    @GetMapping("/relation/list")
    @Operation(summary = "*核算单元关系列表（分页）")
    public R<List<KpiAccountRelationVO>> getRelationList(@Validated KpiAccountRelationQueryDTO dto) {
        return R.ok(kpiAccountUnitService.getAccountRelationPageList2(dto));
    }

    @SysLog("核算单元关系编辑")
    @PostMapping("/relation/edit")
    @Operation(summary = "*核算单元关系编辑")
    public R saveAccountRelation(@Validated @RequestBody KpiAccountRelationDTO dto) {
        kpiAccountUnitService.saveAccountRelation(dto);
        return R.ok();
    }

    @SysLog("核算单元关系复制")
    @PostMapping("/relation/copy")
    @Operation(summary = "*核算单元关系复制")
    public R accountRelationCopy(@Validated @RequestBody BaseIdDTO dto) {
        kpiAccountUnitService.accountRelationCopy(dto.getId());
        return R.ok();
    }

    @SysLog("核算单元关系导入")
    @PostMapping("/relation/import")
    @Operation(summary = "*核算单元关系导入")
    public R<String> accountRelationImport(@RequestParam(value = "categoryCode") String categoryCode,
                                           @RequestParam(value = "busiType",defaultValue = "1",required = false) String busiType,
                                   @RequestParam(value = "file") MultipartFile file) {
        return R.ok(kpiAccountUnitService.accountRelationImport(categoryCode, busiType, file));
    }

    @SysLog("核算单元 导入 overwriteFlag1覆盖导入(清空后插) 2增量导入")
    @Operation(summary = "核算单元 导入 overwriteFlag1覆盖导入(清空后插) 2增量导入")
    @PostMapping("/import")
    public R<List<String>> importData(@RequestParam(value = "file") MultipartFile file,
                                      @RequestParam(value ="overwriteFlag") String overwriteFlag) throws IOException {
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
            return R.ok(kpiAccountUnitService.importData(list,overwriteFlag));
        } catch (BizException e){
            return R.failed(e.getDefaultMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return R.failed(e.getMessage());
        }finally {
            file.getInputStream().close();
        }
    }

    @SysLog("核算单元关系智能匹配")
    @PostMapping("/relation/match")
    @Operation(summary = "*核算单元关系智能匹配")
    public R<String> accountRelationMatch(@RequestParam(value = "categoryCode") String categoryCode,
                                          @RequestParam(value = "busiType",defaultValue = "1",required = false) String busiType) {
        kpiAccountUnitService.accountRelationMatch(categoryCode, busiType);
        return R.ok();
    }
}