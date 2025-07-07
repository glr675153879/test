package com.hscloud.hs.cost.account.service.report;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hscloud.hs.cost.account.model.entity.report.ReportFieldFormula;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 数据集字段自定义公式 服务接口类
 */
public interface IReportFieldFormulaService extends IService<ReportFieldFormula> {

    void createOrEdit(ReportFieldFormula reportFieldFormula);

    List<ReportFieldFormula> listByFieldId(Long reportField);

    ReportFieldFormula findByFieldId(Long id, String accountUnitCode);

    boolean deleteById(Long id);

    List<ReportFieldFormula> listByFieldIds(Collection<Long> fieldIds);

    boolean existsByFieldId(Long fieldId);
}
