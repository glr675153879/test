package com.hscloud.hs.cost.account.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;

import com.hscloud.hs.cost.account.model.dto.*;

import com.hscloud.hs.cost.account.model.entity.CostAccountPlanConfig;
import com.hscloud.hs.cost.account.model.pojo.CostPlanCalculateInfo;
import com.hscloud.hs.cost.account.model.vo.CostAccountPlanConfigVo;
import com.hscloud.hs.cost.account.model.vo.CostAccountPlanReviewVo;
import com.hscloud.hs.cost.account.model.vo.ValidatorResultVo;

public interface CostAccountPlanConfigService extends IService<CostAccountPlanConfig> {

    void saveConfig(CostAccountPlanConfigDto costAccountPlanConfigDto);

    IPage listConfig(CostAccountPlanConfigQueryDto costAccountPlanConfigQueryDto);

    void updateConfig(CostAccountPlanConfigDto costAccountPlanConfigDto);


    List<CostAccountConfigNameDto> listConfigName(Long planId);

    /**
     * 核算方案总公式校验
     * @param dto
     * @return
     */
    ValidatorResultVo verificationCostFormula(CostAccountPlanFormulaVerificationDto dto);

    /**
     * 核算方案配置的核算指标的配置项
     * @param costAccountPlanConfigNewDto 核算方案配置的核算指标的配置项
     * @return Boolean
     */
    Boolean saveConfigNew(CostAccountPlanConfigNewDto costAccountPlanConfigNewDto);



    CostAccountPlanReviewVo parsePlanConfig(Long planId);



    /**
     * 保存核算方案配置的总公式
     * @param configFormulaDto 核算方案配置的总公式
     * @return Boolean
     */
    Boolean saveFormula(CostAccountPlanConfigFormulaDto configFormulaDto);


    /**
     * 获取任务有用的配置
     * @param groupCodes 方案核算组
     * @param planId 方案id
     * @return CostPlanCalculateInfo
     */
    List<CostPlanCalculateInfo> getGroupUsefulConfig(List<String> groupCodes, Long planId);


    /**
     * 获取任务有用的配置
     * @param accountIds 方案核算组
     * @param planId 方案id
     * @return CostPlanCalculateInfo 配置信息
     */
    List<CostPlanCalculateInfo> getCustomUsefulConfig(List<Long> accountIds, Long planId);

    IPage<CostAccountPlanConfigVo> newListConfig(CostAccountPlanConfigQueryDto queryDto);

    Boolean deleteConfig(Long id);
}
