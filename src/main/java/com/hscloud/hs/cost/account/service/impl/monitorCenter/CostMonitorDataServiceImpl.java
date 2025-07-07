package com.hscloud.hs.cost.account.service.impl.monitorCenter;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hscloud.hs.cost.account.mapper.CostMonitorDataMapper;
import com.hscloud.hs.cost.account.model.entity.CostMonitorData;
import com.hscloud.hs.cost.account.service.monitorCenter.CostMonitorDataService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 监测值测试数据
 * @author  lian
 * @date  2023-09-20 10:17
 * 
 */
@Service
public class CostMonitorDataServiceImpl extends ServiceImpl<CostMonitorDataMapper, CostMonitorData> implements CostMonitorDataService {



    private static final String ADD_TIME = " 06:00:00";

    @Override
    public Object batchTestValue(CostMonitorData costMonitorData) {
        //返回指定月份的所有日期
        List<String> allDatesInMonth = getAllDatesInMonth("2023-10-08", "2023-10-08");
        allDatesInMonth.forEach(date->{
            CostMonitorData monitorDataNew;
            monitorDataNew = BeanUtil.copyProperties(costMonitorData, CostMonitorData.class);
            monitorDataNew.setMonitorDate(date);
            monitorDataNew.setId(null);
            monitorDataNew.setWarnTime(date+ADD_TIME);
            save(monitorDataNew);
        });
        return null;
    }

    /**
     * 返回指定月份的所有日期数据
     *@param  startDateStr 开始日期
     *@param  endDateStr 结束日期
     *@return  list<String>
     */
    public static List<String> getAllDatesInMonth(String startDateStr, String endDateStr) {
        List<String> dates = new ArrayList<>();

        LocalDate startDate = LocalDate.parse(startDateStr);
        LocalDate endDate = LocalDate.parse(endDateStr);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        while (!startDate.isAfter(endDate)) {
            dates.add(startDate.format(formatter));
            startDate = startDate.plusDays(1);
        }

        return dates;
    }
}