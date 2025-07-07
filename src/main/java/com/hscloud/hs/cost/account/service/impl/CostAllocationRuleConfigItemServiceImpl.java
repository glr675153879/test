package com.hscloud.hs.cost.account.service.impl;

import com.hscloud.hs.cost.account.model.entity.CostAllocationRuleConfigItem;
import com.hscloud.hs.cost.account.mapper.CostAllocationRuleConfigItemMapper;
import com.hscloud.hs.cost.account.service.ICostAllocationRuleConfigItemService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 分摊规则配置项 服务实现类
 * </p>
 *
 * @author 
 * @since 2023-09-11
 */
@Service
public class CostAllocationRuleConfigItemServiceImpl extends ServiceImpl<CostAllocationRuleConfigItemMapper, CostAllocationRuleConfigItem> implements ICostAllocationRuleConfigItemService {

    @Override
    public void removeByAllocationRuleId(Long id) {
        baseMapper.removeByAllocationRuleId(id);
    }

    @Override
    public List<CostAllocationRuleConfigItem> getByAllocationRuleId(Long id) {

        return baseMapper.getByAllocationRuleId(id);
    }
}
