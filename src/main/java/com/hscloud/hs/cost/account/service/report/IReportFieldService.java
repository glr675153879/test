package com.hscloud.hs.cost.account.service.report;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hscloud.hs.cost.account.model.dto.report.FieldUseFormulaDto;
import com.hscloud.hs.cost.account.model.entity.report.ReportField;
import com.hscloud.hs.cost.account.model.entity.report.ReportFieldFormula;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * 报表字段 服务接口类
 */
public interface IReportFieldService extends IService<ReportField> {

    ReportField info(Long id);

    Long createOrEdit(ReportField reportField);

    List<ReportField> listByDbId(Long reportDbId);

    List<ReportField> listByReportId(Long reportId);

    void fillFieldFlag(ReportField reportField);

    Map<String, Object> caclFieldFormula(Map<String, Object> e, List<ReportField> reportFields);

    void fillFieldData(List<ReportField> reportField);
    // void fillFieldData(ReportField reportField);

    /**
     * 检查是否被表头引用，如果被引用则不允许删除
     * 按 ID 检查和删除
     *
     * @param id 同上
     * @return boolean
     */
    @Transactional(rollbackFor = Exception.class)
    boolean checkAndDeleteById(Long id);

    void removeByDbId(Long id);

    /**
     * 报表当前行字段使用公式
     *
     * @param dto DTO
     * @return {@link ReportFieldFormula}
     */
    ReportFieldFormula fieldUseFormula(FieldUseFormulaDto dto);

}
