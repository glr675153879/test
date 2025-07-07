package com.hscloud.hs.cost.account.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hscloud.hs.cost.account.constant.enums.RationStatusNum;
import com.hscloud.hs.cost.account.mapper.AdsIncomePerformancePayMapper;
import com.hscloud.hs.cost.account.mapper.DistributionAccountStatementMapper;
import com.hscloud.hs.cost.account.mapper.DistributionStatementTargetValueMapper;
import com.hscloud.hs.cost.account.model.dto.CostAccountStatementQueryNewDto;
import com.hscloud.hs.cost.account.model.entity.CostAccountUnit;
import com.hscloud.hs.cost.account.model.entity.CostTaskExecuteResult;
import com.hscloud.hs.cost.account.model.entity.DistributionStatementTargetValue;
import com.hscloud.hs.cost.account.model.pojo.AdsIncomePerformancePay;
import com.hscloud.hs.cost.account.model.pojo.MonthRatio;
import com.hscloud.hs.cost.account.model.pojo.YearRatio;
import com.hscloud.hs.cost.account.model.vo.DistributionResultStatementDetailVo;
import com.hscloud.hs.cost.account.model.vo.DistributionResultStatementVo;
import com.hscloud.hs.cost.account.model.vo.monitorCenter.CostMonitorInRangeVo;
import com.hscloud.hs.cost.account.service.IDistributionAccountStatementService;
import com.hscloud.hs.cost.account.service.impl.monitorCenter.CostMonitorCenterServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author 小小w
 * @date 2023/11/30 15:52
 */

@Service
@RequiredArgsConstructor
public class DistributionAccountStatementServiceImpl extends ServiceImpl<DistributionAccountStatementMapper, CostTaskExecuteResult> implements IDistributionAccountStatementService {

    private final AdsIncomePerformancePayMapper adsIncomePerformancePayMapper;
    private final CostMonitorCenterServiceImpl costMonitorCenterService;
    private final DistributionStatementTargetValueMapper distributionStatementTargetValueMapper;

    /**
     * * 新增报表目标值
     *
     * @param distributionStatementTargetValue
     */
    @Override
    public void addTargetValue(DistributionStatementTargetValue distributionStatementTargetValue) {
        //先删除之前的
        final List<DistributionStatementTargetValue> values = new DistributionStatementTargetValue().selectAll();
        for (DistributionStatementTargetValue value : values) {
            distributionStatementTargetValueMapper.deleteById(value);
        }
        distributionStatementTargetValue.setStatementTypy("ALL");
        distributionStatementTargetValue.insert();
    }

    /**
     * 医院绩效总值
     *
     * @param queryDto
     * @return
     */
    @Override
    public IPage<DistributionResultStatementVo> hospitalStatementPage(CostAccountStatementQueryNewDto queryDto) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMM");
        //获取所有的科室单元绩效总值,并按时间进行过滤
        QueryWrapper<AdsIncomePerformancePay> queryWrapper = Wrappers.query();
        queryWrapper
                .select("account_period", "SUM(amount_total) as amountTotal")
                .groupBy("account_period");

        Page<AdsIncomePerformancePay> page = adsIncomePerformancePayMapper.selectPage(
                new Page<>(queryDto.getCurrent(), queryDto.getSize()),
                queryWrapper.orderByDesc("account_period")
        );

