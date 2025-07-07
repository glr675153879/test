package com.hscloud.hs.cost.account.constant.enums;

import lombok.Getter;

/**
 * @author Admin
 */
@Getter
public enum UnitMapEnum {

    /**
     * 科室单元映射枚举
     */
    DOCKER("HSDX001", "KSDYFW003","医生组"),
    NURSE("HSDX002", "KSDYFW004","护理组"),
    ADMINISTRATION("HSDX003", "KSDYFW006","行政组"),
    MEDICAL_SKILL("HSDX004", "KSDYFW005","医技组"),
    LOGISTICS("HSDX005", null,"后勤组"),
    REAGENT("HSDX006", null,"药剂组"),
    CUSTOM(null, "KSDYFW007","自定义");

    private final String unitGroup;

    private final String  planGroup;

    private final String desc;

    UnitMapEnum(String unitGroup, String planGroup, String desc) {
        this.unitGroup = unitGroup;
        this.planGroup = planGroup;
        this.desc = desc;
    }

    public static String getPlanGroup(String unitGroup) {
        for (UnitMapEnum unitMapEnum : UnitMapEnum.values()) {
            if (unitMapEnum.getUnitGroup().equals(unitGroup)) {
                return unitMapEnum.getPlanGroup();
            }
        }
        return null;
    }

    public static String getUnitGroup(String planGroup) {
        for (UnitMapEnum unitMapEnum : UnitMapEnum.values()) {
            if (unitMapEnum.getPlanGroup().equals(planGroup)) {
                return unitMapEnum.getUnitGroup();
            }
        }
        return null;
    }

    public static String getDesc(String planGroup) {
        for (UnitMapEnum unitMapEnum : UnitMapEnum.values()) {
            if (unitMapEnum.getPlanGroup().equals(planGroup)) {
                return unitMapEnum.getDesc();
            }
        }
        return "";
    }

}
