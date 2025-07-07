package com.hscloud.hs.cost.account.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hscloud.hs.cost.account.model.dto.CostAccountTaskCalculateProcessDto;
import com.hscloud.hs.cost.account.model.dto.CostAccountTaskCalculateTotalProcessDto;
import com.hscloud.hs.cost.account.model.dto.CostAccountTaskNewDto;
import com.hscloud.hs.cost.account.model.dto.CostAccountTaskQueryDto;
import com.hscloud.hs.cost.account.model.entity.CostAccountTask;
import com.hscloud.hs.cost.account.model.entity.DistributionTaskGroup;
import com.hscloud.hs.cost.account.model.vo.*;

import java.text.ParseException;
import java.util.List;

/**
 * @author Admin
 */
public interface CostAccountTaskService extends IService<CostAccountTask> {


    /**
     * 保存任务
     *
     * @param costAccountTask 任务对象
     * @return 是否成功
     */
    Boolean saveTask(CostAccountTask costAccountTask) throws ParseException;

    /**
     * 任务列表
     *
     * @param costAccountTaskQueryDto
     * @return
     */
    IPage<CostAccountTaskVo> listAccountTask(CostAccountTaskQueryDto costAccountTaskQueryDto);

    /**
     * 任务详情
     *
     * @param id
     * @return
     */
    TaskDetailVo getDetail(Long id);


    /**
     * 计算过程总核算值
     *
     * @param dto
     * @return
     */
    CostAccountTaskResultTotalValueVo getTotalProcess(CostAccountTaskCalculateTotalProcessDto dto);

    /**
     * 计算过程详情页
     *
     * @param costAccountTaskCalculateProcessDto
     * @return
     */
    Object getCalculationProcess(CostAccountTaskCalculateProcessDto costAccountTaskCalculateProcessDto);

    CostAccountTaskResultTotalValueVo getNewTotalProcess(CostAccountTaskCalculateTotalProcessDto dto);

    CostAccountTaskResultIndexProcessVo getNewCalculationProcess(CostAccountTaskCalculateProcessDto costAccountTaskCalculateProcessDto);

    List<TaskGroupIdsVo> updateTask(CostAccountTaskNewDto dto);

    Long saveTaskNew(CostAccountTaskNewDto dto);

    List<DistributionTaskGroup> listTaskGroup(Long id);

    /**
     * 任务详情(新)
     * @param
     * @return
     */
    TaskDetailVo getDetailNew(Long taskId,Long taskGroupId);

    /**
     * 根据id查询任务
     * @param id
     * @return
     */
    CostAccountTaskNewVo getTaskById(Long id);

    List<DistributionTaskGroup> getTaskGroup(Long id);

    Boolean deleteTask(Long id);
}
