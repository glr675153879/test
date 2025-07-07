package com.hscloud.hs.cost.account.service.impl.report;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hscloud.hs.cost.account.mapper.report.ReportFieldSonReportMapper;
import com.hscloud.hs.cost.account.model.entity.CostAccountUnit;
import com.hscloud.hs.cost.account.model.entity.report.ReportFieldSonReport;
import com.hscloud.hs.cost.account.service.report.IReportFieldSonReportService;
import com.pig4cloud.pigx.common.core.exception.BizException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 链接报表列表 服务实现类
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportFieldSonReportService extends ServiceImpl<ReportFieldSonReportMapper, ReportFieldSonReport> implements IReportFieldSonReportService {

    @Override
    public List<ReportFieldSonReport> listByFieldId(Long reportField) {
        return super.list(Wrappers.<ReportFieldSonReport>lambdaQuery().eq(ReportFieldSonReport::getReportFieldId, reportField));
    }

    @Override
    public ReportFieldSonReport findByFieldId(Long fieldId, String accountUnitCode) {
        if (accountUnitCode == null) {
            return this.getCommonByFieldId(fieldId);
        }
        ReportFieldSonReport one = this.getOne(Wrappers.<ReportFieldSonReport>lambdaQuery()
                .eq(ReportFieldSonReport::getReportFieldId, fieldId)
                .eq(ReportFieldSonReport::getSonReportType, "2")
                .like(ReportFieldSonReport::getMatchFieldValues, accountUnitCode));
        if (one == null) {
            one = this.getCommonByFieldId(fieldId);
        }
        return one;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createOrEdit(ReportFieldSonReport sonReport) {
        validData(sonReport);
        convert(sonReport);
        super.saveOrUpdate(sonReport);
    }

    @Override
    public List<ReportFieldSonReport> listByFieldIds(List<Long> fieldIds) {
        if (CollUtil.isEmpty(fieldIds)) {
            return new ArrayList<>();
        }
        return super.list(Wrappers.<ReportFieldSonReport>lambdaQuery().in(ReportFieldSonReport::getReportFieldId, fieldIds));
    }

    @Override
    public boolean existsByFieldId(Long fieldId) {
        return super.exists(Wrappers.<ReportFieldSonReport>lambdaQuery().eq(ReportFieldSonReport::getReportFieldId, fieldId));

    }

    private static void convert(ReportFieldSonReport sonReport) {
        List<CostAccountUnit> accountUnitList = sonReport.getAccountUnitList();
        if (CollUtil.isNotEmpty(accountUnitList)) {
            List<Long> matchFieldValues = new ArrayList<>();
            List<String> matchFieldTexts = new ArrayList<>();
            accountUnitList.forEach(costAccountUnit -> {
                matchFieldValues.add(costAccountUnit.getId());
                matchFieldTexts.add(costAccountUnit.getName());
            });
            sonReport.setMatchFieldValues(StrUtil.join(",", matchFieldValues));
            sonReport.setMatchFieldTexts(StrUtil.join(",", matchFieldTexts));
        }
        if (CollUtil.isNotEmpty(sonReport.getParamMappingList())) {
            sonReport.setParamMappingJson(JSON.toJSONString(sonReport.getParamMappingList()));
        }
    }

    private ReportFieldSonReport getCommonByFieldId(Long fieldId) {
        return this.getOne(Wrappers.<ReportFieldSonReport>lambdaQuery()
                .eq(ReportFieldSonReport::getReportFieldId, fieldId)
                .eq(ReportFieldSonReport::getSonReportType, "1"));
    }

    private void validData(ReportFieldSonReport sonReport) {
        Long reportFieldId = sonReport.getReportFieldId();
        if (Objects.isNull(reportFieldId)) {
            throw new BizException("报表字段ID不能为空");
        }
        String sonReportType = sonReport.getSonReportType();
        if (StrUtil.isBlank(sonReportType)) {
            throw new BizException("适用核算单元类型不能为空");
        }
        List<CostAccountUnit> accountUnitList = sonReport.getAccountUnitList();
        if (Objects.equals("2", sonReportType) && CollUtil.isEmpty(accountUnitList)) {
            throw new BizException("未选择核算单元");
        }
        if (Objects.equals("1", sonReportType)) {
            //如果已经存在全部核算单元 formulaType=1的情况，则不允许在添加
            boolean isExistAll = super.exists(Wrappers.<ReportFieldSonReport>lambdaQuery()
                    .eq(ReportFieldSonReport::getReportFieldId, sonReport.getReportFieldId())
                    .eq(ReportFieldSonReport::getSonReportType, "1")
                    .ne(Objects.nonNull(sonReport.getId()), ReportFieldSonReport::getId, sonReport.getId()));
            if (isExistAll) {
                throw new BizException("不允许添加多个[全部核算单元]类型子报表");
            }
        }
        if (Objects.equals("2", sonReportType)) {
            //不允许一个核算单元重复添加在多个子报表
            List<String> existAccountName = new ArrayList<>();
            for (CostAccountUnit costAccountUnit : accountUnitList) {
                boolean isExist = super.exists(Wrappers.<ReportFieldSonReport>lambdaQuery()
                        .eq(ReportFieldSonReport::getReportFieldId, sonReport.getReportFieldId())
                        .eq(ReportFieldSonReport::getSonReportType, sonReportType)
                        .like(ReportFieldSonReport::getMatchFieldValues, costAccountUnit.getId())
                        .ne(Objects.nonNull(sonReport.getId()), ReportFieldSonReport::getId, sonReport.getId()));
                if (isExist) {
                    existAccountName.add(costAccountUnit.getName());
                }
            }
            if (CollUtil.isNotEmpty(existAccountName)) {
                throw new BizException(String.format("核算单元%s已在其他子报表中配置", StrUtil.join(",", existAccountName)));
            }
        }
    }

}
