package com.hscloud.hs.cost.account.service.impl.kpi;

import groovy.transform.AutoImplement;
import lombok.RequiredArgsConstructor;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hscloud.hs.cost.account.mapper.kpi.KpiMemberMapper;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiMember;
import com.hscloud.hs.cost.account.service.kpi.IKpiMemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;

/**
*  服务实现类
*
*/
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class KpiMemberService extends ServiceImpl<KpiMemberMapper, KpiMember> implements IKpiMemberService {

    @Autowired
    private KpiMemberMapper kpiMemberMapper;
    /**
     * 批量插入 仅适用于mysql
     *
     * @param entityList 实体列表
     * @return 影响行数
     */
    public Integer insertBatchSomeColumn(Collection<KpiMember> entityList){
        return kpiMemberMapper.insertBatchSomeColumn(entityList);
    }
}
