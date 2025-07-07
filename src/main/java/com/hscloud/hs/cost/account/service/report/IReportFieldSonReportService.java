package com.hscloud.hs.cost.account.service.report;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hscloud.hs.cost.account.model.entity.report.ReportFieldSonReport;

import java.util.List;

/**
 * 链接报表列表 服务接口类
 */
public interface IReportFieldSonReportService extends IService<ReportFieldSonReport> {

    List<ReportFieldSonReport> listByFieldId(Long reportField);

    ReportFieldSonReport findByFieldId(Long fieldId, String accountUnitCode);

    void createOrEdit(ReportFieldSonReport sonReport);

    List<ReportFieldSonReport> listByFieldIds(List<Long> fieldIds);

    boolean existsByFieldId(Long fieldId);
}
