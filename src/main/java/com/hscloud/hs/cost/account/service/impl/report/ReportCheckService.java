package com.hscloud.hs.cost.account.service.impl.report;

import cn.hutool.core.lang.Pair;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.hscloud.hs.cost.account.model.entity.base.Entity;
import com.hscloud.hs.cost.account.model.entity.report.*;
import com.hscloud.hs.cost.account.service.report.*;
import com.hscloud.hs.cost.account.utils.CyclesUtil;
import com.pig4cloud.pigx.common.core.exception.BizException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 报表设计表 校验实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportCheckService {

    private final IReportService reportService;
    private final IReportDbService reportDbService;
    private final IReportDbParamService dbParamService;
    private final IReportFieldService fieldService;
    private final IReportFieldFormulaService formulaService;
    //    private final ReportFieldFormulaDetailsService formulaDetailsService;
    private final IReportFieldSonReportService sonReportService;
    private final IReportHeadService headService;

    public void check(Long reportId) {
        Report report = reportService.getById(reportId);
        if (Objects.isNull(report)) {
            throw new BizException("报表不存在");
        }
        ReportDb reportDb = reportDbService.getByReportId(reportId);
        if (Objects.isNull(reportDb)) {
            throw new BizException("找不到数据集");
        }
        Long reportDbId = reportDb.getId();

        //校验表头的叶子结点是否都是数据集字段和自定义公式字段
        checkHead(reportId, reportDbId);
        //检查所有公式是否存在循环引用
        checkFormulaCircularReferences(reportId, reportDbId);
    }

    /**
     * 检查公式循环引用
     *
     * @param reportId   报告 ID
     * @param reportDbId 报告数据库 ID
     */
    private void checkFormulaCircularReferences(Long reportId, Long reportDbId) {
        List<ReportField> fields = fieldService.listByDbId(reportDbId);
        Map<Long, ReportField> fieldMap = fields.stream().collect(Collectors.toMap(ReportField::getId, e -> e));
        List<ReportFieldFormula> reportFieldFormulas = formulaService.listByFieldIds(fieldMap.keySet());
        for (ReportFieldFormula reportFieldFormula : reportFieldFormulas) {
            reportFieldFormula.setFieldViewAlias(fieldMap.get(reportFieldFormula.getReportFieldId()).getFieldViewAlias());
        }
        CyclesUtil.Graph<String> graph = new CyclesUtil.Graph<>();
        for (ReportFieldFormula fieldFormula : reportFieldFormulas) {
            String key = "" + fieldFormula.getReportFieldId();
            String value = fieldFormula.getExpression();
            String edgeDesc = String.format("%s[%s]", fieldFormula.getFieldViewAlias(), StrUtil.isBlank(fieldFormula.getMatchFieldTexts()) ? "全部核算单元" : fieldFormula.getMatchFieldTexts());
            Set<String> longs = CyclesUtil.extractLongNumbers(value);
            for (String aLong : longs) {
                graph.addEdge(key, aLong, edgeDesc);
            }
        }
        List<List<Pair<CyclesUtil.Node<String>, String>>> allCycles = CyclesUtil.findAllCycles(graph);
        StringBuilder sb = new StringBuilder();
        allCycles.forEach(cycle -> {
            List<String> collect = cycle.stream().map(Pair::getValue).collect(Collectors.toList());
            String join = StrUtil.join("->", collect);
            sb.append(join).append("\n");
            log.warn("reportId:{} reportDbId:{} cycle:{}", reportId, reportDbId, join);
        });
        if (sb.length() > 0) {
            throw new BizException("公式存在循环引用\n" + sb);
        }
    }

    /**
     * 校验表头的叶子结点是否都是数据集字段和自定义公式字段
     * 叶子结点定义：节点id没有出现在其他节点的parentId中
     *
     * @param reportId   报告 ID
     * @param reportDbId 报告数据库 ID
     */
    private void checkHead(Long reportId, Long reportDbId) {
        List<ReportField> fields = fieldService.listByDbId(reportDbId);
        Map<Long, ReportField> fieldMap = fields.stream().collect(Collectors.toMap(ReportField::getId, e -> e));
        List<ReportHead> heads = headService.listByReportId(reportId);
        List<Long> headParentIds = heads.stream().map(ReportHead::getParentId).filter(Objects::nonNull).collect(Collectors.toList());
        List<Long> leafHeadIds = heads.stream().map(Entity::getId).filter(id -> !headParentIds.contains(id)).collect(Collectors.toList());
        for (ReportHead head : heads) {
            Long fieldId = head.getFieldId();
            Long headId = head.getId();
            if (!fieldMap.containsKey(fieldId)) {
                log.warn("表头节点字段不存在 fieldId:{} headId:{}", fieldId, headId);
                throw new BizException("表头节点字段不存在");
            }
            if (leafHeadIds.contains(headId)) {
                ReportField reportField = fieldMap.get(fieldId);
                if (!StrUtil.equalsAny(reportField.getFieldType(), "1", "2")) {
                    log.warn("叶子节点必须为[数据集字段]或[自定义计算字段],{}", JSON.toJSONString(reportField));
                    throw new BizException(String.format("叶子节点[%s]必须为[数据集字段]或[自定义计算字段]", reportField.getFieldViewAlias()));
                }
            }
        }
    }
}
