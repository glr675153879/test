package com.hscloud.hs.cost.account.constant.enums;

import lombok.Getter;

/**
 * @author Admin
 */
@Getter
public enum TableEnum {

    /**
     * 表枚举
     */
    SYS_DEPT("sys_dept", "部门表"),
    SYS_USER("sys_user", "用户表");

    private final String code;

    private final String desc;

    TableEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
