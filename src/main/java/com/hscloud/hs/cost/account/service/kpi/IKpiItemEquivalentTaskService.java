package com.hscloud.hs.cost.account.service.kpi;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hscloud.hs.cost.account.model.dto.kpi.KpiItemEquivalentTaskDTO;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiItemEquivalentTask;
import com.hscloud.hs.cost.account.model.vo.kpi.KpiItemEquivalentTaskVO;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * 当量核验任务 服务接口类
 */
public interface IKpiItemEquivalentTaskService extends IService<KpiItemEquivalentTask> {
    /**
     * 下发任务
     *
     * @param dto 任务信息
     */
    void issueTask(KpiItemEquivalentTaskDTO dto);

    /**
     * 提交任务
     *
     * @param accountUnitId 科室ID
     * @param period        周期
     */
    void commitTask(Long accountUnitId, Long period);

    /**
     * 审批任务
     *
     * @param dto
     */
    void approveTask(KpiItemEquivalentTaskDTO dto);

    /**
     * 科室核验任务列表
     *
     * @param dto 任务信息
     */
    List<KpiItemEquivalentTaskVO> getList(KpiItemEquivalentTaskDTO dto, boolean isAdmin);

    /**
     * @param period 周期
     * @return 审批状态统计
     */
    Map<String, Long> statusCount(Long period);

    void reIssueTask(Long period, List<Long> itemIds, Long accountUnitId);

    /**
     * @param period 周期
     * @param accountUnitId 科室ID
     * @return 科室任务下当量状态统计
     */
    Map<String, Long> unitTaskStatusCount(Long period, Long accountUnitId);
}
