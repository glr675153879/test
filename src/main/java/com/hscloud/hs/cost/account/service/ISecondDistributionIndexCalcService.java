package com.hscloud.hs.cost.account.service;

import com.hscloud.hs.cost.account.model.vo.UserAttendanceVo;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 二次分配公式指标计算
 * @author banana
 * @create 2023-11-29 18:32
 */
public interface ISecondDistributionIndexCalcService {


    List<UserAttendanceVo> calAttendance(String period, List<Long> userIds);


    Map<String, BigDecimal> analysisPJJX(Long planId, Long accountIndexId, Long taskId, String period);


}