        List<AdsIncomePerformancePay> list = page.getRecords();
        List<DistributionResultStatementVo> voList = list.stream().map(adsIncomePerformancePay -> {
            DistributionResultStatementVo vo = new DistributionResultStatementVo();
            //核算周期
            vo.setDetailDim(adsIncomePerformancePay.getAccountPeriod());
            //目标值
            DistributionStatementTargetValue targetValue = new DistributionStatementTargetValue().selectOne(new LambdaQueryWrapper<DistributionStatementTargetValue>()
                    .eq(DistributionStatementTargetValue::getStatementTypy, "ALL"));
            if (targetValue != null) {
                vo.setTargetValue(targetValue.getNumber());
            }
            //获取核算总值
            final BigDecimal totalCount = new AdsIncomePerformancePay().selectList(new LambdaQueryWrapper<AdsIncomePerformancePay>()
                            .eq(AdsIncomePerformancePay::getAccountPeriod, adsIncomePerformancePay.getAccountPeriod()))
                    .stream()
                    .map(grant -> grant.getAmountTotal() == null ? BigDecimal.ZERO : grant.getAmountTotal()) // 将 amountGrant 转换为 BigDecimal
                    .reduce(BigDecimal.ZERO, BigDecimal::add);// 累加操作
            vo.setTotalCount(totalCount);
            //异常判断
            final CostMonitorInRangeVo costMonitorInRangeVo = costMonitorCenterService.returnRangeInfo(totalCount, targetValue.getNumber());
            vo.setWarnStatus(costMonitorInRangeVo.getWarnStatus());
            vo.setWarnValue(costMonitorInRangeVo.getWarnValue());
            //同比,往前推一年
            YearMonth date = YearMonth.parse(adsIncomePerformancePay.getAccountPeriod(), formatter);
            YearMonth minusYears = date.minusYears(1);
            //获取同比核算总值
            final BigDecimal totalYearCount = new AdsIncomePerformancePay().selectList(new LambdaQueryWrapper<AdsIncomePerformancePay>()
                            .eq(AdsIncomePerformancePay::getAccountPeriod, minusYears.toString().replace("-", "")))
                    .stream()
                    .map(grant -> grant.getAmountTotal() == null ? BigDecimal.ZERO : grant.getAmountTotal())  // 转换为 BigDecimal
                    .reduce(BigDecimal.ZERO, BigDecimal::add);// 累加操作
            BigDecimal yearIncrease = totalCount.subtract(totalYearCount);
            BigDecimal ratio = BigDecimal.ZERO;
            if (totalYearCount.compareTo(BigDecimal.ZERO) != 0) {
                ratio = yearIncrease.divide(totalYearCount, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
            }
            YearRatio yearRatio = new YearRatio(totalYearCount, ratio, yearIncrease);
            vo.setYearRatio(yearRatio);
            //环比,往前推一月
            YearMonth minusMonths = date.minusMonths(1);
            //获取环比核算总值
            final BigDecimal totalMonthCount = new AdsIncomePerformancePay().selectList(new LambdaQueryWrapper<AdsIncomePerformancePay>()
                            .eq(AdsIncomePerformancePay::getAccountPeriod, minusMonths.toString().replace("-", "")))
                    .stream()
                    .map(grant -> grant.getAmountTotal() == null ? BigDecimal.ZERO : grant.getAmountTotal())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);// 累加操作
            BigDecimal monthIncrease = totalCount.subtract(totalMonthCount);
            BigDecimal month = BigDecimal.ZERO;
            if (totalMonthCount.compareTo(BigDecimal.ZERO) != 0) {
                month = monthIncrease.divide(totalMonthCount, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
            }
            MonthRatio monthRatio = new MonthRatio(totalMonthCount, month, monthIncrease);
            vo.setMonthRatio(monthRatio);
            return vo;
        }).collect(Collectors.toList());
        List<DistributionResultStatementVo> result = voList.stream()
                //过滤时间
                .filter(vo -> Optional.ofNullable(queryDto.getStartTime()).map(st -> YearMonth.parse(vo.getDetailDim(), formatter).isAfter(YearMonth.parse(st, formatter)) || YearMonth.parse(vo.getDetailDim(), formatter).equals(YearMonth.parse(st, formatter))).orElse(true))
                .filter(vo -> Optional.ofNullable(queryDto.getEndTime()).map(et -> YearMonth.parse(vo.getDetailDim(), formatter).isBefore(YearMonth.parse(et, formatter)) || YearMonth.parse(vo.getDetailDim(), formatter).equals(YearMonth.parse(et, formatter))).orElse(true))
                //总核算值
                .filter(vo -> {
                    BigDecimal totalCount = vo.getTotalCount();
                    return Optional.ofNullable(queryDto.getTotalCountStart()).map(start -> totalCount.compareTo(start) >= 0).orElse(true)
                            && Optional.ofNullable(queryDto.getTotalCountEnd()).map(end -> totalCount.compareTo(end) <= 0).orElse(true);
                })
                //同比
                .filter(vo -> {
                    YearRatio yearRatio = vo.getYearRatio();
                    return Optional.ofNullable(queryDto.getYearOverYear())
                            .filter(StrUtil::isNotBlank)
                            .map(status -> {
                                if (status.equals(RationStatusNum.RISE.getCode()) && yearRatio != null) { //上升
                                    BigDecimal ratio = yearRatio.getYearRatio();
                                    return ratio.compareTo(BigDecimal.ZERO) > 0;
                                } else if (status.equals(RationStatusNum.FALL.getCode()) && yearRatio != null) { //下降
                                    BigDecimal ratio = yearRatio.getYearRatio();
                                    return ratio.compareTo(BigDecimal.ZERO) < 0;
                                } else {
                                    return false;
                                }
                            })
                            .orElse(true);
                })
                //环比
                .filter(vo -> {
                    MonthRatio monthRatio = vo.getMonthRatio();
                    return Optional.ofNullable(queryDto.getMonthOverMonth())
                            .filter(StrUtil::isNotBlank)
                            .map(status -> {
                                if (status.equals(RationStatusNum.RISE.getCode()) && monthRatio != null) { //上升
                                    BigDecimal ratio = monthRatio.getMonthRatio();
                                    return ratio.compareTo(BigDecimal.ZERO) > 0;
                                } else if (status.equals(RationStatusNum.FALL.getCode()) && monthRatio != null) { //下降
                                    BigDecimal ratio = monthRatio.getMonthRatio();
                                    return ratio.compareTo(BigDecimal.ZERO) < 0;
                                } else {
                                    return false;
                                }
                            })
                            .orElse(true);
                })
                .collect(Collectors.toList());
        int total = result.size();

        long startIndex = (queryDto.getCurrent() - 1) * queryDto.getSize();
        long endIndex = Math.min(queryDto.getCurrent() * queryDto.getSize(), result.size());

        if (startIndex < result.size()) {
            result = result.subList((int) startIndex, (int) endIndex);
        }
        return new Page<DistributionResultStatementVo>(queryDto.getCurrent(), queryDto.getSize()).setTotal(total).setRecords(result);
    }

