package com.hscloud.hs.cost.account.constant.enums.deptCost;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 核算单元树形类型枚举类
 * @author banana
 * @create 2024-09-24 15:02
 */
@Getter
@AllArgsConstructor
public enum AccountUnitTreeTypeEnum {

    ALL("A", "全部"),
    ACCOUNTTYPE("B", "核算类型"),
    ACCOUNTUNIT("C", "核算单元");

    private String code;

    private String desc;

}
