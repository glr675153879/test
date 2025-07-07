package com.hscloud.hs.cost.account.controller.imputation;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hscloud.hs.cost.account.model.dto.imputation.NonAndSpecialDelDTO;
import com.hscloud.hs.cost.account.model.entity.base.PageRequest;
import com.hscloud.hs.cost.account.model.entity.imputation.SpecialImputationPerson;
import com.hscloud.hs.cost.account.service.imputation.ISpecialImputationPersonService;
import com.pig4cloud.pigx.common.core.util.R;
import com.pig4cloud.pigx.common.log.annotation.SysLog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

/**
 * 特殊归集人员
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/imputation/specialImputationPerson")
@Tag(name = "特殊归集人员", description = "特殊归集人员")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class SpecialImputationPersonController {

    private final ISpecialImputationPersonService specialImputationPersonService;

    @SysLog("特殊归集人员page")
    @GetMapping("/page/{imputationId}")
    @Operation(summary = "特殊归集人员page")
    public R<IPage<SpecialImputationPerson>> page(PageRequest<SpecialImputationPerson> pr, @PathVariable Long imputationId) {
        return R.ok(specialImputationPersonService.pageSpecialImputationPerson(pr.getPage(), pr.getWrapper(), imputationId));
    }


    @SysLog("特殊归集人员新增或编辑")
    @PostMapping("/addOrEdit")
    @Operation(summary = "特殊归集人员新增或编辑")
    public R add(@RequestBody SpecialImputationPerson specialImputationPerson) {
        return R.ok(specialImputationPersonService.saveOrEditSpecialImputationPerson(specialImputationPerson));
    }


    @SysLog("特殊归集人员del")
    @PostMapping("/del")
    @Operation(summary = "特殊归集人员del")
    public R del(@RequestBody NonAndSpecialDelDTO nonAndSpecialDelDTO) {
        return R.ok(specialImputationPersonService.removeSpecialImputationPerson(nonAndSpecialDelDTO));
    }

}