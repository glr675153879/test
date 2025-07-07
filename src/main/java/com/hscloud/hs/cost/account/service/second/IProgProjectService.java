package com.hscloud.hs.cost.account.service.second;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hscloud.hs.cost.account.model.entity.second.ProgProject;
import com.hscloud.hs.cost.account.model.entity.second.Programme;

import java.util.List;

/**
* 核算指标 服务接口类
*/
public interface IProgProjectService extends IService<ProgProject> {

    /**
     * 根据programme同步project
     * @param programme
     */
    void syncByProgramme(Programme programme);

    /**
     * 删除projectL及下层数据
     * @param delList
     */
    void delByProjectList(List<ProgProject> delList);

    List<ProgProject> listByPidCache(String cycle, Long programmeId);
}
