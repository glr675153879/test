package com.hscloud.hs.cost.account.constant.enums;


import lombok.Getter;

/**
 * 监测值设置参数
 * @author Admin
 */
@Getter
public enum CostMonitorCenterStatusEnum {
    /**
     *
     */
    NORMAL("0", "正常"),
    ABNORMAL("1", "异常");

    private final String code;

    private final String desc;

    CostMonitorCenterStatusEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

}