    /**
     * 医院绩效总值报表趋势
     *
     * @param queryDto
     * @return
     */
    @Override
    public List<DistributionResultStatementVo> getHospitalTendency(CostAccountStatementQueryNewDto queryDto) {

        final List<DistributionResultStatementVo> voList = generateVoByMonth(queryDto);

        List<DistributionResultStatementVo> result = voList.stream().map(vo -> {
            //获取核算总值
            final BigDecimal totalCount = new AdsIncomePerformancePay().selectList(new LambdaQueryWrapper<AdsIncomePerformancePay>()
                            .eq(AdsIncomePerformancePay::getAccountPeriod, vo.getDetailDim()))
                    .stream()
                    .map(grant -> grant.getAmountTotal() == null ? BigDecimal.ZERO : grant.getAmountTotal())  // 将 amountGrant 转换为 BigDecimal
                    .reduce(BigDecimal.ZERO, BigDecimal::add);// 累加操作
            vo.setTotalCount(totalCount);
            return vo;
        }).collect(Collectors.toList());
        return result;
    }

    /**
     * 此方法用于生成一个以月为单位的Vo集合
     *
     * @param queryDto
     * @return
     */
    private List<DistributionResultStatementVo> generateVoByMonth(CostAccountStatementQueryNewDto queryDto) {
        List<DistributionResultStatementVo> vos = new ArrayList<>();
        String dimStart = queryDto.getStartTime();
        String dimEnd = queryDto.getEndTime();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMM");
        YearMonth currentDateTime = YearMonth.parse(dimStart, formatter);
        YearMonth endTime = YearMonth.parse(dimEnd, formatter);

        while (currentDateTime.isBefore(endTime) || currentDateTime.equals(endTime)) {

            DistributionResultStatementVo vo = new DistributionResultStatementVo();
            vo.setDetailDim(currentDateTime.toString().replace("-", ""));
            vos.add(vo);
            //时间加一月
            currentDateTime = currentDateTime.plusMonths(1);
        }
        return vos;
    }

