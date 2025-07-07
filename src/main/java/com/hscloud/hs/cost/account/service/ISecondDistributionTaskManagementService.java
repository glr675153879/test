package com.hscloud.hs.cost.account.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.hscloud.hs.cost.account.model.entity.SecondDistributionTaskManagement;

/**
 * <p>
 * 二次分配任务管理绩效表 服务类
 * </p>
 *
 * @author 
 * @since 2023-11-17
 */
public interface ISecondDistributionTaskManagementService extends IService<SecondDistributionTaskManagement> {

    Long saveManagement(SecondDistributionTaskManagement secondDistributionTaskManagement);
}
