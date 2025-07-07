package com.hscloud.hs.cost.account.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hscloud.hs.cost.account.model.dto.CostAccountTaskCalculateProcessNewDto;
import com.hscloud.hs.cost.account.model.dto.TaskResultQueryDto;
import com.hscloud.hs.cost.account.model.dto.TaskResultQueryNewDto;
import com.hscloud.hs.cost.account.model.entity.CostTaskExecuteResult;
import com.hscloud.hs.cost.account.model.vo.CostAccountTaskResultDetailNewVo;
import com.hscloud.hs.cost.account.model.vo.CostAccountTaskResultDetailVo;
import com.hscloud.hs.cost.account.model.vo.CostAccountTaskResultIndexProcessNewVo;

import java.util.Map;

/**
 * 任务执行结果
 *
 * @author zjd
 * @email zjd@gmail.com
 * @date 2023-09-17 16:49:08
 */
public interface CostTaskExecuteResultService extends IService<CostTaskExecuteResult> {

    /**
     * 任务执行结果详情
     * @param taskResultQueryDto 查询条件
     * @return 任务执行结果详情
     */
    CostAccountTaskResultDetailVo listResult(TaskResultQueryDto taskResultQueryDto);

    CostAccountTaskResultDetailVo newListResult(TaskResultQueryDto taskResultQueryDto);

    CostAccountTaskResultDetailNewVo getDistributionList(TaskResultQueryNewDto dto);

    /**
     * 结果下转,数据小组
     * @param dto
     * @return
     */
    CostAccountTaskResultIndexProcessNewVo  getDistributionNewList(CostAccountTaskCalculateProcessNewDto dto);
}