    /**
     * 医院绩效总值报表饼状图
     *
     * @param accountTime
     * @return
     */
    @Override
    public DistributionResultStatementDetailVo.detail getHospitalFigure(String accountTime) {
        //查询该月所有科室总绩效
        List<AdsIncomePerformancePay> payList = new AdsIncomePerformancePay().selectList(new LambdaQueryWrapper<AdsIncomePerformancePay>()
                .eq(AdsIncomePerformancePay::getAccountPeriod, accountTime));

        BigDecimal amountDoc = BigDecimal.ZERO;  //医生组
        BigDecimal amountNur = BigDecimal.ZERO;  //护理组
        BigDecimal amountAdm = BigDecimal.ZERO;  //行政组
        BigDecimal amountDocTec = BigDecimal.ZERO;  //医技组
        BigDecimal amountMed = BigDecimal.ZERO;  //药剂组

        for (AdsIncomePerformancePay pay : payList) {
            String value;
            if (JSONUtil.isTypeJSON(pay.getAccountGroupCode())) {
                value = new JSONObject(pay.getAccountGroupCode()).getStr("value");
            } else {
                value = pay.getAccountGroupCode();
            }
            switch (value) {
                //医生组
                case "HSDX001":
                    amountDoc = amountDoc.add(pay.getAmountTotal() == null ? BigDecimal.ZERO : pay.getAmountTotal());
                    break;
                //护理组
                case "HSDX002":
                    amountNur = amountNur.add(pay.getAmountTotal() == null ? BigDecimal.ZERO : pay.getAmountTotal());
                    break;
                //行政组
                case "HSDX003":
                    amountAdm = amountAdm.add(pay.getAmountTotal() == null ? BigDecimal.ZERO : pay.getAmountTotal());
                    break;
                //医技组
                case "HSDX004":
                    amountDocTec = amountDocTec.add(pay.getAmountTotal() == null ? BigDecimal.ZERO : pay.getAmountTotal());
                    break;
                //药剂组
                case "HSDX006":
                    amountMed = amountMed.add(pay.getAmountTotal() == null ? BigDecimal.ZERO : pay.getAmountTotal());
                    break;
            }
        }
        DistributionResultStatementDetailVo.detail detail = new DistributionResultStatementDetailVo.detail();
        detail.setAmountDoc(amountDoc);
        detail.setAmountNur(amountNur);
        detail.setAmountDocTec(amountDocTec);
        detail.setAmountMed(amountMed);
        detail.setAmountAdm(amountAdm);
        //封装信息
        return detail;
    }

