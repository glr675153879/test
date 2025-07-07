package com.hscloud.hs.cost.account.constant.enums;


import lombok.Getter;

/**
 * @author Admin
 */
@Getter
public enum FieldEnum {
    /**
     *
     */
    DATE("date", "日期"),
    NUMBER("number", "数字"),
    STRING("string", "字符串"),
    BOOLEAN("boolean", "布尔值"),
    PARAM("param", "参数字段")

    ;

    private final String code;

    private final String desc;

    FieldEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

}
