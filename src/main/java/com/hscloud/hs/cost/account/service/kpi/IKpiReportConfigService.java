package com.hscloud.hs.cost.account.service.kpi;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hscloud.hs.cost.account.model.dto.kpi.*;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiConfig;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiReportConfig;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiReportConfigImport;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiReportYearImport;
import com.hscloud.hs.cost.account.model.vo.kpi.KpiCalculateReportVO;
import com.hscloud.hs.cost.account.model.vo.kpi.KpiCalculateReportVO2;

import java.util.List;
import java.util.Map;

/**
* 报表多选配置 服务接口类
*/
public interface IKpiReportConfigService extends IService<KpiReportConfig> {

    void enable(KpiIndexEnableDto input);

    KpiCalculateReportVO2 report(KpiReportConfigDto input);

    List<KpiReportYearImport> yearReport(KpiReportYearDto input);

    List<KpiReportYearImport> yearPowerReport(KpiReportYearDto input);

    KpiCalculateReportVO reportId(KpiCalculateReportDTO2 input);

    KpiCalculateReportVO2 reportSecond(KpiReportConfigDto input);

    void powerEd(KpiReportConfigPowerDto input);

    List<KpiReportConfigPowerListDTO> powerDetail(Long reportId);

    List<KpiReportConfig> powerLeft();

    List<KpiConfig> sendList(Long input);

    List<KpiReportConfigImport> importData(Long taskChildId, Long reportId, List<Map<Integer, String>> list,boolean isYearImport,Long period);

    void yearImportData(Long taskChildId, Long reportId, List<Map<Integer, String>> list);

    List<KpiReportConfigListDTO> getList(String group, String type, String name, Long taskChildId,String status);

}
