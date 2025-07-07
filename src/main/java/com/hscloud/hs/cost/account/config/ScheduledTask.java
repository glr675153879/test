package com.hscloud.hs.cost.account.config;

import com.hscloud.hs.cost.account.model.dto.monitorCenter.CostMonitorAbMonthQueryDto;
import com.hscloud.hs.cost.account.service.monitorCenter.CostMonitorAbMonthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 定时任务配置类
 *
 * @author lian
 * @date 2023-09-21 19:10
 */
@Slf4j
@Component
public class ScheduledTask {

    @Autowired
    private CostMonitorAbMonthService monitorAbMonthService;

    /**
     * 月末每天上午7点,查询当月数据是否异常,异常则数据入库
     */
    @Scheduled(cron = "0 0 7 L * ?")
    public void autoDelete() {
        CostMonitorAbMonthQueryDto queryDto = new CostMonitorAbMonthQueryDto();
        monitorAbMonthService.generateCurrentMonth(queryDto);
    }


}
