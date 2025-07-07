package com.hscloud.hs.cost.account.model.pojo;

import java.time.LocalDateTime;

public class QuarterInfo {
    private int year;
    private int quarter;

    public QuarterInfo(int year, int quarter) {
        this.year = year;
        this.quarter = quarter;
    }

    public int getYear() {
        return year;
    }

    public int getQuarter() {
        return quarter;
    }

    /**
     * 返回季度的起始月份
     * @return
     */
    public int getQuarterStartMonth() {
        return (quarter - 1) * 3 + 1;
    }

    /**
     * 返回季度的结束月份
     * @return
     */
    public int getQuarterEndMonth() {
        return (quarter - 1) * 3 + 3;
    }

    /**
     * 返回下一个季度的 QuarterInfo对象
     * @return
     */
    public QuarterInfo getNextQuarter() {
        int nextQuarter = (quarter % 4) + 1;
        int nextYear = year + (quarter == 4 ? 1 : 0);
        return new QuarterInfo(nextYear, nextQuarter);
    }

    public int compareTo(QuarterInfo other) {
        if (this.year != other.year) {
            return Integer.compare(this.year, other.year);
        } else {
            return Integer.compare(this.quarter, other.quarter);
        }
    }

    public static QuarterInfo fromLocalDateTime(LocalDateTime dateTime) {
        int year = dateTime.getYear();
        int month = dateTime.getMonthValue();
        int quarter = (month - 1) / 3 + 1;
        return new QuarterInfo(year, quarter);
    }

    @Override
    public String toString() {
        return year + "年" + String.format("%02d",quarter) + "季";
    }
}