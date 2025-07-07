package com.hscloud.hs.cost.account.service.impl.bi;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.hscloud.hs.cost.account.constant.Constant;
import com.hscloud.hs.cost.account.constant.enums.report.OperatorEnum;
import com.hscloud.hs.cost.account.model.dto.bi.IncomePerformancePayDTO;
import com.hscloud.hs.cost.account.model.dto.bi.MultiDeptSingleMonthDataDTO;
import com.hscloud.hs.cost.account.model.dto.bi.SimpleDataDTO;
import com.hscloud.hs.cost.account.model.dto.bi.SingleDeptMultiMonthDataDTO;
import com.hscloud.hs.cost.account.model.dto.report.CustomParamDto;
import com.hscloud.hs.cost.account.model.dto.report.ParamDto;
import com.hscloud.hs.cost.account.model.dto.report.ReportDataDto;
import com.hscloud.hs.cost.account.model.vo.DistributionResultStatementDetailVo;
import com.hscloud.hs.cost.account.model.vo.bi.HospitalFigureVo;
import com.hscloud.hs.cost.account.model.vo.bi.MultiDeptSingleMonthDataDataVo;
import com.hscloud.hs.cost.account.model.vo.bi.SimpleDataVo;
import com.hscloud.hs.cost.account.model.vo.bi.SingleDeptMultiMonthDataVo;
import com.hscloud.hs.cost.account.model.vo.report.ReportTableDataVo;
import com.hscloud.hs.cost.account.service.IDistributionAccountStatementService;
import com.hscloud.hs.cost.account.service.report.IReportHeadService;
import com.hscloud.hs.cost.account.service.report.IReportService;
import com.hscloud.hs.cost.account.service.second.IGrantUnitService;
import com.hscloud.hs.cost.account.utils.DmoUtil;
import com.pig4cloud.pigx.admin.api.entity.SysRole;
import com.pig4cloud.pigx.admin.api.feign.RemoteRoleService;
import com.pig4cloud.pigx.common.core.exception.BizException;
import com.pig4cloud.pigx.common.core.util.R;
import com.pig4cloud.pigx.common.security.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 大屏 服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BiService {

    private final IReportService reportService;

    private final IReportHeadService headService;

    private final IDistributionAccountStatementService distributionAccountStatementService;

    private final DmoUtil dmoUtil;

    private final RemoteRoleService remoteRoleService;

    private final IGrantUnitService grantUnitService;

    /**
     * 简单数据
     *
     * @param dto DTO
     * @return {@link SimpleDataVo }
     */
    public SimpleDataVo simpleData(SimpleDataDTO dto) {

        String reportCode = dto.getReportCode();
        String accountTime = dto.getAccountTime();

        SimpleDataVo result = new SimpleDataVo();

        //组装表头
        List<Tree<Long>> trees = headService.treeByReportCode(reportCode);
        result.setHeads(trees);

        ReportDataDto reportDataDto = new ReportDataDto();
        reportDataDto.setReportCode(reportCode);
        List<CustomParamDto> customParams = new ArrayList<>();
        CustomParamDto customParamDto = new CustomParamDto();
        customParamDto.setCode(Constant.CYCLE);
        customParamDto.setOperator(OperatorEnum.EQ.getOperator());
        customParamDto.setValue(accountTime);
        customParams.add(customParamDto);
        reportDataDto.setCustomParams(customParams);
        ReportTableDataVo reportTableDataVo = reportService.reportData(reportDataDto);
        result.setRows(reportTableDataVo.getRows());

        return result;
    }

    /**
     * 多部门单月数据
     *
     * @param dto DTO
     * @return {@link MultiDeptSingleMonthDataDataVo }
     */
    public MultiDeptSingleMonthDataDataVo multiDeptSingleMonthData(MultiDeptSingleMonthDataDTO dto) {

        String reportCode = dto.getReportCode();
        List<String> accountUnitIds = dto.getAccountUnitIds();
        String cycle = dto.getAccountTime();
        List<String> qoqKeys = dto.getQoqKeys();
        String sortKey = dto.getSortKey();
        Integer limit = dto.getLimit();

        List<String> accountUnitIds1 = managerAccountUnitIds(accountUnitIds);

        MultiDeptSingleMonthDataDataVo result = new MultiDeptSingleMonthDataDataVo();
        //组装表头
        List<Tree<Long>> trees = headService.treeByReportCode(reportCode);
        result.setHeads(trees);

        //1、查出所有科室这个周期的数据
        List<Map<String, Object>> currentCycleData = getCycleData(reportCode, cycle, accountUnitIds1);

        //2、查出所有科室上个周期的数据
        String beforeCycle = getBeforeCycle(cycle);
        List<Map<String, Object>> beforeData = getCycleData(reportCode, beforeCycle, accountUnitIds1);

        if (CollUtil.isEmpty(currentCycleData)) {
            return result;
        }

        //3、将数据根据sortKey排序
        sortByKey(sortKey, currentCycleData);

        //截取limitN行数据
        List<Map<String, Object>> currentSubData;
        if (Objects.nonNull(limit)) {
            currentSubData = CollUtil.sub(currentCycleData, 0, Math.min(currentCycleData.size(), limit));
        } else {
            currentSubData = currentCycleData;
        }
        result.setRows(currentSubData);

        if (CollUtil.isEmpty(beforeData) || CollUtil.isEmpty(qoqKeys)) {
            //不存在上周期数据或者不存在需要计算环比字段，则直接返回
            return result;
        }

        //4、从上周期中找到对应数据，两者比对生成环比数据
        //将list转为map
        Map<String, Map<String, Object>> beforeDataMap = beforeData.stream()
                .collect((Collectors.toMap(e -> e.get(Constant.ACCOUNT_UNIT_ID).toString(), e -> e, (e1, e2) -> e1)));
        for (Map<String, Object> current : currentSubData) {
            String accountUnitId = current.get(Constant.ACCOUNT_UNIT_ID).toString();
            Map<String, Object> before = beforeDataMap.get(accountUnitId);
            genQoqData(current, before, qoqKeys);
        }

        return result;
    }

    private List<String> managerAccountUnitIds(List<String> accountUnitIds) {
        List<Long> roleIds = SecurityUtils.getRoleIds();
        Long userId = SecurityUtils.getUser().getId();
        R<List<SysRole>> allRole = remoteRoleService.getAllRole();
        if (!allRole.isOk()) {
            throw new BizException("获取角色失败");
        }
        boolean isLeader = false;
        for (SysRole datum : allRole.getData()) {
            if (roleIds.contains(datum.getRoleId()) && StrUtil.containsAny(datum.getRoleCode(), "ROLE_ADMIN", "JXB", "YLD")) {
                isLeader = true;
            }
        }
        if (isLeader) {
            return accountUnitIds;
        }
        Set<String> canAccountUnitIds = grantUnitService.managerUnits(userId);
        if (CollUtil.isEmpty(accountUnitIds)) {
            return new ArrayList<>(canAccountUnitIds);
        }
        //canAccountUnitIds 这是可以看到的科室，accountUnitIds 这是想看到的科室
        //两者取交集
        List<String> collect = accountUnitIds.stream().filter(canAccountUnitIds::contains).collect(Collectors.toList());
        collect.add("-1");
        return collect;
    }

    /**
     * 单部门多月数据
     *
     * @param dto DTO
     * @return {@link SingleDeptMultiMonthDataVo }
     */
    public SingleDeptMultiMonthDataVo singleDeptMultiMonthData(SingleDeptMultiMonthDataDTO dto) {

        String reportCode = dto.getReportCode();
        String accountUnitId = dto.getAccountUnitId();
        String cycle = dto.getAccountTime();
        List<String> qoqKeys = dto.getQoqKeys();

        SingleDeptMultiMonthDataVo result = new SingleDeptMultiMonthDataVo();
        //组装表头
        List<Tree<Long>> trees = headService.treeByReportCode(reportCode);
        result.setHeads(trees);

        //获取传入周期及之前的数据
        List<Map<String, Object>> allCycleData = getCurrentAndBeforeData(reportCode, cycle, accountUnitId);

        if (CollUtil.isEmpty(allCycleData)) {
            return result;
        }
        //3、将数据根据周期排序
        sortByKey(Constant.CYCLE, allCycleData);

        result.setRows(allCycleData);

        if (CollUtil.isEmpty(qoqKeys)) {
            //不存在上周期数据或者不存在需要计算环比字段，则直接返回
            return result;
        }

        //4、从上周期中找到对应数据，两者比对生成环比数据
        //将list转为map
        Map<String, Map<String, Object>> beforeDataMap = allCycleData.stream()
                .collect((Collectors.toMap(e -> e.get(Constant.CYCLE).toString(), e -> e, (e1, e2) -> e1)));
        for (Map<String, Object> current : allCycleData) {
            String currentCycle = current.get(Constant.CYCLE).toString();
            String beforeCycle = getBeforeCycle(currentCycle);
            Map<String, Object> before = beforeDataMap.get(beforeCycle);
            genQoqData(current, before, qoqKeys);
        }

        return result;
    }


    /**
     * 按指定key排序
     *
     * @param sortKey          排序键
     * @param currentCycleData 当前周期数据
     */
    private static void sortByKey(String sortKey, List<Map<String, Object>> currentCycleData) {
        if (StrUtil.isNotBlank(sortKey)) {
            currentCycleData.sort((o1, o2) -> {
                if (!o1.containsKey(sortKey) || Objects.isNull(o1.get(sortKey))) {
                    return -1;
                }
                if (!o2.containsKey(sortKey) || Objects.isNull(o2.get(sortKey))) {
                    return 1;
                }
                try {
                    BigDecimal value1 = new BigDecimal(o1.get(sortKey).toString());
                    BigDecimal value2 = new BigDecimal(o2.get(sortKey).toString());
                    return value2.compareTo(value1);
                } catch (Exception e) {
                    log.info("o1：{}", JSON.toJSONString(o1));
                    log.info("o2：{}", JSON.toJSONString(o2));
                    throw new RuntimeException(e);
                }
            });
        }
    }


    /**
     * 计算环比数据
     *
     * @param current 本周期数据
     * @param before  上周期数据
     * @param qoqKeys QoQ 键
     */
    private static void genQoqData(@NotNull Map<String, Object> current, Map<String, Object> before, List<String> qoqKeys) {
        for (String qoqKey : qoqKeys) {
            if (!current.containsKey(qoqKey) || Objects.isNull(before) || !before.containsKey(qoqKey)) {
                continue;
            }
            try {
                BigDecimal currentVal = new BigDecimal(current.get(qoqKey).toString());
                BigDecimal beforeVal = new BigDecimal(before.get(qoqKey).toString());
                BigDecimal qoqRate = calcQoq(beforeVal, currentVal);
                BigDecimal qoqValue = currentVal.subtract(beforeVal);
                current.put(qoqKey + Constant.QOQ_RATE_SUFFIX, qoqRate);
                current.put(qoqKey + Constant.QOQ_VALUE_SUFFIX, qoqValue);
            } catch (Exception e) {
                log.error("计算环比失败 current:{} before:{} qoqKey{}, {}", JSON.toJSONString(current), JSON.toJSONString(before), JSON.toJSONString(qoqKey), e.getMessage());
            }
        }
    }


    /**
     * 获取上周期
     *
     * @param cycle 周期
     * @return {@link String }
     */
    private String getBeforeCycle(String cycle) {
        return DateUtil.format(DateUtil.offsetMonth(DateUtil.parse(cycle, "yyyyMM"), -1), "yyyyMM");
    }

    /**
     * 获取当前和之前数据
     *
     * @param reportCode    报表编码
     * @param cycle         周期
     * @param accountUnitId 核算单元id
     * @return {@link List }<{@link Map }<{@link String }, {@link Object }>>
     */
    private List<Map<String, Object>> getCurrentAndBeforeData(String reportCode, String cycle, String accountUnitId) {
        ReportDataDto reportDataDto = new ReportDataDto();
        reportDataDto.setReportCode(reportCode);
        List<CustomParamDto> customParams = new ArrayList<>();
        CustomParamDto customParamDto = new CustomParamDto();
        customParamDto.setOperator(OperatorEnum.LE.getOperator());
        customParamDto.setCode(Constant.CYCLE);
        customParamDto.setValue(cycle);
        customParams.add(customParamDto);
        reportDataDto.setCustomParams(customParams);
        List<ParamDto> params = new ArrayList<>();
        ParamDto param = new ParamDto();
        param.setCode("accountUnitId");
        param.setValue(accountUnitId);
        params.add(param);
        reportDataDto.setParams(params);
        ReportTableDataVo reportTableDataVo = reportService.reportData(reportDataDto);
        return reportTableDataVo.getRows();
    }

    /**
     * 获取周期数据
     *
     * @param reportCode     报表编码
     * @param cycle          周期
     * @param accountUnitIds 核算单元ids
     * @return {@link List }<{@link Map }<{@link String }, {@link Object }>>
     */
    private List<Map<String, Object>> getCycleData(String reportCode, String cycle, List<String> accountUnitIds) {
        ReportDataDto reportDataDto = new ReportDataDto();
        reportDataDto.setReportCode(reportCode);
        List<CustomParamDto> customParams = new ArrayList<>();
        CustomParamDto customParamDto = new CustomParamDto();
        customParamDto.setOperator(OperatorEnum.EQ.getOperator());
        customParamDto.setCode(Constant.CYCLE);
        customParamDto.setValue(cycle);
        customParams.add(customParamDto);
        CustomParamDto customParamDto2 = new CustomParamDto();
        customParamDto2.setOperator(OperatorEnum.IN.getOperator());
        customParamDto2.setCode(Constant.ACCOUNT_UNIT_ID);
        customParamDto2.setValue(accountUnitIds);
        customParams.add(customParamDto2);
        reportDataDto.setCustomParams(customParams);
        ReportTableDataVo reportTableDataVo = reportService.reportData(reportDataDto);
        return reportTableDataVo.getRows();
    }

    public HospitalFigureVo getHospitalFigureQoq(String accountTime) {
        String beforeCycle = getBeforeCycle(accountTime);
        DistributionResultStatementDetailVo.detail nowData = distributionAccountStatementService.getHospitalFigure(accountTime);
        DistributionResultStatementDetailVo.detail beforeData = distributionAccountStatementService.getHospitalFigure(beforeCycle);
        HospitalFigureVo hospitalFigureVo = new HospitalFigureVo();
        hospitalFigureVo.getAmountDoc().setValue(nowData.getAmountDoc());
        hospitalFigureVo.getAmountNur().setValue(nowData.getAmountNur());
        hospitalFigureVo.getAmountDocTec().setValue(nowData.getAmountDocTec());
        hospitalFigureVo.getAmountMed().setValue(nowData.getAmountMed());
        hospitalFigureVo.getAmountAdm().setValue(nowData.getAmountAdm());
        hospitalFigureVo.getAmountDoc().setQoq(calcQoq(beforeData.getAmountDoc(), nowData.getAmountDoc()));
        hospitalFigureVo.getAmountNur().setQoq(calcQoq(beforeData.getAmountNur(), nowData.getAmountNur()));
        hospitalFigureVo.getAmountDocTec().setQoq(calcQoq(beforeData.getAmountDocTec(), nowData.getAmountDocTec()));
        hospitalFigureVo.getAmountMed().setQoq(calcQoq(beforeData.getAmountMed(), nowData.getAmountMed()));
        hospitalFigureVo.getAmountAdm().setQoq(calcQoq(beforeData.getAmountAdm(), nowData.getAmountAdm()));
        return hospitalFigureVo;
    }

    public static BigDecimal calcQoq(BigDecimal beforeValue, BigDecimal nowValue) {
        if (Objects.isNull(beforeValue) || Objects.isNull(nowValue)) {
            return null;
        }
        if (beforeValue.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        } else {
            return nowValue.subtract(beforeValue).divide(beforeValue, 4, RoundingMode.HALF_UP).multiply(new BigDecimal(100));
        }
    }

    public IncomePerformancePayDTO performView(String accountTime) {
        List<IncomePerformancePayDTO> performances = dmoUtil.performance(accountTime);
        if (CollUtil.isEmpty(performances)) {
            return new IncomePerformancePayDTO();
        }
        String beforeCycle = getBeforeCycle(accountTime);
        List<IncomePerformancePayDTO> performancesBefore = dmoUtil.performance(beforeCycle);
        IncomePerformancePayDTO payDTO = performances.get(0);
        if (Objects.nonNull(payDTO.getPerformanceIncomeRatio())) {
            //转为百分数
            payDTO.setPerformanceIncomeRatio(payDTO.getPerformanceIncomeRatio().multiply(new BigDecimal(100)).setScale(2, RoundingMode.HALF_UP));
        }
        if (CollUtil.isEmpty(performancesBefore)) {
            return payDTO;
        }
        if (Objects.nonNull(payDTO.getQyTotal()) && Objects.nonNull(performancesBefore.get(0).getQyTotal())) {
            payDTO.setQyTotalQoqRate(calcQoq(performancesBefore.get(0).getQyTotal(), payDTO.getQyTotal()));
        }
        if (Objects.nonNull(payDTO.getQyAvg()) && Objects.nonNull(performancesBefore.get(0).getQyAvg())) {
            payDTO.setQyAvgQoqRate(calcQoq(performancesBefore.get(0).getQyAvg(), payDTO.getQyAvg()));
        }
        return payDTO;
    }

    public static void main(String[] args) {
        System.out.println(new BigDecimal(100).multiply(new BigDecimal(100)).setScale(2, RoundingMode.HALF_UP));
    }

}
