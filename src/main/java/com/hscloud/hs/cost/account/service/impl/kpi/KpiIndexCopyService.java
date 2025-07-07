package com.hscloud.hs.cost.account.service.impl.kpi;

import lombok.RequiredArgsConstructor;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hscloud.hs.cost.account.mapper.kpi.KpiIndexCopyMapper;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiIndexCopy;
import com.hscloud.hs.cost.account.service.kpi.IKpiIndexCopyService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
* 指标表备份 服务实现类
*
*/
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class KpiIndexCopyService extends ServiceImpl<KpiIndexCopyMapper, KpiIndexCopy> implements IKpiIndexCopyService {


}
