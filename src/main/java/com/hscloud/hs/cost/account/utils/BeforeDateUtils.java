package com.hscloud.hs.cost.account.utils;


import com.pig4cloud.pigx.common.core.exception.BizException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * @author 小小w
 * @date 2023/9/19 15:12
 * 时间格式同比/环比转换
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BeforeDateUtils {
    /**
     * 此方法将日期往前/后月
     *
     * @param date   日期
     * @param month  往前/后的月份
     * @return
     */
    public static String getBeforeMonthDate(String date, int month) {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMM");
        try {
            //将传过来的日期设置给calendar
            calendar.setTime(sdf.parse(date));
            //System.out.println("当前日期="+sdf.format(calendar.getTime()));
            //将传过来的月份减去X个月或者加上X个月
            calendar.add(Calendar.MONTH, month);
            //System.out.println("向前推12月之前的日期="+sdf.format(calendar.getTime()));
        } catch (Exception e) {
            log.error("getBeforeMonthDate日期转换异常", e);
            throw new BizException("日期转换异常");

        }
        return sdf.format(calendar.getTime()).toString();
    }

    /**
     * 此方法用于将日期往前推一年
     *
     * @param date  日期 202003
     * @param year  往前/后的年份
     * @return
     */
    public static String getBeforeYearDate(String date, int year) {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMM");
        try {
            //将传过来的日期设置给calendar
            calendar.setTime(sdf.parse(date));
            //System.out.println("当前日期="+sdf.format(calendar.getTime()));
            //将传过来的月份减去X个月或者加上X个月
            calendar.add(Calendar.YEAR, year);
            //System.out.println("向前推12月之前的日期="+sdf.format(calendar.getTime()));
        } catch (Exception e) {
            log.error("getBeforeYearDate日期转换异常", e);
            throw new BizException("日期转换异常");
        }
        return sdf.format(calendar.getTime()).toString();
    }

    /**
     * 此方法用于将日期往前/后推天数
     *
     * @param date  日期:20230307
     * @param days  往前/后的天数:负数表示往前推,正数表示往后推
     * @return
     */
    public static String getBeforeDayDate(String date, int days) {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            //将传过来的日期设置给calendar
            calendar.setTime(sdf.parse(date));
            //System.out.println("当前日期="+sdf.format(calendar.getTime()));
            //将传过来的月份减去X个月或者加上X个月
            calendar.add(Calendar.DAY_OF_YEAR, days);
            //System.out.println("向前推12月之前的日期="+sdf.format(calendar.getTime()));
        } catch (Exception e) {
            log.error("getBeforeDayDate日期转换异常", e);
            throw new BizException("日期转换异常");
        }
        return sdf.format(calendar.getTime()).toString();
    }


    /**
     * 此方法用于将日期往前推一年
     *
     * @param date  日期
     * @param year  往前/后的年份
     * @return
     */
    public static String getBeforeYearDayDate(String date, int year) {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        try {
            //将传过来的日期设置给calendar
            calendar.setTime(sdf.parse(date));
            //System.out.println("当前日期="+sdf.format(calendar.getTime()));
            //将传过来的月份减去X个月或者加上X个月
            calendar.add(Calendar.YEAR, year);
            //System.out.println("向前推12月之前的日期="+sdf.format(calendar.getTime()));
        } catch (Exception e) {
            log.error("getBeforeYearDayDate日期转换异常", e);
            throw new BizException("日期转换异常");
        }
        return sdf.format(calendar.getTime()).toString();
    }

}
