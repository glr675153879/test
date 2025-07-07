package com.hscloud.hs.cost.account.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hscloud.hs.cost.account.mapper.CostVerificationResultIndexMapper;
import com.hscloud.hs.cost.account.model.entity.CostVerificationResultIndex;
import com.hscloud.hs.cost.account.service.CostVerificationResultIndexService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author 小小w
 * @date 2023/10/26 14:32
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CostVerificationResultIndexServiceImpl extends ServiceImpl<CostVerificationResultIndexMapper, CostVerificationResultIndex> implements CostVerificationResultIndexService {
}
