package com.hscloud.hs.cost.account.utils.report;

import cn.hutool.core.util.StrUtil;

import java.time.YearMonth;
import java.util.Objects;

/**
 * @author pc
 * @date 2025/5/8
 */
public class CircleUtil {


    /**
     * 得到下个周期
     * 年：增加12个月
     * 月：增加1个月
     * 季度：增加3个月
     *
     * @param frequencyTypeValue 频率类型值
     * @param calculateCircle          年月
     * @return {@link YearMonth }
     */
    public static YearMonth getNextYearMonth(String frequencyTypeValue, String calculateCircle) {
        YearMonth yearMonth = YearMonth.parse(calculateCircle);
        if (Objects.equals(frequencyTypeValue, "YEAR")) {
            return yearMonth.plusYears(1);
        } else if (Objects.equals(frequencyTypeValue, "MONTH")) {
            return yearMonth.plusMonths(1);
        } else if (Objects.equals(frequencyTypeValue, "QUARTER")) {
            return yearMonth.plusMonths(3);
        } else {
            throw new RuntimeException("frequencyTypeValue is invalid: " + frequencyTypeValue);
        }
    }

    /**
     * 得到上个周期
     * 年：减少12个月
     * 月：减少1个月
     * 季度：减少3个月
     *
     * @param frequencyTypeValue 频率类型值
     * @param calculateCircle          年月
     * @return {@link YearMonth }
     */
    public static YearMonth getPreYearMonth(String frequencyTypeValue, String calculateCircle) {
        YearMonth yearMonth = YearMonth.parse(calculateCircle);
        if (StrUtil.contains(frequencyTypeValue, "YEAR")) {
            return yearMonth.minusMonths(1);
        } else if (StrUtil.contains(frequencyTypeValue, "MONTH")) {
            return yearMonth.minusMonths(1);
        } else if (StrUtil.contains(frequencyTypeValue, "QUARTER")) {
            return yearMonth.minusMonths(3);
        } else {
            throw new RuntimeException("frequencyTypeValue is invalid: " + frequencyTypeValue);
        }
    }

}
