package com.hscloud.hs.cost.account.service;

import com.hscloud.hs.cost.account.model.entity.CostBaseInitialize;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 初始化完成表 服务类
 * </p>
 *
 * @author 
 * @since 2023-09-08
 */
public interface ICostBaseInitializeService extends IService<CostBaseInitialize> {

    void initialize(CostBaseInitialize costBaseInitialize);

    CostBaseInitialize getInitialize(CostBaseInitialize costBaseInitialize);

}
