package com.hscloud.hs.cost.account.service.impl.kpi;

import lombok.RequiredArgsConstructor;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hscloud.hs.cost.account.mapper.kpi.KpiAccountUnitMapper;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiAccountUnit;
import com.hscloud.hs.cost.account.service.kpi.IKpiAccountUnitService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
* 核算单元(cost_account_unit) 服务实现类
*
*/
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class KpiAccountUnitService extends ServiceImpl<KpiAccountUnitMapper, KpiAccountUnit> implements IKpiAccountUnitService {


}
