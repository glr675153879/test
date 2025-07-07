package com.hscloud.hs.cost.account.utils.kpi;

import cn.hutool.core.date.DatePattern;

import static dm.jdbc.util.DateUtil.isLeapYear;

/**
 * 字符串操作工具类
 *
 * @author Administrator
 */
public class StringChangeUtil {
    /**
     * sql语句中的注释信息去除（--开头）
     */
    private static final String SQL_COMMENT_EXPRESSION = "(?<!['\"])--.*?(?=[\\r\\n]|$)";

    /**
     * 驼峰转下划线
     *
     * @param camelCaseStr 入参
     * @return 字符串
     */
    public static String camelCaseToSnakeCase(String camelCaseStr) {
        if (camelCaseStr == null || camelCaseStr.isEmpty()) {
            return camelCaseStr;
        }
        StringBuilder result = new StringBuilder();
        char firstChar = camelCaseStr.charAt(0);

        if (Character.isLowerCase(firstChar)) {
            result.append(firstChar);
        } else {
            result.append(Character.toLowerCase(firstChar));
        }
        for (int i = 1; i < camelCaseStr.length(); i++) {
            char currentChar = camelCaseStr.charAt(i);
            if (Character.isUpperCase(currentChar)) {
                result.append('_').append(Character.toLowerCase(currentChar));
            } else {
                result.append(currentChar);
            }
        }
        return result.toString();
    }

    /**
     * 下划线转驼峰
     *
     * @param input 入参
     * @return 字符串
     */
    public static String snakeCaseToCamelCase(String input) {
        StringBuilder result = new StringBuilder();
        boolean nextUpperCase = false;
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c == '_') {
                nextUpperCase = true;
            } else if (nextUpperCase) {
                result.append(Character.toUpperCase(c));
                nextUpperCase = false;
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }

    /**
     * 去除sql语句中的注释信息（--开头）
     *
     * @param sql sql语句
     * @return sql语句
     */
    public static String removeSqlComment(String sql) {
        return sql.replaceAll(SQL_COMMENT_EXPRESSION, " ");
    }

    /**
     * 周期格式调整
     *
     * @param period sql语句
     * @param style  数据格式 yyyyMM, yyyy--MM
     * @return 周期
     */
    public static String periodChange(String period, String style) {
        if (period == null || period.isEmpty()) {
            return "";
        }
        if (DatePattern.NORM_MONTH_PATTERN.equals(style)) {
            return period.substring(0, 4) + "-" + period.substring(4, 6);
        }
        if (DatePattern.SIMPLE_MONTH_PATTERN.equals(style)) {
            return period.replaceAll("-", "").substring(0, 6);
        }
        return period;
    }


    /**
     * 获取周期的第一天
     *
     * @param period 周期 yyyyMM
     * @return yyyy-MM-dd
     */
    public static String getFirstDayOfPeriod(String period) {
        if (period == null || period.isEmpty()) {
            return "";
        }
        return period.substring(0, 4) + "-" + period.substring(4, 6) + "-01";
    }

    /**
     * 获取周期的最后一天
     *
     * @param period 周期 yyyyMM
     * @return yyyy-MM-dd
     */
    public static String getLastDayOfPeriod(String period) {
        if (period == null || period.isEmpty()) {
            return "";
        }
        String year = period.substring(0, 4);
        String month = period.substring(4, 6);
        int monthInt = Integer.parseInt(month);
        int lastDay = 0;
        if (monthInt == 2) {
            lastDay = isLeapYear(Integer.parseInt(year)) ? 29 : 28;
        } else if (monthInt == 4 || monthInt == 6 || monthInt == 9 || monthInt == 11) {
            lastDay = 30;
        } else {
            lastDay = 31;
        }
        return year + "-" + month + "-" + String.format("%02d", lastDay);
    }
}
