package com.hscloud.hs.cost.account.controller.dataReport;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hscloud.hs.budget.model.vo.export.ImportErrListVo;
import com.hscloud.hs.budget.model.vo.export.ImportErrVo;
import com.hscloud.hs.cost.account.model.entity.base.PageRequest;
import com.hscloud.hs.cost.account.model.entity.dataReport.OdsHisUnidrgswedDboDrgsInfo;
import com.hscloud.hs.cost.account.model.vo.dataReport.DownloadTemplateRwVo;
import com.hscloud.hs.cost.account.model.vo.dataReport.ExportRwVo;
import com.hscloud.hs.cost.account.service.dataReport.IDrgsInfoService;
import com.pig4cloud.pigx.admin.api.feign.RemoteUserService;
import com.pig4cloud.pigx.common.core.util.R;
import com.pig4cloud.pigx.common.excel.annotation.RequestExcel;
import com.pig4cloud.pigx.common.excel.annotation.ResponseExcel;
import com.pig4cloud.pigx.common.log.annotation.SysLog;
import com.pig4cloud.pigx.common.security.annotation.Inner;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;

/**
* RW值信息表
*
*/
@RestController
@RequiredArgsConstructor
@RequestMapping("/dataReport/drgsInfo")
@Tag(name = "drgsInfo", description = "RW值信息表")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class DrgsInfoController {

    private final IDrgsInfoService drgsInfoService;

    private final RemoteUserService remoteUserService;

    @SysLog("RW值信息表info")
    @GetMapping("/info/{id}")
    @Operation(summary = "RW值信息表info")
    public R<OdsHisUnidrgswedDboDrgsInfo> info(@PathVariable Long id) {
        return R.ok(drgsInfoService.getById(id));
    }

    @SysLog("RW值信息表page")
    @GetMapping("/page")
    @Operation(summary = "RW值信息表page")
    public R<IPage<OdsHisUnidrgswedDboDrgsInfo>> page(PageRequest<OdsHisUnidrgswedDboDrgsInfo> pr) {
        pr.getWrapper().orderByAsc("is_editable");
        return R.ok(drgsInfoService.page(pr.getPage(),pr.getWrapper()));
    }

    @SysLog("RW值信息表list")
    @GetMapping("/list")
    @Operation(summary = "RW值信息表list")
    public R<List<OdsHisUnidrgswedDboDrgsInfo>> list(PageRequest<OdsHisUnidrgswedDboDrgsInfo> pr) {
        return R.ok(drgsInfoService.list(pr.getWrapper()));
    }

    @SysLog("RW值信息表add")
    @PostMapping("/add")
    @Operation(summary = "RW值信息表add")
    public R add(@RequestBody OdsHisUnidrgswedDboDrgsInfo drgsInfo)  {
        return R.ok(drgsInfoService.save(drgsInfo));
    }

    @SysLog("RW值信息表edit")
    @PostMapping("/edit")
    @Operation(summary = "RW值信息表edit")
    public R edit(@RequestBody List<OdsHisUnidrgswedDboDrgsInfo> drgsInfos)  {
        for (OdsHisUnidrgswedDboDrgsInfo drgsInfo : drgsInfos) {
            if (drgsInfo.getId() != null) {
                drgsInfoService.updateById(drgsInfo);
            }
        }
        return R.ok();
    }

    @SysLog("RW值信息表del")
    @PostMapping("/del/{id}")
    @Operation(summary = "RW值信息表del")
    public R del(@PathVariable Long id)  {
        return R.ok(drgsInfoService.removeById(id));
    }

    @SysLog("RW值信息表delBatch")
    @PostMapping("/delBatch/{ids}")
    @Operation(summary = "RW值信息表delBatch 1,2,3")
    public R delBatch(@PathVariable String ids)  {
        return R.ok(drgsInfoService.removeBatchByIds(Arrays.asList(ids.split(","))));
    }

    @SysLog("RW值信息 getRwJobHandler")
    @GetMapping("/getRwJobHandler")
    @Operation(summary = "RW值信息 getRwJobHandler")
    public R getRwJobHandler(@RequestParam("dt") String dt, @RequestParam("type")String type)  {
        return R.ok(drgsInfoService.getRwJobHandler(dt, type));
    }

    @SysLog("下载RW值的导入模板")
    @GetMapping("/downloadTemplate")
    @ResponseExcel(name = "RW值导入模板")
    @Operation(summary = "下载RW值的导入模板")
    @Inner(value = false)
    public List<DownloadTemplateRwVo> downloadTemplateRw(HttpServletRequest request,
                                                         PageRequest<OdsHisUnidrgswedDboDrgsInfo> pr) {
        return drgsInfoService.downloadTemplateRw(pr);
    }


    /**
     *
     * @param excelVOList 导入的rw模板excel文件
     * @param  dt 导入的日期
     * @param continueFlag 导入数据不符时 1 继续导入 2整个终止
     * @param overwriteFlag 导入数据：1 覆盖导入 2 增量导入（去重）
     * @param bindingResult 错误日志信息
     * @return
     */
    @SysLog("导入rw模板文件")
    @PostMapping("/uploadFile")
    @Operation(summary = "导入rw模板文件 continueFlag 1继续，2终止；overwriteFlag 1覆盖，2增量导入")
    public R<ImportErrVo> uploadFileRw(@RequestExcel(headRowNumber = 2) List<DownloadTemplateRwVo> excelVOList,
                                       @RequestParam(value="dt", required = false) String dt,
                                       @RequestParam(value="type", required = false) String type,
                                       @RequestParam(value= "continueFlag",
                                  required = false, defaultValue = "1") String continueFlag,
                                       @RequestParam(value= "overwriteFlag",
                                  required = false, defaultValue = "1") String overwriteFlag,
                                       BindingResult bindingResult) {
        return R.ok(drgsInfoService.uploadFileRw(excelVOList, dt, type, continueFlag, overwriteFlag, bindingResult));
    }

    @ResponseExcel
    @GetMapping("/exportErrLog")
    @Operation(summary = "导出错误日志(通用)")
    public List<ImportErrListVo> exportErrLog(@RequestParam("dt") String dt) {
        return drgsInfoService.exportErrLog(dt);
    }


    @SysLog("导出rw值文件")
    @GetMapping("/export")
    @Operation(summary = "导出rw值文件")
    @ResponseExcel(name = "rw值上报任务")
    public List<ExportRwVo> exportRw(@RequestParam(value="dt") String dt,
                                     @RequestParam(value="type") String type) {
        return drgsInfoService.exportRw(dt, type);
    }

}