package com.hscloud.hs.cost.account.listener;

import com.hscloud.hs.cost.account.constant.enums.SecondDistributionTaskStatusEnum;
import com.hscloud.hs.cost.account.model.dto.SecondDistributionGenerateTaskDto;
import com.hscloud.hs.cost.account.model.entity.CostAccountTask;
import com.hscloud.hs.cost.account.model.entity.CostAccountUnit;
import com.hscloud.hs.cost.account.model.entity.SecondDistributionTask;
import com.hscloud.hs.cost.account.model.entity.SecondDistributionTaskUnitInfo;
import com.hscloud.hs.cost.account.service.ISecondDistributionTaskUnitInfoService;
import com.hscloud.hs.cost.account.utils.SqlUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
@Slf4j
@RequiredArgsConstructor
public class SecondDistributionTaskCalculateEventListener implements ApplicationListener<SecondDistributionTaskCalculateEvent> {

    private final SqlUtil sqlUtil;

    private final ISecondDistributionTaskUnitInfoService unitInfoService;

    @Override
    @Async
    @Transactional(rollbackFor = Exception.class)
    public void onApplicationEvent(SecondDistributionTaskCalculateEvent event) {
        Object source = event.getSource();
        if (source instanceof SecondDistributionGenerateTaskDto) {
            SecondDistributionGenerateTaskDto generateTaskDto =   (SecondDistributionGenerateTaskDto)source;
            //插入任务表
            SecondDistributionTask secondDistributionTask = new SecondDistributionTask();
            secondDistributionTask.setName(generateTaskDto.getTaskPeriod() + "二次分配");
            secondDistributionTask.setType("二次分配");
            String detailDim = generateTaskDto.getTaskPeriod().replaceAll("[年月]", "");
            secondDistributionTask.setTaskPeriod(detailDim);
            // TODO 暂定取数据小组数据
            Map<Long, String> deptAndAmount = sqlUtil.getSecondDistributionTask(detailDim);
            Set<Long> unitIds = deptAndAmount.keySet();
            secondDistributionTask.setUnitIds(unitIds.toString());
            secondDistributionTask.setStatus(SecondDistributionTaskStatusEnum.UNDERWAY.getCode());
            secondDistributionTask.insert();
            List<SecondDistributionTaskUnitInfo> unitInfoList = new ArrayList<>();

            for (Map.Entry<Long, String> entry : deptAndAmount.entrySet()) {
                //根据科室单元插入多条
                SecondDistributionTaskUnitInfo taskUnitInfo = new SecondDistributionTaskUnitInfo();
                taskUnitInfo.setTaskId(secondDistributionTask.getId());
                taskUnitInfo.setUnitId(entry.getKey());
                taskUnitInfo.setTotalAmount(new BigDecimal(entry.getValue()));
                taskUnitInfo.setUnitName(new CostAccountUnit().selectById(entry.getKey()).getName());
                taskUnitInfo.setStatus(SecondDistributionTaskStatusEnum.UNCOMMITTED.getCode());
                unitInfoList.add(taskUnitInfo);
            }
            unitInfoService.saveBatch(unitInfoList);
        }
    }
}

