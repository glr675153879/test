package com.hscloud.hs.cost.account.constant.enums;


import lombok.Getter;

/**
 * 监测值设置参数
 * @author Admin
 */
@Getter
public enum CostMonitorSetStatusEnum {
    /**
     *
     */
    NOT_SETTLE("0", "未设置"),
    SETTLED("1", "已设置");

    private final String code;

    private final String desc;

    CostMonitorSetStatusEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

}
