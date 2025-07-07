package com.hscloud.hs.cost.account.constant.enums.kpi;

/**
 * 核算项计算状态
 * @author Administrator
 */

public enum ItemExtStatusEnum {
    /**
     *
     */
    WAIT_EXT("0","未计算"),
    EXT_ING("1","计算中"),
    EXT_SUCCESS("2","已完成"),
    EXT_SUCCESS_ZERO("8","已完成（0结果）"),
    EXT_FAIL("9","计算异常"),

    ;

    private final String status;
    private final String name;

    ItemExtStatusEnum(String status, String name) {
        this.status = status;
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public String getName() {
        return name;
    }

    public static String findByType(String status) {
        for (ItemExtStatusEnum itemExtStatus : ItemExtStatusEnum.values()) {
            if (itemExtStatus.getStatus().equals(status)) {
                return itemExtStatus.getName();
            }
        }
        throw new IllegalArgumentException("未找到类型为【" + status + "】的枚举值");
    }
}
