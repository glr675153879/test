package com.hscloud.hs.cost.account.controller.monitorCenter;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import com.hscloud.hs.cost.account.model.dto.monitorCenter.CostMonitorSetDto;
import com.hscloud.hs.cost.account.model.dto.monitorCenter.CostMonitorSetQueryDto;
import com.hscloud.hs.cost.account.service.monitorCenter.CostMonitorSetService;
import com.pig4cloud.pigx.common.core.util.R;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


/**
 * 监测值设置
 *
 * @author lian
 * @date 2023-09-19 10:58
 */
@RestController
@RequestMapping("account/monitorSet")
@Tag(name = "监测值设置", description = "monitorSet")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class CostMonitorSetController {
    @Autowired
    private CostMonitorSetService costMonitorSetService;

    /**
     * 查询未设置数量
     */
    @GetMapping("/queryCount")
    public R queryUnSettleCount(@Validated CostMonitorSetQueryDto setQueryDto) {
        return R.ok(costMonitorSetService.queryCount(setQueryDto));
    }

    /**
     * 查询监测值
     */
    @GetMapping("/list")
    public R list(@Validated Page page, CostMonitorSetQueryDto setQueryDto) {
        return R.ok(costMonitorSetService.queryListAll(page, setQueryDto));
    }

    /**
     * 保存监测值
     */
    @PreAuthorize("@pms.hasPermission('kpi_monitor_set')")
    @PostMapping("/save")
    public R add(@Validated @RequestBody CostMonitorSetDto costMonitorSet) {
        return R.ok(costMonitorSetService.saveOrUpdateCostMonitorSet(costMonitorSet));
    }

}
