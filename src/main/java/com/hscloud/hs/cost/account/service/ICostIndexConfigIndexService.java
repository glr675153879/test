package com.hscloud.hs.cost.account.service;

import com.hscloud.hs.cost.account.model.entity.CostIndexConfigIndex;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 核算指标配置项为核算指标 服务类
 * </p>
 *
 * @author 
 * @since 2023-09-10
 */
public interface ICostIndexConfigIndexService extends IService<CostIndexConfigIndex> {

    List<CostIndexConfigIndex> getByIndexId(Long id);

    void deleteByIndexId(Long id);
}
