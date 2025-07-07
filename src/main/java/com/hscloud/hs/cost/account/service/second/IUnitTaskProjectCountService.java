package com.hscloud.hs.cost.account.service.second;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hscloud.hs.cost.account.model.entity.second.UnitTaskProject;
import com.hscloud.hs.cost.account.model.entity.second.UnitTaskProjectCount;
import com.hscloud.hs.cost.account.model.entity.second.UnitTaskProjectDetail;
import com.hscloud.hs.cost.account.model.vo.second.UnitTaskDetailItemVo;
import com.hscloud.hs.cost.account.model.vo.second.UnitTaskProjectCountVo;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
* 核算指标分配结果按人汇总 服务接口类
*/
public interface IUnitTaskProjectCountService extends IService<UnitTaskProjectCount> {

    /**
     * 重算所有任务数据+count数据
     * @param unitTaskId
     */
    void doCount(Long unitTaskId);

    List<UnitTaskProjectCountVo> userList(Long unitTaskId);

    /**
     * 同步projectName
     */
    void syncProjectName(List<UnitTaskProject> updateList);

    Map<String, BigDecimal> getUserPointMap(UnitTaskProjectDetail detail, List<UnitTaskDetailItemVo> voList);
}
