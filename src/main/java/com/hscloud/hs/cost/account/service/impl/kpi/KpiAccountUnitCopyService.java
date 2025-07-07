package com.hscloud.hs.cost.account.service.impl.kpi;

import lombok.RequiredArgsConstructor;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hscloud.hs.cost.account.mapper.kpi.KpiAccountUnitCopyMapper;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiAccountUnitCopy;
import com.hscloud.hs.cost.account.service.kpi.IKpiAccountUnitCopyService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
* 核算单元名称备份 服务实现类
*
*/
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class KpiAccountUnitCopyService extends ServiceImpl<KpiAccountUnitCopyMapper, KpiAccountUnitCopy> implements IKpiAccountUnitCopyService {


}
