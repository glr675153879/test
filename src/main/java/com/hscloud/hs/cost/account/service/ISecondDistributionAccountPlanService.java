package com.hscloud.hs.cost.account.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.hscloud.hs.cost.account.model.dto.saveDistributionAccountPlanDto;
import com.hscloud.hs.cost.account.model.entity.SecondDistributionAccountPlan;
import com.hscloud.hs.cost.account.model.vo.SecondDistributionGetAccountPlanDetailsVo;

/**
 * <p>
 * 二次分配方案表 服务类
 * </p>
 *
 * @author 
 * @since 2023-11-17
 */
public interface ISecondDistributionAccountPlanService extends IService<SecondDistributionAccountPlan> {





    default SecondDistributionGetAccountPlanDetailsVo getDistributionAccountPlanDetails(Long unitId){
        return getDistributionAccountPlanDetails(null,unitId);
    };


    SecondDistributionGetAccountPlanDetailsVo getDistributionAccountPlanDetails(Long planId,Long unitId);

    //保存二次分配方案
    void saveDistributionAccountPlan(saveDistributionAccountPlanDto input);



}
