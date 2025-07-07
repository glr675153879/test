package com.hscloud.hs.cost.account.controller.imputation;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hscloud.hs.cost.account.model.entity.base.PageRequest;
import com.hscloud.hs.cost.account.model.entity.imputation.Imputation;
import com.hscloud.hs.cost.account.model.vo.imputation.HistoryVO;
import com.hscloud.hs.cost.account.service.imputation.IImputationService;
import com.pig4cloud.pigx.common.core.util.R;
import com.pig4cloud.pigx.common.log.annotation.SysLog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

/**
 * 归集主档
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/imputation")
@Tag(name = "归集主档", description = "归集主档")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class ImputationController {

    private final IImputationService imputationService;

    @SysLog("归集主档list")
    @GetMapping("/list/{cycle}")
    @Operation(summary = "归集主档list， 入参月份")
    public R<List<Imputation>> list(@PathVariable String cycle) {
        return R.ok(imputationService.list(Wrappers.<Imputation>lambdaQuery().eq(Imputation::getImputationCycle, cycle)));
    }

    @SysLog("生成主档")
    @PostMapping("/generate")
    @Operation(summary = "生成主档，无入参，返回生成的主档的月份")
    public R generate() {
        return R.ok(imputationService.generate());
    }


    @PreAuthorize("@pms.hasPermission('kpi_imputation_lock')")
    @SysLog("锁定主档")
    @PostMapping("/lock/{cycle}")
    @Operation(summary = "锁定，入参月份")
    public R lock(@PathVariable String cycle) {
        return R.ok(imputationService.lock(cycle));
    }

    @PreAuthorize("@pms.hasPermission('kpi_imputation_unlock')")
    @SysLog("解锁主档")
    @PostMapping("/unlock/{cycle}")
    @Operation(summary = "锁定， 入参月份")
    public R unlock(@PathVariable String cycle) {
        return R.ok(imputationService.unlock(cycle));
    }

    @SysLog("分页查询历史记录")
    @GetMapping("/pageHistory")
    @Operation(summary = "分页查询历史记录")
    public R<IPage<HistoryVO>> pageHistory(PageRequest<Imputation> pr) {
        return R.ok(imputationService.pageHistory(pr.getPage()));
    }
}