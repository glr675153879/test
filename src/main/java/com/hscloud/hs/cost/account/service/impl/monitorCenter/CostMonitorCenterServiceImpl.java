package com.hscloud.hs.cost.account.service.impl.monitorCenter;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.enums.SqlKeyword;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.hscloud.hs.cost.account.constant.enums.CostMonitorCenterStatusEnum;
import com.hscloud.hs.cost.account.constant.enums.CostMonitorWarnStatusEnum;
import com.hscloud.hs.cost.account.constant.enums.UnitMapEnum;
import com.hscloud.hs.cost.account.mapper.*;
import com.hscloud.hs.cost.account.model.dto.monitorCenter.*;
import com.hscloud.hs.cost.account.model.entity.*;
import com.hscloud.hs.cost.account.model.pojo.PlanConfigFormulaConfig;
import com.hscloud.hs.cost.account.model.pojo.UnitInfo;
import com.hscloud.hs.cost.account.model.vo.CostAccountTaskIndexVo;
import com.hscloud.hs.cost.account.model.vo.CostAccountTaskResultTotalValueVo;
import com.hscloud.hs.cost.account.model.vo.monitorCenter.*;
import com.hscloud.hs.cost.account.service.CostTaskExecuteResultService;
import com.hscloud.hs.cost.account.service.monitorCenter.CostMonitorCenterService;
import com.hscloud.hs.cost.account.utils.LocalCacheUtils;
import com.hscloud.hs.cost.account.utils.SqlUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.hscloud.hs.cost.account.constant.Constant.DATE_FORMAT_STR;
import static com.hscloud.hs.cost.account.constant.Constant.MONTH_FORMAT_STR;
import static com.hscloud.hs.cost.account.utils.CommonUtils.*;

/**
 * 监测中心impl
 *
 * @author lian
 * @date 2023-09-18 11:22
 */
@Service
public class CostMonitorCenterServiceImpl extends ServiceImpl<CostMonitorCenterMapper, CostMonitorCenter> implements CostMonitorCenterService {


    @Autowired
    private CostMonitorSetMapper costMonitorSetMapper;

    @Autowired
    private AdsMonitorAccountItemMapper adsMonitorAccountItemMapper;

    @Autowired
    private CostMonitorAbMonthMapper costMonitorAbMonthMapper;

    @Autowired
    private CostAccountPlanConfigMapper costAccountPlanConfigMapper;

    @Autowired
    private CostIndexConfigIndexMapper costIndexConfigIndexMapper;

    @Autowired
    private CostAccountTaskMapper costAccountTaskMapper;

    @Autowired
    private CostTaskExecuteResultIndexMapper taskExecuteResultIndexMapper;

    @Autowired
    private CostAccountPlanConfigFormulaMapper configFormulaMapper;

    @Autowired
    private CostAccountIndexMapper costAccountIndexMapper;

    @Autowired
    private CostTaskExecuteResultService costTaskExecuteResultService;

    @Autowired
    private LocalCacheUtils cacheUtils;

    @Autowired
    private  SqlUtil sqlUtil;



    @Override
    public Object queryTrendList(Page page, CostMonitorCenterQueryDto queryDto) {
        IPage<CostMonitorCenterVo> resPage = costMonitorSetMapper.queryTrendList(page, queryDto);
        CostMonitorDataQueryDto dataQueryDto = new CostMonitorDataQueryDto();
        if (StrUtil.isBlank(queryDto.getMonth())) {
            LocalDate localDate = LocalDate.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(MONTH_FORMAT_STR);
            queryDto.setMonth(formatter.format(localDate));
        }
        dataQueryDto.setMonth(queryDto.getMonth());
        //监测值趋势查询条件
        CostMonitorTrendQueryDto trendQueryDto = new CostMonitorTrendQueryDto();
        resPage.getRecords().forEach(vo -> {
            dataQueryDto.setUnitId(vo.getUnitId());
            dataQueryDto.setItemId(vo.getItemId());
            //查询最新监测值,当月监测值,最新预警时间的数据
            CostMonitorDataVo costMonitorDataVo = adsMonitorAccountItemMapper.queryStatisticsValue(dataQueryDto);
            if (null != costMonitorDataVo) {
                vo.setMonitorValue(costMonitorDataVo.getMonitorValue());
                vo.setMonitorValueMonth(costMonitorDataVo.getMonitorValueMonth());
                vo.setWarnTime(costMonitorDataVo.getWarnTime());
            }
            //查询对应的监测趋势信息 并设置
            trendQueryDto.setUnitId(vo.getUnitId());
            trendQueryDto.setItemId(vo.getItemId());
            trendQueryDto.setMonth(queryDto.getMonth());
            vo.setTrendList(queryListTrend(trendQueryDto, vo.getTargetValue()));
            //返回当月累计监测总值情况
            if (null != vo.getMonitorValueMonth()) {
                CostMonitorInRangeVo costMonitorInRangeVo = returnRangeInfo(vo.getMonitorValueMonth(), vo.getTargetValue());
                if (null != costMonitorInRangeVo
                        && !CostMonitorWarnStatusEnum.NORMAL.getCode().equals(costMonitorInRangeVo.getWarnStatus())) {
                    vo.setStatus(costMonitorInRangeVo.getWarnStatus());
                    vo.setWarnValue(costMonitorInRangeVo.getWarnValue());
                }
            }
        });
        return resPage;
    }

