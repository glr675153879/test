package com.hscloud.hs.cost.account.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hscloud.hs.cost.account.model.entity.CostAccountPlanConfigIndex;

import java.util.List;

public interface CostAccountPlanConfigIndexService extends IService<CostAccountPlanConfigIndex> {

    List<CostAccountPlanConfigIndex> listIndex(Long configId);
}
