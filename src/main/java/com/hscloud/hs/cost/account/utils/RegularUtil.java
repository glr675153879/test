package com.hscloud.hs.cost.account.utils;

/**
 * @author 小小w
 * @date 2024/3/5 9:30
 */
import cn.hutool.core.collection.CollectionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author 小小w
 * @date 2024/1/17 11:22
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RegularUtil {

    private static final Pattern VARIABLE_PATTERN = Pattern.compile("[a-zA-Z]+[a-zA-Z0-9]*");
    private static final Pattern DIGIT_PATTERN = Pattern.compile("\\b\\d+\\b");

    /**
     * * 此方法用于解析对象id并返回id集合
     *
     * @param str
     * @return
     * @throws Exception
     */
    public List<Long> getIds(String str) {
        Matcher matcher = DIGIT_PATTERN.matcher(str);
        List<Long> list = new ArrayList<>();
        while (matcher.find()) {
            String result = matcher.group();
            list.add(Long.parseLong(result));
        }
        return list;
    }

    /**
     * * 此方法用于解析对象id并返回id集合
     *
     * @param str
     * @return
     * @throws Exception
     */
    public List<String> getStringIds(String str) {
        Matcher matcher = DIGIT_PATTERN.matcher(str);
        List<String> list = new ArrayList<>();
        while (matcher.find()) {
            String result = matcher.group();
            list.add(result);
        }
        return list;
    }

    /**
     * * 此方法用于判断一串数字是否包含在一个字符串中
     *
     * @param numbersStr
     * @param targetNumber
     * @return
     */
    public boolean isNumberInString(String numbersStr, String targetNumber) {
        String pattern = "\\b" + targetNumber + "\\b";
        Pattern regex = Pattern.compile(pattern);
        Matcher matcher = regex.matcher(numbersStr);
        return matcher.find();
    }

    /**
     * 计算耗时
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return timeFormat
     */
    public static String calculateTimeDuration(LocalDateTime startTime, LocalDateTime endTime) {
        Duration duration = Duration.between(startTime, endTime);
        long minutes = duration.toMinutes();

        if (minutes < 60) {
            return minutes + "分";
        } else if (minutes < 24 * 60) {
            long hours = minutes / 60;
            long remainingMinutes = minutes % 60;
            return hours + "小时" + remainingMinutes + "分";
        } else {
            long days = minutes / (24 * 60);
            long remainingHours = (minutes % (24 * 60)) / 60;
            return days + "天" + remainingHours + "小时";
        }
    }


    /**
     * 解析集合中字符串信息
     */
    public List<List<String>> getStringList(List<String> stringList) {
        List<List<String>> list = new ArrayList<>();
        if (stringList == null || stringList.isEmpty()) {
            return new ArrayList<>(); // 返回一个空列表，如果输入列表为空或为null
        }
        String longestString = ""; // 用于保存拥有最多逗号的字符串
        int maxCommas = -1; // 保存最大逗号数量，初始化为-1
        for (String s : stringList) {
            int commaCount = s.length() - s.replace(",", "").length(); // 计算当前字符串的逗号数量
            // 如果当前字符串的逗号数量大于已知的最大数量，则更新最大数量和对应的字符串
            if (commaCount > maxCommas) {
                longestString = s;
                maxCommas = commaCount;
            }
        }
        // 将拥有最多逗号的字符串转换成List<String>并返回
        List<String> groupList = Arrays.asList(longestString.split(","));
        groupList = groupList.stream()
                .filter(str -> !str.isEmpty())
                .collect(Collectors.toList());
        if (CollectionUtil.isNotEmpty(groupList) && groupList.size() == 1) {
            List<String> oneGroup = new ArrayList<>();
            oneGroup.add("一级分组");
            list.add(oneGroup);
        }
        if (CollectionUtil.isNotEmpty(groupList) && groupList.size() == 2) {
            List<String> oneGroup = new ArrayList<>();
            oneGroup.add("一级分组");
            List<String> twoGroup = new ArrayList<>();
            twoGroup.add("二级分组");
            list.add(oneGroup);
            list.add(twoGroup);
        }
        if (CollectionUtil.isNotEmpty(groupList) && groupList.size() == 3) {
            List<String> oneGroup = new ArrayList<>();
            oneGroup.add("一级分组");
            List<String> twoGroup = new ArrayList<>();
            twoGroup.add("二级分组");
            List<String> threeGroup = new ArrayList<>();
            threeGroup.add("三级分组");
            list.add(oneGroup);
            list.add(twoGroup);
            list.add(threeGroup);
        }
        return list;
    }

}

