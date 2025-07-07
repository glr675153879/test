package com.hscloud.hs.cost.account.controller;


import com.hscloud.hs.cost.account.model.entity.CostBaseInitialize;
import com.hscloud.hs.cost.account.service.ICostBaseInitializeService;
import com.pig4cloud.pigx.common.core.util.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 初始化完成表 前端控制器
 * </p>
 *
 * @author 
 * @since 2023-09-08
 */
@RestController
@RequestMapping("/baseInitialize")
@Tag(name = "通用初始化", description = "initialize")
public class CostBaseInitializeController {

    @Autowired
    private ICostBaseInitializeService costBaseInitializeService;

    @PostMapping
    @Operation(summary = "初始化完成")
    public R initialize(@RequestBody CostBaseInitialize costBaseInitialize) {
        costBaseInitializeService.initialize(costBaseInitialize);
        return R.ok();
    }

    @GetMapping
    @Operation(summary = "获取是否初始化")
    public R getInitialize(CostBaseInitialize costBaseInitialize) {
        return R.ok(costBaseInitializeService.getInitialize(costBaseInitialize));
    }
}
