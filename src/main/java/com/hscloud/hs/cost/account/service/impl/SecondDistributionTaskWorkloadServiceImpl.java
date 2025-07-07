package com.hscloud.hs.cost.account.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hscloud.hs.cost.account.mapper.SecondDistributionTaskWorkloadMapper;
import com.hscloud.hs.cost.account.model.entity.SecondDistributionTaskWorkload;
import com.hscloud.hs.cost.account.service.ISecondDistributionTaskWorkloadService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 二次分配任务工作量绩效表 服务实现类
 * </p>
 *
 * @author 
 * @since 2023-11-17
 */
@Service
public class SecondDistributionTaskWorkloadServiceImpl extends ServiceImpl<SecondDistributionTaskWorkloadMapper, SecondDistributionTaskWorkload> implements ISecondDistributionTaskWorkloadService {



    @Override
    public Long saveWorkload(SecondDistributionTaskWorkload secondDistributionTaskWorkload) {


        save(secondDistributionTaskWorkload);
        return secondDistributionTaskWorkload.getId();
    }
}
