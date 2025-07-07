package com.hscloud.hs.cost.account.service.kpi;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hscloud.hs.cost.account.model.dto.kpi.*;
import com.hscloud.hs.cost.account.model.dto.kpi.excel.ExcelKpiCalculateDTO;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiReport;
import com.hscloud.hs.cost.account.model.vo.kpi.KpiReportCodeDTO;
import com.hscloud.hs.cost.account.model.vo.kpi.KpiReportCodeVO;
import com.hscloud.hs.cost.account.model.vo.kpi.KpiReportDetailVO;
import com.hscloud.hs.cost.account.model.vo.kpi.KpiReportVO;

import java.util.List;
import java.util.Map;

/**
* 核算任务表(cost_account_task) 服务接口类
*/
public interface IKpiReportService extends IService<KpiReport> {

    void cu(KpiCodeDTO input);

    void detailCu(KpiReportDetailDTO input);

    IPage<KpiReportVO> getPage(KpiCodePageDTO input);

    IPage<KpiReportDetailVO> getlist(KpiCodeDetailPageDTO input);

    KpiReportCodeVO report(KpiReportCodeDTO input);

    void reportDel(String reportCode);

    List<String> importData(List<ExcelKpiCalculateDTO> excelList,Long taskChildId);

    List<String> importData(Long taskChildId, List<Map<Integer, String>> list,String imputationCode);
}
