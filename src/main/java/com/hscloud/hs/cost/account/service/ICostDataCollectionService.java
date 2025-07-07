package com.hscloud.hs.cost.account.service;

import com.hscloud.hs.cost.account.model.dto.CostDataCollectionDto;

/**
 * @author banana
 * @create 2023-09-20 14:14
 */
public interface ICostDataCollectionService {
    public Object getDataByAppName(CostDataCollectionDto input);
}
