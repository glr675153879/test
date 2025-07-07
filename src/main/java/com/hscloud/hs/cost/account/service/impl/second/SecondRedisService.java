package com.hscloud.hs.cost.account.service.impl.second;

import com.hscloud.hs.cost.account.constant.CacheConstants;
import com.hscloud.hs.cost.account.model.entity.second.UnitTaskUser;
import com.hscloud.hs.cost.account.service.impl.second.kpi.SecondKpiService;
import com.hscloud.hs.cost.account.utils.DmoUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import java.util.List;
/**
* 二次分配缓存 服务实现类
*
*/
@Service
@RequiredArgsConstructor
public class SecondRedisService {

    private final SecondKpiService secondKpiService;
    @Cacheable(value = CacheConstants.SEC_DEPT_LEADER,key = "#cycle+'_'+#deptIds" ,unless = "#result==null")
    public List<UnitTaskUser> deptLeaderList(String cycle,String deptIds) {
        return secondKpiService.deptLeaderList(cycle, deptIds);
    }

}
