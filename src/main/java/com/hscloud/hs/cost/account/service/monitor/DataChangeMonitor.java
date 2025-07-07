package com.hscloud.hs.cost.account.service.monitor;


import com.pig4cloud.pigx.common.data.monitor.pojo.ChangeData;

import java.util.Map;

/**
 * @author Admin
 */
public interface DataChangeMonitor {

    /**
     * 获取表名
     * @return 表名
     */
    String getTableName();

    /**
     * 处理数据变更
     * @param handleType 处理类型
     * @param record 变更记录
     * @return 处理结果
     */
    Map<String,Object> dealDataChange(String handleType, ChangeData record);
}