    /**
     * 查询监测值趋势
     *
     * @param queryDto    查询参数
     * @param targetValue 目标值
     * @return vo
     */
    public List<CostMonitorCenterTrendVo> queryListTrend(CostMonitorTrendQueryDto queryDto, String targetValue) {
        List<CostMonitorCenterTrendVo> resList = adsMonitorAccountItemMapper.queryList(queryDto);
        resList.forEach(vo -> {
            //校验是否低于目标值,如果高于目标值的话则状态设为异常
            String monitorValueInRange = isMonitorValueInRange(vo.getMonitorValueCount(), targetValue);
            vo.setStatus(monitorValueInRange);
        });
        return resList;
    }

    /**
     * 校验监测值是否在对应目标值范围内
     *
     * @param monitorValueCount 合计值
     * @return boolean
     */
    public String isMonitorValueInRange(BigDecimal monitorValueCount, String targetValue) {
        // 定义正则表达式模式，匹配例如："<310" 或 ">=400" 这样的条件
        // 定义正则表达式模式，匹配例如："200≤目标值≤400" 这样的条件
        String pattern = "(\\d+(\\.\\d+)?)≤目标值≤(\\d+(\\.\\d+)?)";
        Pattern regex = Pattern.compile(pattern);
        Matcher matcher = regex.matcher(targetValue);
        String pattern2 = "(\\d+(\\.\\d+)?)≤目标值<(\\d+(\\.\\d+)?)";
        Pattern regex2 = Pattern.compile(pattern2);
        Matcher matcher2 = regex2.matcher(targetValue);
        String pattern3 = "(\\d+(\\.\\d+)?)<目标值≤(\\d+(\\.\\d+)?)";
        Pattern regex3 = Pattern.compile(pattern3);
        Matcher matcher3 = regex3.matcher(targetValue);
        String pattern4 = "(\\d+(\\.\\d+)?)<目标值<(\\d+(\\.\\d+)?)";
        Pattern regex4 = Pattern.compile(pattern4);
        Matcher matcher4 = regex4.matcher(targetValue);
        if (matcher.find()) {
            BigDecimal lowerBound = new BigDecimal(matcher.group(1));
            BigDecimal upperBound = new BigDecimal(matcher.group(3));
            if (!(monitorValueCount.compareTo(lowerBound) >= 0 && monitorValueCount.compareTo(upperBound) <= 0)) {
                return CostMonitorCenterStatusEnum.ABNORMAL.getCode();
            }
        } else if (matcher2.find()) {
            BigDecimal lowerBound = new BigDecimal(matcher2.group(1));
            BigDecimal upperBound = new BigDecimal(matcher2.group(3));
            if (!(monitorValueCount.compareTo(lowerBound) >= 0 && monitorValueCount.compareTo(upperBound) < 0)) {
                return CostMonitorCenterStatusEnum.ABNORMAL.getCode();
            }
        } else if (matcher3.find()) {
            BigDecimal lowerBound = new BigDecimal(matcher3.group(1));
            BigDecimal upperBound = new BigDecimal(matcher3.group(3));
            if (!(monitorValueCount.compareTo(lowerBound) > 0 && monitorValueCount.compareTo(upperBound) <= 0)) {
                return CostMonitorCenterStatusEnum.ABNORMAL.getCode();
            }
        } else if (matcher4.find()) {
            BigDecimal lowerBound = new BigDecimal(matcher4.group(1));
            BigDecimal upperBound = new BigDecimal(matcher4.group(3));
            if (!(monitorValueCount.compareTo(lowerBound) > 0 && monitorValueCount.compareTo(upperBound) < 0)) {
                return CostMonitorCenterStatusEnum.ABNORMAL.getCode();
            }
        } else {
            // 如果没有匹配到上述格式，尝试匹配 "<310"、">=400" 等格式
            pattern = "(<=|>=|<|>)\\s*(\\d+(\\.\\d+)?)";
            regex = Pattern.compile(pattern);
            matcher = regex.matcher(targetValue);
            if (matcher.find()) {
                String operator = matcher.group(1);
                BigDecimal threshold = new BigDecimal(matcher.group(2));
                if (SqlKeyword.LT.getSqlSegment().equals(operator) && monitorValueCount.compareTo(threshold) >= 0) {
                    return CostMonitorCenterStatusEnum.ABNORMAL.getCode();
                } else if (SqlKeyword.LE.getSqlSegment().equals(operator) && monitorValueCount.compareTo(threshold) > 0) {
                    return CostMonitorCenterStatusEnum.ABNORMAL.getCode();
                } else if (SqlKeyword.GT.getSqlSegment().equals(operator) && monitorValueCount.compareTo(threshold) <= 0) {
                    return CostMonitorCenterStatusEnum.ABNORMAL.getCode();
                } else if (SqlKeyword.GE.getSqlSegment().equals(operator) && monitorValueCount.compareTo(threshold) < 0) {
                    return CostMonitorCenterStatusEnum.ABNORMAL.getCode();
                }
            }
        }
        return CostMonitorCenterStatusEnum.NORMAL.getCode();
    }


