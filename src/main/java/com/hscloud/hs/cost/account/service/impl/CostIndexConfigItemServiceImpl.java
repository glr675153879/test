package com.hscloud.hs.cost.account.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hscloud.hs.cost.account.mapper.CostIndexConfigItemMapper;
import com.hscloud.hs.cost.account.model.entity.CostIndexConfigItem;
import com.hscloud.hs.cost.account.service.ICostIndexConfigItemService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 核算指标配置项 服务实现类
 * </p>
 *
 * @author 
 * @since 2023-09-10
 */
@Service
public class CostIndexConfigItemServiceImpl extends ServiceImpl<CostIndexConfigItemMapper, CostIndexConfigItem> implements ICostIndexConfigItemService {


    @Override
    public List<CostIndexConfigItem> getByIndexId(Long id) {
        return baseMapper.getByIndexId(id);
    }

    @Override
    public void deleteByIndexId(Long id) {
        baseMapper.deleteByIndexId(id);
    }
}
