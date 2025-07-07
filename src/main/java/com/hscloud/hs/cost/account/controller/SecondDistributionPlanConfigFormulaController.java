package com.hscloud.hs.cost.account.controller;


import com.hscloud.hs.cost.account.service.ISecondDistributionPlanConfigFormulaService;
import com.pig4cloud.pigx.common.core.util.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * <p>
 * 二次分配方案公式配置表 前端控制器
 * </p>
 *
 * @author
 * @since 2023-11-17
 */
@RestController
@RequestMapping("/second/plan/config/formula")
@Tag(name = "二次分配方案总公式配置", description = "SecondDistributionPlanConfigFormulaController")
@RequiredArgsConstructor
public class SecondDistributionPlanConfigFormulaController {

    private final ISecondDistributionPlanConfigFormulaService secondDistributionPlanConfigFormulaService;

    @Operation(summary = "获取可选指标")
    @GetMapping("/select")
    public R getAccountIndexSelect(@RequestParam("input") List<Long> input){
        return R.ok(secondDistributionPlanConfigFormulaService.getAccountIndexSelect(input));
    }

}
