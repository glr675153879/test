package com.hscloud.hs.cost.account.controller;


import com.hscloud.hs.cost.account.model.dto.CostAccountIndexDto;
import com.hscloud.hs.cost.account.model.dto.CostAccountIndexQueryDto;
import com.hscloud.hs.cost.account.model.dto.CostAccountIndexStatusDto;
import com.hscloud.hs.cost.account.model.dto.CostAccountIndexVerificationDto;
import com.hscloud.hs.cost.account.service.ICostAccountIndexService;
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
 * <p>
 * 核算指标 前端控制器
 * </p>
 *
 * @author
 * @since 2023-09-04
 */
@RestController
@RequestMapping("/accountIndex")
@Tag(name = "核算指标", description = "accountIndex")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class CostAccountIndexController {


    @Autowired
    private ICostAccountIndexService costAccountIndexService;

    @PostMapping("/saveOrUpdate")
    @Operation(summary = "新增或修改核算指标")
    @PreAuthorize("@pms.hasPermission('kpi_quota_add','kpi_quota_edit')")
    public R saveOrUpdateAccountIndex(@RequestBody @Validated CostAccountIndexDto accountIndexDto) {
        costAccountIndexService.saveOrUpdateAccountIndex(accountIndexDto);
        return R.ok();
    }



    @GetMapping("/getAccountIndexById/{id}")
    @Operation(summary = "根据id查询核算指标")
    public R getAccountIndexById(@PathVariable Long id) {
        return R.ok(costAccountIndexService.getAccountIndexById(id));
    }


    @GetMapping("/list")
    @Operation(summary = "查询核算指标列表")
    public R getAccountIndexPage(CostAccountIndexQueryDto queryDto) {
        return R.ok(costAccountIndexService.getAccountIndexPage(queryDto));
    }


    @DeleteMapping("/delete/{id}")
    @Operation(summary = "删除核算指标")
    @PreAuthorize("@pms.hasPermission('kpi_quota_del')")
    public R deleteAccountIndex(@PathVariable Long id) {
        costAccountIndexService.deleteById(id);
        return R.ok();
    }


    @PutMapping("/updateStatus")
    @Operation(summary = "启停用核算指标")
    @PreAuthorize("@pms.hasPermission('kpi_quota_enable')")
    public R updateStatusAccountIndex(@RequestBody CostAccountIndexStatusDto dto) {
        return R.ok( costAccountIndexService.updateStatusAccountIndex(dto));
    }

    @PostMapping("/verification")
    @Operation(summary = "核算指标校验")
    public R verificationAccountIndex(@RequestBody CostAccountIndexVerificationDto dto) {
            return R.ok( costAccountIndexService.verificationAccountIndex(dto));
    }


    @PostMapping("/update/system")
    @Operation(summary = "修改系统指标")
    public R updateSystemIndex(@RequestBody @Validated CostAccountIndexDto accountIndexDto) {
        costAccountIndexService.updateSystemIndex(accountIndexDto);
        return R.ok();
    }
}
