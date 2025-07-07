package com.hscloud.hs.cost.account.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hscloud.hs.cost.account.model.dto.CostAccountStatementQueryNewDto;
import com.hscloud.hs.cost.account.model.entity.CostTaskExecuteResult;
import com.hscloud.hs.cost.account.model.entity.DistributionStatementTargetValue;
import com.hscloud.hs.cost.account.model.pojo.AdsIncomePerformancePay;
import com.hscloud.hs.cost.account.model.vo.CostResultStatementVo;
import com.hscloud.hs.cost.account.model.vo.DistributionResultStatementDetailVo;
import com.hscloud.hs.cost.account.model.vo.DistributionResultStatementFigureVo;
import com.hscloud.hs.cost.account.model.vo.DistributionResultStatementVo;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author 小小w
 * @date 2023/11/30 15:51
 */
public interface IDistributionAccountStatementService extends IService<CostTaskExecuteResult> {
    /**
     * * 新增报表目标值
     *
     * @param distributionStatementTargetValue
     */
    void addTargetValue(DistributionStatementTargetValue distributionStatementTargetValue);

    /**
     * 医院绩效总值报表
     *
     * @param queryDto
     * @return
     */
    IPage<DistributionResultStatementVo> hospitalStatementPage(CostAccountStatementQueryNewDto queryDto);

    /**
     * 医院绩效总值报表趋势
     *
     * @param queryDto
     * @return
     */
    List<DistributionResultStatementVo> getHospitalTendency(CostAccountStatementQueryNewDto queryDto);

    /**
     * 医院绩效总值报表饼状图
     *
     * @param accountTime
     * @return
     */
    DistributionResultStatementDetailVo.detail getHospitalFigure(String accountTime);

    /**
     * 医院绩效总值报表饼详情
     *
     * @param accountTime
     * @return
     */
    DistributionResultStatementDetailVo getHospitalDetail(String accountTime);

    /**
     * 科室绩效总值报表
     *
     * @param queryDto
     * @return
     */
    IPage<DistributionResultStatementVo> unitStatementPage(CostAccountStatementQueryNewDto queryDto);

    /**
     * 科室绩效总值趋势
     *
     * @param queryDto
     * @return
     */
    List<List<AdsIncomePerformancePay>> getUnitTendency(CostAccountStatementQueryNewDto queryDto);

    BigDecimal getUnitTotalCount(CostAccountStatementQueryNewDto queryDto);
}
