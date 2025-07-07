package com.hscloud.hs.cost.account.controller.kpi;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hscloud.hs.cost.account.mapper.kpi.KpiSignHeadMapper;
import com.hscloud.hs.cost.account.model.entity.base.PageRequest;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiSignHead;
import com.hscloud.hs.cost.account.service.kpi.IKpiSignHeadService;
import com.pig4cloud.pigx.common.core.exception.BizException;
import com.pig4cloud.pigx.common.core.util.R;
import com.pig4cloud.pigx.common.log.annotation.SysLog;
import com.pig4cloud.pigx.common.security.util.SecurityUtils;
import io.netty.util.internal.StringUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;
import java.util.Arrays;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;

/**
* entity名称未取到
*
*/
@RestController
@RequiredArgsConstructor
@RequestMapping("/kpi/kpiSignHead")
@Tag(name = "kpiSignHead", description = "entity名称未取到")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
@Slf4j
public class KpiSignHeadController {

    private final IKpiSignHeadService kpiSignHeadService;
    private final KpiSignHeadMapper kpiSignHeadMapper;

    @SysLog("entity名称未取到info")
    @GetMapping("/info/{id}")
    @Operation(summary = "entity名称未取到info")
    //@PreAuthorize("@pms.hasPermission('kpiSignHead_info')")
    public R<KpiSignHead> info(@PathVariable Long id) {
        return R.ok(kpiSignHeadService.getById(id));
    }

    @SysLog("entity名称未取到page")
    @GetMapping("/page")
    @Operation(summary = "entity名称未取到page")
    //@PreAuthorize("@pms.hasPermission('kpiSignHead_page')")
    public R<IPage<KpiSignHead>> page(PageRequest<KpiSignHead> pr) {
        return R.ok(kpiSignHeadService.page(pr.getPage(),pr.getWrapper()));
    }

    @SysLog("entity名称未取到list")
    @GetMapping("/list")
    @Operation(summary = "entity名称未取到list")
    //@PreAuthorize("@pms.hasPermission('kpiSignHead_list')")
    public R<List<KpiSignHead>> list(PageRequest<KpiSignHead> pr) {
        return R.ok(kpiSignHeadService.list(pr.getWrapper().orderBy(true,true,"status").orderBy(true,true,"seq")));
    }

    @SysLog("entity名称未取到add")
    @PostMapping("/add")
    @Operation(summary = "entity名称未取到add")
   // @PreAuthorize("@pms.hasPermission('kpiSignHead_add')")
    public R add(@RequestBody KpiSignHead kpiSignHead)  {
        kpiSignHead.setTenantId(SecurityUtils.getUser().getTenantId());
        if (!StringUtil.isNullOrEmpty(kpiSignHead.getDelFlag())){
            kpiSignHead.setDelFlag("0");
        }
        if (!StringUtil.isNullOrEmpty(kpiSignHead.getStatus())){
            kpiSignHead.setStatus("0");
        }
        return R.ok(kpiSignHeadService.save(kpiSignHead));
    }

    @SysLog("entity名称未取到edit")
    @PostMapping("/edit")
    @Operation(summary = "entity名称未取到edit")
    //@PreAuthorize("@pms.hasPermission('kpiSignHead_edit')")
    public R edit(@RequestBody KpiSignHead kpiSignHead)  {
        return R.ok(kpiSignHeadService.updateById(kpiSignHead));
    }

    @SysLog("entity名称未取到del")
    @PostMapping("/del/{id}")
    @Operation(summary = "entity名称未取到del")
    //@PreAuthorize("@pms.hasPermission('kpiSignHead_del')")
    public R del(@PathVariable Long id)  {
        int i = kpiSignHeadMapper.getCopyById(Arrays.asList(id.toString()));
        if (i>0){
            throw new BizException("该项目已签发，无法删除");
        }
        return R.ok(kpiSignHeadService.removeById(id));
    }

    @SysLog("entity名称未取到delBatch")
    @PostMapping("/delBatch/{ids}")
    @Operation(summary = "entity名称未取到delBatch 1,2,3")
    //@PreAuthorize("@pms.hasPermission('kpiSignHead_delBatch')")
    public R delBatch(@PathVariable String ids)  {
        int i = kpiSignHeadMapper.getCopyById(Arrays.asList(ids.split(",")));
        if (i>0){
            throw new BizException("该项目已签发，无法删除");
        }
        return R.ok(kpiSignHeadService.removeBatchByIds(Arrays.asList(ids.split(","))));
    }
}