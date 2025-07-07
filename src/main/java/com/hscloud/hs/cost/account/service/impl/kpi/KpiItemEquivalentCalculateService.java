package com.hscloud.hs.cost.account.service.impl.kpi;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.bestvike.linq.Linq;
import com.hscloud.hs.cost.account.constant.enums.EnableEnum;
import com.hscloud.hs.cost.account.constant.enums.YesNoEnum;
import com.hscloud.hs.cost.account.constant.enums.kpi.CaliberEnum;
import com.hscloud.hs.cost.account.constant.enums.kpi.EquivalentDistributeEnum;
import com.hscloud.hs.cost.account.constant.enums.kpi.EquivalentTaskStatusEnum;
import com.hscloud.hs.cost.account.mapper.kpi.KpiItemEquivalentTaskMapper;
import com.hscloud.hs.cost.account.model.dto.kpi.KpiConfigSearchDto;
import com.hscloud.hs.cost.account.model.dto.kpi.KpiItemBatchCalculateDTO;
import com.hscloud.hs.cost.account.model.dto.kpi.KpiItemEquivalentTaskDTO;
import com.hscloud.hs.cost.account.model.entity.kpi.*;
import com.hscloud.hs.cost.account.model.vo.kpi.KpiConfigVO;
import com.hscloud.hs.cost.account.service.kpi.IKpiConfigService;
import com.hscloud.hs.cost.account.service.kpi.IKpiItemEquivalentService;
import com.pig4cloud.pigx.common.core.exception.BizException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class KpiItemEquivalentCalculateService {

    private final IKpiItemEquivalentService iKpiItemEquivalentService;
    private final KpiItemResultService kpiItemResultService;
    private final KpiUserAttendanceService kpiUserAttendanceService;
    private final KpiItemService kpiItemService;
    private final IKpiConfigService iKpiConfigService;
    private final KpiItemEquivalentConfigService kpiItemEquivalentConfigService;
    private final KpiItemEquivalentTaskService kpiItemEquivalentTaskService;

    public void eqItemBatchCalculate(KpiItemBatchCalculateDTO dto) {
        Long period = dto.getPeriod();
        if (null == period) {
            String var1 = iKpiConfigService.getLastCycle(true);
            if (!StringUtils.hasLength(var1)) {
                throw new BizException("未查询到计算周期");
            } else {
                period = Long.valueOf(var1.replace("-", "").substring(0, 6));
            }
        }

        Long finalPeriod = period;
        String kpiItemIds = dto.getItemIds();
        List<Long> ids = new ArrayList<>();

        if (StringUtils.hasText(kpiItemIds)) {
            ids = Arrays.stream(kpiItemIds.split(","))
                    .map(Long::valueOf).collect(Collectors.toList());
        }

        List<KpiItem> kpiItems = kpiItemService.list(Wrappers.<KpiItem>lambdaQuery()
                .eq(KpiItem::getEquivalentFlag, YesNoEnum.YES.getValue())
                .eq(KpiItem::getDelFlag, YesNoEnum.NO.getValue())
                .eq(KpiItem::getStatus, EnableEnum.ENABLE.getType())
                .eq(KpiItem::getBusiType, dto.getBusiType())
                .in(!CollectionUtils.isEmpty(ids), KpiItem::getId, ids));

        boolean isAll;
        if (CollectionUtils.isEmpty(ids)) {
            ids = kpiItems.stream().map(KpiItem::getId).collect(Collectors.toList());
            isAll = true;
        } else {
            isAll = false;
        }

        if (CollectionUtils.isEmpty(ids)) {
            throw new BizException("未查询到需要计算的当量核算项");
        }

        KpiConfigSearchDto kpiConfigSearchDto = new KpiConfigSearchDto();
        kpiConfigSearchDto.setPeriod(period);
        KpiConfigVO configVO = iKpiConfigService.getConfig(kpiConfigSearchDto);
        if (YesNoEnum.YES.getCode().equals(configVO.getEquivalentFlag())) {
            throw new BizException("当量已锁定无法计算");
        }

        if ("0".equals(configVO.getEquivalentIndexFlag())) {
            throw new BizException("数据抽取中请稍等");
        }

        iKpiConfigService.update(Wrappers.<KpiConfig>lambdaUpdate()
                .set(KpiConfig::getEquivalentIndexFlag, "0")
                .eq(KpiConfig::getPeriod, period));

        List<Long> finalIds = ids;
        new Thread(() -> {
            List<Future<Boolean>> futures = kpiItemService.itemBatchCalculate(finalIds, dto.getBusiType(), finalPeriod, YesNoEnum.YES.getValue());
            try {
                for (Future<Boolean> future : futures) {
                    if (!future.get()) {
                        throw new BizException("数据调取失败");
                    }
                }
            } catch (Exception e) {
                log.error("数据调取失败", e);

                iKpiConfigService.update(Wrappers.<KpiConfig>lambdaUpdate()
                        .set(KpiConfig::getEquivalentIndexFlag, "9")
                        .set(KpiConfig::getIndexFlag, "9")
                        .eq(KpiConfig::getPeriod, finalPeriod));

                return;
            }

            try {
                Map<String, KpiItem> itemMap = kpiItems.stream()
                        .collect(Collectors.toMap(KpiItem::getCode, Function.identity(), (item1, item2) -> item1));

                List<KpiItemEquivalentConfig> configList = kpiItemEquivalentConfigService.list(Wrappers.<KpiItemEquivalentConfig>lambdaQuery()
                        .eq(KpiItemEquivalentConfig::getDelFlag, "0"));

                List<KpiItemResult> resultList = kpiItemResultService.list(Wrappers.<KpiItemResult>lambdaQuery()
                        .eq(KpiItemResult::getPeriod, finalPeriod)
                        .eq(KpiItemResult::getBusiType, dto.getBusiType())
                        .in(KpiItemResult::getCode, itemMap.keySet()));

                DateTime yyyyMM = DateUtil.parse(String.valueOf(finalPeriod), "yyyyMM");
                String lastPeriod = DateUtil.format(DateUtil.offsetMonth(yyyyMM, -1), "yyyyMM");

                List<KpiItemEquivalent> lastEquivalentList = iKpiItemEquivalentService.list(Wrappers.<KpiItemEquivalent>lambdaQuery()
                        .eq(KpiItemEquivalent::getPeriod, lastPeriod));

                List<KpiUserAttendance> userAttendanceList = kpiUserAttendanceService.list(Wrappers.<KpiUserAttendance>lambdaQuery()
                        .eq(KpiUserAttendance::getPeriod, finalPeriod)
                        .eq(KpiUserAttendance::getDelFlag, YesNoEnum.NO.getValue())
                        .eq(KpiUserAttendance::getBusiType, "1"));

                List<KpiItemEquivalent> records = new ArrayList<>();

                List<String> deptItemCodes = kpiItems.stream()
                        .filter(kpiItem -> CaliberEnum.DEPT.getType().equals(kpiItem.getCaliber()))
                        .map(KpiItem::getCode).collect(Collectors.toList());
                if (!CollectionUtils.isEmpty(deptItemCodes)) {
                    List<KpiItemResult> deptResultList = Linq.of(resultList)
                            .where(x -> deptItemCodes.contains(x.getCode()) && x.getDeptId() != null && x.getDeptId() != 0)
                            .toList();

                    List<KpiItemEquivalentConfig> configs = configList.stream()
                            .filter(x -> deptItemCodes.contains(x.getItemCode())).collect(Collectors.toList());
                    List<Long> unitIds = configs.stream().map(KpiItemEquivalentConfig::getAccountUnitId).collect(Collectors.toList());
                    List<KpiUserAttendance> userList = userAttendanceList.stream()
                            .filter(x -> unitIds.contains(x.getAccountUnit())).collect(Collectors.toList());
                    List<KpiItemEquivalent> lastEquivalents = lastEquivalentList.stream()
                            .filter(x -> unitIds.contains(x.getAccountUnitId())).collect(Collectors.toList());

                    processDeptResult(deptResultList, finalPeriod, itemMap, userList, lastEquivalents, configs, records);
                }

                List<String> peopleItemCodes = kpiItems.stream()
                        .filter(kpiItem -> CaliberEnum.PEOPLE.getType().equals(kpiItem.getCaliber()))
                        .map(KpiItem::getCode).collect(Collectors.toList());
                if (!CollectionUtils.isEmpty(peopleItemCodes)) {
                    List<KpiItemResult> peopleResultList = Linq.of(resultList)
                            .where(x -> peopleItemCodes.contains(x.getCode())
                                    && x.getDeptId() != null && x.getDeptId() != 0
                                    && x.getUserId() != null && x.getUserId() != 0).toList();

                    List<KpiItemEquivalentConfig> configs = configList.stream()
                            .filter(x -> peopleItemCodes.contains(x.getItemCode())).collect(Collectors.toList());
                    List<Long> unitIds = configs.stream().map(KpiItemEquivalentConfig::getAccountUnitId).collect(Collectors.toList());
                    List<KpiUserAttendance> userList = userAttendanceList.stream()
                            .filter(x -> unitIds.contains(x.getAccountUnit())).collect(Collectors.toList());
                    List<KpiItemEquivalent> lastEquivalents = lastEquivalentList.stream()
                            .filter(x -> unitIds.contains(x.getAccountUnitId())).collect(Collectors.toList());
                    if (!CollectionUtils.isEmpty(configs)) {
                        processPeopleResult(peopleResultList, finalPeriod, itemMap, userList, lastEquivalents, configs, records);
                    }
                }

                iKpiItemEquivalentService.saveData(finalPeriod, itemMap.keySet(), records, isAll);

                kpiItemEquivalentTaskService.reIssueTask(finalPeriod, finalIds, null);

            } catch (Exception e) {
                log.error("当量数据调取失败", e);
                iKpiConfigService.update(Wrappers.<KpiConfig>lambdaUpdate()
                        .set(KpiConfig::getEquivalentIndexFlag, "9")
                        .set(KpiConfig::getIndexFlag, "1")
                        .eq(KpiConfig::getPeriod, finalPeriod));
            }
        }).start();

    }

    private void processPeopleResult(List<KpiItemResult> peopleResultList, Long period, Map<String, KpiItem> itemMap,
                                     List<KpiUserAttendance> userList, List<KpiItemEquivalent> lastEquivalentList,
                                     List<KpiItemEquivalentConfig> configList, List<KpiItemEquivalent> records) {
        configList.stream().collect(Collectors.groupingBy(KpiItemEquivalentConfig::getAccountUnitId))
                .forEach((unitId, configs) -> {
                    Set<Long> userIds = userList.stream()
                            .filter(x -> x.getAccountUnit().equals(unitId))
                            .map(KpiUserAttendance::getUserId)
                            .collect(Collectors.toSet());

                    List<KpiItemResult> unitResults = peopleResultList.stream().filter(x -> x.getDeptId().equals(unitId))
                            .collect(Collectors.toList());

                    if (CollectionUtils.isEmpty(unitResults)) {
                        createZeroUnitEquivalent(period, itemMap, records, unitId, configs, userIds);
                    } else {
                        Map<String, KpiItemEquivalent> equivalentMap = new HashMap<>();
                        Map<String, KpiItemEquivalent> userEquivalentMap = new HashMap<>();
                        Set<String> processedItemCodes = new HashSet<>();
                        Map<String, Set<Long>> processedUserMap = new HashMap<>();

                        for (KpiItemResult itemResult : unitResults) {
                            Long userId = itemResult.getUserId();
                            BigDecimal value = itemResult.getValue();
                            String code = itemResult.getCode();
                            KpiItem kpiItem = itemMap.get(code);
                            if (kpiItem == null) {
                                continue;
                            }

                            KpiItemEquivalentConfig config = Linq.of(configs).where(x -> x.getItemCode().equals(code)).firstOrDefault();
                            if (config == null) {
                                // 有核算项结果 但没有配置当量 标化当量为0 默认继承
                                /*config = new KpiItemEquivalentConfig();
                                config.setInheritFlag(YesNoEnum.YES.getValue());*/
                                continue;
                            }

                            KpiItemEquivalent equivalent = equivalentMap.get(code);
                            if (equivalent == null) {
                                equivalent = createEquivalent(kpiItem, code, period, unitId, 0L,
                                        BigDecimal.ZERO, CaliberEnum.DEPT.getType(), null, config);
                                equivalentMap.put(code, equivalent);
                            }

                            KpiItemEquivalent lastEquivalent = null;
                            if (YesNoEnum.YES.getValue().equals(config.getInheritFlag())) {
                                lastEquivalent = Linq.of(lastEquivalentList).where(x -> x.getCode().equals(code)
                                        && CaliberEnum.PEOPLE.getType().equals(x.getEquivalentType())
                                        && x.getAccountUnitId().equals(unitId)
                                        && x.getUserId().equals(userId)).firstOrDefault();
                            }

                            KpiItemEquivalent userEquivalent;
                            String key = code + "_" + userId;
                            if (userEquivalentMap.containsKey(key)) {
                                userEquivalent = userEquivalentMap.get(key);
                                userEquivalent.setTotalWorkload(userEquivalent.getTotalWorkload().add(value));
                            } else {
                                userEquivalent = createEquivalent(kpiItem, code, period, unitId, userId, value,
                                        CaliberEnum.PEOPLE.getType(), lastEquivalent, config);
                            }
                            userEquivalentMap.put(key, userEquivalent);

                            equivalent.setTotalWorkload(equivalent.getTotalWorkload().add(value));

                            if (processedUserMap.containsKey(code)) {
                                processedUserMap.get(code).add(userId);
                            } else {
                                Set<Long> userSet = new HashSet<>();
                                userSet.add(userId);
                                processedUserMap.put(code, userSet);
                            }

                            processedItemCodes.add(code);
                        }
                        records.addAll(userEquivalentMap.values());
                        records.addAll(equivalentMap.values());

                        // 未处理的用户
                        for (Map.Entry<String, Set<Long>> entry : processedUserMap.entrySet()) {
                            String code = entry.getKey();
                            Set<Long> processedUserIds = entry.getValue();
                            KpiItemEquivalent equivalent = equivalentMap.get(code);
                            if (equivalent == null) {
                                continue;
                            }
                            Set<Long> users = userIds.stream()
                                    .filter(userId -> !processedUserIds.contains(userId))
                                    .collect(Collectors.toSet());
                            createZeroUserEquivalent(records, users, equivalent);
                        }
                        // 未处理的核算项
                        processOtherItems(period, itemMap, records, unitId, configs, processedItemCodes, userIds);
                    }
                });
    }

    private void createZeroUserEquivalent(List<KpiItemEquivalent> records, Set<Long> users, KpiItemEquivalent equivalent) {
        for (Long userId : users) {
            KpiItemEquivalent userEquivalent = new KpiItemEquivalent();
            BeanUtils.copyProperties(equivalent, userEquivalent);
            userEquivalent.setUserId(userId);
            userEquivalent.setTotalWorkload(BigDecimal.ZERO);
            userEquivalent.setEquivalentType(CaliberEnum.PEOPLE.getType());
            records.add(userEquivalent);
        }
    }

    private void processDeptResult(List<KpiItemResult> deptResultList, Long period, Map<String, KpiItem> itemMap,
                                   List<KpiUserAttendance> userList, List<KpiItemEquivalent> lastEquivalentList,
                                   List<KpiItemEquivalentConfig> configList,
                                   List<KpiItemEquivalent> records) {
        configList.stream().collect(Collectors.groupingBy(KpiItemEquivalentConfig::getAccountUnitId))
                .forEach((unitId, configs) -> {
                    Set<Long> userIds = userList.stream()
                            .filter(x -> x.getAccountUnit().equals(unitId))
                            .map(KpiUserAttendance::getUserId)
                            .collect(Collectors.toSet());

                    List<KpiItemResult> unitResults = deptResultList.stream().filter(x -> x.getDeptId().equals(unitId))
                            .collect(Collectors.toList());

                    if (CollectionUtils.isEmpty(unitResults)) {
                        createZeroUnitEquivalent(period, itemMap, records, unitId, configs, userIds);
                    } else {
                        Set<String> processedItemCodes = new HashSet<>();
                        Map<String, KpiItemEquivalent> equivalentMap = new HashMap<>();

                        for (KpiItemResult itemResult : unitResults) {
                            String code = itemResult.getCode();
                            Long deptId = itemResult.getDeptId();
                            Long itemResultUserId = itemResult.getUserId();
                            BigDecimal value = itemResult.getValue();

                            KpiItem kpiItem = itemMap.get(code);
                            if (kpiItem == null) {
                                continue;
                            }

                            KpiItemEquivalentConfig config = Linq.of(configList).where(x -> x.getItemCode().equals(code)
                                    && x.getAccountUnitId().equals(deptId)).firstOrDefault();
                            if (config == null) {
                                /*config = new KpiItemEquivalentConfig();
                                config.setInheritFlag(YesNoEnum.YES.getValue());*/
                                continue;
                            }

                            KpiItemEquivalent lastEquivalent = null;
                            if (YesNoEnum.YES.getValue().equals(config.getInheritFlag())) {
                                lastEquivalent = Linq.of(lastEquivalentList)
                                        .where(x -> x.getCode().equals(code)
                                                && CaliberEnum.DEPT.getType().equals(x.getEquivalentType())
                                                && x.getAccountUnitId().equals(deptId))
                                        .firstOrDefault();
                            }

                            KpiItemEquivalent equivalent;
                            String key = code + "_" + unitId;
                            if (equivalentMap.containsKey(key)) {
                                equivalent = equivalentMap.get(key);
                                equivalent.setTotalWorkload(equivalent.getTotalWorkload().add(value));
                            } else {
                                equivalent = createEquivalent(kpiItem, code, period, deptId, itemResultUserId, value,
                                        CaliberEnum.DEPT.getType(), lastEquivalent, config);
                            }
                            equivalentMap.put(key, equivalent);

                            processedItemCodes.add(code);
                        }
                        records.addAll(equivalentMap.values());
                        // 分配到用户
                        for (KpiItemEquivalent equivalent : equivalentMap.values()) {
                            Map<Long, BigDecimal> lastUserCoefficientMap = null;
                            if (EquivalentDistributeEnum.COEFFICIENT.getCode().equals(equivalent.getDistributeType())) {
                                lastUserCoefficientMap = lastEquivalentList.stream()
                                        .filter(x -> x.getCode().equals(equivalent.getCode())
                                                && CaliberEnum.PEOPLE.getType().equals(x.getEquivalentType())
                                                && x.getAccountUnitId().equals(equivalent.getAccountUnitId()))
                                        .collect(Collectors.toMap(KpiItemEquivalent::getUserId, KpiItemEquivalent::getCoefficient, (v1, v2) -> v1));
                            }

                            distributeToUsers(userIds, equivalent, equivalent.getTotalWorkload(), records, lastUserCoefficientMap);
                        }

                        // 未处理的核算项
                        processOtherItems(period, itemMap, records, unitId, configs, processedItemCodes, userIds);
                    }
                });
    }

    private void processOtherItems(Long period, Map<String, KpiItem> itemMap, List<KpiItemEquivalent> records,
                                   Long unitId, List<KpiItemEquivalentConfig> configs, Set<String> processedItemCodes, Set<Long> userIds) {
        Set<String> unprocessedItemCodes = configs.stream()
                .map(KpiItemEquivalentConfig::getItemCode)
                .filter(code -> !processedItemCodes.contains(code))
                .collect(Collectors.toSet());
        for (String code : unprocessedItemCodes) {
            KpiItem kpiItem = itemMap.get(code);
            if (kpiItem == null) {
                continue;
            }
            KpiItemEquivalentConfig config = Linq.of(configs)
                    .where(x -> x.getItemCode().equals(code)).firstOrDefault();

            KpiItemEquivalent equivalent = createEquivalent(kpiItem, code, period, unitId, 0L,
                    BigDecimal.ZERO, CaliberEnum.DEPT.getType(), null, config);
            records.add(equivalent);

            createZeroUserEquivalent(records, userIds, equivalent);
        }
    }

    private void createZeroUnitEquivalent(Long period, Map<String, KpiItem> itemMap, List<KpiItemEquivalent> records,
                                          Long unitId, List<KpiItemEquivalentConfig> configs, Set<Long> userIds) {
        for (KpiItemEquivalentConfig config : configs) {
            String code = config.getItemCode();
            KpiItem kpiItem = itemMap.get(code);
            if (kpiItem == null) {
                continue;
            }

            KpiItemEquivalent equivalent = createEquivalent(kpiItem, code, period, unitId, 0L,
                    BigDecimal.ZERO, CaliberEnum.DEPT.getType(), null, config);
            records.add(equivalent);

            createZeroUserEquivalent(records, userIds, equivalent);
        }
    }


    private KpiItemEquivalent createEquivalent(KpiItem kpiItem, String code, Long period, Long deptId, Long userId,
                                               BigDecimal value, String equivalentType, KpiItemEquivalent lastEquivalent, KpiItemEquivalentConfig config) {
        KpiItemEquivalent equivalent = new KpiItemEquivalent();
        equivalent.setItemId(kpiItem.getId());
        equivalent.setCode(code);
        equivalent.setPeriod(period);
        equivalent.setAssignFlag(kpiItem.getAssignFlag());
        equivalent.setUnit(kpiItem.getUnit());
        equivalent.setAccountUnitId(deptId);
        equivalent.setUserId(userId);
        equivalent.setTotalWorkload(value);
        equivalent.setEquivalentType(equivalentType);
        equivalent.setItemName(kpiItem.getItemName());
        equivalent.setAcqMethod(kpiItem.getAcqMethod());
        equivalent.setCaliber(kpiItem.getCaliber());

        if (CaliberEnum.DEPT.getType().equals(kpiItem.getCaliber()) &&
                YesNoEnum.YES.getValue().equals(equivalent.getAssignFlag())) {
            if (YesNoEnum.YES.getValue().equals(config.getInheritFlag()) && lastEquivalent != null) {
                equivalent.setCoefficient(lastEquivalent.getCoefficient());
                equivalent.setDistributeType(lastEquivalent.getDistributeType());
            } else {
                equivalent.setDistributeType(EquivalentDistributeEnum.AVERAGE.getCode());
            }
        }

        return equivalent;
    }

    private void distributeToUsers(Set<Long> userIds, KpiItemEquivalent equivalent, BigDecimal value,
                                   List<KpiItemEquivalent> records, Map<Long, BigDecimal> lastUserCoefficientMap) {
        if (CollectionUtils.isEmpty(userIds)) {
            return;
        }

        BigDecimal totalCoefficient = BigDecimal.ZERO;
        for (Long userId : userIds) {
            if (lastUserCoefficientMap != null && lastUserCoefficientMap.containsKey(userId)) {
                totalCoefficient = totalCoefficient.add(lastUserCoefficientMap.get(userId));
            }
        }

        for (Long userId : userIds) {
            KpiItemEquivalent userEquivalent = new KpiItemEquivalent();
            BeanUtils.copyProperties(equivalent, userEquivalent);
            userEquivalent.setUserId(userId);
            userEquivalent.setEquivalentType(CaliberEnum.PEOPLE.getType());
            userEquivalent.setTotalWorkload(BigDecimal.ZERO);

            if (YesNoEnum.NO.getValue().equals(equivalent.getAssignFlag())) {
                records.add(userEquivalent);
                continue;
            }

            switch (EquivalentDistributeEnum.getByCode(equivalent.getDistributeType())) {
                case AVERAGE: // 平均分配
                    userEquivalent.setTotalWorkload(value.divide(new BigDecimal(userIds.size()), 6, RoundingMode.HALF_UP));
                    break;
                case COEFFICIENT: // 系数分配
                    if (lastUserCoefficientMap != null && lastUserCoefficientMap.containsKey(userId)) {
                        if (totalCoefficient.compareTo(BigDecimal.ZERO) > 0) {
                            BigDecimal coefficient = lastUserCoefficientMap.get(userId);
                            userEquivalent.setCoefficient(coefficient);
                            userEquivalent.setTotalWorkload(value.multiply(coefficient).divide(totalCoefficient, 6, RoundingMode.HALF_UP));
                        }else {
                            userEquivalent.setCoefficient(BigDecimal.ZERO);
                            userEquivalent.setTotalWorkload(BigDecimal.ZERO);
                        }
                    }
                    break;
                default:
                    break;
            }

            records.add(userEquivalent);
        }
    }
}
