package com.hscloud.hs.cost.account.controller.kpi;

import cn.hutool.core.lang.tree.Tree;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hscloud.hs.cost.account.model.dto.kpi.*;
import com.hscloud.hs.cost.account.model.entity.base.PageRequest;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiCategory;
import com.hscloud.hs.cost.account.service.kpi.IKpiCategoryService;
import com.pig4cloud.pigx.common.core.util.R;
import com.pig4cloud.pigx.common.log.annotation.SysLog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.Arrays;
import java.util.List;

/**
* 分组表
*
*/
@RestController
@RequiredArgsConstructor
@RequestMapping("/kpi/kpiCategory")
@Tag(name = "*kpiCategory分组", description = "分组表")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class KpiCategoryController {

    private final IKpiCategoryService kpiCategoryService;

    @SysLog("分组表info")
    @GetMapping("/info/{id}")
    @Operation(summary = "*分组表info")
    public R<KpiCategory> info(@PathVariable Long id) {
        return R.ok(kpiCategoryService.getById(id));
    }

//    @SysLog("分组表page")
//    @GetMapping("/page")
//    @Operation(summary = "分组表page")
//    public R<IPage<KpiCategory>> page(PageRequest<KpiCategory> pr) {
//        return R.ok(kpiCategoryService.page(pr.getPage(),pr.getWrapper()));
//    }

    @SysLog("分组表list")
    @GetMapping("/list")
    @Operation(summary = "*分组表list")
    public R<List<Tree<Long>>> list(KpiGroupListSearchDto dto ) {
        return R.ok(kpiCategoryService.getTreeForCategory(dto));
    }

//    @SysLog("分组表add")
//    @PostMapping("/add")
//    @Operation(summary = "分组表add")
//    public R add(@RequestBody KpiCategory kpiCategory)  {
//        return R.ok(kpiCategoryService.save(kpiCategory));
//    }

    @SysLog("分组表edit")
    @PostMapping("/edit")
    @Operation(summary = "*分组表新增修改停用")
    public R edit(@RequestBody KpiCategoryDto kpiCategory)  {
        return R.ok(kpiCategoryService.saveOrUpdateGroup(kpiCategory));
    }

    @SysLog("分组表del")
    @PostMapping("/del")
    @Operation(summary = "*分组表del")
    public R del(@RequestBody KpiGroupDelDto dto)  {
        kpiCategoryService.deleteGroup(dto);
        return R.ok();
    }

    @SysLog("分组copy")
    @PostMapping("/copy")
    @Operation(summary = "*分组copy")
    public R copy(@RequestBody @Validated CategoryCopyDto dto)  {
        kpiCategoryService.copy(dto);
        return R.ok();
    }

}