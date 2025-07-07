package com.hscloud.hs.cost.account.constant.enums;

import lombok.Getter;

/**
 * @author banana
 * @create 2023-09-18 17:02
 */
@Getter
public enum CostAccountProportionType {
    //科室单元
    SELFGROUP("KSDYFW001", "GROUP","本科室单元"),
    ALLGROUP("KSDYFW002", "GROUP","所有考核室单元"),
    DOCGROUP("KSDYFW003", "GROUP","医生组科室单元"),
    NURSEGROUP("KSDYFW004", "GROUP","护理组科室单元"),
    MEDICALTECHNOLOGYGROUP("KSDYFW005","GROUP","医技组科室单元"),
    OFFICEGROUP("KSDYFW006", "GROUP","行政组科室单元"),
    CUSTOMGROUP("KSDYFW007", "GROUP","自定义科室单元"),
    DOCNURSEGROUP("KSDYFW010", "GROUP","医护对应科室单元组"),
    REAGENTGROUP("KSDYFW011", "GROUP","药剂组科室单元"),
    //部门
    DEPT("KSDYFW008", "DEPT","自定义科室"),
    //人员
    USER("KSDYFW009", "USER","自定义人员");

    private final String groupArrange;

    private final String type;

    private final String desc;

    CostAccountProportionType(String groupArrange, String type,String desc) {
        this.groupArrange = groupArrange;
        this.type = type;
        this.desc = desc;
    }


    public static String getDescByGroupArrange(String groupArrange) {
        for (CostAccountProportionType value : CostAccountProportionType.values()) {
            if (value.getGroupArrange().equals(groupArrange)) {
                return value.getDesc();
            }
        }
        return null;
    }

}
