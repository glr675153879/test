package com.hscloud.hs.cost.account.service.second;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hscloud.hs.cost.account.model.entity.second.UnitTaskDetailItemWork;
import com.hscloud.hs.cost.account.model.entity.second.UnitTaskProjectDetail;
import com.hscloud.hs.cost.account.model.entity.second.UnitTaskUser;

import java.util.List;

/**
 * 任务科室二次分配工作量系数 服务接口类
 */
public interface IUnitTaskDetailItemWorkService extends IService<UnitTaskDetailItemWork> {

    /**
     * @param detailId
     * @return {@link List }<{@link UnitTaskDetailItemWork }>
     */
    List<UnitTaskDetailItemWork> listByDetailId(Long detailId);

    void delByProjectId(List<Long> projectIds);

    void initUserData(Long taskId, UnitTaskProjectDetail unitTaskProjectDetail, List<UnitTaskUser> userList);

    void delByDetailId(List<Long> detailIds);
}
