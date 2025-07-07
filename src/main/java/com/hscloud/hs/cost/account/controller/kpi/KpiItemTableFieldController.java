package com.hscloud.hs.cost.account.controller.kpi;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hscloud.hs.cost.account.model.dto.kpi.BaseIdStatusDTO;
import com.hscloud.hs.cost.account.model.dto.kpi.KpiItemTableFieldDto;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiItemTableField;
import com.hscloud.hs.cost.account.model.vo.kpi.KpiItemTableFieldVO;
import com.hscloud.hs.cost.account.service.kpi.IKpiItemTableFieldService;
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
@RequestMapping("/kpi/kpiItemTableField")
@Tag(name = "k_item_table_field", description = "核算项基础表字段")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class KpiItemTableFieldController {

    private final IKpiItemTableFieldService kpiItemTableFieldService;

    @SysLog("字段列表")
    @GetMapping("/list/{tableId}")
    @Operation(summary = "*字段列表")
    public R<List<KpiItemTableFieldVO>> list(@PathVariable Long tableId) {
        return R.ok(kpiItemTableFieldService.getListByTableId(tableId));
    }

    @GetMapping("/page")
    @Operation(summary = "*字段page")
    public R<IPage<KpiItemTableFieldVO>> getPage(@Validated KpiItemTableFieldDto dto) {
        return R.ok(kpiItemTableFieldService.getPage(dto));
    }

    @SysLog("字段同步")
    @PostMapping("/save/{tableId}")
    @Operation(summary = "*字段同步")
    public R<List<KpiItemTableField>> save(@PathVariable Long tableId) {
        return R.ok(kpiItemTableFieldService.saveFields(tableId));
    }

    @SysLog("状态修改")
    @PostMapping("/switchStatus")
    @Operation(summary = "*状态修改")
    public R switchStatus(@RequestBody BaseIdStatusDTO dto) {
        kpiItemTableFieldService.switchStatus(dto);
        return R.ok();
    }

    @SysLog("字段字典设置")
    @PostMapping("/updateDictCode")
    @Operation(summary = "*字段字典设置")
    public R switchStatus(@RequestBody KpiItemTableFieldDto dto) {
        kpiItemTableFieldService.updateDictCodeById(dto.getId(), dto.getDictCode());
        return R.ok();
    }

    @SysLog("字段添加")
    @PostMapping("/saveOrUpdate")
    @Operation(summary = "*字段添加")
    public R add(@RequestBody @Validated KpiItemTableFieldDto dto) {
        return R.ok(kpiItemTableFieldService.saveOrUpdate(dto));
    }

    @SysLog("字段删除")
    @PostMapping("/del/{id}")
    @Operation(summary = "*字段删除")
    public R delete(@PathVariable Long id) {
        kpiItemTableFieldService.removeById(id);
        return R.ok();
    }

}
