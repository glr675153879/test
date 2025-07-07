package com.hscloud.hs.cost.account.controller.kpi;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hscloud.hs.cost.account.model.dto.kpi.KpiItemTableFieldDictDto;
import com.hscloud.hs.cost.account.model.entity.base.PageRequest;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiItemTableFieldDict;
import com.hscloud.hs.cost.account.service.impl.kpi.KpiItemTableFieldDictService;
import com.pig4cloud.pigx.common.core.util.R;
import com.pig4cloud.pigx.common.log.annotation.SysLog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/kpi/kpiItemTableFieldDict")
@Tag(name = "k_item_table_field_dict", description = "核算项基础表字段字典")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class KpiItemTableFieldDictController {
    private final KpiItemTableFieldDictService kpiItemTableFieldDictService;

    @SysLog("核算项基础表字段字典add")
    @PostMapping("/saveOrUpdate")
    @Operation(summary = "*字典add")
    public R saveOrUpdate(@RequestBody @Validated KpiItemTableFieldDictDto dto) {
        return R.ok(kpiItemTableFieldDictService.saveOrUpdate(dto));
    }

    @SysLog("核算项基础表字段字典删除")
    @PostMapping("/del/{id}")
    @Operation(summary = "*字典删除")
    public R del(@PathVariable Long id) {
        return R.ok(kpiItemTableFieldDictService.removeById(id));
    }

    @GetMapping("/list")
    @Operation(summary = "*字典列表")
    public R<List<KpiItemTableFieldDict>> list(PageRequest<KpiItemTableFieldDict> pr) {
        return R.ok(kpiItemTableFieldDictService.list(pr.getWrapper()));
    }

    @GetMapping("/page")
    @Operation(summary = "*字典page")
    public R<IPage<KpiItemTableFieldDict>> getPage(KpiItemTableFieldDictDto dto) {
        return R.ok(kpiItemTableFieldDictService.getPage(dto));
    }

    @GetMapping("/info/{id}")
    @Operation(summary = "*字典info")
    public R<KpiItemTableFieldDict> info(@PathVariable Long id) {
        return R.ok(kpiItemTableFieldDictService.getById(id));
    }

    @GetMapping("/getByCode/{code}")
    @Operation(summary = "*根据code获取字典")
    public R<KpiItemTableFieldDict> getByCode(@PathVariable String code) {
        return R.ok(kpiItemTableFieldDictService.getByCode(code));
    }
}
