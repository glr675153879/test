package com.hscloud.hs.cost.account.service.impl.report;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hscloud.hs.cost.account.mapper.report.ReportFieldFormulaDetailsMapper;
import com.hscloud.hs.cost.account.model.entity.report.ReportFieldFormula;
import com.hscloud.hs.cost.account.model.entity.report.ReportFieldFormulaDetails;
import com.hscloud.hs.cost.account.service.report.IReportFieldFormulaDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 数据集字段自定义公式包含的字段 服务实现类
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportFieldFormulaDetailsService extends ServiceImpl<ReportFieldFormulaDetailsMapper, ReportFieldFormulaDetails> implements IReportFieldFormulaDetailsService {

    @Override
    public void saveList(ReportFieldFormula reportFieldFormula) {
        Long fieldFormulaId = reportFieldFormula.getId();
        List<ReportFieldFormulaDetails> paramsList = reportFieldFormula.getParamsList();
        paramsList.forEach(e -> e.setReportFormulaId(reportFieldFormula.getId()));
        List<Long> ids = paramsList.stream().map(ReportFieldFormulaDetails::getId).filter(Objects::nonNull).collect(Collectors.toList());
        //找出要删除的details
        this.remove(Wrappers.<ReportFieldFormulaDetails>lambdaQuery()
                .eq(ReportFieldFormulaDetails::getReportFormulaId, fieldFormulaId)
                .notIn(!ids.isEmpty(), ReportFieldFormulaDetails::getId, ids));

        //新增details
        List<ReportFieldFormulaDetails> addList = paramsList.stream().filter(item -> item.getId() == null).collect(Collectors.toList());
        if (!addList.isEmpty()) {
            this.saveBatch(addList);
        }
    }

    // @Cacheable(cacheManager = "localCacheManager", value = "reportFieldFormulaDetails", key = "#formulaId", sync = true)
    // @Override
    // public List<ReportFieldFormulaDetails> listByPid(Long formulaId) {
    //     return this.list(Wrappers.<ReportFieldFormulaDetails>lambdaQuery().eq(ReportFieldFormulaDetails::getReportFormulaId, formulaId));
    // }

    @Override
    public List<ReportFieldFormulaDetails> listByFormulaId(Long sourceFormulaId) {
        return super.list(Wrappers.<ReportFieldFormulaDetails>lambdaQuery().eq(ReportFieldFormulaDetails::getReportFormulaId, sourceFormulaId));
    }

    @Override
    public List<ReportFieldFormulaDetails> listByFieldIds(List<Long> fieldIds) {
        return super.list(Wrappers.<ReportFieldFormulaDetails>lambdaQuery().in(ReportFieldFormulaDetails::getReportFieldId, fieldIds));
    }
}
