package com.hscloud.hs.cost.account.controller.kpi;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hscloud.hs.cost.account.mapper.kpi.KpiConfigMapper;
import com.hscloud.hs.cost.account.model.entity.base.PageRequest;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiConfig;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiSignRight;
import com.hscloud.hs.cost.account.service.kpi.IKpiSignRightService;
import com.pig4cloud.pigx.common.core.exception.BizException;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;

/**
* 绩效签发 右侧不固定
*
*/
@RestController
@RequiredArgsConstructor
@RequestMapping("/kpi/kpiSignRight")
@Tag(name = "kpiSignRight", description = "绩效签发 右侧不固定")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
@Slf4j
public class KpiSignRightController {

    private final IKpiSignRightService kpiSignRightService;
    private final KpiConfigMapper kpiConfigMapper;

    @SysLog("绩效签发 右侧不固定info")
    @GetMapping("/info/{id}")
    @Operation(summary = "绩效签发 右侧不固定info")
    //@PreAuthorize("@pms.hasPermission('kpiSignRight_info')")
    public R<KpiSignRight> info(@PathVariable Long id) {
        return R.ok(kpiSignRightService.getById(id));
    }

    @SysLog("绩效签发 右侧不固定page")
    @GetMapping("/page")
    @Operation(summary = "绩效签发 右侧不固定page")
    //@PreAuthorize("@pms.hasPermission('kpiSignRight_page')")
    public R<IPage<KpiSignRight>> page(PageRequest<KpiSignRight> pr) {
        return R.ok(kpiSignRightService.page(pr.getPage(),pr.getWrapper()));
    }

    @SysLog("绩效签发 右侧不固定list")
    @GetMapping("/list")
    @Operation(summary = "绩效签发 右侧不固定list")
    //@PreAuthorize("@pms.hasPermission('kpiSignRight_list')")
    public R<List<KpiSignRight>> list(PageRequest<KpiSignRight> pr) {
        return R.ok(kpiSignRightService.list(pr.getWrapper()));
    }

    @SysLog("绩效签发 右侧不固定add")
    @PostMapping("/add")
    @Operation(summary = "绩效签发 右侧不固定add")
    //@PreAuthorize("@pms.hasPermission('kpiSignRight_add')")
    public R add(@RequestBody KpiSignRight kpiSignRight)  {
        return R.ok(kpiSignRightService.save(kpiSignRight));
    }

    @SysLog("绩效签发 右侧不固定edit")
    @PostMapping("/edit")
    @Operation(summary = "绩效签发 右侧不固定edit")
    //@PreAuthorize("@pms.hasPermission('kpiSignRight_edit')")
    public R edit(@RequestBody KpiSignRight kpiSignRight)  {
        KpiConfig config = kpiConfigMapper.selectOne(
                new QueryWrapper<KpiConfig>()
                        .eq("period", kpiSignRight.getPeriod())
        );
        if (config == null){
            throw new BizException("周期不存在");
        }
        if ("Y".equals(config.getSignFlag())){
            throw new BizException("已签发 无法修改");
        }
        return R.ok(kpiSignRightService.updateById(kpiSignRight));
    }

    @SysLog("绩效签发 右侧不固定del")
    @PostMapping("/del/{id}")
    @Operation(summary = "绩效签发 右侧不固定del")
    //@PreAuthorize("@pms.hasPermission('kpiSignRight_del')")
    public R del(@PathVariable Long id)  {
        return R.ok(kpiSignRightService.removeById(id));
    }

    @SysLog("绩效签发 右侧不固定delBatch")
    @PostMapping("/delBatch/{ids}")
    @Operation(summary = "绩效签发 右侧不固定delBatch 1,2,3")
    //@PreAuthorize("@pms.hasPermission('kpiSignRight_delBatch')")
    public R delBatch(@PathVariable String ids)  {
        return R.ok(kpiSignRightService.removeBatchByIds(Arrays.asList(ids.split(","))));
    }
}