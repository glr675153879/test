package com.hscloud.hs.cost.account.constant.enums.kpi;

import java.math.RoundingMode;

/**
 * 进位规则对应实际math枚举
 * @author Administrator
 */

public enum RoundingEnum {
    /**
     *
     */
    ROUND_UP("1", "四舍五入", RoundingMode.HALF_UP),
    ROUND_DOWN("2", "向上取整", RoundingMode.CEILING),
    ROUND_HALF_UP("3", "向下取整", RoundingMode.FLOOR),

    ;

    private final String type;

    private final String name;

    private final RoundingMode roundingMode;

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public RoundingMode getRoundingMode() {
        return roundingMode;
    }

    RoundingEnum(String type, String name, RoundingMode roundingMode) {
        this.type = type;
        this.name = name;
        this.roundingMode = roundingMode;
    }

    /**
     * 获取实际进位规则
     * @param type 自定义进位规则码
     * @return 实际进位规则
     */
    public static RoundingMode getCodeByDesc(String type) {
        for (RoundingEnum roundingEnum : RoundingEnum.values()) {
            if (roundingEnum.getType().equals(type)) {
                return roundingEnum.getRoundingMode();
            }
        }
        return RoundingMode.HALF_UP;
    }
}
