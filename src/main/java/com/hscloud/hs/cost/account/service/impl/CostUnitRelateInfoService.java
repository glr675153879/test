package com.hscloud.hs.cost.account.service.impl;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hscloud.hs.cost.account.mapper.CostUnitRelateInfoMapper;
import com.hscloud.hs.cost.account.mapper.second.GrantUnitLogMapper;
import com.hscloud.hs.cost.account.model.entity.CostUnitRelateInfo;
import com.hscloud.hs.cost.account.model.entity.second.GrantUnitLog;
import com.hscloud.hs.cost.account.service.ICostUnitRelateInfoService;
import com.hscloud.hs.cost.account.service.second.IGrantUnitLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

/**
 * 核算单元关联科室人员 服务实现类
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CostUnitRelateInfoService extends ServiceImpl<CostUnitRelateInfoMapper, CostUnitRelateInfo> implements ICostUnitRelateInfoService {


    @Override
    public List<CostUnitRelateInfo> listByAccountUnitIds(List<Long> accountUnitIds) {
        if (CollectionUtils.isEmpty(accountUnitIds)){
            return Collections.emptyList();
        }
        return list(Wrappers.<CostUnitRelateInfo>lambdaQuery().in(CostUnitRelateInfo::getAccountUnitId, accountUnitIds));
    }
}
