package com.hscloud.hs.cost.account.service.impl.dataReport;

import lombok.RequiredArgsConstructor;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hscloud.hs.cost.account.mapper.dataReport.CostReportDetailCostMapper;
import com.hscloud.hs.cost.account.model.entity.dataReport.CostReportDetailCost;
import com.hscloud.hs.cost.account.service.dataReport.ICostReportDetailCostService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
* 上报详情-费用表 服务实现类
*
*/
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CostReportDetailCostService extends ServiceImpl<CostReportDetailCostMapper, CostReportDetailCost> implements ICostReportDetailCostService {


}
