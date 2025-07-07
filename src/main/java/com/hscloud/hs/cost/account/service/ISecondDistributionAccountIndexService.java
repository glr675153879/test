package com.hscloud.hs.cost.account.service;

import com.hscloud.hs.cost.account.model.entity.SecondDistributionAccountIndex;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hscloud.hs.cost.account.model.pojo.SecondDistributionFormula;

import java.util.List;

/**
 * <p>
 * 二次分配方案核算指标表 服务类
 * </p>
 *
 * @author 
 * @since 2023-11-24
 */
public interface ISecondDistributionAccountIndexService extends IService<SecondDistributionAccountIndex> {

    List<SecondDistributionFormula.FormulaParam> getAccountIndexSelect(String accountIndex, Long unitId);
}
