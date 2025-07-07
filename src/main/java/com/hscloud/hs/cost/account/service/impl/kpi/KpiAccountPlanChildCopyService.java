package com.hscloud.hs.cost.account.service.impl.kpi;

import lombok.RequiredArgsConstructor;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hscloud.hs.cost.account.mapper.kpi.KpiAccountPlanChildCopyMapper;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiAccountPlanChildCopy;
import com.hscloud.hs.cost.account.service.kpi.IKpiAccountPlanChildCopyService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
* 核算子方案备份 服务实现类
*
*/
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class KpiAccountPlanChildCopyService extends ServiceImpl<KpiAccountPlanChildCopyMapper, KpiAccountPlanChildCopy> implements IKpiAccountPlanChildCopyService {


}
