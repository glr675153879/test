package com.hscloud.hs.cost.account.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.hscloud.hs.cost.account.model.entity.SecondDistributionTaskWorkload;

/**
 * <p>
 * 二次分配任务工作量绩效表 服务类
 * </p>
 *
 * @author 
 * @since 2023-11-17
 */
public interface ISecondDistributionTaskWorkloadService extends IService<SecondDistributionTaskWorkload> {

    Long saveWorkload(SecondDistributionTaskWorkload secondDistributionTaskWorkload);
}
