package com.hscloud.hs.cost.account.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.hscloud.hs.cost.account.model.entity.SecondDistributionTaskAverage;

/**
 * <p>
 * 二次分配任务平均绩效表 服务类
 * </p>
 *
 * @author 
 * @since 2023-11-17
 */
public interface ISecondDistributionTaskAverageService extends IService<SecondDistributionTaskAverage> {

    Long saveAverage(SecondDistributionTaskAverage secondDistributionTaskAverage);
}
