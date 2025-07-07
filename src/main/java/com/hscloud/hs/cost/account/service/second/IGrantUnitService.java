package com.hscloud.hs.cost.account.service.second;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hscloud.hs.cost.account.model.entity.second.GrantUnit;

import java.util.Set;

/**
* 发放单元 服务接口类
*/
public interface IGrantUnitService extends IService<GrantUnit> {

    void init();

    Boolean ifInit();

    void updateLog(GrantUnit grantUnitDB, GrantUnit grantUnit);

    Set<String> managerUnits(Long currentUserId);

    /**
     * 获取 管辖的deptids
     * @param grantUnitId
     * @return
     */
    String getDeptIds(Long grantUnitId);
}
