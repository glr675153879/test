package com.hscloud.hs.cost.account.constant.enums;

import lombok.Getter;

@Getter
public enum StatisticalPeriodEnum {
    YEAR_ON_YEAR("同比"),
    RING_RATIO("环比");

    private String description;

    StatisticalPeriodEnum(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
