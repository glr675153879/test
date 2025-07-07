package com.hscloud.hs.cost.account.service.impl;

import com.hscloud.hs.cost.account.model.entity.CostIndexConfigIndex;
import com.hscloud.hs.cost.account.mapper.CostIndexConfigIndexMapper;
import com.hscloud.hs.cost.account.service.ICostIndexConfigIndexService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 核算指标配置项为核算指标 服务实现类
 * </p>
 *
 * @author 
 * @since 2023-09-10
 */
@Service
public class CostIndexConfigIndexServiceImpl extends ServiceImpl<CostIndexConfigIndexMapper, CostIndexConfigIndex> implements ICostIndexConfigIndexService {

    @Override
    public List<CostIndexConfigIndex> getByIndexId(Long id) {

        return baseMapper.getByIndexId(id);
    }

    @Override
    public void deleteByIndexId(Long id) {
        baseMapper.deleteByIndexId(id);
    }
}
