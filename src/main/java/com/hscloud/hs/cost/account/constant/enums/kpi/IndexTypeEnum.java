package com.hscloud.hs.cost.account.constant.enums.kpi;

public enum IndexTypeEnum {
    /**
     * 指标类型
     */
    NOT_COND("1", "非条件指标"),
    COND("2","条件指标"),
    ALLOCATION("3","分摊指标");

    private final String type;
    private final String name;

    IndexTypeEnum(String type, String name) {
        this.type = type;
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public static IndexTypeEnum findByType(String type) {
        for (IndexTypeEnum categoryEnum : IndexTypeEnum.values()) {
            if (categoryEnum.getType().equals(type)) {
                return categoryEnum;
            }
        }
        throw new IllegalArgumentException("未找到类型为" + type + "的枚举");
    }
}
