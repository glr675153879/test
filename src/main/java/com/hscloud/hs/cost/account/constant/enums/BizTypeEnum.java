package com.hscloud.hs.cost.account.constant.enums;

public enum BizTypeEnum {
    KPI_UNIT_DEPT("科室单元"),
    KPI_UNIT_IMPUTATION("归集单元");
    private String description;

    BizTypeEnum(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