    /**
     * 返回当月累计监测总值超出或低于的数值
     *
     * @param monitorValueCount 当月累计总值
     * @param targetValue       目标值
     * @return vo
     */

    public CostMonitorInRangeVo returnRangeInfo(BigDecimal monitorValueCount, String targetValue) {
        // 定义正则表达式模式，匹配例如："<310" 或 ">=400" 这样的条件
        // 定义正则表达式模式，匹配例如："200≤目标值≤400" 这样的条件
        String pattern = "(\\d+(\\.\\d+)?)≤目标值≤(\\d+(\\.\\d+)?)";
        Pattern regex = Pattern.compile(pattern);
        Matcher matcher = regex.matcher(targetValue);
        String pattern2 = "(\\d+(\\.\\d+)?)≤目标值<(\\d+(\\.\\d+)?)";
        Pattern regex2 = Pattern.compile(pattern2);
        Matcher matcher2 = regex2.matcher(targetValue);
        String pattern3 = "(\\d+(\\.\\d+)?)<目标值≤(\\d+(\\.\\d+)?)";
        Pattern regex3 = Pattern.compile(pattern3);
        Matcher matcher3 = regex3.matcher(targetValue);
        String pattern4 = "(\\d+(\\.\\d+)?)<目标值<(\\d+(\\.\\d+)?)";
        Pattern regex4 = Pattern.compile(pattern4);
        Matcher matcher4 = regex4.matcher(targetValue);
        CostMonitorInRangeVo res = new CostMonitorInRangeVo();
        if (matcher.find()) {
            BigDecimal lowerBound = new BigDecimal(matcher.group(1));
            BigDecimal upperBound = new BigDecimal(matcher.group(3));
            commonReturnRes(res, lowerBound, upperBound, monitorValueCount);
        } else if (matcher2.find()) {
            BigDecimal lowerBound = new BigDecimal(matcher2.group(1));
            BigDecimal upperBound = new BigDecimal(matcher2.group(3));
            commonReturnRes(res, lowerBound, upperBound, monitorValueCount);
        } else if (matcher3.find()) {
            BigDecimal lowerBound = new BigDecimal(matcher3.group(1));
            BigDecimal upperBound = new BigDecimal(matcher3.group(3));
            commonReturnRes(res, lowerBound, upperBound, monitorValueCount);
        } else if (matcher4.find()) {
            BigDecimal lowerBound = new BigDecimal(matcher4.group(1));
            BigDecimal upperBound = new BigDecimal(matcher4.group(3));
            commonReturnRes(res, lowerBound, upperBound, monitorValueCount);
        } else {
            // 如果没有匹配到上述格式，尝试匹配 "<310"、">=400" 等格式
            pattern = "(<=|>=|<|>|≥|≤)\\s*([-+]?(\\d+(\\.\\d*)?|\\.\\d+))";
            regex = Pattern.compile(pattern);
            matcher = regex.matcher(targetValue);
            if (matcher.find()) {
                String operator = matcher.group(1);
                BigDecimal threshold = new BigDecimal(matcher.group(2));
                if ((operator.contains("<") && monitorValueCount.compareTo(threshold) >= 0)
                        || (operator.contains("≤") && monitorValueCount.compareTo(threshold) > 0)) {
                    BigDecimal diff = threshold.subtract(monitorValueCount);
                    res.setWarnValue(diff.abs().setScale(2, RoundingMode.HALF_UP));
                    res.setWarnStatus(CostMonitorWarnStatusEnum.GREATER.getCode());
                    return res;
                } else if ((operator.contains(">") && monitorValueCount.compareTo(threshold) <= 0)
                ||(operator.contains("≥") && monitorValueCount.compareTo(threshold) < 0)) {
                    BigDecimal diff = monitorValueCount.subtract(threshold);
                    res.setWarnValue(diff.abs().setScale(2, RoundingMode.HALF_UP));
                    res.setWarnStatus(CostMonitorWarnStatusEnum.LOWER.getCode());
                    return res;
                }
            }
        }
        return res;
    }

