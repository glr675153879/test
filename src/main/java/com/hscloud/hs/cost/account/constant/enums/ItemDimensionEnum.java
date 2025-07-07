package com.hscloud.hs.cost.account.constant.enums;

import lombok.Getter;

/**
 * @author Admin
 */
@Getter
public enum ItemDimensionEnum {


    /**
     * sql统计维度
     */
    DEPT("HSDX004", "科室"),
    USER("HSDX003", "人员"),
    DEPT_UNIT("HSDX001", "科室单元");


    private final String code;

    private final String desc;

    ItemDimensionEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
