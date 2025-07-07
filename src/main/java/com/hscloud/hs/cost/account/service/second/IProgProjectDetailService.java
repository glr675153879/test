package com.hscloud.hs.cost.account.service.second;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hscloud.hs.cost.account.model.dto.second.ProgProjectDetailSaveDTO;
import com.hscloud.hs.cost.account.model.entity.second.ProgProject;
import com.hscloud.hs.cost.account.model.entity.second.ProgProjectDetail;

import java.util.List;

/**
* 核算指标明细 服务接口类
*/
public interface IProgProjectDetailService extends IService<ProgProjectDetail> {


    /**
     * 删除发放单元 科室二次 detail ，item也删
     * @param progDetailtId
     */
    void delErciById(Long progDetailtId);


    /**
     * 根据 project 同步 detail
     * @param progProject
     */
    void syncByProject(ProgProject progProject);

    /**
     * 根据 comProject 为 progProject 增出detail
     * @param progProject
     * @param comProject
     */
    void createByProject(ProgProject progProject, ProgProject comProject);

    /**
     * 删除 detail 及下层数据
     * @param progProjectDetailList
     */
    void delByDetailList(List<ProgProjectDetail> progProjectDetailList);

    List<ProgProjectDetail> listByPidCache(String cycle, Long projectId);
}