    /**
     * 公共代码设置res
     *
     * @param res               res
     * @param lowerBound        下限
     * @param upperBound        上限
     * @param monitorValueCount 监测值
     */
    void commonReturnRes(CostMonitorInRangeVo res, BigDecimal lowerBound, BigDecimal upperBound, BigDecimal monitorValueCount) {
        if (monitorValueCount.compareTo(lowerBound) < 0) {
            BigDecimal diff = lowerBound.subtract(monitorValueCount);
            res.setWarnValue(diff);
            res.setWarnStatus(CostMonitorWarnStatusEnum.LOWER.getCode());
        } else if (monitorValueCount.compareTo(upperBound) > 0) {
            BigDecimal diff = monitorValueCount.subtract(upperBound);
            res.setWarnValue(diff);
            res.setWarnStatus(CostMonitorWarnStatusEnum.GREATER.getCode());
        }
    }


    @Override
    public Object getStatistics(CostMonitorCenterQueryDto queryDto) {
        return null;
    }

    @Override
    public Object queryAbnormalCount(CostMonitorAbnormalCountQueryDto queryDto) {
        CostMonitorAbnormalCountVo countVo = new CostMonitorAbnormalCountVo();
        CostMonitorAbnormalItemQueryDto itemQueryDto = new CostMonitorAbnormalItemQueryDto();
        itemQueryDto.setMonth(queryDto.getMonth());
        List<CostMonitorAbnormalItemVo> costMonitorAbnormalItemVos = queryAbnormalItemList(itemQueryDto);
        countVo.setAbnormalItem(costMonitorAbnormalItemVos.size());
        CostMonitorAbnormalUnitQueryDto unitQueryDto = new CostMonitorAbnormalUnitQueryDto();
        countVo.setAbnormalUnit(queryAbnormalUnitList(unitQueryDto, costMonitorAbnormalItemVos).size());
        return countVo;
    }

