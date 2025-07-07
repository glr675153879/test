package com.hscloud.hs.cost.account.model.dto.kpi;

import lombok.Data;

@Data
public class KpiReportConfigIndexDto {
    private String code;
    private String name;
    private String headName;
    // 1,核算项 2 ，指标/分摊
    private String type;
    //是否取上月
    private String lastMonth;
    //是否汇总
    private String sum;
    //{"code":"z_awl","name":"测试","lastMonth":"Y","sum":"Y"}
}
