package com.hscloud.hs.cost.account.service.impl.report;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hscloud.hs.cost.account.mapper.report.ReportDbMapper;
import com.hscloud.hs.cost.account.model.dto.report.QueryFieldBySqlDto;
import com.hscloud.hs.cost.account.model.entity.report.ReportDb;
import com.hscloud.hs.cost.account.model.entity.report.ReportDbParam;
import com.hscloud.hs.cost.account.model.entity.report.ReportField;
import com.hscloud.hs.cost.account.model.vo.report.MetaDataBySqlVo;
import com.hscloud.hs.cost.account.service.report.IReportDbParamService;
import com.hscloud.hs.cost.account.service.report.IReportDbService;
import com.hscloud.hs.cost.account.service.report.IReportFieldService;
import com.hscloud.hs.cost.account.utils.JdbcUtil;
import com.pig4cloud.pigx.common.core.exception.BizException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 数据集设计表 服务实现类
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportDbService extends ServiceImpl<ReportDbMapper, ReportDb> implements IReportDbService {

    private final IReportDbParamService reportDbParamService;
    private final IReportFieldService reportFieldService;
    private final JdbcUtil jdbcUtil;
    private final ReportHeadService reportHeadService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createOrEdit(ReportDb entity) {
        super.saveOrUpdate(entity);
        if (CollUtil.isNotEmpty(entity.getDbFields())) {
            entity.getDbFields().forEach(e -> {
                e.setReportId(entity.getReportId());
                e.setReportDbId(entity.getId());
                if (Objects.nonNull(e.getId()) && StrUtil.isEmpty(e.getFieldViewAlias())) {
                    e.setFieldViewAlias(e.getFieldText());
                }
                reportFieldService.saveOrUpdate(e);
            });
        }
        List<ReportDbParam> reportDbParamsNew = entity.getReportDbParams();
        //数据库中删除不存在的ids
        List<Long> ids = reportDbParamsNew.stream().map(ReportDbParam::getId).filter(Objects::nonNull).collect(Collectors.toList());
        reportDbParamService.remove(
                Wrappers.<ReportDbParam>lambdaQuery().eq(ReportDbParam::getReportDbId, entity.getId())
                        .notIn(CollUtil.isNotEmpty(ids), ReportDbParam::getId, ids));
        //保存或更新
        reportDbParamsNew.forEach(e -> {
            e.setReportId(entity.getReportId());
            e.setReportDbId(entity.getId());
        });
        reportDbParamService.saveOrUpdateBatch(reportDbParamsNew);
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeById(Long id) {
        ReportDb byId = getById(id);
        if (Objects.isNull(byId)) {
            throw new BizException("数据集不存在");
        }
        super.removeById(id);
        reportFieldService.removeByDbId(id);
        reportDbParamService.removeByDbId(id);
        //删除表头
        reportHeadService.removeByReportId(byId.getReportId());
        return true;
    }

    @Override
    public ReportDb getByReportId(Long reportId) {
        return super.getOne(Wrappers.<ReportDb>lambdaQuery().eq(ReportDb::getReportId, reportId));
    }

    @Override
    public ReportDb loadDbAllDataByReportId(Long reportId) {
        ReportDb byId = getByReportId(reportId);
        fillReportDb(byId);
        return byId;
    }

    @Override
    public ReportDb loadDbData(Long id) {
        ReportDb byId = super.getById(id);
        fillReportDb(byId);
        return byId;
    }

    @Override
    public List<MetaDataBySqlVo> queryFieldBySql(QueryFieldBySqlDto dto) {
        return jdbcUtil.getMetaDataBySql(dto.getSql());
    }

    public void fillReportDb(ReportDb byId) {
        if (Objects.isNull(byId)) {
            return;
        }
        byId.setReportDbParams(reportDbParamService.listByDbId(byId.getId()));
        List<ReportField> reportFields = reportFieldService.listByDbId(byId.getId());
        if (CollUtil.isNotEmpty(reportFields)) {
            reportFieldService.fillFieldData(reportFields);
            byId.setDbFields(reportFields.stream().filter(rf -> Objects.equals(rf.getFieldType(), "1")).collect(Collectors.toList()));
            byId.setCalcFields(reportFields.stream().filter(rf -> Objects.equals(rf.getFieldType(), "2")).collect(Collectors.toList()));
            byId.setHeadFields(reportFields.stream().filter(rf -> Objects.equals(rf.getFieldType(), "3")).collect(Collectors.toList()));
        }
        byId.setReportFields(reportFields);
    }

}
