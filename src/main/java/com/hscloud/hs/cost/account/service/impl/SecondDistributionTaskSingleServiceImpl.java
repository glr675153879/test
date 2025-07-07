package com.hscloud.hs.cost.account.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hscloud.hs.cost.account.mapper.SecondDistributionTaskSingleMapper;
import com.hscloud.hs.cost.account.model.entity.SecondDistributionTaskSingle;
import com.hscloud.hs.cost.account.service.ISecondDistributionTaskSingleService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 二次分配任务单项绩效表 服务实现类
 * </p>
 *
 * @author 
 * @since 2023-11-17
 */
@Service
public class SecondDistributionTaskSingleServiceImpl extends ServiceImpl<SecondDistributionTaskSingleMapper, SecondDistributionTaskSingle> implements ISecondDistributionTaskSingleService {




    @Override
    public Long saveSingle(SecondDistributionTaskSingle secondDistributionTaskSingle) {


        save(secondDistributionTaskSingle);
        return secondDistributionTaskSingle.getId();
    }

}
