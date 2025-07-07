package com.hscloud.hs.cost.account.constant.enums;

import lombok.Getter;

@Getter
public enum RationStatusNum {

    RISE("RISE","上升"),
    FALL("FALL","下降");


    private String code;
    public String name;

    RationStatusNum(String code, String name) {
        this.code = code;
        this.name = name;
    }
}
