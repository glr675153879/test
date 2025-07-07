package com.hscloud.hs.cost.account.controller.kpi;

import com.hscloud.hs.cost.account.model.dto.kpi.KpiValidatorDTO;
import com.hscloud.hs.cost.account.model.vo.ValidatorResultVo;
import com.hscloud.hs.cost.account.service.kpi.KpiValidatorService;
import com.pig4cloud.pigx.common.core.util.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Administrator
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/kpi/validator")
@Tag(name = "k_validator", description = "核算项校验管理")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class KpiValidatorController {
    private final KpiValidatorService kpiValidatorService;

    @PostMapping("/sql")
    @Operation(summary = "*sql配置校验")
    public R<ValidatorResultVo> sqlConfigValidator(@RequestBody @Validated KpiValidatorDTO dto) {
        return R.ok(kpiValidatorService.itemValidator(dto,true,null,null));
    }

}
