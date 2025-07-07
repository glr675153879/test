package com.hscloud.hs.cost.account.service.impl.second;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hscloud.hs.cost.account.mapper.second.UnitTaskDetailCountMapper;
import com.hscloud.hs.cost.account.model.entity.second.UnitTaskDetailCount;
import com.hscloud.hs.cost.account.service.second.IUnitTaskDetailCountService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
* 科室二次分配detail结果按人汇总 服务实现类
*
*/
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UnitTaskDetailCountService extends ServiceImpl<UnitTaskDetailCountMapper, UnitTaskDetailCount> implements IUnitTaskDetailCountService {


    @Override
    public List<UnitTaskDetailCount> listByDetailId(Long detailId) {
        return this.list(Wrappers.<UnitTaskDetailCount>lambdaQuery().eq(UnitTaskDetailCount::getDetailId,detailId));
    }

    @Override
    public List<UnitTaskDetailCount> listByProjectId(Long projectId) {
        return this.list(Wrappers.<UnitTaskDetailCount>lambdaQuery().eq(UnitTaskDetailCount::getProjectId,projectId));
    }

}
