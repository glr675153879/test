package com.hscloud.hs.cost.account.service.impl.kpi;

import lombok.RequiredArgsConstructor;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hscloud.hs.cost.account.mapper.kpi.KpiAccountPlanCopyMapper;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiAccountPlanCopy;
import com.hscloud.hs.cost.account.service.kpi.IKpiAccountPlanCopyService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
* 核算方案表备份 服务实现类
*
*/
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class KpiAccountPlanCopyService extends ServiceImpl<KpiAccountPlanCopyMapper, KpiAccountPlanCopy> implements IKpiAccountPlanCopyService {


}
