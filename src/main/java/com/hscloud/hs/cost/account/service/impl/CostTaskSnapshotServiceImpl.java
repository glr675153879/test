package com.hscloud.hs.cost.account.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hscloud.hs.cost.account.mapper.CostTaskSnapshotMapper;
import com.hscloud.hs.cost.account.model.entity.CostTaskSnapshot;
import com.hscloud.hs.cost.account.service.CostTaskSnapshotService;
import org.springframework.stereotype.Service;


/**
 * @author Admin
 */
@Service("costTaskSnapshotService")
public class CostTaskSnapshotServiceImpl extends ServiceImpl<CostTaskSnapshotMapper, CostTaskSnapshot> implements CostTaskSnapshotService {

}