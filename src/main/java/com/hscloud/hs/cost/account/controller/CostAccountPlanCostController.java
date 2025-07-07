package com.hscloud.hs.cost.account.controller;

import com.hscloud.hs.cost.account.model.dto.CostAccountPlanCostQueryDto;
import com.hscloud.hs.cost.account.model.entity.CostAccountPlanCost;
import com.hscloud.hs.cost.account.service.CostAccountPlanCostService;
import com.pig4cloud.pigx.common.core.util.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "方案公式", description = "方案公式")
@RequestMapping("account/plan/cost")
public class CostAccountPlanCostController {
    @Autowired
    private CostAccountPlanCostService costAccountPlanCostService;

    @PostMapping("/add")
    public R addCostFormula(@RequestBody CostAccountPlanCost costAccountPlanCost){
        return R.ok(costAccountPlanCostService.save(costAccountPlanCost));
    }

    @PutMapping("/update")
    public R updateCostFormula(@RequestBody CostAccountPlanCostQueryDto costAccountPlanCostQueryDto){
        return R.ok(costAccountPlanCostService.updateCostFormula(costAccountPlanCostQueryDto));
    }

    @DeleteMapping("/delete/{id}")
    public R deleteCostFormula(@PathVariable Long id){
        return R.ok(costAccountPlanCostService.removeById(id));
    }

    @GetMapping("/listAll")
    @Operation(summary = "查询公式列表")
    public R listAllCostFormula(Long planId){
        return R.ok(costAccountPlanCostService.listAllCostFormula(planId));
    }

    @GetMapping("/list/{id}")
    @Operation(summary = "查询单个公式")
    public R listCostFormula(@PathVariable Long id){
        return R.ok(costAccountPlanCostService.listCostFormula(id));
    }
}
