package com.hscloud.hs.cost.account.constant.enums;

import lombok.Getter;

/**
 * @author Admin
 */

@Getter
public enum CalculateEnum {

    /**
     * 计算类型
     */
    ITEM("item","核算项"),
    INDEX("index","核算指标");

    private final String type;

    private final String desc;

    CalculateEnum(String type, String desc) {
        this.type = type;
        this.desc = desc;
    }


}
