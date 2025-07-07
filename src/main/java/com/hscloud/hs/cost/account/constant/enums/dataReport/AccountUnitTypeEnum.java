package com.hscloud.hs.cost.account.constant.enums.dataReport;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author banana
 * @create 2024-12-05 17:52
 */
@Getter
@AllArgsConstructor
public enum AccountUnitTypeEnum {

    KSDY("0", "科室单元"),
    GJDY("1", "归集单元"),
    HSRY("2", "核算人员");

    private String val;
    private String desc;

}
