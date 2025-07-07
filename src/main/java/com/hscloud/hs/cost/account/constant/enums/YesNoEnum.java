package com.hscloud.hs.cost.account.constant.enums;

import lombok.Getter;

/**
 * @author Admin
 */

@Getter
public enum YesNoEnum {


    /**
     * 是否枚举
     */
    YES("Y", "1", "是"),
    NO("N", "0", "否");

    private final String code;


    private final String value;

    private final String desc;

    YesNoEnum(String code, String value, String desc) {
        this.code = code;
        this.value = value;
        this.desc = desc;
    }
}
