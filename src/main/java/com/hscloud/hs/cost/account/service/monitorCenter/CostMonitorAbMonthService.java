package com.hscloud.hs.cost.account.service.monitorCenter;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hscloud.hs.cost.account.model.dto.monitorCenter.CostMonitorAbMonthQueryDto;
import com.hscloud.hs.cost.account.model.entity.CostMonitorAbMonth;

/**
 * 异常月份入库记录
 *
 * @author lian
 * @date 2023-09-20 16:48
 */
public interface CostMonitorAbMonthService extends IService<CostMonitorAbMonth> {

    /**
     * 生成异常月份数据
     *
     * @param queryDto 查询参数
     */
    void generateCurrentMonth(CostMonitorAbMonthQueryDto queryDto);
}

