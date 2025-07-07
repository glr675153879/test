package com.hscloud.hs.cost.account.constant;

/**
 * 常量
 *
 * @author lian
 * @date 2023-09-20 15:30
 */
public final class Constant {

    /**
     * 绩效二次分配
     */
    public static final String SECOND_TASK_CODE = "COST";
    /**
     * 日期格式化
     */
    public static final String DATE_FORMAT_STR = "yyyy-MM-dd";
    /**
     * 月份格式化
     */
    public static final String MONTH_FORMAT_STR = "yyyy-MM";
    public static final String NULL_STR = "null";
    public static final String UNDEFINED_STR = "undefined";

    //动态报表 核算单元字段code
    public static final String ACCOUNT_UNIT_ID = "account_unit_id";
    //动态报表 周期字段
    public static final String CYCLE = "account_time";
    //动态报表 环比值后缀
    public static final String QOQ_RATE_SUFFIX = "_qoq_rate";
    //动态报表 涨幅值后缀
    public static final String QOQ_VALUE_SUFFIX = "_qoq_value";

    /**
     * 核算项校验策略名称
     */
    public static final String VALIDATOR_ITEM = "VALIDATOR_ITEM_";
    public static final String VALIDATOR_ITEM_REPORT_ID = "VALIDATOR_ITEM_REPORT_ID";
}
