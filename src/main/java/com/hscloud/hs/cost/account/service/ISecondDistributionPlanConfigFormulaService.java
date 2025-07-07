package com.hscloud.hs.cost.account.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.hscloud.hs.cost.account.model.dto.SecondDistributionPlanConfigFormulaDto;
import com.hscloud.hs.cost.account.model.entity.SecondDistributionPlanConfigFormula;
import com.hscloud.hs.cost.account.model.pojo.SecondDistributionFormula;

import java.util.List;

/**
 * <p>
 * 二次分配方案公式配置表 服务类
 * </p>
 *
 * @author 
 * @since 2023-11-17
 */
public interface ISecondDistributionPlanConfigFormulaService extends IService<SecondDistributionPlanConfigFormula> {

    List<SecondDistributionFormula.FormulaParam> getAccountIndexSelect(List<Long> input);
}
