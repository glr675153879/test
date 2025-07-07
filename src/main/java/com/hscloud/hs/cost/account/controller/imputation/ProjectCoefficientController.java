package com.hscloud.hs.cost.account.controller.imputation;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.hscloud.hs.cost.account.model.dto.imputation.ProjectCoefficientForbiddenDTO;
import com.hscloud.hs.cost.account.model.entity.base.PageRequest;
import com.hscloud.hs.cost.account.model.entity.imputation.ProjectCoefficient;
import com.hscloud.hs.cost.account.service.imputation.IProjectCoefficientService;
import com.pig4cloud.pigx.common.core.exception.BizException;
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
import java.util.Objects;

/**
 * 项目系数
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/imputation/projectCoefficient")
@Tag(name = "项目系数", description = "项目系数")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class ProjectCoefficientController {

    private final IProjectCoefficientService projectCoefficientService;

    @SysLog("项目系数page")
    @GetMapping("/page")
    @Operation(summary = "项目系数page")
    public R<IPage<ProjectCoefficient>> page(PageRequest<ProjectCoefficient> pr) {
        return R.ok(projectCoefficientService.page(pr.getPage(), pr.getWrapper()));
    }


    @SysLog("项目系数add")
    @PostMapping("/add")
    @Operation(summary = "项目系数add")
    public R add(@Validated @RequestBody ProjectCoefficient projectCoefficient) {
        LambdaQueryWrapper<ProjectCoefficient> wrapper = Wrappers.<ProjectCoefficient>lambdaQuery()
                .eq(ProjectCoefficient::getAssessmentProject, projectCoefficient.getAssessmentProject())
                .eq(ProjectCoefficient::getAccountUnitId, projectCoefficient.getAccountUnitId());
        ProjectCoefficient one = projectCoefficientService.getOne(wrapper);
        if (ObjectUtils.isNotNull(one)) {
            throw new BizException("考核项目名称已经存在！");
        }
        return R.ok(projectCoefficientService.save(projectCoefficient));
    }

    @SysLog("项目系数edit")
    @PostMapping("/edit")
    @Operation(summary = "项目系数edit")
    public R edit(@RequestBody ProjectCoefficient projectCoefficient) {
        LambdaQueryWrapper<ProjectCoefficient> wrapper = Wrappers.<ProjectCoefficient>lambdaQuery()
                .eq(ProjectCoefficient::getAssessmentProject, projectCoefficient.getAssessmentProject())
                .eq(ProjectCoefficient::getAccountUnitId, projectCoefficient.getAccountUnitId());
        ProjectCoefficient one = projectCoefficientService.getOne(wrapper);
        if (ObjectUtils.isNotNull(one) && !Objects.equals(one.getId(), projectCoefficient.getId())) {
            throw new BizException("考核项目名称已经存在！");
        }
        return R.ok(projectCoefficientService.updateById(projectCoefficient));
    }

    @SysLog("项目系数停用")
    @PostMapping("/forbiddenOrEnable")
    @Operation(summary = "项目系数停用/启用")
    public R forbidden(@Validated @RequestBody ProjectCoefficientForbiddenDTO projectCoefficientForbiddenDTO) {
        return R.ok(projectCoefficientService.forbidden(projectCoefficientForbiddenDTO));
    }


}