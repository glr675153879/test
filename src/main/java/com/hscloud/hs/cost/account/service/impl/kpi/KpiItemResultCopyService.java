package com.hscloud.hs.cost.account.service.impl.kpi;

import lombok.RequiredArgsConstructor;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hscloud.hs.cost.account.mapper.kpi.KpiItemResultCopyMapper;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiItemResultCopy;
import com.hscloud.hs.cost.account.service.kpi.IKpiItemResultCopyService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
* 核算项结果集备份 服务实现类
*
*/
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class KpiItemResultCopyService extends ServiceImpl<KpiItemResultCopyMapper, KpiItemResultCopy> implements IKpiItemResultCopyService {


}
