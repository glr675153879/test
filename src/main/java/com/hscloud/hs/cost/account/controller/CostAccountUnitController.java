package com.hscloud.hs.cost.account.controller;

import com.hscloud.hs.cost.account.model.dto.CostAccountUnitDto;
import com.hscloud.hs.cost.account.model.dto.CostAccountUnitQueryDtoNew;
import com.hscloud.hs.cost.account.model.vo.CostAccountUnitExcelVO;
import com.hscloud.hs.cost.account.service.CostAccountUnitService;
import com.pig4cloud.pigx.common.core.util.R;
import com.pig4cloud.pigx.common.excel.annotation.RequestExcel;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author YJM
 * @date 2023-09-05 09:19
 * 核算单元
 */
@RestController
@RequestMapping("/account/unit")
@Tag(name = "unit", description = "核算单元")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class CostAccountUnitController {

    @Autowired
    private CostAccountUnitService costAccountUnitService;

    @PreAuthorize("@pms.hasPermission('kpi_unit_dept_add')")
    @PostMapping("/add")
    public R addUnit(@RequestBody @Validated CostAccountUnitDto costAccountUnitDto) {
        costAccountUnitService.saveUnit(costAccountUnitDto);
        return R.ok();
    }

    @PreAuthorize("@pms.hasPermission('kpi_unit_dept_del')")
    @DeleteMapping("/delete/{id}")
    public R deleteUnit(@PathVariable Long id) {
        costAccountUnitService.deleteUnitById(id);
        costAccountUnitService.removeById(id);
        return R.ok();
    }

    @PreAuthorize("@pms.hasPermission('kpi_unit_dept_edit')")
    @PutMapping("/update")
    public R updateUnit(@RequestBody CostAccountUnitDto costAccountUnitDto) {
        costAccountUnitService.updateUnit(costAccountUnitDto);
        return R.ok();
    }

    @PreAuthorize("@pms.hasPermission('kpi_unit_dept_enable')")
    @PutMapping("/switch")
    @Operation(summary = "启用/停用")
    public R switchStatus(@RequestBody CostAccountUnitDto costAccountUnitDto) {
        costAccountUnitService.switchUnit(costAccountUnitDto);
        return R.ok();
    }
    @GetMapping("/list")
    @Operation(summary = "科室单元列表")
    public R listUnit(CostAccountUnitQueryDtoNew input) {

        return R.ok(costAccountUnitService.listUnit(input));
    }

    /**
     * 导入用户
     *
     * @param excelVOList   用户列表
     * @param bindingResult 错误信息列表
     * @return R
     */
    @PostMapping("/import")
    public R importUnit(@RequestExcel List<CostAccountUnitExcelVO> excelVOList, BindingResult bindingResult) {
        return R.ok(costAccountUnitService.importUnit(excelVOList, bindingResult));
    }

    /**
     * 获取单个核算单元下所有的人员信息
     * @return R
     */

    @GetMapping("/listUser")
    @Operation(summary = "获取单个核算单元下所有的人员信息")
    public R listUser(Long unitId) {
        return R.ok(costAccountUnitService.listUser(unitId));
    }

}
