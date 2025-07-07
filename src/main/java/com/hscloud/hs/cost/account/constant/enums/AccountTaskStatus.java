package com.hscloud.hs.cost.account.constant.enums;

public enum AccountTaskStatus {



    TO_BE_SUBMITTED("TO_BE_SUBMITTED","待提交"),
    CALCULATING("CALCULATING","计算中"),
    EXCEPTION("EXCEPTION","计算异常"),
    PENDING("PENDING","待发起"),
    COMPLETED("COMPLETED","完成");

    private final String code;

    private final String name;

    AccountTaskStatus(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }
}