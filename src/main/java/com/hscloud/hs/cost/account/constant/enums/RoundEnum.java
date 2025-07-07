package com.hscloud.hs.cost.account.constant.enums;

import lombok.Getter;

import java.math.BigDecimal;

/**
 * @author Admin
 */
@Getter
public enum RoundEnum {


    /**
     * 进位方式
     */
    ROUND_CEILING(BigDecimal.ROUND_CEILING, "向上取整"),
    ROUND_FLOOR(BigDecimal.ROUND_FLOOR, "向下取整"),
    ROUND_HALF_UP(BigDecimal.ROUND_HALF_UP, "四舍五入");


    private final Integer code;
    private final String desc;

    RoundEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }


    public static Integer getCodeByDesc(String desc) {
        for (RoundEnum roundEnum : RoundEnum.values()) {
            if (roundEnum.getDesc().equals(desc)) {
                return roundEnum.getCode();
            }
        }
        return RoundEnum.ROUND_HALF_UP.getCode();
    }


}
