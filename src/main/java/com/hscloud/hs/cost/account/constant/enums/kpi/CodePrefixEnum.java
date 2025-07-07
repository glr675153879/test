package com.hscloud.hs.cost.account.constant.enums.kpi;

public enum CodePrefixEnum {

    GROUP("g_", "分组"),
    /**
     * 核算单元
     */
    ACCOUNT("a_", "核算单元"),
    /**
     * 核算单元
     */
    /**
     * 核算项
     */
    ITEM("h_", "核算项"),
    INDEX("z_", "指标"),
    ALLOCATION("f_", "分摊指标"),
    PLAN("p_", "方案")
    ;

    private final String prefix;
    private final String name;

    CodePrefixEnum(String prefix, String name) {
        this.prefix = prefix;
        this.name = name;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getName() {
        return name;
    }

    public static CodePrefixEnum findByPrefix(String prefix) {
        for (CodePrefixEnum item : values()) {
            if (item.prefix.equals(prefix)) {
                return item;
            }
        }
        throw new IllegalArgumentException("未找到前缀为" + prefix + "的枚举");
    }
}
