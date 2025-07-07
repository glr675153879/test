package com.hscloud.hs.cost.account.controller.kpi;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hscloud.hs.cost.account.model.dto.kpi.KpiItemEquivalentConfigCopyDTO;
import com.hscloud.hs.cost.account.model.dto.kpi.KpiItemEquivalentConfigDTO;
import com.hscloud.hs.cost.account.model.dto.userAttendance.ExcelImportDTO;
import com.hscloud.hs.cost.account.model.vo.kpi.KpiItemEquivalentConfigVO;
import com.hscloud.hs.cost.account.service.kpi.IKpiItemEquivalentConfigService;
import com.hscloud.hs.cost.account.utils.ExcelUtil;
import com.pig4cloud.pigx.common.core.util.R;
import com.pig4cloud.pigx.common.log.annotation.SysLog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/kpi/kpiItemEquivalentConfig")
@Tag(name = "k_当量配置", description = "当量配置")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class KpiItemEquivalentConfigController {

    private final IKpiItemEquivalentConfigService kpiItemEquivalentConfigService;

    @GetMapping("/info/{id}")
    @Operation(summary = "*info")
    public R<KpiItemEquivalentConfigVO> info(@PathVariable Long id) {
        return R.ok(kpiItemEquivalentConfigService.getInfo(id));
    }

    @SysLog("列表")
    @GetMapping("/list")
    @Operation(summary = "*列表")
    public R<List<KpiItemEquivalentConfigVO>> list(KpiItemEquivalentConfigDTO dto) {
        return R.ok(kpiItemEquivalentConfigService.getList(dto));
    }

    @GetMapping("/page")
    @Operation(summary = "*分页查询")
    public R<IPage<KpiItemEquivalentConfigVO>> getPage(KpiItemEquivalentConfigDTO dto) {
        return R.ok(kpiItemEquivalentConfigService.getPage(dto));
    }

    @SysLog("新增OR编辑")
    @PostMapping("/saveOrUpdate")
    @Operation(summary = "*新增")
    public R add(@RequestBody @Validated List<KpiItemEquivalentConfigDTO> dtos) {
        kpiItemEquivalentConfigService.saveOrUpdate(dtos);
        return R.ok();
    }

    @SysLog("复制")
    @PostMapping("/copy")
    @Operation(summary = "*复制")
    public R copy(@RequestBody KpiItemEquivalentConfigCopyDTO dto) {
        kpiItemEquivalentConfigService.copy(dto);
        return R.ok();
    }

    @SysLog("导入")
    @PostMapping("/uploadFile")
    @Operation(summary = "*导入")
    public R uploadFile(@RequestParam("file") MultipartFile file, ExcelImportDTO dto, Long accountUnitId) throws Exception {
        String[][] xlsDataArr = ExcelUtil.doExcelH(file, 0);
        return R.ok(kpiItemEquivalentConfigService.uploadFile(xlsDataArr, dto, accountUnitId));
    }

    @SysLog("是否继承")
    @PostMapping("/updateInheritFlag/{id}/{inheritFlag}")
    @Operation(summary = "*修改是否继承")
    public R updateInheritFlag(@PathVariable Long id, @PathVariable String inheritFlag) {
        kpiItemEquivalentConfigService.updateInheritFlag(id, inheritFlag);
        return R.ok();
    }

    @SysLog("删除")
    @DeleteMapping("/del/{id}")
    @Operation(summary = "*删除")
    public R delete(@PathVariable Long id) {
        kpiItemEquivalentConfigService.removeById(id);
        return R.ok();
    }

    @SysLog("批量删除")
    @DeleteMapping("/del/batch")
    @Operation(summary = "*批量删除")
    public R deleteBatch(@RequestBody List<Long> ids) {
        if (ids != null && !ids.isEmpty()) {
            kpiItemEquivalentConfigService.removeByIds(ids);
        }

        return R.ok();
    }

    @SysLog("排序")
    @PostMapping("/updateSeq")
    @Operation(summary = "*排序")
    public R updateSeq(@RequestBody List<KpiItemEquivalentConfigDTO> dtos) {
        kpiItemEquivalentConfigService.updateSeq(dtos);
        return R.ok();
    }
}
