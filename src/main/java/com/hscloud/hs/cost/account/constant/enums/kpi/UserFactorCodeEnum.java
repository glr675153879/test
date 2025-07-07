package com.hscloud.hs.cost.account.constant.enums.kpi;

/**
 * @Classname ErrorCodeEnum
 * @Description TODO
 * @Date 2025/4/16 13:55
 * @Created by sch
 */

public enum UserFactorCodeEnum {
    USER("user", "人员"),
    OFFICE("office", "职务这类大分组字典"),
    COEFFICIENT("coefficient", "系数"),
    SUBSIDY("subsidy", "补贴");
    private final String code;

    private final String description;

    UserFactorCodeEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}


