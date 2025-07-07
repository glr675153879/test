package com.hscloud.hs.cost.account.service.impl.kpi;

import lombok.RequiredArgsConstructor;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hscloud.hs.cost.account.mapper.kpi.KpiMemberCopyMapper;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiMemberCopy;
import com.hscloud.hs.cost.account.service.kpi.IKpiMemberCopyService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
*  服务实现类
*
*/
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class KpiMemberCopyService extends ServiceImpl<KpiMemberCopyMapper, KpiMemberCopy> implements IKpiMemberCopyService {


}
