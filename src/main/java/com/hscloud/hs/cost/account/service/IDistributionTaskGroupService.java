package com.hscloud.hs.cost.account.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hscloud.hs.cost.account.model.dto.DistributionTaskGroupQueryDto;
import com.hscloud.hs.cost.account.model.entity.DistributionTaskGroup;
import com.hscloud.hs.cost.account.model.vo.TaskResultVo;
import com.pig4cloud.pigx.common.core.util.R;

import java.util.List;

/**
 * <p>
 * 任务分组 服务类
 * </p>
 *
 * @author author
 * @since 2023-11-20
 */
public interface IDistributionTaskGroupService extends IService<DistributionTaskGroup> {

    /**
     *启停用任务分组
     * @param id
     * @param status
     * @return
     */
    Boolean enableTaskGroup(long id, String status);

    IPage<DistributionTaskGroup> listTaskGroup(DistributionTaskGroupQueryDto dto);

    void saveTaskGroup(DistributionTaskGroup distributionTaskGroup);


    List<TaskResultVo> getTaskGroupNames();

    R updateTaskGroup(DistributionTaskGroup distributionTaskGroup);
}
