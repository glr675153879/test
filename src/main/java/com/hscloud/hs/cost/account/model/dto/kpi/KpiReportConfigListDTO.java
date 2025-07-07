package com.hscloud.hs.cost.account.model.dto.kpi;

import com.hscloud.hs.cost.account.model.entity.kpi.KpiReportConfig;
import lombok.Data;

@Data
public class KpiReportConfigListDTO extends KpiReportConfig {
    private String groupName;
}
