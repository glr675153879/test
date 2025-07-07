package com.hscloud.hs.cost.account.constant.enums.deptCost;

import lombok.Getter;

/**
 * @author tianbo
 * @version 1.0
 * @date 2024-09-23 14:40
 **/
@Getter
public enum FormulaSetTypeEnum {
    COMPOSITE("0", "复合计算"),
    SINGLE("1", "单项"),
    DEPT_AVERAGE("2", "科室均摊"),
    HOSPITAL_LEADER_WAGE("3", "院领导工资"),
    NON_HOSPITAL_LEADER_WAGE("4", "非院领导工资（含转科）");

    private final String code;
    private final String desc;

    FormulaSetTypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static FormulaSetTypeEnum getByCode(String code) {
        for (FormulaSetTypeEnum formulaSetTypeEnum : FormulaSetTypeEnum.values()) {
            if (formulaSetTypeEnum.getCode().equals(code)) {
                return formulaSetTypeEnum;
            }
        }
        return null;
    }

}
