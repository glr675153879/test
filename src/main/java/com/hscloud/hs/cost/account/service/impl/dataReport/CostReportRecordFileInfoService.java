package com.hscloud.hs.cost.account.service.impl.dataReport;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hscloud.hs.cost.account.mapper.dataReport.CostReportRecordFileInfoMapper;
import com.hscloud.hs.cost.account.model.entity.dataReport.CostReportRecordFileInfo;
import com.hscloud.hs.cost.account.service.dataReport.ICostReportRecordFileInfoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
* 我的上报附件表 服务实现类
*
*/
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CostReportRecordFileInfoService extends ServiceImpl<CostReportRecordFileInfoMapper, CostReportRecordFileInfo> implements ICostReportRecordFileInfoService {


    @Override
    public List<CostReportRecordFileInfo> listData(Long recordId) {
        List<CostReportRecordFileInfo> list = this.list(new QueryWrapper<CostReportRecordFileInfo>().lambda().eq(CostReportRecordFileInfo::getRecordId, recordId));
        return list;
    }
}
