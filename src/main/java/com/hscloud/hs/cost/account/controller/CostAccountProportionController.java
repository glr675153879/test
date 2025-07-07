package com.hscloud.hs.cost.account.controller;

import com.hscloud.hs.cost.account.model.dto.*;
import com.hscloud.hs.cost.account.service.ICostAccountProportionService;
import com.pig4cloud.pigx.common.core.util.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 核算比例 前端控制器
 * @author banana
 * @create 2023-09-13 10:09
 */
@RestController
@RequestMapping("/account/proportion")
@Tag(name = "核算比例", description = "accountProportion")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class CostAccountProportionController {

    @Autowired
    private ICostAccountProportionService iCostAccountProportionService;

    @PreAuthorize("@pms.hasPermission('kpi_rule_rate_add')")
    @Operation(summary = "新增核算比例项")
    @PostMapping("/add")
    public R CostAccountProportionAdd(@Validated @RequestBody CostAccountProportionAddDto input){
        return R.ok(iCostAccountProportionService.CostAccountProportionAdd(input));
    }

    @Operation(summary = "查询核算比例项信息")
    @GetMapping("/get")
    public R CostAccountList(CostAccountListDto input){
        return R.ok(iCostAccountProportionService.CostAccountList(input));
    }

    @Operation(summary = "查询核算比例项的关联信息")
    @GetMapping("/get/relation")
    public R CostAccountProportionList(@Validated CostAccountProportionListDto input){
        return R.ok(iCostAccountProportionService.CostAccountProportionList(input));
    }

    @PreAuthorize("@pms.hasPermission('kpi_rule_rate_del')")
    @Operation(summary = "删除核算比例项")
    @DeleteMapping("/del/{id}")
    public R CostAccountProportionDel(@PathVariable Long id){
        return R.ok(iCostAccountProportionService.removeById(id));
    }

    @PreAuthorize("@pms.hasPermission('kpi_rule_rate_enable')")
    @Operation(summary = "启停用核算比例项")
    @PutMapping("/updateStatus")
    public R CostAccountProportionChange(@RequestBody CostAccountProportionStatusDto input){
        return R.ok(iCostAccountProportionService.CostAccountProportionChange(input));
    }

    @PreAuthorize("@pms.hasPermission('kpi_rule_rate_edit')")
    @Operation(summary = "编辑核算项信息")
    @PostMapping("/edit")
    public R CostAccountProportionEdit(@RequestBody List<CostAccountProportionEditDto> input){
        iCostAccountProportionService.CostAccountProportionEdit(input);
        return R.ok();
    }

    /**
     * 根据核算项和核算范围获取核算比例
     * @param input
     * @return
     */
    @Operation(summary = "根据核算项和核算范围获取核算比例")
    @PostMapping("/get/proportion")
    public R getProportion(@RequestBody @Validated CostAccountProportionDto input){

        return R.ok(iCostAccountProportionService.getProportion(input));
    }

}
