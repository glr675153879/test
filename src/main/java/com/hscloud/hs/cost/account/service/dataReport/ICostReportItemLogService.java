package com.hscloud.hs.cost.account.service.dataReport;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hscloud.hs.cost.account.model.entity.dataReport.CostReportItem;
import com.hscloud.hs.cost.account.model.entity.dataReport.CostReportItemLog;

/**
* 上报项变更日志 服务接口类
*/
public interface ICostReportItemLogService extends IService<CostReportItemLog> {

    void generateLog(String opsType, String originName, CostReportItem costReportItem);

}
