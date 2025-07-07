package com.hscloud.hs.cost.account.constant.enums.deptCost;

import lombok.Getter;

/**
 * @author tianbo
 * @version 1.0
 * @date 2024-09-20 11:24
 **/
@Getter
public enum DcLevelEnum {
    LEVEL_DIRECT("直接分摊"),
    LEVEL_COMPUTE("计算计入"),
    LEVEL_1("一次分摊"),
    LEVEL_2("二次分摊"),
    LEVEL_3("三次分摊"),
    LEVEL_4("四次分摊");
    private final String name;

    DcLevelEnum(String name) {
        this.name = name;
    }
}
