package com.hscloud.hs.cost.account.service.dataReport;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hscloud.hs.cost.account.model.entity.dataReport.CostReportTask;

/**
* 上报任务 服务接口类
*/
public interface ICostReportTaskService extends IService<CostReportTask> {
    Object insert(CostReportTask costReportTask);

    Object updateTask(CostReportTask costReportTask);

    Boolean initiate();

    Boolean activate(CostReportTask costReportTask);

    /**
     * 根据当前周期更新下个周期
     *
     * @param costReportTask 成本报告任务
     */
    void updateNextCalculateCircle(CostReportTask costReportTask);
}
