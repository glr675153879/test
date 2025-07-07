package com.hscloud.hs.cost.account.service.impl.kpi;

import lombok.RequiredArgsConstructor;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hscloud.hs.cost.account.mapper.kpi.KpiCategoryCopyMapper;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiCategoryCopy;
import com.hscloud.hs.cost.account.service.kpi.IKpiCategoryCopyService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
* 分组表备份 服务实现类
*
*/
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class KpiCategoryCopyService extends ServiceImpl<KpiCategoryCopyMapper, KpiCategoryCopy> implements IKpiCategoryCopyService {


}
