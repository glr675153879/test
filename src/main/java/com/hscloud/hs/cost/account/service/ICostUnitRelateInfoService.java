package com.hscloud.hs.cost.account.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hscloud.hs.cost.account.model.entity.CostUnitRelateInfo;
import com.hscloud.hs.cost.account.model.entity.second.SecondTask;

import java.util.List;

/**
 * 核算单元关联科室人员 服务接口类
 */
public interface ICostUnitRelateInfoService extends IService<CostUnitRelateInfo> {
    List<CostUnitRelateInfo> listByAccountUnitIds(List<Long> accountUnitIds);


}
