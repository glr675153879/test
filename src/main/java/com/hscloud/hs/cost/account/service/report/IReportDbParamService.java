package com.hscloud.hs.cost.account.service.report;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hscloud.hs.cost.account.model.entity.report.ReportDbParam;

import java.util.List;

/**
 * 报表入参表 服务接口类
 */
public interface IReportDbParamService extends IService<ReportDbParam> {


    List<ReportDbParam> listByDbId(Long reportDbId);

    void removeByDbId(Long reportDbId);
}
