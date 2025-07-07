package com.hscloud.hs.cost.account.constant.enums.dataReport;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 数据上报中心业务类型
 * @author banana
 * @create 2024-09-20 14:08
 */
@Getter
@AllArgsConstructor
public enum ReportRecordTypeEnum {

    KPI("0", "数据上报中心-绩效"),
    DEPT_COST("1", "数据上报中心-科室成本");

    private String val;

    private String desc;

}
