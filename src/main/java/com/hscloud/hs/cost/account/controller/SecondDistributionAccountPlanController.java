package com.hscloud.hs.cost.account.controller;


import com.hscloud.hs.cost.account.model.dto.saveDistributionAccountPlanDto;
import com.hscloud.hs.cost.account.service.ISecondDistributionAccountPlanService;
import com.pig4cloud.pigx.common.core.util.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * <p>
 * 二次分配方案表 前端控制器
 * </p>
 *
 * @author 
 * @since 2023-11-17
 */
@RestController
@RequestMapping("/second/account/plan")
@RequiredArgsConstructor
@Tag(name = "二次分配方案配置", description = "secondDistributionAccountPlanController")
public class SecondDistributionAccountPlanController {

    private final ISecondDistributionAccountPlanService secondDistributionAccountPlanService;

    @Operation(summary = "获取最近一次分配方案详情")
    @GetMapping("/details")
    public R getDistributionAccountPlanDetails(@Valid @NotNull(message = "科室单元id不能为空")  Long unitId){
        return R.ok(secondDistributionAccountPlanService.getDistributionAccountPlanDetails(unitId));
    }

    @Operation(summary = "保存分配方案")
    @PostMapping("/save")
    public R saveDistributionAccountPlan(@Validated @RequestBody saveDistributionAccountPlanDto input){
        secondDistributionAccountPlanService.saveDistributionAccountPlan(input);
        return R.ok();
    }

}
