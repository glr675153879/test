package com.hscloud.hs.cost.account.constant.enums;

import lombok.Getter;

@Getter
public enum DimensionEnum {

    MONTH("MONTH", "月份"),
    YEAR("YEAR", "年"),
    QUARTER("QUARTER", "季度"),
    DAY("DAY", "天");

    private final String code;

    private final String desc;

    DimensionEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
