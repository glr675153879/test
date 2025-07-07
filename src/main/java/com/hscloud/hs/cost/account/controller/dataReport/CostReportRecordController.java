package com.hscloud.hs.cost.account.controller.dataReport;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hscloud.hs.cost.account.model.dto.CostDataCollectionDto;
import com.hscloud.hs.cost.account.model.dto.dataReport.BatchAssignDto;
import com.hscloud.hs.cost.account.model.dto.dataReport.CostReportDetailInfoDto;
import com.hscloud.hs.cost.account.model.dto.dataReport.CostReportDetailRecordDto;
import com.hscloud.hs.cost.account.model.entity.base.PageRequest;
import com.hscloud.hs.cost.account.model.entity.dataReport.CostReportRecord;
import com.hscloud.hs.cost.account.model.entity.dataReport.CostReportRecordFileInfo;
import com.hscloud.hs.cost.account.model.vo.dataReport.AssignResultVo;
import com.hscloud.hs.cost.account.service.dataReport.ICostReportRecordService;
import com.hscloud.hs.cost.account.utils.RedisUtil;
import com.pig4cloud.pigx.common.core.util.R;
import com.pig4cloud.pigx.common.log.annotation.SysLog;
import com.pig4cloud.pigx.common.security.service.PigxUser;
import com.pig4cloud.pigx.common.security.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
* 我的上报
*
*/
@RestController
@RequiredArgsConstructor
@RequestMapping("/dataReport/costReportRecord")
@Tag(name = "costReportRecord", description = "我的上报")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class CostReportRecordController {

    private final ICostReportRecordService costReportRecordService;

    private final RedisUtil redisUtil;

    @SysLog("我的上报info")
    @GetMapping("/info/{id}")
    @Operation(summary = "我的上报info")
    public R<CostReportRecord> info(@PathVariable Long id) {
        return R.ok(costReportRecordService.getById(id));
    }

    @SysLog("我的上报page")
    @GetMapping("/page")
    @Operation(summary = "我的上报page")
    public R<IPage<CostReportRecord>> page(PageRequest<CostReportRecord> pr) {
        /*IPage<CostReportRecord> pageData = costReportRecordService.page(pr.getPage(),pr.getWrapper());
        List<CostReportRecord> filteredRecords = pageData.getRecords().stream()
                .filter(record -> !record.getStatus().equals("0"))
                .collect(Collectors.toList());
        pageData.setRecords(filteredRecords);*/
        IPage<CostReportRecord> pageData = costReportRecordService.page(pr.getPage(),pr.getWrapper().ne("status","0"));
        return R.ok(pageData);
    }

    @SysLog("我的上报list")
    @GetMapping("/list")
    @Operation(summary = "我的上报list")
    public R<List<CostReportRecord>> list(PageRequest<CostReportRecord> pr) {
        return R.ok(costReportRecordService.list(pr.getWrapper()));
    }

    @SysLog("我的上报详情list")
    @GetMapping("/detailList")
    @Operation(summary = "我的上报详情list")
    public R<CostReportDetailRecordDto> detailList(CostReportRecord costReportRecord) {
        return R.ok(costReportRecordService.detailList(costReportRecord));
    }

    /*@SysLog("我的上报add")
    @PostMapping("/add")
    @Operation(summary = "我的上报add")
    public R add(@RequestBody CostReportRecord costReportRecord)  {
        return R.ok(costReportRecordService.save(costReportRecord));
    }*/

    /*@SysLog("我的上报edit")
    @PostMapping("/edit")
    @Operation(summary = "我的上报edit")
    public R edit(@RequestBody CostReportRecord costReportRecord)  {
        return R.ok(costReportRecordService.updateById(costReportRecord));
    }*/

    @SysLog("我的上报提交")
    @PostMapping("/submit")
    @Operation(summary = "我的上报提交")
    public R submit(@RequestBody CostReportRecord costReportRecord)  {
        return R.ok(costReportRecordService.submit(costReportRecord));
    }

    @SysLog("我的上报通过")
    @PostMapping("/approve")
    @Operation(summary = "我的上报通过")
    public R approve(@RequestBody CostReportRecord costReportRecord)  {
        return R.ok(costReportRecordService.approve(costReportRecord));
    }

    @SysLog("我的上报驳回")
    @PostMapping("/reject")
    @Operation(summary = "我的上报驳回")
    public R reject(@RequestBody CostReportRecord costReportRecord)  {
        return R.ok(costReportRecordService.reject(costReportRecord));
    }

    @SysLog("我的上报del")
    @PostMapping("/del/{id}")
    @Operation(summary = "我的上报del")
    public R del(@PathVariable Long id)  {
        return R.ok(costReportRecordService.removeById(id));
    }

    @SysLog("我的上报delBatch")
    @PostMapping("/delBatch/{ids}")
    @Operation(summary = "我的上报delBatch 1,2,3")
    public R delBatch(@PathVariable String ids)  {
        return R.ok(costReportRecordService.removeBatchByIds(Arrays.asList(ids.split(","))));
    }

    @SysLog("我的上报模版下载")
    @GetMapping("/downloadTemplate")
    @Operation(summary = "我的上报模版下载")
    public R downloadTemplate(Long recordId, HttpServletResponse response) {
        return costReportRecordService.downloadTemplate(recordId, response);
    }

    @SysLog("导入数据")
    @PostMapping("/handleFileUpload")
    @Operation(summary = "导入数据")
    public R handleFileUpload(@RequestParam("file") MultipartFile file, @RequestParam("recordId") Long recordId) {
        return costReportRecordService.handleFileUpload(file, recordId);
    }

    @SysLog("导入数据错误日志下载")
    @GetMapping("/downloadErrorLog")
    @Operation(summary = "导入数据错误日志下载")
    public ResponseEntity<byte[]> downloadErrorLog(HttpServletResponse response) {
        PigxUser user = SecurityUtils.getUser();
        String key = "errorLog" + user.getId();
        Map<String, String> errorLog = (Map<String, String>) redisUtil.get(key);
        byte[] fileContent;
        if (errorLog != null) {
            // 生成Excel文件
            fileContent = costReportRecordService.generateExcelLogFile(errorLog);
        } else {
            fileContent = new byte[0];
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        return new ResponseEntity<>(fileContent, headers, HttpStatus.OK);
    }

    @SysLog("删除错误日志文件")
    @GetMapping("/removeErrorLog")
    @Operation(summary = "删除错误日志文件")
    public R removeErrorLog() {
        return R.ok(costReportRecordService.removeErrorLog());
    }

    @SysLog("发起任务")
    @PostMapping("/initiateJobHandler")
    @Operation(summary = "发起任务,每月初执行")
    public R initiateJobHandler() {
         costReportRecordService.initiateJobHandler();
        return R.ok();
    }

    @SysLog("激活任务")
    @PostMapping("/assignJobHandler")
    @Operation(summary = "激活任务,每日末执行")
    public R assignJobHandler() {
        costReportRecordService.assignJobHandler();
        return R.ok();
    }

    @SysLog("手动下发任务")
    @PostMapping("/assign")
    @Operation(summary = "手动下发任务")
    public R assign(Long taskId){
        costReportRecordService.assign(taskId);
        return R.ok();
    }

    @SysLog("获取上报任务状态")
    @GetMapping("/status")
    @Operation(summary = "获取上报任务状态")
    public R getReportRecordStatus(@RequestParam(name = "taskId") Long taskId) {
        return R.ok(costReportRecordService.getReportRecordStatus(taskId));
    }

    /**
     * 保存上报记录,执行增删改查等一系列操作
     * @param costReportDetailInfo 上报记录
     * @return R
     */
    @SysLog("编辑完成")
    @PostMapping("/editAndSave")
    @Operation(summary = "编辑完成")
    public R editAndSave(@RequestBody List<CostReportDetailInfoDto> costReportDetailInfo)  {
        return costReportRecordService.editAndSave(costReportDetailInfo);
    }

    @SysLog("RW数据")
    @PostMapping("/rw")
    @Operation(summary = "RW数据")
    public R rwData(@RequestBody CostDataCollectionDto input)  {
        return R.ok(costReportRecordService.getRwData(input));
    }

    @SysLog("保存上传文件信息")
    @PostMapping("/uploadFile")
    @Operation(summary = "保存上传文件信息")
    public R uploadFile(@RequestBody CostReportRecordFileInfo fileInfo)  {
        return R.ok(costReportRecordService.uploadFile(fileInfo));
    }


    @SysLog("快捷引入")
    @PostMapping("/faseExtend")
    @Operation(summary = "快捷引入")
    public R<?> faseExtend(Long taskId){
        costReportRecordService.inheritPreviousRecordHandle(taskId);
        return R.ok();
    }

    @SysLog("批量下发任务")
    @PostMapping("/batchAssign")
    @Operation(summary = "批量下发任务")
    public R<List<AssignResultVo>> batchAssign(@Valid @RequestBody BatchAssignDto batchAssignDto){
        return R.ok(costReportRecordService.batchAssign(batchAssignDto));
    }

}