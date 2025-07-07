package com.hscloud.hs.cost.account.service.impl.kpi;

import lombok.RequiredArgsConstructor;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hscloud.hs.cost.account.mapper.kpi.KpiImputationRuleMapper;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiImputationRule;
import com.hscloud.hs.cost.account.service.kpi.IKpiImputationRuleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
* 归集规则表 服务实现类
*
*/
@Service
@RequiredArgsConstructor
@Transactional(readOnly = false)
public class KpiImputationRuleService extends ServiceImpl<KpiImputationRuleMapper, KpiImputationRule> implements IKpiImputationRuleService {


}
