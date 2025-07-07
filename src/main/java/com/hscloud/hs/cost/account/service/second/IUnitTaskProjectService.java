package com.hscloud.hs.cost.account.service.second;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hscloud.hs.cost.account.model.dto.second.SecStartDataDTO;
import com.hscloud.hs.cost.account.model.entity.second.*;

import java.util.List;

/**
* 任务核算指标 服务接口类
*/
public interface IUnitTaskProjectService extends IService<UnitTaskProject> {

    /**
     * 根据发放单元方案 新增project
     *
     * @param unitTask
     * @param programme
     */
    void createByProg(UnitTask unitTask, Programme programme);

    /**
     * 为人员 初始化所有考核指标数据
     * @param unitTask
     * @param addList
     */
    void initUserData(UnitTask unitTask, List<UnitTaskUser> addList);

    /**
     * 根据发放单元方案 同步任务数据
     * @param unitTask
     */
    void syncByUnitTask(UnitTask unitTask);

    /**
     * 根据方案 ，在任务下新增project
     * @param unitTask
     * @param progProjectList
     */
    void createByProgProjectList(UnitTask unitTask, List<ProgProject> progProjectList);

    /**
     * 分配结果明细
     * @param unitTask
     * @param empCode
     * @return
     */
    List<UnitTaskProject> detailCountByUser(UnitTask unitTask, String empCode);

    /**
     * 删除某人的业务数据 detail 和 detailItem
     * @param unitTaskUser
     */
    void removeByUserId(UnitTaskUser unitTaskUser);

    List<UnitTaskProject> listByUnitTask(Long unitTaskId);

    List<UnitTaskProject> listByUnitTaskIds(List<Long> taskIds);
}
