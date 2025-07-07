package com.hscloud.hs.cost.account.service.impl.dataReport;

import lombok.RequiredArgsConstructor;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hscloud.hs.cost.account.mapper.dataReport.CostReportDetailInfoMapper;
import com.hscloud.hs.cost.account.model.entity.dataReport.CostReportDetailInfo;
import com.hscloud.hs.cost.account.service.dataReport.ICostReportDetailInfoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
* 上报详情基本信息表 服务实现类
*
*/
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CostReportDetailInfoService extends ServiceImpl<CostReportDetailInfoMapper, CostReportDetailInfo> implements ICostReportDetailInfoService {


}
