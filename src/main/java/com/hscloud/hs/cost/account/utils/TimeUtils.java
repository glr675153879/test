package com.hscloud.hs.cost.account.utils;


import com.hscloud.hs.cost.account.constant.enums.DimensionEnum;
import com.hscloud.hs.cost.account.model.entity.CostAccountTask;
import com.hscloud.hs.cost.account.model.pojo.QuarterInfo;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.YearMonth;
import java.util.Date;

@Component
public class TimeUtils {

    public static CostAccountTask parseCostTime(CostAccountTask costAccountTask) throws ParseException {

        //年
        if(costAccountTask.getDimension().equals(DimensionEnum.YEAR.getCode())){
            return parseByYear(costAccountTask);
        }
        //月
        else if(costAccountTask.getDimension().equals(DimensionEnum.MONTH.getCode())){
            return parseByMonth(costAccountTask);
        }
        //季度
        else if(costAccountTask.getDimension().equals(DimensionEnum.QUARTER.getCode())){
            return parseByQuarter(costAccountTask);
        }
        //日
        else{
            return parseByDay(costAccountTask);
        }
    }

    public static CostAccountTask parseByYear(CostAccountTask costAccountTask) throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy年");
        Date date = dateFormat.parse(costAccountTask.getDetailDim());
        SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy");

        int year = Integer.parseInt(yearFormat.format(date));
        // 创建开始时间
        LocalDateTime startDate = LocalDateTime.of(year, Month.JANUARY, 1,0,0,0);
        // 创建结束时间
        LocalDateTime endDate = LocalDateTime.of(year, Month.DECEMBER, Month.DECEMBER.maxLength(),0,0,0);
        costAccountTask.setAccountStartTime(startDate);
        costAccountTask.setAccountEndTime(endDate);

        return costAccountTask;
    }

    public static CostAccountTask parseByMonth(CostAccountTask costAccountTask) throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy年MM月");

        Date date = dateFormat.parse(costAccountTask.getDetailDim());

        SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy");
        SimpleDateFormat monthFormat = new SimpleDateFormat("MM");

        int year = Integer.parseInt(yearFormat.format(date));
        Month month = Month.of(Integer.parseInt(monthFormat.format(date)));
        YearMonth yearMonth = YearMonth.of(year, month);

        // 创建开始时间
        LocalDateTime startDate = LocalDateTime.of(year, month, 1,0,0,0);
        // 创建结束时间
        LocalDateTime endDate = LocalDateTime.of(year, month,yearMonth.atEndOfMonth().getDayOfMonth(),0,0,0);
        costAccountTask.setAccountStartTime(startDate);
        costAccountTask.setAccountEndTime(endDate);

        return costAccountTask;
    }

    public static CostAccountTask parseByQuarter(CostAccountTask costAccountTask) {

        QuarterInfo quarterInfo = parseQuarterData(costAccountTask.getDetailDim());

        LocalDateTime startDate = LocalDateTime.of(quarterInfo.getYear(), quarterInfo.getQuarterStartMonth(), 1, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(quarterInfo.getYear(), quarterInfo.getQuarterEndMonth(), Month.of(quarterInfo.getQuarterEndMonth()).maxLength(), 0, 0);
        costAccountTask.setAccountStartTime(startDate);
        costAccountTask.setAccountEndTime(endDate);
        return costAccountTask;
    }

    public static CostAccountTask parseByDay(CostAccountTask costAccountTask) throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy年MM月dd日");
        Date date = dateFormat.parse(costAccountTask.getDetailDim());

        SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy");
        SimpleDateFormat monthFormat = new SimpleDateFormat("MM");
        SimpleDateFormat dayFormat = new SimpleDateFormat("dd");

        int year = Integer.parseInt(yearFormat.format(date));
        int month = Integer.parseInt(monthFormat.format(date));
        int day = Integer.parseInt(dayFormat.format(date));
        // 创建开始时间
        LocalDateTime startDate = LocalDateTime.of(year, month, day,0,0,0);
        // 创建结束时间
        LocalDateTime endDate = LocalDateTime.of(year, month, day,0,0,0);
        costAccountTask.setAccountStartTime(startDate);
        costAccountTask.setAccountEndTime(endDate);

        return costAccountTask;
    }

    /**
     * 将时间字符串转为QuarterInfo
     * @param quarterData
     * @return
     */
    public static QuarterInfo parseQuarterData(String quarterData) {
        String[] parts = quarterData.split("年|季");
        int year = Integer.parseInt(parts[0]);
        int quarter = Integer.parseInt(parts[1]);

        return new QuarterInfo(year, quarter);
    }

    /**
     * 将时间转为时间字符串（2023年01季）
     * @param dateTime
     * @return
     */
    public static String formatLocalDateTimeAsQuarter(LocalDateTime dateTime) {
        QuarterInfo quarterInfo = QuarterInfo.fromLocalDateTime(dateTime);
        return quarterInfo.toString();
    }

}
