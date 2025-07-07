package com.hscloud.hs.cost.account.controller;


import com.hscloud.hs.cost.account.service.ISecondDistributionAccountIndexService;
import com.pig4cloud.pigx.common.core.util.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 二次分配方案核算指标表 前端控制器
 * </p>
 *
 * @author 
 * @since 2023-11-24
 */
@RestController
@RequestMapping("/second/account/index")
@Tag(name = "二次分配核算指标配置", description = "secondDistributionAccountPlanController")
public class SecondDistributionAccountIndexController {

    @Autowired
    private ISecondDistributionAccountIndexService secondDistributionAccountIndexService;

    /**
     * 获取可选指标
     */
    @Operation(summary = "获取固定可选指标")
    @GetMapping("/select")
    public R getAccountIndexSelect(@RequestParam("accountIndex") String accountIndex, @RequestParam("unitId")Long unitId){
        return R.ok(secondDistributionAccountIndexService.getAccountIndexSelect(accountIndex, unitId));
    }

}
