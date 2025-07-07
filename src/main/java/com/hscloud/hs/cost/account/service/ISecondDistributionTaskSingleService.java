package com.hscloud.hs.cost.account.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.hscloud.hs.cost.account.model.entity.SecondDistributionTaskSingle;

/**
 * <p>
 * 二次分配任务单项绩效表 服务类
 * </p>
 *
 * @author 
 * @since 2023-11-17
 */
public interface ISecondDistributionTaskSingleService extends IService<SecondDistributionTaskSingle> {

    Long saveSingle(SecondDistributionTaskSingle secondDistributionTaskSingle);
}
