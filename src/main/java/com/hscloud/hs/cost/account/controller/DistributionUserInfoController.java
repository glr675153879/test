package com.hscloud.hs.cost.account.controller;


import com.hscloud.hs.cost.account.model.dto.DistributionUserInfoQueryDto;
import com.hscloud.hs.cost.account.model.entity.DistributionUserInfo;
import com.hscloud.hs.cost.account.service.IDistributionUserInfoService;
import com.pig4cloud.pigx.common.core.util.R;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 人员信息 前端控制器
 * </p>
 *
 * @author author
 * @since 2023-11-20
 */
@RestController
@RequestMapping("/distribution/user/info")
@Tag(description = "distribution_user_info", name = "人员信息")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
@RequiredArgsConstructor
public class DistributionUserInfoController {

    private final IDistributionUserInfoService userInfoService;

    /**
     * 修改人员信息
     */
    @PreAuthorize("@pms.hasPermission('kpi_user_edit')")
    @PutMapping("/update")
    public R updateItem(@RequestBody DistributionUserInfo distributionUserInfo) {
        return R.ok(userInfoService.updateById(distributionUserInfo));
    }


    /**
     * 分页模糊匹配查询
     */
    @GetMapping("/list")
    public R listItem(@Validated DistributionUserInfoQueryDto dto) {
        return R.ok(userInfoService.listUserInfo(dto));
    }

}
