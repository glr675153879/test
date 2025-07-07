package com.hscloud.hs.cost.account.service.monitorCenter;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hscloud.hs.cost.account.model.entity.CostMonitorData;

/**
 * 监测值设置
 * @author  lian
 * @date  2023-09-20 16:47
 *
 */
public interface CostMonitorDataService extends IService<CostMonitorData> {

    /**
     * 批量插入一个月的测试数据
     *@param  costMonitorData 参数
     *@return  void
     */
    Object batchTestValue(CostMonitorData costMonitorData);
}

