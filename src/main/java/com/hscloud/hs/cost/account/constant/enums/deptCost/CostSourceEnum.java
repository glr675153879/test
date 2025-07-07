package com.hscloud.hs.cost.account.constant.enums.deptCost;

import lombok.Getter;

import java.util.Objects;

/**
 * @author tianbo
 * @version 1.0
 * @date 2024-09-20 11:24
 **/
@Getter
public enum CostSourceEnum {
    DEPT_AREA("DEPT_AREA", "科室面积"),
    SOFTWARE("SOFTWARE", "软件信息维护");

    private final String name;
    private final String code;

    CostSourceEnum(String code, String name) {
        this.name = name;
        this.code = code;
    }

    public static CostSourceEnum getByCode(String code) {
        for (CostSourceEnum en : CostSourceEnum.values()) {
            if (Objects.equals(en.getCode(), code)) {
                return en;
            }
        }
        return null;
    }
}
