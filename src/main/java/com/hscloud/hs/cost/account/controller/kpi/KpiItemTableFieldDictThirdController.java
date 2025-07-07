package com.hscloud.hs.cost.account.controller.kpi;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hscloud.hs.cost.account.model.dto.kpi.KpiItemTableFieldDictDto;
import com.hscloud.hs.cost.account.model.dto.kpi.KpiItemTableFieldDictThirdDto;
import com.hscloud.hs.cost.account.model.entity.base.PageRequest;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiItemTableFieldDict;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiItemTableFieldDictThird;
import com.hscloud.hs.cost.account.model.vo.kpi.KpiItemTableFieldDictThirdVO;
import com.hscloud.hs.cost.account.service.impl.kpi.KpiItemTableFieldDictThirdService;
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
@RequestMapping("/kpi/kpiItemTableFieldDictThird")
@Tag(name = "k_item_table_field_dict_third", description = "核算项基础表字段字典映射")
public class KpiItemTableFieldDictThirdController {
    private final KpiItemTableFieldDictThirdService kpiItemTableFieldDictThirdService;

    @SysLog("核算项基础表字段字典映射add")
    @PostMapping("/saveOrUpdate")
    @Operation(summary = "*映射add")
    public R saveOrUpdate(@RequestBody @Validated KpiItemTableFieldDictThirdDto dto) {
        return R.ok(kpiItemTableFieldDictThirdService.saveOrUpdate(dto));
    }

    @SysLog("核算项基础表字段字典映射删除")
    @PostMapping("/del/{id}")
    @Operation(summary = "*映射删除")
    public R del(@PathVariable Long id) {
        return R.ok(kpiItemTableFieldDictThirdService.removeById(id));
    }

    @GetMapping("/list")
    @Operation(summary = "*映射列表")
    public R<List<KpiItemTableFieldDictThird>> list(PageRequest<KpiItemTableFieldDictThird> pr) {
        return R.ok(kpiItemTableFieldDictThirdService.list(pr.getWrapper()));
    }

    @GetMapping("/page")
    @Operation(summary = "*映射page")
    public R<IPage<KpiItemTableFieldDictThirdVO>> getPage(KpiItemTableFieldDictThirdDto dto) {
        return R.ok(kpiItemTableFieldDictThirdService.getPage(dto));
    }
}
