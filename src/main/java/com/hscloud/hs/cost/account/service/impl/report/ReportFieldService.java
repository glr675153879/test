package com.hscloud.hs.cost.account.service.impl.report;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hscloud.hs.cost.account.constant.Constant;
import com.hscloud.hs.cost.account.mapper.report.ReportFieldMapper;
import com.hscloud.hs.cost.account.mapper.report.ReportHeadMapper;
import com.hscloud.hs.cost.account.model.dto.report.FieldUseFormulaDto;
import com.hscloud.hs.cost.account.model.entity.base.Entity;
import com.hscloud.hs.cost.account.model.entity.report.*;
import com.hscloud.hs.cost.account.model.pojo.report.ParamMapping;
import com.hscloud.hs.cost.account.service.report.*;
import com.hscloud.hs.cost.account.utils.CommonUtils;
import com.pig4cloud.pigx.common.core.exception.BizException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 报表字段 服务实现类
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ReportFieldService extends ServiceImpl<ReportFieldMapper, ReportField> implements IReportFieldService {

    private final IReportFieldFormulaService formulaService;
    private final IReportFieldFormulaDetailsService formulaDetailsService;
    private final IReportFieldSonReportService sonReportService;
    private final ReportHeadMapper reportHeadMapper;
    @Lazy
    @Resource
    private IReportService reportService;

    @Override
    public ReportField info(Long id) {
        ReportField reportField = getById(id);
        if (Objects.isNull(reportField)) {
            throw new BizException("报表字段不存在");
        }
        List<ReportFieldFormula> reportFieldFormulas = formulaService.listByFieldId(reportField.getId());
        reportField.setReportFieldFormulas(reportFieldFormulas);
        if (CollUtil.isNotEmpty(reportFieldFormulas)) {
            reportField.setFormulaFlag("1");
            List<ReportField> reportFields = listByReportId(reportField.getReportId());
            Map<Long, String> fieldTextMap = reportFields.stream().collect(Collectors.toMap(ReportField::getId, ReportField::getFieldViewAlias));
            for (ReportFieldFormula reportFieldFormula : reportFieldFormulas) {
                List<ReportFieldFormulaDetails> reportFieldFormulaDetails = formulaDetailsService.listByFormulaId(reportFieldFormula.getId());
                reportFieldFormulaDetails.forEach(e -> {
                    e.setReportFieldViewAlias(fieldTextMap.get(e.getReportFieldId()));
                });
                reportFieldFormula.setParamsList(reportFieldFormulaDetails);
            }
        }
        List<ReportFieldSonReport> sonReports = sonReportService.listByFieldId(reportField.getId());
        if (CollUtil.isNotEmpty(sonReports)) {
            for (ReportFieldSonReport sonReport : sonReports) {
                Report byId = reportService.getById(sonReport.getSonReportId());
                if (Objects.nonNull(byId)) {
                    sonReport.setSonReportCode(byId.getCode());
                    sonReport.setSonReportName(byId.getName());
                }
                if (StrUtil.isNotBlank(sonReport.getParamMappingJson())) {
                    sonReport.setParamMappingList(JSON.parseArray(sonReport.getParamMappingJson(), ParamMapping.class));
                }
            }
        }
        reportField.setSonReports(sonReports);
        if (CollUtil.isNotEmpty(sonReports)) {
            reportField.setSonReportFlag("1");
        }
        return reportField;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createOrEdit(ReportField reportField) {
        if (Objects.equals("2", reportField.getFieldType()) && StrUtil.isEmpty(reportField.getFieldName())) {
            reportField.setFieldName(UUID.fastUUID().toString(true).substring(0, 10));
        }
        super.saveOrUpdate(reportField);
        return reportField.getId();
    }

    @Override
    public List<ReportField> listByDbId(Long reportDbId) {
        return super.list(Wrappers.<ReportField>lambdaQuery().eq(ReportField::getReportDbId, reportDbId).orderByAsc(ReportField::getSort));
    }

    @Override
    public List<ReportField> listByReportId(Long reportId) {
        return super.list(Wrappers.<ReportField>lambdaQuery().eq(ReportField::getReportId, reportId).orderByAsc(ReportField::getSort));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean checkAndDeleteById(Long id) {
        // 校验是否被表头引用
        boolean exists = reportHeadMapper.exists(Wrappers.<ReportHead>lambdaQuery().eq(ReportHead::getFieldId, id));
        if (exists) {
            throw new BizException("字段已被表头引用，不允许删除");
        }
        // 删除字段
        deleteById(id);
        return true;
    }

    /**
     * 删除数据集关联的
     * 字段以及关联的公式、公式详情
     *
     * @param dbId 数据集ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeByDbId(Long dbId) {
        // 删除字段以及关联的公式、公式详情
        List<ReportField> reportFields = this.listByDbId(dbId);
        if (CollUtil.isEmpty(reportFields)) {
            return;
        }
        for (ReportField reportField : reportFields) {
            deleteById(reportField.getId());
        }
    }

    @Override
    public ReportFieldFormula fieldUseFormula(FieldUseFormulaDto dto) {
        Map<String, Object> rowData = dto.getRowData();
        Long reportId = dto.getReportId();
        if (rowData == null) throw new BizException("查无数据");
        String accountUnitCode = rowData.get(Constant.ACCOUNT_UNIT_ID) == null ? null : rowData.get(Constant.ACCOUNT_UNIT_ID) + "";
        // 找到公式
        ReportFieldFormula reportFieldFormula = formulaService.findByFieldId(dto.getFieldId(), accountUnitCode);
        if (reportFieldFormula == null) {
            ReportField reportField = this.getById(dto.getFieldId());
            throw new BizException(reportField.getFieldViewAlias() + "[" + reportField.getFieldName() + "]未找到公式");
        }
        // 找到公式详情
        List<ReportFieldFormulaDetails> reportFieldFormulaDetails = formulaDetailsService.listByFormulaId(reportFieldFormula.getId());
        if (CollUtil.isNotEmpty(reportFieldFormulaDetails)) {
            List<ReportField> reportFields = listByReportId(reportId);
            Map<Long, String> fieldTextMap = reportFields.stream().collect(Collectors.toMap(ReportField::getId, ReportField::getFieldViewAlias));
            reportFieldFormulaDetails.forEach(e -> {
                e.setReportFieldViewAlias(fieldTextMap.get(e.getReportFieldId()));
                e.setReportFieldValue(rowData.get(e.getReportFieldName()));
            });
        }
        reportFieldFormula.setParamsList(reportFieldFormulaDetails);
        return reportFieldFormula;
    }

    @Override
    public void fillFieldData(List<ReportField> reportFields) {
        List<Long> collect = reportFields.stream().map(Entity::getId).collect(Collectors.toList());
        // 第二层
        List<ReportFieldFormula> reportFieldFormulas = formulaService.listByFieldIds(collect);
        List<ReportFieldSonReport> sonReports = sonReportService.listByFieldIds(collect);
        // 第三层数据
        List<ReportFieldFormulaDetails> fieldFormulaDetails = formulaDetailsService.listByFieldIds(collect);

        for (ReportFieldFormula reportFieldFormula : reportFieldFormulas) {
            List<ReportFieldFormulaDetails> formulaDetails = fieldFormulaDetails.stream().filter(e -> Objects.equals(e.getReportFormulaId(), reportFieldFormula.getId())).collect(Collectors.toList());
            reportFieldFormula.setParamsList(formulaDetails);
        }

        for (ReportField reportField : reportFields) {
            List<ReportFieldFormula> collect1 = reportFieldFormulas.stream().filter(e -> Objects.equals(e.getReportFieldId(), reportField.getId())).collect(Collectors.toList());
            reportField.setReportFieldFormulas(collect1);
            if (CollUtil.isNotEmpty(collect1)) {
                reportField.setFormulaFlag("1");
            }
            List<ReportFieldSonReport> collect2 = sonReports.stream().filter(e -> Objects.equals(e.getReportFieldId(), reportField.getId())).collect(Collectors.toList());
            reportField.setSonReports(collect2);
            if (CollUtil.isNotEmpty(collect2)) {
                reportField.setSonReportFlag("1");
            }
        }
    }

    @Override
    public void fillFieldFlag(ReportField reportField) {
        boolean formulaExistFlag = formulaService.existsByFieldId(reportField.getId());
        if (formulaExistFlag) {
            reportField.setFormulaFlag("1");
        }
        boolean sonReportExistFlag = sonReportService.existsByFieldId(reportField.getId());
        if (sonReportExistFlag) {
            reportField.setSonReportFlag("1");
        }
    }

    @Override
    public Map<String, Object> caclFieldFormula(Map<String, Object> rowData, List<ReportField> reportFields) {
        if (reportFields == null || reportFields.isEmpty()) {
            return rowData;
        }
        List<ReportField> formulaReportFields = reportFields.stream().filter(rf -> Objects.equals(rf.getFieldType(), "2")).collect(Collectors.toList());
        if (formulaReportFields.isEmpty()) {
            return rowData;
        }
        String accountUnitCode = rowData.get(Constant.ACCOUNT_UNIT_ID) == null ? null : rowData.get(Constant.ACCOUNT_UNIT_ID) + "";
        // 计算值
        for (ReportField reportField : formulaReportFields) {
            this.calcFieldValue(rowData, reportField, accountUnitCode, reportFields);
        }
        return rowData;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean deleteById(Long id) {
        // 删除字段
        super.removeById(id);
        // 删除字段公式
        List<ReportFieldFormula> reportFieldFormulas = formulaService.listByFieldId(id);
        if (CollUtil.isNotEmpty(reportFieldFormulas)) {
            for (ReportFieldFormula reportFieldFormula : reportFieldFormulas) {
                // 删除公式
                formulaService.deleteById(reportFieldFormula.getId());
            }
        }
        // 删除子报表
        List<ReportFieldSonReport> reportFieldSonReports = sonReportService.listByFieldId(id);
        if (CollUtil.isNotEmpty(reportFieldSonReports)) {
            for (ReportFieldSonReport reportFieldSonReport : reportFieldSonReports) {
                sonReportService.removeById(reportFieldSonReport);
            }
        }
        return true;
    }

    private void calcFieldValue(Map<String, Object> rowData, ReportField reportField, String accountUnitCode, List<ReportField> reportFields) {
        String NAN = "-";
        String fieldName = reportField.getFieldName();
        // rowData中已有值了，则返回
        if (StringUtils.isBlank(fieldName) || rowData.get(fieldName) != null) {
            return;
        }
        // 找到公式
        ReportFieldFormula reportFieldFormula = findMatchFormula(reportField, accountUnitCode);
        if (reportFieldFormula == null) {
            reportField = this.getById(reportField.getId());
            throw new BizException(reportField.getFieldViewAlias() + "[" + fieldName + "]未找到公式");
        }
        Map<Long, ReportField> fieldIdMap = reportFields.stream().collect(Collectors.toMap(ReportField::getId, item -> item, (k1, k2) -> k2));
        // 找到公式用到的字段，递归获取值
        List<ReportFieldFormulaDetails> details = reportFieldFormula.getParamsList();
        for (ReportFieldFormulaDetails detail : details) {
            String fieldCode = detail.getReportFieldName();
            if (rowData.get(fieldCode) == null) {
                ReportField any = fieldIdMap.get(detail.getReportFieldId());
                log.info("ReportField any:{}", any);
                if (any == null) {
                    throw new BizException(reportField.getFieldViewAlias() + "[" + fieldName + "]使用的公式引用的关联字段不存在");
                } else if ("1".equals(any.getFieldType())) {// 数据集字段 null转 NaN
                    rowData.put(fieldCode, NAN);
                } else {
                    this.calcFieldValue(rowData, any, accountUnitCode, reportFields);
                }
            }
        }
        // 根据公式计算出值，塞入rowData
        String expression = reportFieldFormula.getExpressionJs();
        for (ReportFieldFormulaDetails detail : details) {
            String fieldId = detail.getReportFieldId() + "";
            String fieldCode = detail.getReportFieldName();

            if (NAN.equals(rowData.get(fieldCode) + "")) {
                rowData.put(fieldName, NAN);
                return;
            }

            BigDecimal value = new BigDecimal(rowData.get(fieldCode) + "");
            expression = expression.replaceAll(fieldId, value + "");
        }
        try {
            Object result = CommonUtils.caclByEval(expression);
            // 公式计算结果保留有效位数
            int reservedDecimal = reportFieldFormula.getReservedDecimal() == null ? 2 : reportFieldFormula.getReservedDecimal();
            rowData.put(fieldName, new BigDecimal(result + "").setScale(reservedDecimal, RoundingMode.HALF_UP));
        } catch (Exception e) {
            reportField = this.getById(reportField.getId());
            String msg = reportField.getFieldViewAlias() + "[" + fieldName + "]计算异常：" + expression;
            // log.error(msg, e);
            rowData.put(fieldName, NAN);
        }
    }

    private ReportFieldFormula findMatchFormula(ReportField reportField, String accountUnitCode) {
        List<ReportFieldFormula> reportFieldFormulas = reportField.getReportFieldFormulas();
        if (accountUnitCode == null) {
            return reportFieldFormulas.stream().filter(e -> Objects.equals(e.getFormulaType(), "1")).findAny()
                    .orElseThrow(() -> new BizException(reportField.getFieldViewAlias() + "[" + reportField.getFieldName() + "]未找到公式"));
        }
        Optional<ReportFieldFormula> any = reportFieldFormulas.stream().filter(e -> Objects.equals(e.getFormulaType(), "2") && e.getMatchFieldValues().contains(accountUnitCode)).findAny();
        if (any.isPresent()) {
            return any.get();
        } else {
            log.info("未找到匹配的公式，使用默认公式：{}：{}", accountUnitCode, reportField.getFieldViewAlias());
            return reportFieldFormulas.stream().filter(e -> Objects.equals(e.getFormulaType(), "1")).findAny()
                    .orElseThrow(() -> new BizException(reportField.getFieldViewAlias() + "[" + reportField.getFieldName() + "]未找到公式"));
        }
    }


}
