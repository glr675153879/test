package com.hscloud.hs.cost.account.controller.imputation;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hscloud.hs.cost.account.model.entity.base.PageRequest;
import com.hscloud.hs.cost.account.model.entity.imputation.ImputationIndex;
import com.hscloud.hs.cost.account.service.imputation.IImputationIndexService;
import com.pig4cloud.pigx.common.core.util.R;
import com.pig4cloud.pigx.common.log.annotation.SysLog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

/**
 * 归集指标
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/imputation/imputationIndex")
@Tag(name = "归集指标", description = "归集指标")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class ImputationIndexController {

    private final IImputationIndexService imputationIndexService;

    @SysLog("归集指标page")
    @GetMapping("/page/{imputationId}")
    @Operation(summary = "归集指标page")
    public R<IPage<ImputationIndex>> page(PageRequest<ImputationIndex> pr, @PathVariable Long imputationId) {
        return R.ok(imputationIndexService.pageImputationIndex(pr.getPage(), pr.getWrapper(), imputationId));
    }

    @SysLog("归集指标新增或编辑")
    @PostMapping("/addOrEdit")
    @Operation(summary = "归集指标新增或编辑")
    public R add(@RequestBody ImputationIndex imputationIndex) {
        return R.ok(imputationIndexService.saveOrUpdateImputationIndex(imputationIndex));
    }

    @SysLog("归集指标del")
    @PostMapping("/del/{id}")
    @Operation(summary = "归集指标del")
    public R del(@PathVariable Long id) {
        return R.ok(imputationIndexService.removeImputationIndexById(id));
    }

}