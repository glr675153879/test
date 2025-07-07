package com.hscloud.hs.cost.account.controller.imputation;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hscloud.hs.cost.account.model.entity.base.PageRequest;
import com.hscloud.hs.cost.account.model.entity.imputation.ImputationChangeLog;
import com.hscloud.hs.cost.account.model.vo.imputation.ImputationChangeLogExcel;
import com.hscloud.hs.cost.account.service.imputation.IImputationChangeLogService;
import com.pig4cloud.pigx.common.core.util.R;
import com.pig4cloud.pigx.common.excel.annotation.ResponseExcel;
import com.pig4cloud.pigx.common.log.annotation.SysLog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 归集变更日志
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/imputation/imputationChangeLog")
@Tag(name = "归集变更日志", description = "归集变更日志")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class ImputationChangeLogController {

    private final IImputationChangeLogService imputationChangeLogService;

    @SysLog("归集变更日志page")
    @GetMapping("/page/{imputationId}")
    @Operation(summary = "归集变更日志page")
    public R<IPage<ImputationChangeLog>> page(PageRequest<ImputationChangeLog> pr, @PathVariable Long imputationId) {
        return R.ok(imputationChangeLogService.pageImputationChangeLog(pr.getPage(), pr.getWrapper(), imputationId));
    }

    @SysLog("归集变更日志list")
    @GetMapping("/list")
    @Operation(summary = "归集变更日志list")
    public R<List<ImputationChangeLog>> list(PageRequest<ImputationChangeLog> pr) {
        return R.ok(imputationChangeLogService.list(pr.getWrapper()));
    }

    @SysLog("归集变更日志pageList")
    @GetMapping("/pageList")
    @Operation(summary = "归集变更日志pageList")
    public R<IPage<ImputationChangeLog>> pageList(PageRequest<ImputationChangeLog> pr) {
        return R.ok(imputationChangeLogService.pageImputationChangeLog(pr.getPage(),pr.getWrapper()));
    }

    @ResponseExcel
    @SysLog("归集变更日志导出")
    @GetMapping("/export/{imputationId}")
    @Operation(summary = "归集变更日志导出")
    public List<ImputationChangeLogExcel> export(@PathVariable Long imputationId, String changeType, String changeModel) {
        return imputationChangeLogService.exportChangeLog(imputationId, changeType, changeModel);
    }

    @ResponseExcel
    @SysLog("物资收费管理变更日志导出")
    @GetMapping("/export")
    @Operation(summary = "物资收费管理变更日志导出")
    public List<ImputationChangeLogExcel> export(String changeType, String changeModel,String imputationCode) {
        return imputationChangeLogService.exportChangeLog( changeType, changeModel,imputationCode);
    }
}