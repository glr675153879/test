package com.hscloud.hs.cost.account.controller;


import com.hscloud.hs.cost.account.model.dto.CostCollectionUnitDto;
import com.hscloud.hs.cost.account.model.dto.CostCollectionUnitQueryDto;
import com.hscloud.hs.cost.account.service.ICostCollectionUnitService;
import com.pig4cloud.pigx.common.core.util.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 归集单元表 前端控制器
 * </p>
 *
 * @author 
 * @since 2023-09-05
 */
@RestController
@RequestMapping("/collectionUnit")
@Tag(name = "归集单元", description = "collectionUnit")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class CostCollectionUnitController {

    @Autowired
    private ICostCollectionUnitService costCollectionUnitService;

    @PostMapping("/save")
    @Operation(summary = "新增归集单元")
    public R saveCollectionUnit(@RequestBody CostCollectionUnitDto dto) {
        costCollectionUnitService.saveCollectionUnit(dto);
        return R.ok();
    }

    @PutMapping("/update")
    @Operation(summary = "修改归集单元")
    public R updateCollectionUnit(@RequestBody CostCollectionUnitDto dto) {
        return R.ok( costCollectionUnitService.updateCollectionUnit(dto));
    }

    @GetMapping("/list")
    @Operation(summary = "获取归集单元列表")
    public R listCollectionUnit(CostCollectionUnitQueryDto queryDto) {
        return R.ok( costCollectionUnitService.listCollectionUnit(queryDto));
    }

    @DeleteMapping("/delete/{id}")
    @Operation(summary = "删除归集单元")
    public R deleteCollectionUnitById(@PathVariable Long id) {
        costCollectionUnitService.deleteCollectionUnitById(id);
        return R.ok();
    }

    @PutMapping("/updateStatus")
    @Operation(summary = "启停用归集单元")
    public R updateStatusCollectionUnit(@RequestBody CostCollectionUnitDto dto) {
        return R.ok( costCollectionUnitService.updateStatusCollectionUnit(dto));
    }

}
