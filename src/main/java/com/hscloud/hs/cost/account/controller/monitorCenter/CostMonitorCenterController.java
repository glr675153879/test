package com.hscloud.hs.cost.account.controller.monitorCenter;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import com.hscloud.hs.cost.account.model.dto.monitorCenter.*;
import com.hscloud.hs.cost.account.service.monitorCenter.CostMonitorCenterService;
import com.pig4cloud.pigx.common.core.util.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * 监测动态
 *
 * @author lian
 * @date 2023-09-18 10:51
 */
@RestController
@RequestMapping("account/monitorCenter")
@Tag(name = "监测动态", description = "监测动态")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class CostMonitorCenterController {
    @Autowired
    private CostMonitorCenterService costMonitorCenterService;

    /**
     * 分页查询监测动态数据
     *
     * @param queryDto 参数
     * @param page 参数
     */
    @GetMapping("/queryTrendList")
    @Operation(summary = "分页查询监测动态数据")
    public R getCostList(Page page, CostMonitorCenterQueryDto queryDto) {
        return R.ok(costMonitorCenterService.queryTrendList(page,queryDto));
    }

    /**
     * 查询异常信息统计
     *
     * @param queryDto 参数
     * @param queryDto 参数
     */
    @GetMapping("/queryAbnormalCount")
    @Operation(summary = "查询异常信息统计")
    public R queryAbnormalCount(CostMonitorAbnormalCountQueryDto queryDto) {
        return R.ok(costMonitorCenterService.queryAbnormalCount(queryDto));
    }


    /**
     * 监测值趋势查询
     *
     * @param queryDto 参数
     */
    @GetMapping("/queryMonitorValueTrend")
    @Operation(summary = "监测值趋势查询")
    public R queryMonitorValueTrend( CostMonitorValTrendQueryDto queryDto) {
        return R.ok(costMonitorCenterService.queryMonitorValueTrend(queryDto));
    }

    /**
     * 查询年度异常月份详情
     *
     * @param queryDto 参数
     */
    @GetMapping("/queryAbnormalMonthList")
    @Operation(summary = "查询年度异常月份详情")
    public R queryAbnormalMonthList(@Validated CostMonitorAbnormalMonQueryDto queryDto) {
        return R.ok(costMonitorCenterService.queryAbnormalMonthList(queryDto));
    }

    /**
     * 查询异常核算项详情
     *
     * @param queryDto 参数
     */
    @GetMapping("/queryAbnormalItemList")
    @Operation(summary = "查询异常核算项详情")
    public R queryAbnormalItemList(CostMonitorAbnormalItemQueryDto queryDto) {
        return R.ok(costMonitorCenterService.queryAbnormalItemList(queryDto));
    }


    /**
     * 查询异常科室单元
     *
     * @param queryDto 参数
     */
    @GetMapping("/queryAbnormalUnitList")
    @Operation(summary = "查询异常科室单元")
    public R queryAbnormalUnitList(CostMonitorAbnormalUnitQueryDto queryDto) {
        return R.ok(costMonitorCenterService.queryAbnormalUnitList(queryDto,null));
    }

    /**
     * 查询异常科室单元核算项详情
     *
     * @param queryDto 参数
     */
    @GetMapping("/queryUnitItemDetailList")
    @Operation(summary = "查询异常科室单元核算项详情")
    public R queryUnitItemDetailList(@Validated CostMonitorAbnormalItemDetailQueryDto queryDto) {
        return R.ok(costMonitorCenterService.queryUnitItemDetailList(queryDto));
    }

    /**
     * 测试任务保存
     * @author  lian
     * @date  2023-09-26 10:19
     *
     */
    @GetMapping("/saveToTaskExecuteResult")
    @Operation(summary = "测试任务保存")
    Object saveToTaskExecuteResult(CostMonitorCenterQueryDto queryDto){
      return R.ok(costMonitorCenterService.saveToTaskExecuteResult(queryDto.getId(),queryDto.getTestUnitId()));
    }

    /**
     * 查询返回所有核算项
     *
     * @param queryDto 参数
     */
    @GetMapping("/queryAllList")
    @Operation(summary = "查询返回所有核算项")
    public R queryAllList( CostMonitorCenterQueryDto queryDto) {
        return R.ok(costMonitorCenterService.queryAllList(queryDto.getId()));
    }
}
