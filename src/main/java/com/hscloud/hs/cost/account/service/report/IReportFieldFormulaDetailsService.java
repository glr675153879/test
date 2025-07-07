package com.hscloud.hs.cost.account.service.report;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hscloud.hs.cost.account.model.entity.report.ReportFieldFormula;
import com.hscloud.hs.cost.account.model.entity.report.ReportFieldFormulaDetails;

import java.util.List;

/**
* 数据集字段自定义公式包含的字段 服务接口类
*/
public interface IReportFieldFormulaDetailsService extends IService<ReportFieldFormulaDetails> {

    void saveList(ReportFieldFormula reportFieldFormula);

    List<ReportFieldFormulaDetails> listByFormulaId(Long sourceFormulaId);

    List<ReportFieldFormulaDetails> listByFieldIds(List<Long> collect);

    // List<ReportFieldFormulaDetails> listByPid(Long formulaId);
}
