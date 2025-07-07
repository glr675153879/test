package com.hscloud.hs.cost.account.constant.enums.kpi;

import lombok.Getter;

@Getter
public enum EquivalentDistributeEnum {
    AVERAGE("0", "平均分配"),
    COEFFICIENT("1", "系数分配"),
    CUSTOM("2", "自定义分配");

    private final String code;
    private final String desc;

    EquivalentDistributeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static EquivalentDistributeEnum getByCode(String code) {
        for (EquivalentDistributeEnum distribute : EquivalentDistributeEnum.values()) {
            if (distribute.getCode().equals(code)) {
                return distribute;
            }
        }

        throw new IllegalArgumentException("未找到类型为" + code + "的枚举");
    }
}
