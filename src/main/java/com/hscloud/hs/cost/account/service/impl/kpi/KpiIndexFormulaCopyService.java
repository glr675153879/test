package com.hscloud.hs.cost.account.service.impl.kpi;

import lombok.RequiredArgsConstructor;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hscloud.hs.cost.account.mapper.kpi.KpiIndexFormulaCopyMapper;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiIndexFormulaCopy;
import com.hscloud.hs.cost.account.service.kpi.IKpiIndexFormulaCopyService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
* 指标公式表备份 服务实现类
*
*/
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class KpiIndexFormulaCopyService extends ServiceImpl<KpiIndexFormulaCopyMapper, KpiIndexFormulaCopy> implements IKpiIndexFormulaCopyService {


}
