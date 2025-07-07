package com.hscloud.hs.cost.account.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hscloud.hs.cost.account.mapper.DistributionTaskMapper;
import com.hscloud.hs.cost.account.model.dto.TaskResultQueryDto;
import com.hscloud.hs.cost.account.model.pojo.AdsCostShareTempNew;
import com.hscloud.hs.cost.account.model.vo.CostAccountTaskResultDetailVo;
import com.hscloud.hs.cost.account.service.IDistributionTaskService;
import com.hscloud.hs.cost.account.utils.LocalCacheUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DistributionTaskServiceImpl extends ServiceImpl<DistributionTaskMapper, AdsCostShareTempNew> implements IDistributionTaskService {

    private final DistributionTaskMapper distributionTaskMapper;

    private final StringRedisTemplate redisTemplate;

    private final LocalCacheUtils cacheUtils;


    @Override
    public CostAccountTaskResultDetailVo listResult(TaskResultQueryDto taskResultQueryDto) {
        return null;
    }
}