    /**
     * 医院绩效总值报表详情
     *
     * @param
     * @return
     */
    @Override
    public DistributionResultStatementDetailVo getHospitalDetail(String accountTime) {
        DistributionResultStatementDetailVo vo = new DistributionResultStatementDetailVo();
        //查询该月所有科室总绩效
        List<AdsIncomePerformancePay> payList = new AdsIncomePerformancePay().selectList(new LambdaQueryWrapper<AdsIncomePerformancePay>()
                .eq(AdsIncomePerformancePay::getAccountPeriod, accountTime));
        BigDecimal totalValue = BigDecimal.ZERO;  //总核算值
        BigDecimal unitTotalValue = BigDecimal.ZERO; //科室绩效
        BigDecimal manageTotalValue = BigDecimal.ZERO;//管理绩效
        List<DistributionResultStatementDetailVo.detail> detailList = new ArrayList<>();
        for (AdsIncomePerformancePay pay : payList) {
            DistributionResultStatementDetailVo.detail detail = new DistributionResultStatementDetailVo.detail();
            detail.setName(pay.getAccountUnitName());
            if (StrUtil.isNotEmpty(pay.getAccountGroupCode())) {
                String value;
                if (JSONUtil.isTypeJSON(pay.getAccountGroupCode())) {
                    value = new JSONObject(pay.getAccountGroupCode()).getStr("value");
                } else {
                    value = pay.getAccountGroupCode();
                }
                switch (value) {
                    //医生组
                    case "HSDX001":
                        detail.setAmountDoc(pay.getAmountTeam() == null ? BigDecimal.ZERO : pay.getAmountTeam());
                        detail.setAmountDocHead(pay.getAmountHead() == null ? BigDecimal.ZERO : pay.getAmountHead());
                        detail.setTotal(pay.getAmountTotal() == null ? BigDecimal.ZERO : pay.getAmountTotal());
                        totalValue = totalValue.add(pay.getAmountTotal() == null ? BigDecimal.ZERO : pay.getAmountTotal());
                        unitTotalValue = unitTotalValue.add(pay.getAmountTeam() == null ? BigDecimal.ZERO : pay.getAmountTeam());
                        manageTotalValue = manageTotalValue.add(pay.getAmountHead() == null ? BigDecimal.ZERO : pay.getAmountHead());
                        break;
                    //护理组
                    case "HSDX002":
                        detail.setAmountNur(pay.getAmountTeam() == null ? BigDecimal.ZERO : pay.getAmountTeam());
                        detail.setAmountNurHead(pay.getAmountHead() == null ? BigDecimal.ZERO : pay.getAmountHead());
                        detail.setTotal(pay.getAmountTotal() == null ? BigDecimal.ZERO : pay.getAmountTotal());
                        totalValue = totalValue.add(pay.getAmountTotal() == null ? BigDecimal.ZERO : pay.getAmountTotal());
                        unitTotalValue = unitTotalValue.add(pay.getAmountTeam() == null ? BigDecimal.ZERO : pay.getAmountTeam());
                        manageTotalValue = manageTotalValue.add(pay.getAmountHead() == null ? BigDecimal.ZERO : pay.getAmountHead());
                        break;
                    //行政组
                    case "HSDX003":
                        detail.setAmountAdm(pay.getAmountTeam() == null ? BigDecimal.ZERO : pay.getAmountTeam());
                        detail.setTotal(pay.getAmountTotal() == null ? BigDecimal.ZERO : pay.getAmountTotal());
                        detail.setAmountDocHead(pay.getAmountHead() == null ? BigDecimal.ZERO : pay.getAmountHead());
                        totalValue = totalValue.add(pay.getAmountTotal() == null ? BigDecimal.ZERO : pay.getAmountTotal());
                        unitTotalValue = unitTotalValue.add(pay.getAmountTeam() == null ? BigDecimal.ZERO : pay.getAmountTeam());
                        manageTotalValue = manageTotalValue.add(pay.getAmountHead() == null ? BigDecimal.ZERO : pay.getAmountHead());
                        break;
                    //医技组
                    case "HSDX004":
                        detail.setAmountDocTec(pay.getAmountTeam() == null ? BigDecimal.ZERO : pay.getAmountTeam());
                        detail.setTotal(pay.getAmountTotal() == null ? BigDecimal.ZERO : pay.getAmountTotal());
                        detail.setAmountDocHead(pay.getAmountHead() == null ? BigDecimal.ZERO : pay.getAmountHead());
                        totalValue = totalValue.add(pay.getAmountTotal() == null ? BigDecimal.ZERO : pay.getAmountTotal());
                        unitTotalValue = unitTotalValue.add(pay.getAmountTeam() == null ? BigDecimal.ZERO : pay.getAmountTeam());
                        manageTotalValue = manageTotalValue.add(pay.getAmountHead() == null ? BigDecimal.ZERO : pay.getAmountHead());
                        break;
                    //药剂组
                    case "HSDX006":
                        detail.setAmountMed(pay.getAmountTeam() == null ? BigDecimal.ZERO : pay.getAmountTeam());
                        detail.setTotal(pay.getAmountTotal() == null ? BigDecimal.ZERO : pay.getAmountTotal());
                        totalValue = totalValue.add(pay.getAmountTotal() == null ? BigDecimal.ZERO : pay.getAmountTotal());
                        unitTotalValue = unitTotalValue.add(pay.getAmountTeam() == null ? BigDecimal.ZERO : pay.getAmountTeam());
                        manageTotalValue = manageTotalValue.add(pay.getAmountHead() == null ? BigDecimal.ZERO : pay.getAmountHead());
                        break;
                }
                detailList.add(detail);
            }
        }
        //封装信息
        vo.setTotalValue(totalValue);
        vo.setUnitTotalValue(unitTotalValue);
        vo.setManageTotalValue(manageTotalValue);
        vo.setDetailList(detailList);
        return vo;
    }

