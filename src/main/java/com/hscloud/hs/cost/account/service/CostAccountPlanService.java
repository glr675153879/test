package com.hscloud.hs.cost.account.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hscloud.hs.cost.account.model.dto.CostAccountPlanQueryDto;
import com.hscloud.hs.cost.account.model.dto.CostAccountPlanStatusDto;
import com.hscloud.hs.cost.account.model.dto.CostAccountUnitDto;
import com.hscloud.hs.cost.account.model.entity.CostAccountPlan;
import com.hscloud.hs.cost.account.model.entity.CostAccountPlanConfigFormula;
import com.hscloud.hs.cost.account.model.vo.CostPlanAccountObjectVo;

import java.util.List;

/**
 * @author Administrator
 */
public interface CostAccountPlanService extends IService<CostAccountPlan> {

    IPage listPlan(CostAccountPlanQueryDto costAccountPlanQueryDto);

    void switchStatus(CostAccountPlanStatusDto costAccountPlanStatusDto);



    void check(CostAccountPlanStatusDto costAccountPlanStatusDto);


    CostAccountPlanQueryDto byId(Long id);

    /**
     * 获取核算方案下的核算单位
     * @param planId 核算方案下的核算单位
     * @return Object
     */
    List<CostPlanAccountObjectVo> listAccountObject(Long  planId);

    void copyById(Long id);
}
