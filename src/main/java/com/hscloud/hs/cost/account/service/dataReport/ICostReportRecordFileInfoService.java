package com.hscloud.hs.cost.account.service.dataReport;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hscloud.hs.cost.account.model.entity.dataReport.CostReportRecordFileInfo;

import java.util.List;

/**
* 我的上报附件表 服务接口类
*/
public interface ICostReportRecordFileInfoService extends IService<CostReportRecordFileInfo> {

    List<CostReportRecordFileInfo> listData(Long recordId);
}
