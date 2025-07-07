package com.hscloud.hs.cost.account.controller.imputation;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hscloud.hs.cost.account.model.entity.base.PageRequest;
import com.hscloud.hs.cost.account.model.entity.imputation.CmiCoefficient;
import com.hscloud.hs.cost.account.service.imputation.ICmiCoefficientService;
import com.pig4cloud.pigx.common.core.util.R;
import com.pig4cloud.pigx.common.log.annotation.SysLog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

/**
 * CMI系数
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/imputation/cmiCoefficient")
@Tag(name = "CMI系数", description = "CMI系数")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class CmiCoefficientController {

    private final ICmiCoefficientService cmiCoefficientService;

    @SysLog("CMI系数page")
    @GetMapping("/page")
    @Operation(summary = "CMI系数page")
    public R<IPage<CmiCoefficient>> page(PageRequest<CmiCoefficient> pr) {
        return R.ok(cmiCoefficientService.page(pr.getPage(), pr.getWrapper()));
    }

    @SysLog("CMI系数新增或编辑")
    @PostMapping("/addOrEdit")
    @Operation(summary = "CMI系数新增或编辑")
    public R add(@Validated @RequestBody CmiCoefficient cmiCoefficient) {
        return R.ok(cmiCoefficientService.saveOrUpdateCmi(cmiCoefficient));
    }


}