package com.hscloud.hs.cost.account.service.impl;

import com.hscloud.hs.cost.account.model.entity.CostAllocationRuleConfigIndex;
import com.hscloud.hs.cost.account.mapper.CostAllocationRuleConfigIndexMapper;
import com.hscloud.hs.cost.account.service.ICostAllocationRuleConfigIndexService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 分摊规则配置项为核算指标 服务实现类
 * </p>
 *
 * @author 
 * @since 2023-09-11
 */
@Service
public class CostAllocationRuleConfigIndexServiceImpl extends ServiceImpl<CostAllocationRuleConfigIndexMapper, CostAllocationRuleConfigIndex> implements ICostAllocationRuleConfigIndexService {

    @Override
    public void removeByAllocationRuleId(Long id) {
        baseMapper.removeByAllocationRuleId(id);
    }

    @Override
    public List<CostAllocationRuleConfigIndex> getByAllocationRuleId(Long id) {

        return baseMapper.getByAllocationRuleId(id);
    }
}
