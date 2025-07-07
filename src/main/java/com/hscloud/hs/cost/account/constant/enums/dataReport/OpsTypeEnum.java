package com.hscloud.hs.cost.account.constant.enums.dataReport;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

/**
 * 变更日志操作枚举类
 * @author banana
 * @create 2024-09-19 11:23
 */
@Getter
@AllArgsConstructor
public enum OpsTypeEnum {

    ADD("1", "新增"),
    UPDATE("2", "变更"),
    DEL("3", "删除"),
    ENABLE("4", "启停用");

    private String val;

    private String desc;


    /**
     * 根据val值获取对应的枚举类
     * @param val val值
     * @return 对应枚举类
     */
    public static OpsTypeEnum getByVal(String val) {
        OpsTypeEnum[] values = values();
        for (OpsTypeEnum value : values) {
            if(value.getVal().equals(val)) {
                return value;
            }
        }
        return null;
    }



}
