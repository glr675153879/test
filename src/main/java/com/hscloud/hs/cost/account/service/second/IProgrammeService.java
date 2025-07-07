package com.hscloud.hs.cost.account.service.second;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hscloud.hs.cost.account.model.dto.second.ProgrammePublishDTO;
import com.hscloud.hs.cost.account.model.entity.second.Programme;
import com.hscloud.hs.cost.account.model.vo.second.ProgrammeInfoVo;

/**
* 核算方案 服务接口类
*/
public interface IProgrammeService extends IService<Programme> {

    Programme getByUnitId(Long unitId);

    /**
     * 方案发布
     * @param programmePublishDTO
     * @return
     */
    void publish(ProgrammePublishDTO programmePublishDTO);

    /**
     * 复制方案
     * @param programmeId
     */
    void copy(Long programmeId);

    /**
     * 方案详情
     * @param id
     * @return
     */
    ProgrammeInfoVo getProgrammeInfo(Long id);

    /**
     * 是否 同个发放单元有 多个启用的方案
     * 有：是
     * @param programme
     * @return
     */
    Boolean validGrantUnit(Programme programme);

    /**
     * 根据公共方案 同步发放单元方案
     * @param progCommon
     */
    void syncByProgCommon(Programme progCommon);

    /**
     * 开启 禁用方案，同步
     * @param programme
     * @param status
     */
    void startStatus(Programme programme, String status);

    /**
     * 删除公共方案，同步删除发放单元方案
     * @param id
     */
    void deleteById(Long id);
}
