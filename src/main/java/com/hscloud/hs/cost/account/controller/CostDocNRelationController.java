package com.hscloud.hs.cost.account.controller;

import com.hscloud.hs.cost.account.model.dto.BindDocNDto;
import com.hscloud.hs.cost.account.model.dto.listDocNRelationDto;
import com.hscloud.hs.cost.account.service.ICostDocNRelationService;
import com.pig4cloud.pigx.common.core.util.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 医护对应组
 *
 * @author banana
 * @create 2023-09-11 13:53
 */
@RestController
@Tag(name = "医护对应组", description = "docNRelation")
@RequestMapping("/docn/relation")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class CostDocNRelationController {

    @Autowired
    private ICostDocNRelationService iCostDocNRelationService;

    @Operation(summary = "获取医护对应组列表")
    @GetMapping("/list")
    public R listDocNRelation(listDocNRelationDto input) {
        return R.ok(iCostDocNRelationService.listDocNRelation(input));
    }

    @PreAuthorize("@pms.hasPermission('kpi_rule_relation_set')")
    @Operation(summary = "医护对应关系绑定")
    @PostMapping("/bind")
    public R bindDocNRelation(@Validated @RequestBody BindDocNDto input) {
        iCostDocNRelationService.bindDocNRelation(input);
        return R.ok();
    }

}
