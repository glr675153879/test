package com.hscloud.hs.cost.account.service.impl.imputation;

import lombok.RequiredArgsConstructor;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hscloud.hs.cost.account.mapper.imputation.ImputationIndexDetailsMapper;
import com.hscloud.hs.cost.account.model.entity.imputation.ImputationIndexDetails;
import com.hscloud.hs.cost.account.service.imputation.IImputationIndexDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
* 归集指标明细 服务实现类
*
*/
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ImputationIndexDetailsService extends ServiceImpl<ImputationIndexDetailsMapper, ImputationIndexDetails> implements IImputationIndexDetailsService {


}
