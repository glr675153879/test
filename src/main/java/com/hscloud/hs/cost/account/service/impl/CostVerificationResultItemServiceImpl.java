package com.hscloud.hs.cost.account.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hscloud.hs.cost.account.mapper.CostVerificationResultItemMapper;
import com.hscloud.hs.cost.account.model.entity.CostVerificationResultItem;
import com.hscloud.hs.cost.account.service.CostVerificationResultItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author 小小w
 * @date 2023/11/7 16:53
 */

@Service
@RequiredArgsConstructor
@Slf4j
public class CostVerificationResultItemServiceImpl extends ServiceImpl<CostVerificationResultItemMapper, CostVerificationResultItem> implements CostVerificationResultItemService {
}
