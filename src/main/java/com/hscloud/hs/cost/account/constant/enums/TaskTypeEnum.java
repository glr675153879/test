package com.hscloud.hs.cost.account.constant.enums;

import lombok.Getter;

/**
 * @author 小小w
 * @date 2023/11/29 13:53
 */
@Getter
public enum TaskTypeEnum {
    /**
     * 核算任务类型对应表
     */

    DOCTOR_TECH_REVENUE("DOCTOR_TECH_REVENUE", "医生医技收入绩效"),
    NURSE_REVENUE("NURSE_REVENUE", "护理收入绩效"),
    COST_PERFORMANCE("COST_PERFORMANCE", "成本绩效"),
    DOCTOR_TECH_ACHIEVEMENT("DOCTOR_TECH_ACHIEVEMENT", "医生医技业绩绩效"),
    NURSE_ACHIEVEMENT("NURSE_ACHIEVEMENT", "护理业绩绩效"),
    DOCTOR_TECH_WORKLOAD("DOCTOR_TECH_WORKLOAD", "医生医技工作量绩效"),
    NURSE_WORKLOAD("NURSE_WORKLOAD", "护理工作量绩效"),
    HOSPITAL_REWARD_PUNISHMENT_DETAIL("HOSPITAL_REWARD_PUNISHMENT_DETAIL", "医院奖罚明细"),
    CLINICAL_DOCTOR_TECH_PERFORMANCE("CLINICAL_DOCTOR_TECH_PERFORMANCE", "临床医生医技绩效"),
    CLINICAL_NURSE_PERFORMANCE("CLINICAL_NURSE_PERFORMANCE", "临床护士绩效"),
    OTHER_ACCOUNTING_UNIT_PERFORMANCE("OTHER_ACCOUNTING_UNIT_PERFORMANCE", "其他核算单元绩效"),
    DEPARTMENT_HEAD_PERFORMANCE("DEPARTMENT_HEAD_PERFORMANCE", "科主任绩效"),
    NURSE_CHIEF_PERFORMANCE("NURSE_CHIEF_PERFORMANCE", "护士长绩效"),
    ADMIN_MIDDLE_HIGH_PERFORMANCE("ADMIN_MIDDLE_HIGH_PERFORMANCE", "行政中高层绩效"),
    ADMIN_GENERAL_PERFORMANCE("ADMIN_GENERAL_PERFORMANCE", "行政普通职工绩效"),
    ADMIN_NON_STAFF_PERFORMANCE("ADMIN_NON_STAFF_PERFORMANCE", "行政编外绩效");

    private final String code;
    private final String description;

    TaskTypeEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    // 根据code获取对应的枚举值
    public static TaskTypeEnum getByCode(String code) {
        for (TaskTypeEnum typeEnum : TaskTypeEnum.values()) {
            if (typeEnum.getCode().equals(code)) {
                return typeEnum;
            }
        }
        throw new IllegalArgumentException("Invalid code: " + code);
    }
    // 根据枚举获取对应的code
    public static TaskTypeEnum getByDescription(String description) {
        for (TaskTypeEnum typeEnum : TaskTypeEnum.values()) {
            if (typeEnum.getDescription().equals(description)) {
                return typeEnum;
            }
        }
        throw new IllegalArgumentException("Invalid description: " + description);
    }
}
