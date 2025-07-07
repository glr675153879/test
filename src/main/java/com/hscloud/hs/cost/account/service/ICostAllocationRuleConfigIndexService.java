package com.hscloud.hs.cost.account.service;

import com.hscloud.hs.cost.account.model.entity.CostAllocationRuleConfigIndex;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 分摊规则配置项为核算指标 服务类
 * </p>
 *
 * @author 
 * @since 2023-09-11
 */
public interface ICostAllocationRuleConfigIndexService extends IService<CostAllocationRuleConfigIndex> {

    void removeByAllocationRuleId(Long id);

    List<CostAllocationRuleConfigIndex> getByAllocationRuleId(Long id);
}
