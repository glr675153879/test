package com.hscloud.hs.cost.account.listener.kpi;


import com.hscloud.hs.cost.account.listener.TaskCalculateEvent;
import com.hscloud.hs.cost.account.mapper.kpi.KpiAccountTaskMapper;
import com.hscloud.hs.cost.account.model.dto.kpi.KpiRunTestDTO;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiAccountTask;
import com.hscloud.hs.cost.account.service.kpi.task.TaskService;
import com.pig4cloud.pigx.common.core.exception.BizException;
import com.pig4cloud.pigx.common.data.tenant.TenantContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.concurrent.*;

/**
 * @author Admin
 */
@Component
@Slf4j
public class KpiTaskCalculateEventListener implements ApplicationListener<KpiTaskCalculateEvent> {

    @Autowired
    private TaskService taskService;
    @Autowired
    private KpiAccountTaskMapper kpiAccountTaskMapper;
    /*private final ExecutorService executorService = new ThreadPoolExecutor(8, 8,
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>(1), r -> {
        Thread thread = new Thread(r);
        thread.setName("kpi-task-calculate"+thread.getId());
        return thread;
    }, new ThreadPoolExecutor.CallerRunsPolicy());*/

    @Override
    @Async
    public void onApplicationEvent(@NotNull KpiTaskCalculateEvent event) {
        Object source = event.getSource();
        if (source instanceof KpiRunTestDTO) {
            KpiRunTestDTO input = (KpiRunTestDTO) source;
            KpiAccountTask task = kpiAccountTaskMapper.selectById(input.getId());
            if (task.getReportId() != null || "Y".equals(task.getIssuedFlag())){
                return;
            }
            TenantContextHolder.setTenantId(task.getTenantId());
            /*executorService.execute(()->{
                if ("Y".equals(task.getTestFlag())){
                    taskService.calculateTest(input.getId());
                }else {
                    taskService.calculate(input.getId());
                }
            });*/

            if ("Y".equals(task.getTestFlag())){
                taskService.calculateTest(input.getId());
            }else {
                taskService.calculate(input.getId(), input.isItemRefresh(), input.isEmpRefresh(),input.isEquivalent());
            }
        }
    }
}

