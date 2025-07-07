package com.hscloud.hs.cost.account.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hscloud.hs.cost.account.mapper.SecondDistributionTaskManagementMapper;
import com.hscloud.hs.cost.account.model.entity.SecondDistributionTaskManagement;
import com.hscloud.hs.cost.account.service.ISecondDistributionTaskManagementService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 二次分配任务管理绩效表 服务实现类
 * </p>
 *
 * @author 
 * @since 2023-11-17
 */
@Service
public class SecondDistributionTaskManagementServiceImpl extends ServiceImpl<SecondDistributionTaskManagementMapper, SecondDistributionTaskManagement> implements ISecondDistributionTaskManagementService {



    @Override
    public Long saveManagement(SecondDistributionTaskManagement secondDistributionTaskManagement) {

        save(secondDistributionTaskManagement);
        return secondDistributionTaskManagement.getId();
    }
}
