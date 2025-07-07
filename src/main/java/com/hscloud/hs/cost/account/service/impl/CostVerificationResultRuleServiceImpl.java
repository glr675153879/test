package com.hscloud.hs.cost.account.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hscloud.hs.cost.account.mapper.CostVerificationResultRuleMapper;
import com.hscloud.hs.cost.account.model.entity.CostVerificationResultRule;
import com.hscloud.hs.cost.account.service.CostVerificationResultRuleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author 小小w
 * @date 2023/10/31 13:36
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CostVerificationResultRuleServiceImpl extends ServiceImpl<CostVerificationResultRuleMapper, CostVerificationResultRule> implements CostVerificationResultRuleService {
}
