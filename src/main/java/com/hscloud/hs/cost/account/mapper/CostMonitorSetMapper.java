package com.hscloud.hs.cost.account.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hscloud.hs.cost.account.model.dto.monitorCenter.CostMonitorCenterQueryDto;
import com.hscloud.hs.cost.account.model.dto.monitorCenter.CostMonitorSetQueryDto;
import com.hscloud.hs.cost.account.model.dto.monitorCenter.CostMonitorValTrendQueryDto;
import com.hscloud.hs.cost.account.model.entity.CostMonitorSet;
import com.hscloud.hs.cost.account.model.vo.monitorCenter.CostMonitorCenterVo;
import com.hscloud.hs.cost.account.model.vo.monitorCenter.CostMonitorSetVo;
import com.hscloud.hs.cost.account.model.vo.monitorCenter.CostMonitorValTrendVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 监测中心
 * @author  lian
 * @date  2023-09-19 18:19
 *
 */
@Mapper
public interface CostMonitorSetMapper extends BaseMapper<CostMonitorSet> {

    /**
     * 查询核算单元和核算指标合集
     *@param dto 参数
     *@param page 参数
     *@return list
     */
    IPage<CostMonitorSetVo> queryListAll(Page page, @Param("query")  CostMonitorSetQueryDto dto);

    /**
     * 查询数量
     *@param dto 查询参数
     *@return int
     */
    Integer queryCount(CostMonitorSetQueryDto dto);

    /**
     * 监测动态
     *@param dto 查询参数
     *@param page 查询参数
     *@return list
     */
    IPage<CostMonitorCenterVo> queryTrendList(Page page,@Param("query") CostMonitorCenterQueryDto dto);
    /**
     * 监测值趋势
     *@param  dto 参数
     *@return  list
     */
    CostMonitorValTrendVo queryMonitorValue(@Param("query") CostMonitorValTrendQueryDto dto);

}
