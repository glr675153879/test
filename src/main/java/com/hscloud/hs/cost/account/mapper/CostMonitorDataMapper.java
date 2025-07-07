package com.hscloud.hs.cost.account.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hscloud.hs.cost.account.model.dto.monitorCenter.*;
import com.hscloud.hs.cost.account.model.entity.CostMonitorAbMonth;
import com.hscloud.hs.cost.account.model.entity.CostMonitorData;
import com.hscloud.hs.cost.account.model.vo.monitorCenter.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

/**
 * 监测动态监测值测试数据
 * @author  lian
 * @date  2023-09-19 18:19
 *
 */
@Mapper
public interface CostMonitorDataMapper extends BaseMapper<CostMonitorData> {


    /**
     * 查询监测值数据统计信息
     *@param dataQueryDto 查询参数
     *@return 返回vo
     */
    CostMonitorDataVo queryStatisticsValue(CostMonitorDataQueryDto dataQueryDto);

    /**
     * 查询监测值趋势
     *@param  queryDto 查询参数
     *@return  list
     */
    List<CostMonitorCenterTrendVo> queryList(CostMonitorTrendQueryDto queryDto);

    /**
     * 查询监测值趋势详情
     *@param  queryDto 查询参数
     *@return  list
     */
    List<CostMonitorValTrendVo> queryMonitorValTrendList(CostMonitorTrendQueryDto queryDto);

    /**
     * 查询月份监测值统计信息
     *@param  queryDto 查询参数
     *@return  list
     */
    List<CostMonitorCenterMonthVo> queryMonitorMonthValue(CostMonitorAbnormalMonthQueryDto queryDto);
    /**
     * 查询异常月份监测值统计信息,同时返回目标值和单位
     *@param  queryDto 查询参数
     *@return  list
     */
    List<CostMonitorAbnormalMonVo> queryMonitorAbnormalMonth(CostMonitorAbnormalMonQueryDto queryDto);

    /**
     * 查询异常月份监测值统计信息,同时返回目标值和单位 定时器生成用到
     *@param  queryDto 查询参数
     *@return  list
     */
    List<CostMonitorAbMonth> queryGenerateMonitorAbnormalMonth(CostMonitorAbMonthQueryDto queryDto);


    /**
     * 计算年平均值
     *@param  unitId 核算单元id
     *@param  itemId 核算项id
     *@param  year 年份
     *@return 年度平均值
     */
    BigDecimal queryYearAvg(@Param("unitId") String unitId, @Param("itemId")String itemId, @Param("year") int year);
    /**
     * 计算统计值
     *@param  countQueryDto 参数
     *@return  返回统计值
     */
    BigDecimal queryCount(CostMonitorCountQueryDto countQueryDto);

    /**
     * 查询本月所有的
     *@param  queryDto 查询参数
     *@return  list
     */
    List<CostMonitorAbnormalItemVo> queryMonitorAbnormalItem(@Param("query") CostMonitorAbnormalItemQueryDto queryDto);
}
