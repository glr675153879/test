package com.hscloud.hs.cost.account.service.second;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hscloud.hs.cost.account.model.entity.second.SecondTaskCount;
import com.hscloud.hs.cost.account.model.vo.second.ProgrammeInfoVo;
import com.hscloud.hs.cost.account.model.vo.second.SecondDetailCountVo;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
* 二次分配结果按人汇总 服务接口类
*/
public interface ISecondTaskCountService extends IService<SecondTaskCount> {

    void doCount(Long secondTaskId, Long unitTaskId);

    @Transactional(rollbackFor = Exception.class)
    void doCountAll(Long secondTaskId);

    List<SecondDetailCountVo> detailUserList(Long secondTaskId, Long unitTaskId, String empCode);

    List<ProgrammeInfoVo> detailTitleList(Long secondTaskId,Long unitTaskId, String empCode);

    /**
     * 重新计算某人的 secCount
     * @param secondTaskId
     * @param empCode
     */
    void doCountByEmpCode(Long secondTaskId, String empCode);
}
