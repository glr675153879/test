package com.hscloud.hs.cost.account.handler;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.hscloud.hs.cost.account.service.monitor.DataChangeMonitor;
import com.pig4cloud.pigx.common.core.exception.BizException;
import com.pig4cloud.pigx.common.data.monitor.handler.LogicHandler;
import com.pig4cloud.pigx.common.data.monitor.pojo.ChangeData;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

/**
 * @author Admin
 */
@Component
public class CostAccountUnitHandler implements LogicHandler {

    private static final String[] DB_TYPE_LIST = new String[]{"mysql", "oracle", "sqlserver"};


    @Override
    public boolean support(String s) {
        return Arrays.stream(DB_TYPE_LIST).anyMatch(dbType -> dbType.equalsIgnoreCase(s));
    }

    @Override
    public Map<String, Object> handle(String handleType, ChangeData changeData, String s) {
        if (StrUtil.isBlank(s)){
            throw new BizException("找不到对应的表");
        }
        Map<String, DataChangeMonitor> dataChangeMonitorMap = SpringUtil
                .getBeansOfType(DataChangeMonitor.class);

        Optional<DataChangeMonitor> optional = dataChangeMonitorMap.values().stream()
                .filter(service -> service.getTableName().equalsIgnoreCase(s)
                ).findFirst();

        if (!optional.isPresent()) {
            throw new BizException("DataChangeMonitor error , not register");
        }
       return optional.get().dealDataChange(handleType,changeData);

    }

    @Override
    public int getOrder() {
        return 0;
    }
}
