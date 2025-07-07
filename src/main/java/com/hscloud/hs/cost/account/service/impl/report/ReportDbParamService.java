package com.hscloud.hs.cost.account.service.impl.report;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hscloud.hs.cost.account.mapper.report.ReportDbParamMapper;
import com.hscloud.hs.cost.account.model.entity.report.ReportDbParam;
import com.hscloud.hs.cost.account.service.report.IReportDbParamService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 报表入参表 服务实现类
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportDbParamService extends ServiceImpl<ReportDbParamMapper, ReportDbParam> implements IReportDbParamService {

    @Override
    public List<ReportDbParam> listByDbId(Long reportDbId) {
        return super.list(Wrappers.<ReportDbParam>lambdaQuery().eq(ReportDbParam::getReportDbId, reportDbId).orderByAsc(ReportDbParam::getSort));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeByDbId(Long reportDbId) {
        super.remove(Wrappers.<ReportDbParam>lambdaQuery().eq(ReportDbParam::getReportDbId, reportDbId));
    }
}
