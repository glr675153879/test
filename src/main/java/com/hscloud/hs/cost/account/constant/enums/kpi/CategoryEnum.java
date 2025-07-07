package com.hscloud.hs.cost.account.constant.enums.kpi;

public enum CategoryEnum {
    /**
     * 人员分组
     */
    USER_GROUP("user_group", "人员分组"),
    REPORT_GROUP("report_group", "报表分组"),

    IMPUTATION_GROUP("imputation_type", "归集管理分组"),
    UNIT_RELATION("unit_relation", "医护关系分组"),
    DEPT_GROUP("dept_group", "科室单元类型"),
    ACCOUNT_GROUP("account_group", "核算分组(科室)"),
    ACCOUNT_UNIT_GROUP("account_unit_group", "科室单元目录分组"),
    ITEM_GROUP("item_group","核算项分组"),
    INDEX_GROUP("index_group","核算指标分组"),
    PLAN_GROUP("plan_group", "核算方案分组");

    private final String type;
    private final String name;

    CategoryEnum(String type, String name) {
        this.type = type;
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public static CategoryEnum findByType(String type) {
        for (CategoryEnum categoryEnum : CategoryEnum.values()) {
            if (categoryEnum.getType().equals(type)) {
                return categoryEnum;
            }
        }
        throw new IllegalArgumentException("未找到类型为" + type + "的枚举");
    }
}