    @Override
    public Object queryMonitorValueTrend(CostMonitorValTrendQueryDto queryDto) {
        List<CostMonitorValTrendVo> resList = new ArrayList<>();
        CostMonitorValTrendVo costMonitorValTrendVo = costMonitorSetMapper.queryMonitorValue(queryDto);
        if (null != costMonitorValTrendVo) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(MONTH_FORMAT_STR);
            LocalDate localDate = LocalDate.now();
            //查询当前月份的监测数据信息
            if (StrUtil.isBlank(queryDto.getMonth())) {
                queryDto.setMonth(formatter.format(localDate));
            }
            CostMonitorTrendQueryDto trendQueryDto = new CostMonitorTrendQueryDto();
            trendQueryDto.setMonth(queryDto.getMonth());
            trendQueryDto.setUnitId(queryDto.getUnitId());
            trendQueryDto.setItemId(queryDto.getItemId());
            //查询趋势详情 返回当月监测值和当月累计监测值
            resList = adsMonitorAccountItemMapper.queryMonitorValTrendList(trendQueryDto);
            resList.forEach(vo -> {
                //返回当月累计监测总值是否超出或低于的监测数值,并返回数值
                CostMonitorInRangeVo costMonitorInRangeVo = returnRangeInfo(vo.getMonitorValueMonth(), costMonitorValTrendVo.getTargetValue());
                if (null != costMonitorInRangeVo
                        && !CostMonitorWarnStatusEnum.NORMAL.getCode().equals(costMonitorInRangeVo.getWarnStatus())) {
                    vo.setStatus(costMonitorInRangeVo.getWarnStatus());
                    vo.setWarnValue(costMonitorInRangeVo.getWarnValue());
                }
                //设置返回相关信息
                new CostMonitorValTrendVo().returnCostMonitorValTrendVo(vo, costMonitorValTrendVo);
                //计算同比 环比  计算周期 本年度的1月1号截止到现在
                calculateGrowth(vo);
            });
            //年平均值
            if (!CollectionUtils.isEmpty(resList)) {
                resList.get(0).setYearAvg(calculateYearAvg(trendQueryDto.getUnitId(), trendQueryDto.getItemId(), LocalDate.now().getYear()));
            }
        }
        return resList;
    }

    /**
     * 计算监测值趋势 同比和环比
     *
     * @param vo 参数
     */
    public void calculateGrowth(CostMonitorValTrendVo vo) {
        //同比 本年度1月1号至现在 比上年度1月1号至当前日期
        // 获取当前日期
        LocalDate currentDate = LocalDate.now();
        // 获取本年度的1月1日
        LocalDate startDate = LocalDate.of(currentDate.getYear(), 1, 1);
        // 将日期格式化为"yyyy-MM-dd"字符串
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMAT_STR);
        String startDateString = startDate.format(formatter);
        //当前日期使用
        if (StringUtils.isNotBlank(vo.getMonitorDate())) {
            String currentDateString = vo.getMonitorDate();
            //同比日期
            List<String> yearOnYears = returnYearOnYear(startDateString, currentDateString);
            //环比日期
            List<String> sequential = returnSequential(startDateString, currentDateString);
            //计算当前日期
            CostMonitorCountQueryDto queryDto = new CostMonitorCountQueryDto();
            queryDto.setUnitId(vo.getUnitId());
            queryDto.setItemId(vo.getItemId());
            queryDto.setStartDate(startDateString);
            queryDto.setEndDate(currentDateString);
            BigDecimal currentMonitorCount = adsMonitorAccountItemMapper.queryCount(queryDto);

            queryDto.setStartDate(yearOnYears.get(0));
            queryDto.setEndDate(yearOnYears.get(1));
            BigDecimal yearToYearMonitorCount = adsMonitorAccountItemMapper.queryCount(queryDto);

            //同比增长率
            vo.setYearOnYearGrowth(calculateYearOnYearGrowth(currentMonitorCount, yearToYearMonitorCount) + "%");

            queryDto.setStartDate(sequential.get(0));
            queryDto.setEndDate(sequential.get(1));
            BigDecimal sequentialCount = adsMonitorAccountItemMapper.queryCount(queryDto);
            //环比增长率
            vo.setSequentialGrowth(calculateSequentialGrowth(currentMonitorCount, sequentialCount) + "%");
        }

    }


    @Override
    public Object queryAbnormalMonthList(CostMonitorAbnormalMonQueryDto queryDto) {
        if (null == (queryDto.getYear())) {
            LocalDate localDate = LocalDate.now();
            queryDto.setYear(localDate.getYear());
        }
        List<CostMonitorAbnormalMonVo> costMonitorAbMonths = costMonitorAbMonthMapper.queryMonitorAbnormalMonth(queryDto);
        costMonitorAbMonths = costMonitorAbMonths.stream().filter(costMonitorCenterMonthVos ->
                CostMonitorCenterStatusEnum.ABNORMAL.getCode().equals(costMonitorCenterMonthVos.getStatus())
        ).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(costMonitorAbMonths)) {
            costMonitorAbMonths.get(0).setAbnormalMonthCount(costMonitorAbMonths.size());
        }
        //返回异常数
        return costMonitorAbMonths;
    }

    @Override
    public List<CostMonitorAbnormalItemVo> queryAbnormalItemList(CostMonitorAbnormalItemQueryDto queryDto) {
        //获取当前月份 查询所有有效的核算项
        if (StringUtils.isBlank(queryDto.getMonth())) {
            String currentMonthStr = DateTimeFormatter.ofPattern(MONTH_FORMAT_STR).format(LocalDate.now());
            queryDto.setMonth(currentMonthStr);
        }
        //查询本月所有核算项
        List<CostMonitorAbnormalItemVo> resList = adsMonitorAccountItemMapper.queryMonitorAbnormalItem(queryDto);
        CostMonitorTrendQueryDto trendQueryDto = new CostMonitorTrendQueryDto();
        resList.forEach(vo -> {
            //返回监测值趋势
            trendQueryDto.setUnitId(vo.getUnitId());
            trendQueryDto.setItemId(vo.getItemId());
            trendQueryDto.setMonth(queryDto.getMonth());
            vo.setTrendList(queryListTrend(trendQueryDto, vo.getTargetValue()));
            //返回当月累计监测总值情况
            if (null != vo.getMonitorValueMonth() && StringUtils.isNotBlank(vo.getTargetValue())) {
                CostMonitorInRangeVo costMonitorInRangeVo = returnRangeInfo(vo.getMonitorValueMonth(), vo.getTargetValue());
                if (null != costMonitorInRangeVo
                        && !CostMonitorWarnStatusEnum.NORMAL.getCode().equals(costMonitorInRangeVo.getWarnStatus())) {
                    vo.setStatus(costMonitorInRangeVo.getWarnStatus());
                    vo.setWarnValue(costMonitorInRangeVo.getWarnValue());
                }
            }
        });
        //过滤掉正常的记录
        resList = resList.stream().filter(vo -> !CostMonitorWarnStatusEnum.NORMAL.getCode().equals(vo.getStatus())).collect(Collectors.toList());
        return resList;
    }

    @Override
    public List<CostMonitorAbnormalUnitVo> queryAbnormalUnitList(CostMonitorAbnormalUnitQueryDto queryDto, List<CostMonitorAbnormalItemVo> resItemList) {
        CostMonitorAbnormalItemQueryDto queryItemDto = new CostMonitorAbnormalItemQueryDto();
        queryItemDto.setUnitName(queryDto.getUnitName());
        if (StringUtils.isNotBlank(queryDto.getMonth())) {
            queryItemDto.setMonth(queryDto.getMonth());
        }
        //查询异常项
        if (resItemList == null) {
            resItemList = queryAbnormalItemList(queryItemDto);
        }
        Map<String, CostMonitorAbnormalUnitVo> unitMap = new HashMap<>();
        for (CostMonitorAbnormalItemVo item : resItemList) {
            String unitId = item.getUnitId();
            String unitName = item.getUnitName();
            String warnTime = item.getWarnTime();
            // 检查是否已存在该核算单元
            if (unitMap.containsKey(unitId)) {
                CostMonitorAbnormalUnitVo unitVo = unitMap.get(unitId);
                unitVo.setAbnormalCount(unitVo.getAbnormalCount() + 1);
                // 比较预警时间，保留最新的
                if (warnTime.compareTo(unitVo.getWarnTime()) > 0) {
                    unitVo.setWarnTime(warnTime);
                }
            } else {
                CostMonitorAbnormalUnitVo unitVo = new CostMonitorAbnormalUnitVo();
                unitVo.setUnitId(unitId);
                unitVo.setUnitName(unitName);
                unitVo.setAbnormalCount(1);
                unitVo.setWarnTime(warnTime);
                unitMap.put(unitId, unitVo);
            }
        }
        List<CostMonitorAbnormalUnitVo> costMonitorAbnormalUnitVos = new ArrayList<>(unitMap.values());
        costMonitorAbnormalUnitVos = costMonitorAbnormalUnitVos.stream().filter(vo -> {
            // 如果查询条件 queryDto.getItemNum() 不为 null，才进行筛选
            if (queryDto.getItemNum() != null) {
                return vo.getAbnormalCount() != null && vo.getAbnormalCount().equals(queryDto.getItemNum());
            }
            // 如果查询条件为 null，保留所有元素
            return true;
        }).collect(Collectors.toList());
        return costMonitorAbnormalUnitVos;
    }

    @Override
    public Object queryUnitItemDetailList(CostMonitorAbnormalItemDetailQueryDto queryDto) {
        CostMonitorAbnormalItemQueryDto costMonitorAbnormalItemQueryDto = new CostMonitorAbnormalItemQueryDto();
        costMonitorAbnormalItemQueryDto.setUnitId(queryDto.getUnitId());
        if (StringUtils.isNotBlank(queryDto.getMonth())) {
            costMonitorAbnormalItemQueryDto.setMonth(queryDto.getMonth());
        }
        costMonitorAbnormalItemQueryDto.setUnitId(queryDto.getUnitId());
        return queryAbnormalItemList(costMonitorAbnormalItemQueryDto);
    }

    /**
     * 计算年平均值
     *
     * @param unitId 科室单元id
     * @param itemId 核算项id
     * @param year   年份
     * @return 年平均值
     */
    private BigDecimal calculateYearAvg(String unitId, String itemId, int year) {
        BigDecimal bigDecimal = adsMonitorAccountItemMapper.queryYearAvg(unitId, itemId, year);
        return bigDecimal.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 保存数据至taskResult
     *
     * @author lian
     * @date 2023-09-25 17:19
     */
    @Transactional(rollbackFor = Exception.class)
    public Object saveToTaskExecuteResult(Long taskId, Long unitId) {
        //根据unitId获取核算分组信息
        CostTaskExecuteResult result = new CostTaskExecuteResult();
        CostAccountUnit costAccountUnit = cacheUtils.getCostAccountUnit(unitId);
        result.setGroupId(costAccountUnit.getAccountGroupCode());
        result.setTaskId(taskId);
        result.setUnitId(unitId);
        result.setUnitName(costAccountUnit.getName());
        //根据taskId unitId parentId = 0获取 cost_task_execute_result_index parent_id 为0的值 totalValue 获取总值 获取totalValue
        //BigDecimal bigDecimal = taskExecuteResultIndexMapper.querySum(taskId, unitId, null);
        //根据taskId获取核算任务并拿到planId
        CostAccountTask costAccountTask = costAccountTaskMapper.selectById(taskId);
        if (null != costAccountTask) {
            CostAccountTaskResultTotalValueVo taskResultTotalValueVo = new CostAccountTaskResultTotalValueVo();
            //返回公式
//            returnOverAllFormula(taskResultTotalValueVo, costAccountTask, costAccountUnit);
            //返回公式和查询返回List
            returnConfigListNew(taskResultTotalValueVo, costAccountTask, taskId, unitId);
            result.setTotalCount(taskResultTotalValueVo.getTotalValue());
            if (!CollectionUtils.isEmpty(taskResultTotalValueVo.getConfigIndexList())) {
                Gson gson = new Gson();
                result.setCalculateDetail(gson.toJson(taskResultTotalValueVo));
            }
        }
        costTaskExecuteResultService.save(result);
        return result;
    }

    /**
     * 返回configList
     *
     * @param costAccountTask        costAccountTask
     * @param taskResultTotalValueVo vo
     * @param taskId                 任务id
     * @param unitId                 核算单元id
     */
    void returnConfigList(CostAccountTaskResultTotalValueVo taskResultTotalValueVo, CostAccountTask costAccountTask, Long taskId, Long unitId) {
        Long planId = costAccountTask.getPlanId();
        LambdaQueryWrapper<CostAccountPlanConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CostAccountPlanConfig::getPlanId, planId);
        List<CostAccountPlanConfig> costAccountPlanConfigList = costAccountPlanConfigMapper.selectList(wrapper);
        if (!CollectionUtils.isEmpty(costAccountPlanConfigList)) {
            //过滤调核算公式外的核算项
            costAccountPlanConfigList = costAccountPlanConfigList.stream()
                    .filter(accountPlanConfig -> StringUtils.isNotBlank(accountPlanConfig.getConfigKey()) && taskResultTotalValueVo.getOverAllFormula().contains(accountPlanConfig.getConfigKey()))
                    .collect(Collectors.toList());
            List<CostAccountTaskIndexVo> configIndexList = new ArrayList<>();
            costAccountPlanConfigList.forEach(accountPlanConfig -> {
                CostAccountTaskIndexVo indexVo = new CostAccountTaskIndexVo();
                indexVo.setConfigKey(accountPlanConfig.getConfigKey());
                indexVo.setIndexId(accountPlanConfig.getIndexId());
                //查询indexName
                String name = costAccountIndexMapper.selectNameById(accountPlanConfig.getIndexId());
                indexVo.setConfigIndexName(name);
                configIndexList.add(indexVo);
                //查询对应的index的indexTotalValue
                BigDecimal totalValue = taskExecuteResultIndexMapper.querySum(taskId, unitId, accountPlanConfig.getIndexId());
                indexVo.setIndexTotalValue(totalValue);
            });
            taskResultTotalValueVo.setConfigIndexList(configIndexList);
        }
    }

    void returnConfigListNew(CostAccountTaskResultTotalValueVo taskResultTotalValueVo, CostAccountTask costAccountTask, Long taskId, Long unitId) {

        String detailDim = costAccountTask.getDetailDim().replaceAll("[年月]", "");

        List<Long> unitIdList = new ArrayList<>();
        unitIdList.add(unitId);
        BigDecimal totalCost = sqlUtil.geTotalCount( unitIdList, detailDim);
        //填充总值
        taskResultTotalValueVo.setTotalValue(totalCost);
        //根据科室单元id取出核算对象类型
        CostAccountUnit costAccountUnit = cacheUtils.getCostAccountUnit(unitId);
        CostAccountPlanConfigFormula costAccountPlanConfigFormula = new CostAccountPlanConfigFormula();
        LambdaQueryWrapper<CostAccountPlanConfigFormula> lambdaQueryWrapper = new LambdaQueryWrapper<CostAccountPlanConfigFormula>()
                .eq(CostAccountPlanConfigFormula::getPlanId, costAccountTask.getPlanId())
                .eq(CostAccountPlanConfigFormula::getCustomUnitId,unitId);
        costAccountPlanConfigFormula = configFormulaMapper.selectOne(lambdaQueryWrapper);
        if (costAccountPlanConfigFormula == null) {
            //根据planId查询formula返回
            String accountGroupCode = costAccountUnit.getAccountGroupCode();
            Gson gson = new Gson();
            UnitInfo unitInfo = gson.fromJson(accountGroupCode, UnitInfo.class);
            String value = UnitMapEnum.getPlanGroup(unitInfo.getValue());
            //根据方案id和核算对象类型取出公式
            LambdaQueryWrapper<CostAccountPlanConfigFormula> queryWrapper = new LambdaQueryWrapper<CostAccountPlanConfigFormula>()
                    .eq(CostAccountPlanConfigFormula::getPlanId, costAccountTask.getPlanId())
                    .eq(CostAccountPlanConfigFormula::getAccountObject, value)
                    .isNull(CostAccountPlanConfigFormula::getCustomUnitId);
            //根据方案id取出公式
            //CostAccountPlanConfigFormula costAccountPlanConfigFormula = new CostAccountPlanConfigFormula().selectOne(queryWrapper);
            costAccountPlanConfigFormula = configFormulaMapper.selectOne(queryWrapper);
        }
//
//        String accountGroupCode = costAccountUnit.getAccountGroupCode();
//        Gson gson = new Gson();
//        UnitInfo unitInfo = gson.fromJson(accountGroupCode, UnitInfo.class);
//        String value = UnitMapEnum.getPlanGroup(unitInfo.getValue());
//        //根据方案id和核算对象类型取出公式
//        LambdaQueryWrapper<CostAccountPlanConfigFormula> queryWrapper = new LambdaQueryWrapper<CostAccountPlanConfigFormula>()
//                .eq(CostAccountPlanConfigFormula::getPlanId, costAccountTask.getPlanId())
//                .eq(CostAccountPlanConfigFormula::getAccountObject, value);
//        if (value.equals(UnitMapEnum.CUSTOM.getPlanGroup())) {
//            queryWrapper.eq(CostAccountPlanConfigFormula::getCustomUnitId, unitId);
//        } else {
//            queryWrapper.isNull(CostAccountPlanConfigFormula::getCustomUnitId);
//        }
//        //根据方案id取出公式
//        CostAccountPlanConfigFormula costAccountPlanConfigFormula = new CostAccountPlanConfigFormula().selectOne(queryWrapper);
        // 创建 ObjectMapper 实例
        ObjectMapper objectMapper = new ObjectMapper();
        List<PlanConfigFormulaConfig> formulaConfigs = new ArrayList<>();
        // 使用 readValue 方法将 JSON 字符串转换为 List<PlanConfigFormulaConfig> 对象
        try {
            formulaConfigs = objectMapper.readValue(costAccountPlanConfigFormula.getConfig(), new TypeReference<List<PlanConfigFormulaConfig>>() {});
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        List<CostAccountTaskIndexVo> configIndexList = new ArrayList<>();
        for (PlanConfigFormulaConfig formulaConfig : formulaConfigs) {
            CostAccountIndex costAccountIndex = new CostAccountIndex().selectOne(new LambdaQueryWrapper<CostAccountIndex>().eq(CostAccountIndex::getName, formulaConfig.getName()).eq(CostAccountIndex::getDelFlag, "0"));
            CostAccountTaskIndexVo indexVo = new CostAccountTaskIndexVo();
            indexVo.setIndexId(costAccountIndex.getId());
            indexVo.setConfigIndexName(formulaConfig.getName());
            indexVo.setConfigKey(formulaConfig.getKey());
            //查询指标总值
            BigDecimal indexCount = sqlUtil.getIndexCount(detailDim,unitIdList, formulaConfig.getName());
            // TODO 取出指标的总值 目前置为 0
            indexVo.setIndexTotalValue(indexCount);
            configIndexList.add(indexVo);
        }
        //填充总公式

        taskResultTotalValueVo.setOverAllFormula(costAccountPlanConfigFormula.getPlanCostFormula());
        taskResultTotalValueVo.setConfigIndexList(configIndexList);

    }

    /**
     * 返回公式
     *
     * @param resVo  resVo
     * @param result result
     */
    void returnOverAllFormula(CostAccountTaskResultTotalValueVo resVo, CostAccountTask result, CostAccountUnit costAccountUnit) {
        //根据方案id和核算对象类型取出公式
        CostAccountPlanConfigFormula costAccountPlanConfigFormula = new CostAccountPlanConfigFormula();
        LambdaQueryWrapper<CostAccountPlanConfigFormula> lambdaQueryWrapper = new LambdaQueryWrapper<CostAccountPlanConfigFormula>()
                .eq(CostAccountPlanConfigFormula::getPlanId, result.getPlanId())
                .eq(CostAccountPlanConfigFormula::getCustomUnitId,costAccountUnit.getId());
        costAccountPlanConfigFormula = configFormulaMapper.selectOne(lambdaQueryWrapper);
        if (costAccountPlanConfigFormula == null) {
            //根据planId查询formula返回
            String accountGroupCode = costAccountUnit.getAccountGroupCode();
            Gson gson = new Gson();
            UnitInfo unitInfo = gson.fromJson(accountGroupCode, UnitInfo.class);
            String value = UnitMapEnum.getPlanGroup(unitInfo.getValue());
            //根据方案id和核算对象类型取出公式
            LambdaQueryWrapper<CostAccountPlanConfigFormula> queryWrapper = new LambdaQueryWrapper<CostAccountPlanConfigFormula>()
                    .eq(CostAccountPlanConfigFormula::getPlanId, result.getPlanId())
                    .eq(CostAccountPlanConfigFormula::getAccountObject, value)
                    .isNull(CostAccountPlanConfigFormula::getCustomUnitId);
            //根据方案id取出公式
            //CostAccountPlanConfigFormula costAccountPlanConfigFormula = new CostAccountPlanConfigFormula().selectOne(queryWrapper);
            costAccountPlanConfigFormula = configFormulaMapper.selectOne(queryWrapper);
        }

        if (null != costAccountPlanConfigFormula) {
            resVo.setOverAllFormula(costAccountPlanConfigFormula.getPlanCostFormula());
        }
    }

    public List<CostIndexConfigIndex> queryAllList(Long indexId) {
        List<CostIndexConfigIndex> resList = costIndexConfigIndexMapper.getByIndexId(indexId);
        if (!CollectionUtils.isEmpty(resList)) {
            List<CostIndexConfigIndex> tempList = new ArrayList<>();
            for (CostIndexConfigIndex entity : resList) {
                List<CostIndexConfigIndex> sonIndexList = queryAllList(entity.getConfigIndexId());
                if (!CollectionUtils.isEmpty(sonIndexList)) {
                    tempList.addAll(sonIndexList);
                }
            }
            resList.addAll(tempList);
        }
        return resList;
    }
}