    /**
     * 科室绩效总值报表
     *
     * @param queryDto
     * @return
     */
    @Override
    public IPage<DistributionResultStatementVo> unitStatementPage(CostAccountStatementQueryNewDto queryDto) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMM");
        LambdaQueryWrapper<AdsIncomePerformancePay> queryWrapper = buildQueryWrapper(queryDto);

        Page<AdsIncomePerformancePay> page = adsIncomePerformancePayMapper.selectPage(
                new Page<>(queryDto.getCurrent(), queryDto.getSize()),
                queryWrapper.orderByDesc(AdsIncomePerformancePay::getAccountPeriod)
        );

        List<AdsIncomePerformancePay> list = page.getRecords();
        List<DistributionResultStatementVo> voList = list.stream().map(adsIncomePerformancePay -> {
            DistributionResultStatementVo vo = new DistributionResultStatementVo();
            //核算周期,id,name
            vo.setDetailDim(adsIncomePerformancePay.getAccountPeriod());
            vo.setUnitId(adsIncomePerformancePay.getAccountUnitId());
            vo.setUnitName(adsIncomePerformancePay.getAccountUnitName());
            //获取核算总值
            vo.setTotalCount(adsIncomePerformancePay.getAmountTotal() == null ? BigDecimal.ZERO : adsIncomePerformancePay.getAmountTotal());
            //同比,往前推一年
            YearMonth date = YearMonth.parse(adsIncomePerformancePay.getAccountPeriod(), formatter);
            YearMonth minusYears = date.minusYears(1);
            AdsIncomePerformancePay newPay = new AdsIncomePerformancePay().selectOne(new LambdaQueryWrapper<AdsIncomePerformancePay>()
                    .eq(AdsIncomePerformancePay::getAccountPeriod, minusYears)
                    .eq(AdsIncomePerformancePay::getAccountUnitId, adsIncomePerformancePay.getAccountUnitId()));
            if (newPay != null) {
                BigDecimal yearIncrease = adsIncomePerformancePay.getAmountTotal().subtract(newPay.getAmountTotal());
                BigDecimal ratio = BigDecimal.ZERO;
                if (newPay.getAmountTotal().compareTo(BigDecimal.ZERO) != 0) {
                    ratio = yearIncrease.divide(newPay.getAmountTotal(), 4, RoundingMode.HALF_UP);
                }
                YearRatio yearRatio = new YearRatio(newPay.getAmountTotal(), yearIncrease, ratio);
                vo.setYearRatio(yearRatio);
            }
            //环比,往前推一月
            YearMonth minusMonths = date.minusMonths(1);
            AdsIncomePerformancePay monthPay = new AdsIncomePerformancePay().selectOne(new LambdaQueryWrapper<AdsIncomePerformancePay>()
                    .eq(AdsIncomePerformancePay::getAccountPeriod, minusMonths)
                    .eq(AdsIncomePerformancePay::getAccountUnitId, adsIncomePerformancePay.getAccountUnitId()));
            if (monthPay != null) {
                BigDecimal monthIncrease = adsIncomePerformancePay.getAmountTotal().subtract(monthPay.getAmountTotal());
                BigDecimal month = BigDecimal.ZERO;
                if (monthPay.getAmountTotal().compareTo(BigDecimal.ZERO) != 0) {
                    month = monthIncrease.divide(monthPay.getAmountTotal(), 4, RoundingMode.HALF_UP);
                }
                MonthRatio monthRatio = new MonthRatio(monthPay.getAmountTotal(), monthIncrease, month);
                vo.setMonthRatio(monthRatio);
            }
            return vo;
        }).collect(Collectors.toList());
        List<DistributionResultStatementVo> result = voList.stream()
                //过滤时间
                .filter(vo -> Optional.ofNullable(queryDto.getStartTime()).map(st -> YearMonth.parse(vo.getDetailDim(), formatter).isAfter(YearMonth.parse(st, formatter)) || YearMonth.parse(vo.getDetailDim(), formatter).equals(YearMonth.parse(st, formatter))).orElse(true))
                .filter(vo -> Optional.ofNullable(queryDto.getEndTime()).map(et -> YearMonth.parse(vo.getDetailDim(), formatter).isBefore(YearMonth.parse(et, formatter)) || YearMonth.parse(vo.getDetailDim(), formatter).equals(YearMonth.parse(et, formatter))).orElse(true))
                //总核算值
                .filter(vo -> {
                    BigDecimal totalCount = vo.getTotalCount();
                    return Optional.ofNullable(queryDto.getTotalCountStart()).map(start -> totalCount.compareTo(start) >= 0).orElse(true)
                            && Optional.ofNullable(queryDto.getTotalCountEnd()).map(end -> totalCount.compareTo(end) <= 0).orElse(true);
                })
                //同比
                .filter(vo -> {
                    YearRatio yearRatio = vo.getYearRatio();
                    return Optional.ofNullable(queryDto.getYearOverYear())
                            .filter(StrUtil::isNotBlank)
                            .map(status -> {
                                if (status.equals(RationStatusNum.RISE.getCode()) && yearRatio != null) { //上升
                                    BigDecimal ratio = yearRatio.getYearRatio();
                                    return ratio.compareTo(BigDecimal.ZERO) > 0;
                                } else if (status.equals(RationStatusNum.FALL.getCode()) && yearRatio != null) { //下降
                                    BigDecimal ratio = yearRatio.getYearRatio();
                                    return ratio.compareTo(BigDecimal.ZERO) < 0;
                                } else {
                                    return false;
                                }
                            })
                            .orElse(true);
                })
                //环比
                .filter(vo -> {
                    MonthRatio monthRatio = vo.getMonthRatio();
                    return Optional.ofNullable(queryDto.getMonthOverMonth())
                            .filter(StrUtil::isNotBlank)
                            .map(status -> {
                                if (status.equals(RationStatusNum.RISE.getCode()) && monthRatio != null) { //上升
                                    BigDecimal ratio = monthRatio.getMonthRatio();
                                    return ratio.compareTo(BigDecimal.ZERO) > 0;
                                } else if (status.equals(RationStatusNum.FALL.getCode()) && monthRatio != null) { //下降
                                    BigDecimal ratio = monthRatio.getMonthRatio();
                                    return ratio.compareTo(BigDecimal.ZERO) < 0;
                                } else {
                                    return false;
                                }
                            })
                            .orElse(true);
                })
                .collect(Collectors.toList());
        //int total = result.size();
        long total = page.getTotal();

