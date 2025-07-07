package com.hscloud.hs.cost.account.utils;

import cn.hutool.core.map.MapUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.hscloud.hs.cost.account.constant.enums.second.CarryRule;
import com.hscloud.hs.cost.account.model.dto.imputation.SpecialPersonIndexOrUnitDTO;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static com.hscloud.hs.cost.account.constant.Constant.DATE_FORMAT_STR;

public class CommonUtils {

    @SneakyThrows
    public static String getEroLog(Exception exception) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        exception.printStackTrace(pw);
        String result = sw.toString(); //异常字符串
        sw.close();
        pw.close();
        return result;
    }

    /**
     * 返回同比的开始和结束日期
     *
     * @param startDateString 当年开始日期
     * @param endDateString   当年结束日期
     * @return list
     */
    public static List<String> returnYearOnYear(String startDateString, String endDateString) {
        LocalDate startDateThisYear = LocalDate.parse(startDateString);
        LocalDate endDateThisYear = LocalDate.parse(endDateString);

        // 计算去年同一日期范围
        LocalDate startDateLastYear = startDateThisYear.minus(Period.ofYears(1));
        LocalDate endDateLastYear = endDateThisYear.minus(Period.ofYears(1));

        // 格式化日期为字符串
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMAT_STR);
        List<String> resLastList = new ArrayList<>();
        resLastList.add(startDateLastYear.format(formatter));
        resLastList.add(endDateLastYear.format(formatter));
        return resLastList;
    }

    /**
     * 返回环比日期
     *
     * @param startDateString 开始日期
     * @param endDateString   结束日期
     * @return list
     */
    public static List<String> returnSequential(String startDateString, String endDateString) {
        // 输入的开始日期和结束日期
        LocalDate startDate = LocalDate.parse(startDateString);
        LocalDate endDate = LocalDate.parse(endDateString);
        //计算当前日期相差天数 并往前推对应的日期
        long daysBetween = ChronoUnit.DAYS.between(startDate, endDate);

        // 计算前一个日期范围
        LocalDate previousStartDate = startDate.minusDays(daysBetween);
        LocalDate previousEndDate = startDate.minus(Period.ofDays(1));

        // 格式化日期为字符串
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMAT_STR);
        List<String> resLastList = new ArrayList<>();
        resLastList.add(previousStartDate.format(formatter));
        resLastList.add(previousEndDate.format(formatter));
        return resLastList;

    }


    /**
     * 计算环比增长率
     *
     * @param currentPeriodValue 当前周期的值
     * @param lastPeriodValue    上一个周期的值
     * @return 环比增长率
     */
    public static BigDecimal calculateSequentialGrowth(BigDecimal currentPeriodValue, BigDecimal lastPeriodValue) {
        if (lastPeriodValue.equals(BigDecimal.ZERO)) {
            throw new IllegalArgumentException("上一周期的数不能为0");
        }
        BigDecimal difference = currentPeriodValue.subtract(lastPeriodValue);
        return difference.divide(lastPeriodValue, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 计算同比增长率
     *
     * @param currentPeriodValue      当前周期的值
     * @param samePeriodLastYearValue 上一年同期的值
     * @return 同比增长率
     */
    public static BigDecimal calculateYearOnYearGrowth(BigDecimal currentPeriodValue, BigDecimal samePeriodLastYearValue) {
        if (samePeriodLastYearValue.equals(BigDecimal.ZERO)) {
            throw new IllegalArgumentException("同比周期的值不能为0");
        }
        BigDecimal difference = currentPeriodValue.subtract(samePeriodLastYearValue);
        return difference.divide(samePeriodLastYearValue, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);
    }


    @NotNull
    public static Map<String, Object> getUserObj(String ids, String names, String listKey) {
        Map<String, Object> leaderUser = new HashMap<>();
        if (com.baomidou.mybatisplus.core.toolkit.StringUtils.isBlank(ids) || com.baomidou.mybatisplus.core.toolkit.StringUtils.isBlank(names)) {
            return leaderUser;
        }
        List<Map<String, Object>> userList = new ArrayList<>();
        String[] idsArr = Arrays.stream(ids.split(","))
                .filter(s -> !s.isEmpty())
                .toArray(String[]::new);
        String[] namesArr = Arrays.stream(names.split(","))
                .filter(s -> !s.isEmpty())
                .toArray(String[]::new);
        for (int i = 0; i < idsArr.length; i++) {
            Map<String, Object> user = new HashMap<>();
            user.put("id", idsArr[i]);
            user.put("name", namesArr[i]);
            userList.add(user);
        }
        leaderUser.put(listKey, userList);
        return leaderUser;
    }

    /**
     * 按进位规则计算
     *
     * @param amt
     * @param retainDecimal
     * @param carryRule
     * @return
     */
    public static BigDecimal amtSetScale(BigDecimal amt, Integer retainDecimal, String carryRule) {
        if (amt == null) {
            return null;
        }
        RoundingMode roundingMode = RoundingMode.DOWN;
        try {
            if (carryRule != null) {
                JSONObject obj = JSONObject.parseObject(carryRule);
                String key = obj.getString("value");
                if (CarryRule.up.toString().equals(key)) {
                    roundingMode = RoundingMode.UP;
                } else if (CarryRule.halfup.toString().equals(key)) {
                    roundingMode = RoundingMode.HALF_UP;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        retainDecimal = retainDecimal == null ? 2 : retainDecimal;

        return amt.setScale(retainDecimal, roundingMode);
    }

    public static RoundingMode getCarryRule(String carryRule) {
        RoundingMode roundingMode = RoundingMode.DOWN;
        try {
            if (carryRule != null) {
                JSONObject obj = JSONObject.parseObject(carryRule);
                String key = obj.getString("value");
                if (CarryRule.up.toString().equals(key)) {
                    roundingMode = RoundingMode.UP;
                } else if (CarryRule.halfup.toString().equals(key)) {
                    roundingMode = RoundingMode.HALF_UP;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return roundingMode;
    }

    public static String getValueFromUserObj(Map<String, Object> leaderUser, String key, String listKey) {
        if (MapUtil.isEmpty(leaderUser)) {
            return "";
        }
        List<Map<String, Object>> userList = (List<Map<String, Object>>) leaderUser.get(listKey);
        return userList.stream().map(item -> (String) item.get(key)).collect(Collectors.joining(","));
    }

    public static String getDicVal(String dicText) {
        if (dicText == null) return null;
        JSONObject object = JSON.parseObject(dicText);
        return object.getString("value");
    }

    public static String getDicLabel(Object dicText) {
        if (Objects.isNull(dicText)) {
            return "";
        }
        JSONObject object = JSON.parseObject(dicText.toString());
        return object.getString("label");
    }

    public static List<Long> longs2List(String ids) {
        return Arrays.stream(ids.split(","))
                .map(Long::valueOf)
                .collect(Collectors.toList());
    }

    public static List<String> str2List(String codes) {
        return Arrays.asList(codes.split(","));
    }


    private static ScriptEngine engine;
    static {
        ScriptEngineManager manager = new ScriptEngineManager();
        engine = manager.getEngineByName("JavaScript");
    }
    public static Object caclByEval(String expression) throws ScriptException {

        // 使用eval方法计算表达式的值
        expression = expression + ";" +
                "function MAX() {return Math.max.apply(null,arguments)};" +
                "function MIN() {return Math.min.apply(null,arguments)};" +
                "function IF(a,b,c){return a?b:c;};" +
                "function AND() { for (var i=0;i<arguments.length;i++) { if (!arguments[i]) { return false } } return true };" +
                "function OR() { for (var i=0;i<arguments.length;i++) { if (arguments[i]) { return true } } return false }";

//        System.out.println(expression);
        engine.createBindings();
        Object eval = engine.eval(expression);
        try {
            return new BigDecimal(eval + "").setScale(10, RoundingMode.HALF_UP);
        } catch (Exception e) {
            return eval;
        }
    }

    public static List<SpecialPersonIndexOrUnitDTO> getIndexOrUnitList(String ids, String names) {
        List<SpecialPersonIndexOrUnitDTO> list = new ArrayList<>();
        if (StringUtils.isBlank(ids) || StringUtils.isBlank(names)) {
            return list;
        }
        String[] idsArr = Arrays.stream(ids.split(","))
                .filter(s -> !s.isEmpty())
                .toArray(String[]::new);
        String[] namesArr = Arrays.stream(names.split(","))
                .filter(s -> !s.isEmpty())
                .toArray(String[]::new);

        for (int i = 0; i < idsArr.length; i++) {
            SpecialPersonIndexOrUnitDTO specialPersonIndexOrUnitDTO = new SpecialPersonIndexOrUnitDTO();
            specialPersonIndexOrUnitDTO.setId(idsArr[i]);
            specialPersonIndexOrUnitDTO.setName(namesArr[i]);
            list.add(specialPersonIndexOrUnitDTO);
        }
        return list;

    }

    public static String getNullOrObject(Object o) {
        if (Objects.isNull(o)) {
            return "";
        }
        return o.toString();
    }

}
