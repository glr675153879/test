package com.hscloud.hs.cost.account.constant.enums;

import lombok.Getter;

/**
 * @author Admin
 */

@Getter
public enum SecondDistributionEnum {


    /**
     * 二次分配指标枚举
     */
    INDIVIDUAL_POST("individualPost", "个人岗位绩效"),
    MANAGEMENT("management", "管理绩效"),
    WORKLOAD("workload", "工作量绩效"),
    SINGLE("single", "单项绩效"),
    AVERAGE("average", "平均分配");

    private final String code;


    private final String desc;


    SecondDistributionEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
