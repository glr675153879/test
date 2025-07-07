package com.hscloud.hs.cost.account.service.impl.monitorCenter;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hscloud.hs.cost.account.constant.enums.CostMonitorWarnStatusEnum;
import com.hscloud.hs.cost.account.mapper.AdsMonitorAccountItemMapper;
import com.hscloud.hs.cost.account.mapper.CostMonitorAbMonthMapper;
import com.hscloud.hs.cost.account.model.dto.monitorCenter.CostMonitorAbMonthQueryDto;
import com.hscloud.hs.cost.account.model.dto.monitorCenter.CostMonitorCountQueryDto;
import com.hscloud.hs.cost.account.model.entity.CostMonitorAbMonth;
import com.hscloud.hs.cost.account.model.vo.monitorCenter.CostMonitorInRangeVo;
import com.hscloud.hs.cost.account.service.monitorCenter.CostMonitorAbMonthService;
import com.hscloud.hs.cost.account.service.monitorCenter.CostMonitorCenterService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import static com.hscloud.hs.cost.account.constant.Constant.DATE_FORMAT_STR;
import static com.hscloud.hs.cost.account.constant.Constant.MONTH_FORMAT_STR;
import static com.hscloud.hs.cost.account.utils.CommonUtils.*;

/**
 * 年度异常月份入库
 *
 * @author lian
 * @date 2023-09-22 9:55
 */
@Service
public class CostMonitorAbMonthServiceImpl extends ServiceImpl<CostMonitorAbMonthMapper, CostMonitorAbMonth> implements CostMonitorAbMonthService {

    @Autowired
    private AdsMonitorAccountItemMapper adsMonitorAccountItemMapper;

    @Autowired
    private CostMonitorCenterService centerService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void generateCurrentMonth(CostMonitorAbMonthQueryDto queryDto) {
        if (StringUtils.isBlank(queryDto.getStartMonth()) || StringUtils.isBlank(queryDto.getEndMonth())) {
            String currentMonthStr = DateTimeFormatter.ofPattern(MONTH_FORMAT_STR).format(LocalDate.now());
            queryDto.setStartMonth(currentMonthStr);
            queryDto.setEndMonth(currentMonthStr);
        }
        //查询指定月份的月监测值是否超标
        List<CostMonitorAbMonth> costMonitorAbMonths = adsMonitorAccountItemMapper.queryGenerateMonitorAbnormalMonth(queryDto);
        costMonitorAbMonths.forEach(entity -> {
            CostMonitorInRangeVo costMonitorInRangeVo = centerService.returnRangeInfo(entity.getMonitorValueMonth(), entity.getTargetValue());
            if (null != costMonitorInRangeVo
                    && !CostMonitorWarnStatusEnum.NORMAL.getCode().equals(costMonitorInRangeVo.getWarnStatus())) {
                entity.setStatus(costMonitorInRangeVo.getWarnStatus());
                entity.setWarnValue(costMonitorInRangeVo.getWarnValue());
            }
        });
        //过滤掉正常的记录,异常的月份落库
        costMonitorAbMonths = costMonitorAbMonths.stream().filter(entity ->
                !CostMonitorWarnStatusEnum.NORMAL.getCode().equals(entity.getStatus())
        ).collect(Collectors.toList());
        costMonitorAbMonths.forEach(costMonitorAbMonth->{
            //计算同比环比信息
            calculateAbnormalMonthGrowth(costMonitorAbMonth);
            //插入记录
            save(costMonitorAbMonth);
        });
    }

    /**
     * 计算同比环比信息
     *@param  vo 参数
     */
    public void calculateAbnormalMonthGrowth(CostMonitorAbMonth vo) {
        // 解析传入的月份参数
        YearMonth yearMonth = YearMonth.parse(vo.getMonth(), DateTimeFormatter.ofPattern(MONTH_FORMAT_STR));

        // 获取指定月份的第一天（1号）
        LocalDate firstDayOfMonth = yearMonth.atDay(1);
        // 获取指定月份的最后一天
        LocalDate lastDayOfMonth = yearMonth.atEndOfMonth();
        // 将日期格式化为"yyyy-MM-dd"字符串
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMAT_STR);
        String startDateString = firstDayOfMonth.format(formatter);
        String currentDateString = lastDayOfMonth.format(formatter);
        //同比日期
        List<String> yearOnYears = returnYearOnYear(startDateString, currentDateString);
        //环比日期
        List<String> sequential = returnSequential(startDateString, currentDateString);
        //计算当前日期
        CostMonitorCountQueryDto queryDto = new CostMonitorCountQueryDto();
        queryDto.setUnitId(vo.getUnitId());
        queryDto.setItemId(vo.getItemId());

        queryDto.setStartDate(yearOnYears.get(0));
        queryDto.setEndDate(yearOnYears.get(1));
        BigDecimal yearToYearMonitorCount = adsMonitorAccountItemMapper.queryCount(queryDto);

        //同比增长率
        vo.setYearOnYearGrowth(calculateYearOnYearGrowth(vo.getMonitorValueMonth(), yearToYearMonitorCount) + "%");

        queryDto.setStartDate(sequential.get(0));
        queryDto.setEndDate(sequential.get(1));
        BigDecimal sequentialCount = adsMonitorAccountItemMapper.queryCount(queryDto);
        //环比增长率
        vo.setSequentialGrowth(calculateSequentialGrowth(vo.getMonitorValueMonth(), sequentialCount) + "%");
    }
}