package com.hscloud.hs.cost.account.controller.imputation;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hscloud.hs.cost.account.model.dto.imputation.NonAndSpecialDelDTO;
import com.hscloud.hs.cost.account.model.entity.base.PageRequest;
import com.hscloud.hs.cost.account.model.entity.imputation.NonIncomePerson;
import com.hscloud.hs.cost.account.service.imputation.INonIncomePersonService;
import com.pig4cloud.pigx.common.core.util.R;
import com.pig4cloud.pigx.common.log.annotation.SysLog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 不计收入人员
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/imputation/nonIncomePerson")
@Tag(name = "不计收入人员", description = "不计收入人员")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class NonIncomePersonController {

    private final INonIncomePersonService nonIncomePersonService;

    @SysLog("不计收入人员page")
    @GetMapping("/page/{imputationId}")
    @Operation(summary = "不计收入人员page")
    public R<IPage<NonIncomePerson>> page(PageRequest<NonIncomePerson> pr, @PathVariable Long imputationId) {
        return R.ok(nonIncomePersonService.pageNonIncomePerson(pr.getPage(), pr.getWrapper(), imputationId));
    }


    @SysLog("不计收入人员addOrEdit")
    @PostMapping("/addOrEdit")
    @Operation(summary = "不计收入人员addOrEdit")
    public R add(@Validated @RequestBody NonIncomePerson nonIncomePerson) {
        return R.ok(nonIncomePersonService.saveOrUpdateNonIncomePerson(nonIncomePerson));
    }

    @SysLog("不计收入人员del")
    @PostMapping("/del")
    @Operation(summary = "不计收入人员del")
    public R del(@RequestBody NonAndSpecialDelDTO nonAndSpecialDelDTO) {
        return R.ok(nonIncomePersonService.removeNonIncomePerson(nonAndSpecialDelDTO));
    }

}