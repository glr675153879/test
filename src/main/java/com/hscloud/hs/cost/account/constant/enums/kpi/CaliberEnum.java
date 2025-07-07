package com.hscloud.hs.cost.account.constant.enums.kpi;

public enum CaliberEnum {
    /**
     * 口径颗粒度
     */
    PEOPLE("1", "人"),
    DEPT("2","科室"),
    IMPUTATION("3","归集"),
    FIXED("4","固定值");

    private final String type;
    private final String name;

    CaliberEnum(String type, String name) {
        this.type = type;
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public static CaliberEnum findByType(String type) {
        for (CaliberEnum categoryEnum : CaliberEnum.values()) {
            if (categoryEnum.getType().equals(type)) {
                return categoryEnum;
            }
        }
        throw new IllegalArgumentException("未找到类型为" + type + "的枚举");
    }
}
