package com.hscloud.hs.cost.account.controller.kpi;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hscloud.hs.cost.account.model.dto.kpi.BaseIdStatusDTO;
import com.hscloud.hs.cost.account.model.dto.kpi.KpiItemTableFieldDictItemDto;
import com.hscloud.hs.cost.account.model.entity.base.PageRequest;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiItemTableFieldDictItem;
import com.hscloud.hs.cost.account.service.impl.kpi.KpiItemTableFieldDictItemService;
import com.pig4cloud.pigx.common.core.util.R;
import com.pig4cloud.pigx.common.log.annotation.SysLog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/kpi/kpiItemTableFieldDictItem")
@Tag(name = "k_item_table_field_dict_item", description = "核算项基础表字段字典值")
public class KpiItemTableFieldDictItemController {
    private final KpiItemTableFieldDictItemService kpiItemTableFieldDictItemService;

    @SysLog("核算项基础表字段字典值add")
    @PostMapping("/saveOrUpdate")
    @Operation(summary = "*字典值add")
    public R saveOrUpdate(@RequestBody @Validated KpiItemTableFieldDictItemDto dto) {
        return R.ok(kpiItemTableFieldDictItemService.saveOrUpdate(dto));
    }

    @SysLog("核算项基础表字段字典值删除")
    @PostMapping("/del/{id}")
    @Operation(summary = "*字典值删除")
    public R del(@PathVariable Long id) {
        return R.ok(kpiItemTableFieldDictItemService.removeById(id));
    }

    @SysLog("状态修改")
    @PostMapping("/switchStatus")
    @Operation(summary = "*状态修改")
    public R switchStatus(@RequestBody BaseIdStatusDTO dto) {
        kpiItemTableFieldDictItemService.switchStatus(dto);
        return R.ok();
    }

    @GetMapping("/list")
    @Operation(summary = "*字典值列表")
    public R<List<KpiItemTableFieldDictItem>> list(PageRequest<KpiItemTableFieldDictItem> pr) {
        return R.ok(kpiItemTableFieldDictItemService.list(pr.getWrapper()));
    }

    @GetMapping("/page")
    @Operation(summary = "*字典值page")
    public R<IPage<KpiItemTableFieldDictItem>> getPage(KpiItemTableFieldDictItemDto dto) {
        return R.ok(kpiItemTableFieldDictItemService.getPage(dto));
    }

    @GetMapping("/info/{id}")
    @Operation(summary = "*字典值info")
    public R<KpiItemTableFieldDictItem> info(@PathVariable Long id) {
        return R.ok(kpiItemTableFieldDictItemService.getById(id));
    }

    @GetMapping("/getByDictCode/{dictCode}")
    @Operation(summary = "*根据dictCode获取字典值list")
    public R<List<KpiItemTableFieldDictItem>> getByDictCode(@PathVariable String dictCode) {
        return R.ok(kpiItemTableFieldDictItemService.getByDictCode(dictCode));
    }

    @GetMapping("/getByItemCode/{itemCode}")
    @Operation(summary = "*根据itemCode获取字典值")
    public R<KpiItemTableFieldDictItem> getByItemCode(@PathVariable String itemCode) {
        return R.ok(kpiItemTableFieldDictItemService.getByItemCode(itemCode));
    }
}
