package com.hscloud.hs.cost.account.service.impl.kpi;

import lombok.RequiredArgsConstructor;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hscloud.hs.cost.account.mapper.kpi.KpiSignRightMapper;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiSignRight;
import com.hscloud.hs.cost.account.service.kpi.IKpiSignRightService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

/**
* 绩效签发 右侧不固定 服务实现类
*
*/
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class KpiSignRightService extends ServiceImpl<KpiSignRightMapper, KpiSignRight> implements IKpiSignRightService {


}
