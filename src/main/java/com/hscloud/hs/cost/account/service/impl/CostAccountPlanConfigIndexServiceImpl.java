package com.hscloud.hs.cost.account.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hscloud.hs.cost.account.mapper.CostAccountPlanConfigIndexMapper;
import com.hscloud.hs.cost.account.model.entity.CostAccountPlanConfigIndex;
import com.hscloud.hs.cost.account.service.CostAccountPlanConfigIndexService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CostAccountPlanConfigIndexServiceImpl extends ServiceImpl<CostAccountPlanConfigIndexMapper, CostAccountPlanConfigIndex> implements CostAccountPlanConfigIndexService {

    @Autowired
    CostAccountPlanConfigIndexMapper costAccountPlanConfigIndexMapper;
    public List<CostAccountPlanConfigIndex> listIndex(Long configId){
        return costAccountPlanConfigIndexMapper.selectIndexByConfigId(configId);
    }

}
