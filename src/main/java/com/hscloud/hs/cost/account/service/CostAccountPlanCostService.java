package com.hscloud.hs.cost.account.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hscloud.hs.cost.account.model.dto.CostAccountPlanCostQueryDto;
import com.hscloud.hs.cost.account.model.dto.CostAccountPlanFormulaDto;
import com.hscloud.hs.cost.account.model.entity.CostAccountPlanConfigFormula;
import com.hscloud.hs.cost.account.model.entity.CostAccountPlanCost;
import com.hscloud.hs.cost.account.model.vo.PlanCostPreviewVo;

import java.util.List;

public interface CostAccountPlanCostService extends IService<CostAccountPlanCost> {

    void saveCostFormula(CostAccountPlanFormulaDto costAccountPlanFormulaDto);

    List<CostAccountPlanCost> listAllCostFormula(Long planId);

   // List<CostAccountPlanCost> listCosts(Long planId);

    CostAccountPlanCost listCostFormula(Long id);

    int updateCostFormula(CostAccountPlanCostQueryDto costAccountPlanCostQueryDto);

//    List<PlanCostPreviewVo> parsePlanCost(List<CostAccountPlanCost> costList);

    List<PlanCostPreviewVo> parsePlanCost(List<CostAccountPlanConfigFormula> formulas);
}

