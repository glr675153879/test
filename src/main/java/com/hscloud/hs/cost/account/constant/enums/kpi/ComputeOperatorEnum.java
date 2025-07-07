package com.hscloud.hs.cost.account.constant.enums.kpi;

import lombok.Getter;

/**
 * 计算符号枚举
 */
@Getter
public enum ComputeOperatorEnum {
    ADD("add", "加"),
    SUB("sub", "减"),
    MUL("mul", "乘"),
    DIV("div", "除"),
    EQ("eq", "等于"),
    GT("gt", "大于"),
    LT("lt", "小于"),
    GE("ge", "大于等于"),
    LE("le", "小于等于");

    private final String code;
    private final String desc;

    ComputeOperatorEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static ComputeOperatorEnum getByCode(String code) {
        for (ComputeOperatorEnum operator : ComputeOperatorEnum.values()) {
            if (operator.getCode().equals(code)) {
                return operator;
            }
        }

        throw new IllegalArgumentException("未找到类型为" + code + "的枚举");
    }
}