        long startIndex = (queryDto.getCurrent() - 1) * queryDto.getSize();
        long endIndex = Math.min(queryDto.getCurrent() * queryDto.getSize(), result.size());

        if (startIndex < result.size()) {
            result = result.subList((int) startIndex, (int) endIndex);
        }
        return new Page<DistributionResultStatementVo>(queryDto.getCurrent(), queryDto.getSize()).setTotal(total).setRecords(result);
    }

    /**
     * * 科室绩效趋势
     *
     * @param queryDto
     * @return
     */
    @Override
    public List<List<AdsIncomePerformancePay>> getUnitTendency(CostAccountStatementQueryNewDto queryDto) {
        //先查询主对象的值
        List<List<AdsIncomePerformancePay>> result = new ArrayList<>();
        List<DistributionResultStatementVo> voList = generateVoByMonth(queryDto);
        List<AdsIncomePerformancePay> collect = voList.stream().map(vo -> {
            //查询对象
            AdsIncomePerformancePay adsIncomePerformancePay = new AdsIncomePerformancePay().selectOne(new LambdaQueryWrapper<AdsIncomePerformancePay>()
                    .eq(AdsIncomePerformancePay::getAccountPeriod, vo.getDetailDim())
                    .eq(AdsIncomePerformancePay::getAccountUnitId, queryDto.getUnitId()));
            if (adsIncomePerformancePay == null) {
                CostAccountUnit costAccountUnit = new CostAccountUnit().selectById(queryDto.getUnitId());
                adsIncomePerformancePay = new AdsIncomePerformancePay();
                adsIncomePerformancePay.setAccountPeriod(vo.getDetailDim());
                adsIncomePerformancePay.setAccountUnitId(queryDto.getUnitId());
                adsIncomePerformancePay.setAccountUnitName(costAccountUnit.getName());
                adsIncomePerformancePay.setAmountTotal(BigDecimal.ZERO);
            }
            return adsIncomePerformancePay;
        }).collect(Collectors.toList());
        result.add(collect);
        //判断是否有比较
        if (CollUtil.isNotEmpty(queryDto.getOtherUnitIds())) {
            for (Long otherUnitId : queryDto.getOtherUnitIds()) {
                List<AdsIncomePerformancePay> otherList = voList.stream().map(vo -> {
                    //查询对象
                    AdsIncomePerformancePay adsIncomePerformancePay = new AdsIncomePerformancePay().selectOne(new LambdaQueryWrapper<AdsIncomePerformancePay>()
                            .eq(AdsIncomePerformancePay::getAccountPeriod, vo.getDetailDim())
                            .eq(AdsIncomePerformancePay::getAccountUnitId, otherUnitId));
                    if (adsIncomePerformancePay == null) {
                        CostAccountUnit costAccountUnit = new CostAccountUnit().selectById(otherUnitId);
                        adsIncomePerformancePay = new AdsIncomePerformancePay();
                        adsIncomePerformancePay.setAccountPeriod(vo.getDetailDim());
                        adsIncomePerformancePay.setAccountUnitId(otherUnitId);
                        adsIncomePerformancePay.setAccountUnitName(costAccountUnit.getName());
                        adsIncomePerformancePay.setAmountTotal(BigDecimal.ZERO);
                    }
                    return adsIncomePerformancePay;
                }).collect(Collectors.toList());
                result.add(otherList);
            }
        }
        return result;
    }

    @Override
    public BigDecimal getUnitTotalCount(CostAccountStatementQueryNewDto queryDto) {
        LambdaQueryWrapper<AdsIncomePerformancePay> queryWrapper = buildQueryWrapper(queryDto);

        List<AdsIncomePerformancePay> list = adsIncomePerformancePayMapper.selectList(queryWrapper);
        return list.stream().map(AdsIncomePerformancePay::getAmountTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private LambdaQueryWrapper<AdsIncomePerformancePay> buildQueryWrapper(CostAccountStatementQueryNewDto queryDto) {
        //获取所有的科室单元绩效总值,并按时间进行过滤
        LambdaQueryWrapper<AdsIncomePerformancePay> queryWrapper = new LambdaQueryWrapper<>();
        //判断各个条件是否为空
        if (StrUtil.isNotBlank(queryDto.getUnitName())) {
            queryWrapper.eq(AdsIncomePerformancePay::getAccountUnitName, queryDto.getUnitName());
        }
        if (StrUtil.isNotBlank(queryDto.getStartTime())) {
            queryWrapper.ge(AdsIncomePerformancePay::getAccountPeriod, queryDto.getStartTime());
        }
        if (StrUtil.isNotBlank(queryDto.getEndTime())) {
            queryWrapper.le(AdsIncomePerformancePay::getAccountPeriod, queryDto.getEndTime());
        }
        if (queryDto.getTotalCountStart() != null) {
            queryWrapper.ge(AdsIncomePerformancePay::getAmountTotal, queryDto.getTotalCountStart());
        }
        if (queryDto.getTotalCountEnd() != null) {
            queryWrapper.le(AdsIncomePerformancePay::getAmountTotal, queryDto.getTotalCountEnd());
        }
        return queryWrapper;
    }
}
