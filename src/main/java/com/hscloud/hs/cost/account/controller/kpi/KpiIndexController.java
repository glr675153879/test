package com.hscloud.hs.cost.account.controller.kpi;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hscloud.hs.cost.account.model.dto.CostAccountIndexDto;
import com.hscloud.hs.cost.account.model.dto.kpi.KpiIndexDto;
import com.hscloud.hs.cost.account.model.dto.kpi.KpiIndexEnableDto;
import com.hscloud.hs.cost.account.model.dto.kpi.KpiIndexListDto;
import com.hscloud.hs.cost.account.model.entity.base.PageRequest;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiIndex;
import com.hscloud.hs.cost.account.model.vo.kpi.KpiIndexListVO;
import com.hscloud.hs.cost.account.model.vo.kpi.KpiIndexVO;
import com.hscloud.hs.cost.account.service.kpi.IKpiIndexService;
import com.pig4cloud.pigx.admin.api.entity.SysUser;
import com.pig4cloud.pigx.admin.api.feign.RemoteUserService;
import com.pig4cloud.pigx.admin.api.vo.UserCoreVo;
import com.pig4cloud.pigx.common.core.constant.SecurityConstants;
import com.pig4cloud.pigx.common.core.util.R;
import com.pig4cloud.pigx.common.log.annotation.SysLog;
import groovy.transform.AutoImplement;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.Arrays;
import java.util.List;

/**
* 指标表
*
*/
@RestController
@RequiredArgsConstructor
@RequestMapping("/kpi/kpiIndex")
@Tag(name = "k_指标", description = "指标表")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class KpiIndexController {

    private final IKpiIndexService kpiIndexService;

    @SysLog("指标表list")
    @GetMapping("/list")
    @Operation(summary = "*指标表list")
    public R<List<KpiIndexListVO>> list(KpiIndexListDto input) {
        return R.ok(kpiIndexService.list(input));
    }

    @SysLog("指标表page")
    @GetMapping("/page")
    @Operation(summary = "*指标表page")
    public R<IPage<KpiIndexListVO>> page(KpiIndexListDto input) {
        return R.ok(kpiIndexService.getPage(input));
    }

    @SysLog("指标表info")
    @GetMapping("/info/{id}")
    @Operation(summary = "*指标表info")
    public R<KpiIndexVO> info(@PathVariable Long id) {
        return R.ok(kpiIndexService.getInfo(id));
    }

    @SysLog("新增修改核算指标")
    @PostMapping("/saveOrUpdate")
    @Operation(summary = "*新增修改核算指标")
    public R saveOrUpdateAccountIndex(@RequestBody @Validated KpiIndexDto dto) {
        return R.ok(kpiIndexService.saveOrUpdateKpiIndex(dto));
    }

    @SysLog("启用停用")
    @PostMapping("/enable")
    @Operation(summary = "*启用停用指标")
    public R enable(@RequestBody @Validated KpiIndexEnableDto dto) {
        kpiIndexService.enable(dto);
        return R.ok();
    }

    @SysLog("指标表del")
    @PostMapping("/del/{id}")
    @Operation(summary = "指标表del")
    public R del(@PathVariable Long id)  {
        kpiIndexService.del(id);
        return R.ok();
    }
    @Autowired
    private RemoteUserService remoteUserService;

    @SysLog("获取所有人员")
    @GetMapping("/remoteUsers")
    @Operation(summary = "*获取所有人员")
    public R remoteUsers()  {
        List<UserCoreVo> var1 = remoteUserService.listMainDetails(SecurityConstants.FROM_IN).getData();
        return R.ok(var1);
    }

}