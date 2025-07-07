package com.hscloud.hs.cost.account.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hscloud.hs.cost.account.model.entity.base.PageRequest;
import com.hscloud.hs.cost.account.model.entity.imputation.CostUnitChangeLog;
import com.hscloud.hs.cost.account.model.vo.userAttendance.CostUnitChangeLogExcel;
import com.hscloud.hs.cost.account.service.ICostUnitChangeLogService;
import com.pig4cloud.pigx.common.core.util.R;
import com.pig4cloud.pigx.common.excel.annotation.ResponseExcel;
import com.pig4cloud.pigx.common.log.annotation.SysLog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author tianbo
 * @version 1.0
 * @date 2024-08-09 16:04
 **/

@RestController
@RequestMapping("/account/log")
@Tag(name = "核算单元变更日志", description = "核算单元变更日志")
@RequiredArgsConstructor
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class CostUnitChangeLogController {

    private final ICostUnitChangeLogService costUnitChangeLogService;

    @SysLog("核算单元变更日志分页")
    @GetMapping("/page")
    @Operation(summary = "归集变更日志page")
    public R<IPage<CostUnitChangeLog>> page(PageRequest<CostUnitChangeLog> pr) {
        return R.ok(costUnitChangeLogService.pageCostUnitChangeLog(pr.getPage(), pr.getWrapper()));
    }


    @ResponseExcel
    @SysLog("核算单元变更日志分页导出")
    @GetMapping("/export")
    @Operation(summary = "归集变更日志导出")
    public List<CostUnitChangeLogExcel> export(@RequestParam(value = "type") String type,
                                               @RequestParam(value = "userIds", required = false) String userIds,
                                               @RequestParam(value = "startTime", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
                                               @RequestParam(value = "endTime", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        return costUnitChangeLogService.exportCostUnitChangeLog(type, userIds, startTime, endTime);
    }
}
