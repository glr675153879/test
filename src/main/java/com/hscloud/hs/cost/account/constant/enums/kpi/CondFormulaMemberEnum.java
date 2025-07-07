package com.hscloud.hs.cost.account.constant.enums.kpi;

public enum CondFormulaMemberEnum {

    BRKS("-100","病人科室","brks"),
    ZDYSKS("-101","主刀医生科室","zdysks"),
    KZYSKS("-102","开嘱医生科室","kzysks"),
    ZDYS("-201","主刀医生","zdys"),
    KZR("-202","开嘱人","kzys")
    ;

    private final String type;
    private final String name;
    private final String key;

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public String getKey() {
        return key;
    }

    CondFormulaMemberEnum(String type, String name, String key) {
        this.type = type;
        this.name = name;
        this.key = key;
    }
    public static CondFormulaMemberEnum find(String type) {
        for (CondFormulaMemberEnum e : CondFormulaMemberEnum.values()) {
            if (e.getType().equals(type)) {
                return e;
            }
        }
        return null;
    }

}
