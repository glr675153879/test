package com.hscloud.hs.cost.account.service.impl.report;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.hscloud.hs.cost.account.model.dto.report.ReportCopyDto;
import com.hscloud.hs.cost.account.model.entity.report.*;
import com.hscloud.hs.cost.account.service.report.*;
import com.pig4cloud.pigx.common.core.exception.BizException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 报表设计表 复制实现类
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportCopyService {

    private final IReportService reportService;
    private final IReportDbService reportDbService;
    private final IReportDbParamService dbParamService;
    private final IReportFieldService fieldService;
    private final IReportFieldFormulaService formulaService;
    private final IReportFieldFormulaDetailsService formulaDetailsService;
    private final IReportFieldSonReportService sonReportService;
    private final IReportHeadService headService;

    @Transactional(rollbackFor = Exception.class)
    public void copy(ReportCopyDto dto) {
        //保存或更新时这个报表是否和其他报表重名，忽略本身
        boolean existFlag = reportService.exists(Wrappers.<Report>lambdaQuery()
                .eq(Report::getName, dto.getReportName()));
        if (existFlag) {
            throw new BizException("报表名称已存在");
        }
        //复制报表
        Long sourceReportId = dto.getSourceReportId();
        Report sourceReport = reportService.getById(sourceReportId);
        if (Objects.isNull(sourceReport)) {
            throw new BizException("报表不存在");
        }
        Report targetReport = BeanUtil.copyProperties(sourceReport, Report.class);
        targetReport.clearCommonField();
        targetReport.setCode(UUID.fastUUID().toString(true));
        targetReport.setName(dto.getReportName());
        targetReport.setGroupId(dto.getGroupId());
        reportService.save(targetReport);

        //复制数据集
        ReportDb sourceReportDb = reportDbService.getByReportId(sourceReport.getId());
        if (Objects.isNull(sourceReportDb)) {
            throw new BizException("找不到数据集");
        }
        ReportDb targetReportDb = BeanUtil.copyProperties(sourceReportDb, ReportDb.class);
        targetReportDb.clearCommonField();
        targetReportDb.setReportId(targetReport.getId());
        reportDbService.save(targetReportDb);

        //复制数据集入参
        copyReportDbParam(sourceReport.getId(), sourceReportDb.getId(), targetReport.getId(), targetReportDb.getId());
        //复制字段
        Map<Long, Long> fieldIdMap = copyReportField(sourceReport.getId(), sourceReportDb.getId(), targetReport.getId(), targetReportDb.getId());
        //复制表头
        copyReportHead(sourceReport.getId(), targetReport.getId(), fieldIdMap);
    }

    private void copyReportHead(Long sourceReportId, Long targetReportId, Map<Long, Long> fieldIdMap) {
        Map<Long, Long> headIdMap = new HashMap<>();
        //复制表头
        List<ReportHead> sourceHeads = headService.listByReportId(sourceReportId);
        if (CollUtil.isNotEmpty(sourceHeads)) {
            List<ReportHead> targetHeads = new ArrayList<>();
            //1、先更新id，并且建立新旧id映射关系
            for (ReportHead sourceHead : sourceHeads) {
                ReportHead targetHead = BeanUtil.copyProperties(sourceHead, ReportHead.class);
                targetHead.clearCommonField();
                targetHead.setReportId(targetReportId);
                targetHead.setFieldId(fieldIdMap.get(sourceHead.getFieldId()));
                headService.save(targetHead);
                targetHeads.add(targetHead);
                headIdMap.put(sourceHead.getId(), targetHead.getId());
            }
            //2、根据id映射关系对应更新parentId
            for (ReportHead targetHead : targetHeads) {
                if (Objects.nonNull(targetHead.getParentId()) && headIdMap.containsKey(targetHead.getParentId())) {
                    targetHead.setParentId(headIdMap.get(targetHead.getParentId()));
                    headService.updateById(targetHead);
                }
            }
        }
    }

    private Map<Long, Long> copyReportField(Long sourceReportId, Long sourceReportDbId, Long targetReportId, Long targetReportDbId) {
        Map<Long, Long> fieldIdMap = new HashMap<>();
        //复制字段
        List<ReportField> sourceFields = fieldService.listByDbId(sourceReportDbId);
        if (CollUtil.isNotEmpty(sourceFields)) {
            for (ReportField sourceField : sourceFields) {
                ReportField targetField = BeanUtil.copyProperties(sourceField, ReportField.class);
                targetField.clearCommonField();
                targetField.setReportId(targetReportId);
                targetField.setReportDbId(targetReportDbId);
                fieldService.save(targetField);
                fieldIdMap.put(sourceField.getId(), targetField.getId());
            }
            for (ReportField sourceField : sourceFields) {
                //复制字段公式
                copyReportFieldFormula(sourceField.getId(), fieldIdMap.get(sourceField.getId()), fieldIdMap);
                //复制字段子报表
                copyReportFieldSonReport(sourceField.getId(), fieldIdMap.get(sourceField.getId()), fieldIdMap);
            }
        }
        return fieldIdMap;
    }

    private void copyReportFieldSonReport(Long sourceFieldId, Long targetFieldId, Map<Long, Long> fieldIdMap) {
        //复制字段子报表
        List<ReportFieldSonReport> reportFieldSonReports = sonReportService.listByFieldId(sourceFieldId);
        if (CollUtil.isNotEmpty(reportFieldSonReports)) {
            for (ReportFieldSonReport reportFieldSonReport : reportFieldSonReports) {
                ReportFieldSonReport target = BeanUtil.copyProperties(reportFieldSonReport, ReportFieldSonReport.class);
                target.clearCommonField();
                target.setReportFieldId(targetFieldId);
                sonReportService.save(target);
            }
        }
    }

    private void copyReportFieldFormula(Long sourceFieldId, Long targetFieldId, Map<Long, Long> fieldIdMap) {
        //复制字段公式
        List<ReportFieldFormula> reportFieldFormulas = formulaService.listByFieldId(sourceFieldId);
        if (CollUtil.isNotEmpty(reportFieldFormulas)) {
            for (ReportFieldFormula reportFieldFormula : reportFieldFormulas) {
                ReportFieldFormula target = BeanUtil.copyProperties(reportFieldFormula, ReportFieldFormula.class);
                target.clearCommonField();
                target.setReportFieldId(targetFieldId);
                //替换公式中的字段id
                if (CollUtil.isNotEmpty(fieldIdMap) && StrUtil.isNotBlank(target.getExpression())) {
                    String expression = target.getExpression();
                    for (Map.Entry<Long, Long> longLongEntry : fieldIdMap.entrySet()) {
                        expression = expression.replace(longLongEntry.getKey().toString(), longLongEntry.getValue().toString());
                    }
                    target.setExpression(expression);
                }
                //替换公式中的字段id
                if (CollUtil.isNotEmpty(fieldIdMap) && StrUtil.isNotBlank(target.getExpressionJs())) {
                    String expression = target.getExpressionJs();
                    for (Map.Entry<Long, Long> longLongEntry : fieldIdMap.entrySet()) {
                        expression = expression.replace(longLongEntry.getKey().toString(), longLongEntry.getValue().toString());
                    }
                    target.setExpressionJs(expression);
                }
                formulaService.save(target);
                //复制字段公式明细
                copyReportFieldFormulaDetail(reportFieldFormula.getId(), target.getId(), fieldIdMap);
            }
        }

    }

    private void copyReportFieldFormulaDetail(Long sourceFormulaId, Long targetFormulaId, Map<Long, Long> fieldIdMap) {
        //复制字段公式明细
        List<ReportFieldFormulaDetails> reportFieldFormulaDetailsList = formulaDetailsService.listByFormulaId(sourceFormulaId);
        if (CollUtil.isNotEmpty(reportFieldFormulaDetailsList)) {
            for (ReportFieldFormulaDetails reportFieldFormulaDetails : reportFieldFormulaDetailsList) {
                ReportFieldFormulaDetails target = BeanUtil.copyProperties(reportFieldFormulaDetails, ReportFieldFormulaDetails.class);
                target.clearCommonField();
                target.setReportFormulaId(targetFormulaId);
                //替换包含字段的字段id
                if (CollUtil.isNotEmpty(fieldIdMap) && Objects.nonNull(target.getReportFieldId())) {
                    Long reportFieldId = target.getReportFieldId();
                    target.setReportFieldId(fieldIdMap.get(reportFieldId));
                }
                formulaDetailsService.save(target);
            }
        }
    }


    private void copyReportDbParam(Long sourceReportId, Long sourceReportDbId, Long targetReportId, Long targetReportDbId) {
        //复制数据集参数
        List<ReportDbParam> reportDbParams = dbParamService.listByDbId(sourceReportDbId);
        if (CollUtil.isNotEmpty(reportDbParams)) {
            for (ReportDbParam reportDbParam : reportDbParams) {
                ReportDbParam target = BeanUtil.copyProperties(reportDbParam, ReportDbParam.class);
                target.clearCommonField();
                target.setReportId(targetReportId);
                target.setReportDbId(targetReportDbId);
                dbParamService.save(target);
            }
        }
    }

}
