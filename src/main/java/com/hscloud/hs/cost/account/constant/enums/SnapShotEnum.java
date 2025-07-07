package com.hscloud.hs.cost.account.constant.enums;

public enum SnapShotEnum {

    UNIT("unit","核算单元"),
    PLAN("plan","方案");

    private final String code;

    private final String name;

    SnapShotEnum(String code, String name) {
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
