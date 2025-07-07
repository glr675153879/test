package com.hscloud.hs.cost.account.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hscloud.hs.cost.account.mapper.SecondDistributionTaskAverageMapper;
import com.hscloud.hs.cost.account.model.entity.SecondDistributionTaskAverage;
import com.hscloud.hs.cost.account.service.ISecondDistributionTaskAverageService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 二次分配任务平均绩效表 服务实现类
 * </p>
 *
 * @author 
 * @since 2023-11-17
 */
@Service
public class SecondDistributionTaskAverageServiceImpl extends ServiceImpl<SecondDistributionTaskAverageMapper, SecondDistributionTaskAverage> implements ISecondDistributionTaskAverageService {


    @Override
    public Long saveAverage(SecondDistributionTaskAverage secondDistributionTaskAverage) {

        save(secondDistributionTaskAverage);
        return secondDistributionTaskAverage.getId();
    }
}
