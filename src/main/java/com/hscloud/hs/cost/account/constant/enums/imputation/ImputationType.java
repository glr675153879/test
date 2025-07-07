package com.hscloud.hs.cost.account.constant.enums.imputation;

import lombok.Getter;

/**
 * @author xiechenyu
 * @Description：归集类型
 * @date 2024/4/17 18:59
 */
@Getter
public enum ImputationType {
    /*
     * 工作量数据归集
     */
    WORKLOAD_DATA_IMPUTATION("工作量数据归集", "DEPT"),
    /*
     * 收入数据归集
     */
    INCOME_DATA_IMPUTATION("收入数据归集", "DEPT"),
    /*
     * 临床医护绩效数据归集
     */
    CLINICAL_NURSE_PERFORMANCE_DATA_IMPUTATION("临床医护绩效数据归集", "DEPT"),
    /*
     * 行政中高层数据归集
     */
    ADMIN_MIDDLE_HIGH_PERFORMANCE_DATA_IMPUTATION("行政中高层数据归集", "USER"),
    /*
     * 行政普通职工数据归集
     */
    ADMIN_GENERAL_PERFORMANCE_DATA_IMPUTATION("行政普通职工数据归集", "USER"),
    /*
     * 行政编外数据归集
     */
    ADMIN_NON_STAFF_PERFORMANCE_DATA_IMPUTATION("行政编外数据归集", "USER"),
    /*
     * 其他核算单元数据归集
     */
    OTHER_ACCOUNTING_UNIT_PERFORMANCE_DATA_IMPUTATION("其他核算单元数据归集", "DEPT"),
    /*
     * 成本支出数据归集
     */
    COST_PERFORMANCE_DATA_IMPUTATION("成本支出数据归集", "DEPT"),
    /*
     * 奖罚数据归集
     */
    REWARD_PUNISHMENT_DATA_IMPUTATION("奖罚数据归集", "DEPT"),
    /*
     *科主任护士长数据归集
     */
    ADMIN_CHIEF_NURSE_PERFORMANCE_DATA_IMPUTATION("科主任护士长数据归集", "USER");


    private final String name;
    private final String type;

    ImputationType(String name, String type) {
        this.name = name;
        this.type = type;
    }


}
