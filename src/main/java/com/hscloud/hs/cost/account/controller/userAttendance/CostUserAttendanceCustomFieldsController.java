package com.hscloud.hs.cost.account.controller.userAttendance;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hscloud.hs.cost.account.model.dto.userAttendance.CostUserAttendanceCustomFieldsDto;
import com.hscloud.hs.cost.account.model.entity.base.PageRequest;
import com.hscloud.hs.cost.account.model.entity.userAttendance.CostUserAttendanceCustomFields;
import com.hscloud.hs.cost.account.service.userAttendance.ICostUserAttendanceCustomFieldsService;
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
 * 人员考勤自定义字段表
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/userAttendance/costUserAttendanceCustomFields")
@Tag(name = "costUserAttendanceCustomFields", description = "人员考勤自定义字段表")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class CostUserAttendanceCustomFieldsController {

    private final ICostUserAttendanceCustomFieldsService costUserAttendanceCustomFieldsService;

    @SysLog("人员考勤自定义字段表info")
    @GetMapping("/info/{id}")
    @Operation(summary = "人员考勤自定义字段表info")
    public R<CostUserAttendanceCustomFields> info(@PathVariable Long id) {
        return R.ok(costUserAttendanceCustomFieldsService.getById(id));
    }

    @SysLog("人员考勤自定义字段表page")
    @GetMapping("/page")
    @Operation(summary = "人员考勤自定义字段表page")
    public R<IPage<CostUserAttendanceCustomFields>> page(PageRequest<CostUserAttendanceCustomFields> pr) {
        return R.ok(costUserAttendanceCustomFieldsService.page(pr.getPage(), pr.getWrapper().lambda().orderByAsc(CostUserAttendanceCustomFields::getSortNum)));
    }

    @SysLog("人员考勤自定义字段表list")
    @GetMapping("/list")
    @Operation(summary = "人员考勤自定义字段表list")
    public R<List<CostUserAttendanceCustomFields>> list(PageRequest<CostUserAttendanceCustomFields> pr) {
        return R.ok(costUserAttendanceCustomFieldsService.list(pr.getWrapper().lambda().orderByAsc(CostUserAttendanceCustomFields::getSortNum)));
    }

    @SysLog("人员考勤自定义字段表list")
    @GetMapping("/list/group")
    @Operation(summary = "人员考勤自定义字段表list")
    public R<List<CostUserAttendanceCustomFields>> listGroup() {
        return R.ok(costUserAttendanceCustomFieldsService.listGroup());
    }

    @SysLog("人员考勤自定义字段表add")
    @PostMapping("/add")
    @Operation(summary = "人员考勤自定义字段表add")
    public R add(@RequestBody CostUserAttendanceCustomFields costUserAttendanceCustomFields) {
        return R.ok(costUserAttendanceCustomFieldsService.save(costUserAttendanceCustomFields));
    }

    @SysLog("人员考勤自定义字段表edit")
    @PostMapping("/edit")
    @Operation(summary = "人员考勤自定义字段表edit")
    public R edit(@RequestBody CostUserAttendanceCustomFieldsDto customParams) {
        return R.ok(costUserAttendanceCustomFieldsService.saveUpdate(customParams));
    }

    @SysLog("人员考勤自定义字段表del")
    @PostMapping("/del/{id}")
    @Operation(summary = "人员考勤自定义字段表del")
    public R del(@PathVariable Long id) {
        return R.ok(costUserAttendanceCustomFieldsService.removeById(id));
    }

    @SysLog("人员考勤自定义字段表delBatch")
    @PostMapping("/delBatch/{ids}")
    @Operation(summary = "人员考勤自定义字段表delBatch 1,2,3")
    public R delBatch(@PathVariable String ids) {
        return R.ok(costUserAttendanceCustomFieldsService.removeBatchByIds(Arrays.asList(ids.split(","))));
    }

    @SysLog("人人员考勤自定义字段表启停用")
    @PostMapping("/activate")
    @Operation(summary = "人员考勤自定义字段表启停用")
    public R activate(@RequestBody List<Long> ids) {
        return R.ok(costUserAttendanceCustomFieldsService.activate(ids));
    }
}