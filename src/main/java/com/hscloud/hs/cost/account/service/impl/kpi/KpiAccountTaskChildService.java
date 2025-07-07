package com.hscloud.hs.cost.account.service.impl.kpi;

import lombok.RequiredArgsConstructor;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hscloud.hs.cost.account.mapper.kpi.KpiAccountTaskChildMapper;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiAccountTaskChild;
import com.hscloud.hs.cost.account.service.kpi.IKpiAccountTaskChildService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
* 核算任务表 服务实现类
*
*/
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class KpiAccountTaskChildService extends ServiceImpl<KpiAccountTaskChildMapper, KpiAccountTaskChild> implements IKpiAccountTaskChildService {


}
