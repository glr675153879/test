package com.hscloud.hs.cost.account.service.impl.kpi;

import lombok.RequiredArgsConstructor;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hscloud.hs.cost.account.mapper.kpi.KpiCalculateMapper;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiCalculate;
import com.hscloud.hs.cost.account.service.kpi.IKpiCalculateService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
* 计算结果 服务实现类
*
*/
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class KpiCalculateService extends ServiceImpl<KpiCalculateMapper, KpiCalculate> implements IKpiCalculateService {


}
