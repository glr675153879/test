package com.hscloud.hs.cost.account.constant.enums.kpi;

import lombok.Getter;

@Getter
public enum EquivalentTaskStatusEnum {
    REJECTED("-1", "驳回"),
    PENDING_SUBMIT("0", "待提交"),
    PENDING_APPROVE("10", "待审批"),
    APPROVED("20", "通过");

    private final String code;
    private final String desc;

    EquivalentTaskStatusEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static EquivalentTaskStatusEnum getByCode(String code) {
        for (EquivalentTaskStatusEnum status : EquivalentTaskStatusEnum.values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("未找到类型为" + code + "的枚举");
    }
}
