package com.hscloud.hs.cost.account.service.monitorCenter;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hscloud.hs.cost.account.model.dto.monitorCenter.CostMonitorSetDto;
import com.hscloud.hs.cost.account.model.dto.monitorCenter.CostMonitorSetQueryDto;
import com.hscloud.hs.cost.account.model.entity.CostMonitorSet;

/**
 * 监测值设置
 * @author  lian
 * @date  2023-09-20 16:48
 *
 */
public interface CostMonitorSetService extends IService<CostMonitorSet> {

    /**
     * 统计已设置和未设置的数量
     *@param dto 查询参数
     *@return vo
     */
    Object queryCount (CostMonitorSetQueryDto dto);

    /**
     * 查询所有核算单元*核算项
     *@param  page 参数
     *@param  dto 参数
     *@return  list
     */
    IPage queryListAll (Page page,CostMonitorSetQueryDto dto);

    /**
     * 新增编辑监测值
     *@param costMonitorSet 参数
     *@return id
     */
    Object saveOrUpdateCostMonitorSet(CostMonitorSetDto costMonitorSet);
}

