package com.hscloud.hs.cost.account.service.kpi;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hscloud.hs.cost.account.model.dto.kpi.*;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiAccountTask;
import com.hscloud.hs.cost.account.model.vo.kpi.KpiAccountTaskListVO;
import com.hscloud.hs.cost.account.model.vo.kpi.KpiCalculateReportVO;
import com.hscloud.hs.cost.account.model.vo.kpi.KpiTaskReportLeftVO;

import java.util.List;

/**
* 核算任务表(cost_account_task) 服务接口类
*/
public interface IKpiAccountTaskService extends IService<KpiAccountTask> {

    List<KpiAccountTaskListVO> list(KpiAccountTaskListDto input);

    IPage<KpiAccountTaskListVO> getPage(KpiAccountTaskListDto input);

    void saveOrUpdate(KpiAccountTaskAddDto dto);

    void del(Long id);

    void issued(IssuedDTO input);

    void unlock(Long id);

    String log(Long taskChildId);

    String send_log(Long taskId);

    String logErro(Long taskChildId);

    KpiTaskReportLeftVO reportLeft(Long taskChildId);

    KpiCalculateReportVO reportCalculate(KpiCalculateReportDTO input);
    KpiCalculateReportVO reportCalculate2(KpiCalculateReportDTO input);

    KpiCalculateReportVO reportDetail(KpiCalculateDetailDTO input);
    KpiCalculateReportVO reportDetail2(KpiCalculateDetailDTO input);

    void saveTest(KpiAccountTaskTestAddDto dto);

    List<KpiReportAlloValue> reportAlloValue(KpiCalculateDetailDTO input);

    void reportConfig(ReportConfigCopyDTO input);

    void change(KpiConfigChangeDTO dto);
}
