package com.hscloud.hs.cost.account.service.dataReport;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hscloud.hs.cost.account.model.entity.dataReport.CostReportItem;
import com.hscloud.hs.cost.account.model.entity.dataReport.CostReportTask;
import com.hscloud.hs.cost.account.model.entity.dataReport.CostReportTaskLog;
import com.pig4cloud.pigx.common.security.service.PigxUser;

/**
* 上报任务变更日志 服务接口类
*/
public interface ICostReportTaskLogService extends IService<CostReportTaskLog> {

    void generateLog(String opsType, String originName, CostReportTask costReportTask);
}
