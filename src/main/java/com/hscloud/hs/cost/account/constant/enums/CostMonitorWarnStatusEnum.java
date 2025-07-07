package com.hscloud.hs.cost.account.constant.enums;


import lombok.Getter;

/**
 * 监测值设置参数
 * @author Admin
 */
@Getter
public enum CostMonitorWarnStatusEnum {
    /**
     *
     */
    NORMAL("0", "正常"),
    GREATER("1", "超出"),
    LOWER("2", "低于");

    private final String code;

    private final String desc;

    CostMonitorWarnStatusEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

}
