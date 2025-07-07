package com.hscloud.hs.cost.account.service;

import com.hscloud.hs.cost.account.model.entity.CostAllocationRuleConfigItem;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 分摊规则配置项 服务类
 * </p>
 *
 * @author 
 * @since 2023-09-11
 */
public interface ICostAllocationRuleConfigItemService extends IService<CostAllocationRuleConfigItem> {

    void removeByAllocationRuleId(Long id);

    List<CostAllocationRuleConfigItem> getByAllocationRuleId(Long id);
}
