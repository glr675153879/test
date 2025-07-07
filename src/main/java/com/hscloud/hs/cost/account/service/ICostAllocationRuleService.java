package com.hscloud.hs.cost.account.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hscloud.hs.cost.account.model.dto.CostAllocationRuleDto;
import com.hscloud.hs.cost.account.model.dto.CostAllocationRuleQueryDto;
import com.hscloud.hs.cost.account.model.dto.CostAllocationRuleStatusDto;
import com.hscloud.hs.cost.account.model.dto.CostAllocationRuleVerificationDto;
import com.hscloud.hs.cost.account.model.entity.CostAllocationRule;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hscloud.hs.cost.account.model.vo.ValidatorResultVo;
import com.hscloud.hs.cost.account.model.vo.CostAllocationRuleVo;

import java.util.Map;

/**
 * <p>
 * 分摊规则表 服务类
 * </p>
 *
 * @author
 * @since 2023-09-11
 */
public interface ICostAllocationRuleService extends IService<CostAllocationRule> {
    /**
     * 保存或更新分摊规则
     *
     * @param dto
     */
    void saveOrUpdateAccountRule(CostAllocationRuleDto dto);

    /**
     * 启停用分摊规则
     *
     * @param dto
     */
    Boolean updateStatusAllocationRule(CostAllocationRuleStatusDto dto);

    IPage getAllocationRulePage(CostAllocationRuleQueryDto queryDto);

    /**
     * 分摊规则校验
     *
     * @param dto
     * @return
     */
    ValidatorResultVo verificationAllocationRule(CostAllocationRuleVerificationDto dto);

    CostAllocationRuleVo getAllocationRuleById(Long id);

    /**
     *  分摊规则计算
     *  @param map       分摊规则计算参数
     * @param id        分摊规则id
     * @param startTime 开始时间
     * @param endTime    结束时间
     * @param objectId   核算对象id
     * @return
     */
    ValidatorResultVo getAllocationRuleData(Map<String,Double> map, Long id, String startTime, String endTime, String objectId, String dimension);
}
