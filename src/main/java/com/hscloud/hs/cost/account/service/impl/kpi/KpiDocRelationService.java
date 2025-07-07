package com.hscloud.hs.cost.account.service.impl.kpi;

import lombok.RequiredArgsConstructor;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hscloud.hs.cost.account.mapper.kpi.KpiDocRelationMapper;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiDocRelation;
import com.hscloud.hs.cost.account.service.kpi.IKpiDocRelationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
* 医护关系 服务实现类
*
*/
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class KpiDocRelationService extends ServiceImpl<KpiDocRelationMapper, KpiDocRelation> implements IKpiDocRelationService {


}
