package com.hscloud.hs.cost.account.service.impl.kpi;

import lombok.RequiredArgsConstructor;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hscloud.hs.cost.account.mapper.kpi.KpiIdMapper;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiId;
import com.hscloud.hs.cost.account.service.kpi.IKpiIdService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
* id自增表 服务实现类
*
*/
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class KpiIdService extends ServiceImpl<KpiIdMapper, KpiId> implements IKpiIdService {


}
