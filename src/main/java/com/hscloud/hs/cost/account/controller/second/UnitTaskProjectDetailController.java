package com.hscloud.hs.cost.account.controller.second;

import com.alibaba.excel.util.ListUtils;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hscloud.hs.cost.account.constant.CacheConstants;
import com.hscloud.hs.cost.account.model.dto.second.ProgProjectDetail2SaveBatchDTO;
import com.hscloud.hs.cost.account.model.dto.second.ProgProjectDetailSaveDTO;
import com.hscloud.hs.cost.account.model.dto.second.UnitTaskProjectDetailSave2DTO;
import com.hscloud.hs.cost.account.model.dto.second.UnitTaskProjectDetailSaveDTO;
import com.hscloud.hs.cost.account.model.entity.base.PageRequest;
import com.hscloud.hs.cost.account.model.entity.second.UnitTaskProjectDetail;
import com.hscloud.hs.cost.account.model.vo.second.UnitTaskProjectDetailVo;
import com.hscloud.hs.cost.account.model.vo.second.export.DetailExcelVo;
import com.hscloud.hs.cost.account.model.vo.second.export.ImportErrLogVo;
import com.hscloud.hs.cost.account.model.vo.second.importXls.ImportResultVo;
import com.hscloud.hs.cost.account.service.second.IUnitTaskProjectDetailService;
import com.hscloud.hs.cost.account.utils.ExcelUtil;
import com.hscloud.hs.cost.account.utils.RedisUtil;
import com.pig4cloud.pigx.common.core.util.R;
import com.pig4cloud.pigx.common.excel.annotation.ResponseExcel;
import com.pig4cloud.pigx.common.log.annotation.SysLog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
* 任务核算指标明细值
*
*/
@RestController
@RequiredArgsConstructor
@RequestMapping("/second/unitTaskProjectDetail")
@Tag(name = "unitTaskProjectDetail", description = "任务核算指标明细值")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class UnitTaskProjectDetailController {

    private final IUnitTaskProjectDetailService unitTaskProjectDetailService;

    private final RedisUtil redisUtil;
    @SysLog("任务核算指标明细值info")
    @GetMapping("/info/{id}")
    @Operation(summary = "任务核算指标明细值info")
    public R<UnitTaskProjectDetail> info(@PathVariable Long id) {
        return R.ok(unitTaskProjectDetailService.getById(id));
    }

    @SysLog("任务核算指标明细值page")
    @GetMapping("/page")
    @Operation(summary = "任务核算指标明细值page")
    public R<IPage<UnitTaskProjectDetail>> page(PageRequest<UnitTaskProjectDetail> pr) {
        return R.ok(unitTaskProjectDetailService.page(pr.getPage(),pr.getWrapper()));
    }

    @SysLog("任务核算指标明细值list")
    @GetMapping("/list")
    @Operation(summary = "任务核算指标明细值list")
    public R<List<UnitTaskProjectDetail>> list(PageRequest<UnitTaskProjectDetail> pr) {
        return R.ok(unitTaskProjectDetailService.list(pr.getWrapper()));
    }

    @SysLog("任务核算指标明细值userList")
    @GetMapping("/userList")
    @Operation(summary = "任务核算指标明细值userList")
    public R<List<UnitTaskProjectDetailVo>> userList(Long projectId) {
        return R.ok(unitTaskProjectDetailService.userList(projectId,null));
    }

    @SysLog("任务核算指标明细值add")
    @PostMapping("/add")
    @Operation(summary = "任务核算指标明细值add")
    public R add(@RequestBody UnitTaskProjectDetail unitTaskProjectDetail)  {
        return R.ok(unitTaskProjectDetailService.save(unitTaskProjectDetail));
    }

    @SysLog("任务核算指标明细值save")
    @PostMapping("/saveDetail")
    @Operation(summary = "任务核算指标明细值save")
    public R saveDetail(@RequestBody UnitTaskProjectDetailSaveDTO unitTaskProjectDetailSaveDTO)  {
        unitTaskProjectDetailService.saveDetail(unitTaskProjectDetailSaveDTO);
        return R.ok();
    }

    @SysLog("任务核算指标明细值save2")
    @PostMapping("/saveDetail2")
    @Operation(summary = "任务核算指标明细值save2 科室二次分配 修改")
    public R saveDetail2(@RequestBody UnitTaskProjectDetailSave2DTO unitTaskProjectDetailSave2DTO)  {
        unitTaskProjectDetailService.saveDetail2(unitTaskProjectDetailSave2DTO);
        return R.ok();
    }

    @SysLog("方案核算指标明细 saveProgDetail 单项绩效调整方案")
    @PostMapping("/saveProgDetail")
    @Operation(summary = "方案核算指标明细 saveProgDetail 单项绩效调整方案")
    public R saveProgDetail(@RequestBody ProgProjectDetailSaveDTO progProjectDetailSaveDTO)  {
        unitTaskProjectDetailService.saveProgDetail(progProjectDetailSaveDTO);
        return R.ok();
    }

    // @SysLog("方案核算指标明细 updateProgDetailIndex 单项绩效更新排序")
    // @PostMapping("/updateProgDetailIndex")
    // @Operation(summary = "方案核算指标明细 updateProgDetailIndex 单项绩效更新排序")
    // public R updateProgDetailIndex(@RequestBody ProgProjectDetailSaveDTO progProjectDetailSaveDTO) {
    //     unitTaskProjectDetailService.updateProgDetailIndex(progProjectDetailSaveDTO);
    //     return R.ok();
    // }

    @SysLog("方案核算指标明细 saveProgDetail2 科室二次分配调整方案")
    @PostMapping("/saveProgDetail2")
    @Operation(summary = "方案核算指标明细 saveProgDetail2 科室二次分配调整方案")
    public R saveProgDetail2(@RequestBody UnitTaskProjectDetail unitTaskProjectDetail)  {
        unitTaskProjectDetailService.addProgDetail2(unitTaskProjectDetail);
        return R.ok();
    }

    @SysLog("方案核算指标明细 saveProgDetail2Batch 科室二次分配调整方案")
    @PostMapping("/saveProgDetail2Batch")
    @Operation(summary = "方案核算指标明细 saveProgDetail2Batch 科室二次分配调整方案 新增")
    public R saveProgDetail2Batch(@RequestBody ProgProjectDetail2SaveBatchDTO progProjectDetail2SaveBatchDTO)  {
        unitTaskProjectDetailService.addProgDetail2Batch(progProjectDetail2SaveBatchDTO);
        return R.ok();
    }
    @SysLog("方案核算指标明细 delProgDetail2 科室二次分配 删除detail")
    @PostMapping("/delProgDetail2/{id}")
    @Operation(summary = "方案核算指标明细 delProgDetail2 科室二次分配 删除detail")
    public R delProgDetail2(@PathVariable Long id)  {
        unitTaskProjectDetailService.delProgDetail2(id);
        return R.ok();
    }


    @SysLog("任务核算指标明细值edit")
    @PostMapping("/edit")
    @Operation(summary = "任务核算指标明细值edit")
    public R edit(@RequestBody UnitTaskProjectDetail unitTaskProjectDetail)  {
        return R.ok(unitTaskProjectDetailService.updateById(unitTaskProjectDetail));
    }

    @SysLog("任务核算指标明细值del")
    @PostMapping("/del/{id}")
    @Operation(summary = "任务核算指标明细值del")
    public R del(@PathVariable Long id)  {
        return R.ok(unitTaskProjectDetailService.removeById(id));
    }

    @SysLog("任务核算指标明细值delBatch")
    @PostMapping("/delBatch/{ids}")
    @Operation(summary = "任务核算指标明细值delBatch 1,2,3")
    public R delBatch(@PathVariable String ids)  {
        return R.ok(unitTaskProjectDetailService.removeBatchByIds(Arrays.asList(ids.split(","))));
    }


    @ResponseExcel
    @GetMapping("/exportDanxiang1")
    public List<DetailExcelVo> export() {
        List<DetailExcelVo> list = ListUtils.newArrayList();
        for (int i = 0; i < 10; i++) {
            DetailExcelVo data = new DetailExcelVo();
            data. setCode("字符串" + i);
            data.setName("名称"+i);
            list.add(data);
        }
        return list;
        //return unitTaskProjectDetailService.listExcelVo();
    }

    @GetMapping("/exportDanxiang")
    @Operation(summary = "单项导出")
    public void exportDanxiang(Long unitTaskProjectId,HttpServletResponse response) {
        unitTaskProjectDetailService.exportDanxiang(unitTaskProjectId,response);
    }

    @PostMapping("/importDanxiang/{unitTaskProjectId}")
    @Operation(summary = "单项导入")
    public R<ImportResultVo> importDanxiang(@PathVariable Long unitTaskProjectId, @RequestPart("file") MultipartFile file) throws Exception {
        String[][] xlsDataArr = ExcelUtil.doExcelH(file,2);
        return R.ok(unitTaskProjectDetailService.importDanxiang(unitTaskProjectId,xlsDataArr));
    }

    @ResponseExcel
    @GetMapping("/exportErrLog")
    @Operation(summary = "导入错误日志(通用)")
    public List<ImportErrLogVo> exportErrLog(Long id){
        List<ImportErrLogVo> errList = new ArrayList<>();

        List<String> redisList = (List<String>)redisUtil.get(CacheConstants.SEC_IMPORT_ERRLOG+id);
        for (String err : redisList){
            ImportErrLogVo vo = new ImportErrLogVo();
            vo.setContent(err);
            errList.add(vo);
        }
        return errList;
    }

}