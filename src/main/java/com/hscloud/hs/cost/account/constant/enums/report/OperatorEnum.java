package com.hscloud.hs.cost.account.constant.enums.report;

import lombok.Getter;

/**
 * like, =, !=, >, <, >=, <=, in
 */
@Getter
public enum OperatorEnum {
    LIKE("like"),
    EQ("="),
    NE("!="),
    LT("<"),
    GT(">"),
    LE("<="),
    GE(">="),
    IN("in"),
    NOT_IN("not in"),
    IS_NULL("is null"),
    IS_NOT_NULL("is not null"),
    NOT_LIKE("not like"),
    ;

    private final String operator;

    OperatorEnum(String operator) {
        this.operator = operator;
    }

    public static OperatorEnum getByCode(String code) {
        for (OperatorEnum en : OperatorEnum.values()) {
            if (en.toString().equals(code)) {
                return en;
            }
        }
        return null;
    }
}
