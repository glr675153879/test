package com.hscloud.hs.cost.account.constant.enums;

import lombok.Getter;

@Getter
public enum AccountRangeEnum {
    THIS_DEPT_UNIT("KSDYFW001","本科室单元"),
    DOCTOR_NURSE_DEPT_UNIT("KSDYFW010","医护对应科室单元组"),
    CUSTOM_DEPT_UNIT( "KSDYFW007","自定义科室单元"),
    CUSTOM_DEPT( "KSDYFW008","自定义科室"),
    CUSTOM_PEOPLE( "KSDYFW009","自定义人员"),
    ALL("HSWD005","全院");

    private final String planGroup;

    private final String desc;

    AccountRangeEnum(String planGroup, String desc) {
        this.planGroup = planGroup;
        this.desc = desc;
    }

    public static AccountRangeEnum fromPlanGroup(String planGroup) {
        for (AccountRangeEnum rangeEnum : AccountRangeEnum.values()) {
            if (rangeEnum.getPlanGroup().equals(planGroup)) {
                return rangeEnum;
            }
        }
        return null;
    }
}
