package com.hscloud.hs.cost.account.service.monitorCenter;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hscloud.hs.cost.account.model.dto.monitorCenter.*;
import com.hscloud.hs.cost.account.model.entity.CostMonitorCenter;
import com.hscloud.hs.cost.account.model.vo.monitorCenter.CostMonitorAbnormalItemVo;
import com.hscloud.hs.cost.account.model.vo.monitorCenter.CostMonitorAbnormalMonVo;
import com.hscloud.hs.cost.account.model.vo.monitorCenter.CostMonitorInRangeVo;

import java.math.BigDecimal;
import java.util.List;

/**
 * 监测中心
 *
 * @author zjd
 * @email zjd@gmail.com
 * @date 2023-09-18 10:32:18
 */
public interface CostMonitorCenterService extends IService<CostMonitorCenter> {

    /**
     * 查询监测动态
     *@param  queryDto 参数
     *@return  list
     */
    Object queryTrendList(Page page, CostMonitorCenterQueryDto queryDto);

    /**
     * 查询统计数据
     *@param  queryDto 参数
     *@return  list
     */
    Object getStatistics(CostMonitorCenterQueryDto queryDto);

    /**
     * 异常核算信息统计
     *@param queryDto 查询参数
     *@return vo
     */
    Object queryAbnormalCount(CostMonitorAbnormalCountQueryDto queryDto);

    /**
     * 查询监测值趋势
     *@param  queryDto 参数
     *@return  list
     */
    Object queryMonitorValueTrend(CostMonitorValTrendQueryDto queryDto);

    /**
     * 查询年度异常月份数据
     *@param  queryDto 查询参数
     *@return  list
     */
    Object queryAbnormalMonthList(CostMonitorAbnormalMonQueryDto queryDto);

    /**
     * 查询异常核算项
     *@param  queryDto 参数
     *@return  list
     */
    Object queryAbnormalItemList(CostMonitorAbnormalItemQueryDto queryDto);
    /**
     * 查询异常科室单元
     *@param  queryDto 查询参数
     *@return  list
     */
    Object queryAbnormalUnitList(CostMonitorAbnormalUnitQueryDto queryDto, List<CostMonitorAbnormalItemVo> resItemList);

    /**
     * 查询科室单元异常核算项详情
     *@param  queryDto 参数
     *@return  list
     */
    Object queryUnitItemDetailList(CostMonitorAbnormalItemDetailQueryDto queryDto);

    /**
     * 判断当前值是否超标 并且返回超标值
     *@param  monitorValueCount 统计值
     *@param  targetValue 目标值
     *@return  vo
     */
    CostMonitorInRangeVo returnRangeInfo(BigDecimal monitorValueCount, String targetValue);

    /**
     * 返回所有子集config
     *@param  indexId 核算项id
     *@return  list
     */
    Object queryAllList(Long indexId);

    /**
     * 保存任务
     *@param
     *@return
     */
    Object saveToTaskExecuteResult(Long id, Long unitId);
}

