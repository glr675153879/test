package com.hscloud.hs.cost.account.service.impl.kpi;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bestvike.linq.Linq;
import com.hscloud.hs.cost.account.constant.enums.EnableEnum;
import com.hscloud.hs.cost.account.constant.enums.YesNoEnum;
import com.hscloud.hs.cost.account.constant.enums.kpi.CaliberEnum;
import com.hscloud.hs.cost.account.constant.enums.kpi.EquivalentDistributeEnum;
import com.hscloud.hs.cost.account.constant.enums.kpi.EquivalentTaskStatusEnum;
import com.hscloud.hs.cost.account.mapper.kpi.KpiItemEquivalentChangeRecordMapper;
import com.hscloud.hs.cost.account.mapper.kpi.KpiItemEquivalentMapper;
import com.hscloud.hs.cost.account.mapper.kpi.KpiItemEquivalentTaskMapper;
import com.hscloud.hs.cost.account.model.dto.kpi.*;
import com.hscloud.hs.cost.account.model.entity.kpi.*;
import com.hscloud.hs.cost.account.model.vo.kpi.*;
import com.hscloud.hs.cost.account.service.kpi.IKpiConfigService;
import com.hscloud.hs.cost.account.service.kpi.IKpiItemEquivalentService;
import com.hscloud.hs.cost.account.service.kpi.KpiAccountUnitService;
import com.pig4cloud.pigx.common.core.exception.BizException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 核算项当量 服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KpiItemEquivalentService extends ServiceImpl<KpiItemEquivalentMapper, KpiItemEquivalent> implements IKpiItemEquivalentService {
    private final KpiItemService kpiItemService;
    private final IKpiConfigService iKpiConfigService;
    private final KpiUserAttendanceService kpiUserAttendanceService;
    private final KpiItemEquivalentConfigService kpiItemEquivalentConfigService;
    private final KpiAccountUnitService kpiAccountUnitService;
    private final KpiConfigService kpiConfigService;
    private final KpiItemEquivalentChangeRecordMapper kpiItemEquivalentChangeRecordMapper;
    private final KpiItemEquivalentTaskMapper kpiItemEquivalentTaskMapper;
    private final KpiItemEquivalentMapper kpiItemEquivalentMapper;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveData(Long period, Set<String> itemCodes, List<KpiItemEquivalent> records, boolean isAll) {
        List<Long> equivalentIds = this.list(Wrappers.<KpiItemEquivalent>lambdaQuery()
                        .eq(KpiItemEquivalent::getPeriod, period)
                        .in(!isAll, KpiItemEquivalent::getCode, itemCodes))
                .stream().map(KpiItemEquivalent::getId).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(equivalentIds)) {
            kpiItemEquivalentChangeRecordMapper.delete(Wrappers.<KpiItemEquivalentChangeRecord>lambdaQuery()
                    .in(KpiItemEquivalentChangeRecord::getEquivalentId, equivalentIds));
            log.info("数据调取：删除当量调整记录数量：{}", equivalentIds.size());

            this.remove(Wrappers.<KpiItemEquivalent>lambdaQuery().in(KpiItemEquivalent::getId, equivalentIds));
            log.info("数据调取：删除当量数据数量：{}", equivalentIds.size());
        }

        this.saveBatch(records);
        log.info("数据调取：保存当量数据数量：{}", records.size());

        iKpiConfigService.update(Wrappers.<KpiConfig>lambdaUpdate()
                .set(KpiConfig::getEquivalentIndexUpdateDate, new Date())
                .set(KpiConfig::getIndexFlag, YesNoEnum.YES.getValue())
                .set(KpiConfig::getEquivalentIndexFlag, YesNoEnum.YES.getValue())
                .eq(KpiConfig::getPeriod, period));
    }

    @Override
    public List<KpiItemEquivalentVO> getList(KpiItemEquivalentDTO dto, boolean isAdmin) {
        Long period = dto.getPeriod();
        if (null == period) {
            String var1 = iKpiConfigService.getLastCycle(true);
            if (!StringUtils.hasLength(var1)) {
                throw new BizException("未查询到周期");
            } else {
                period = Long.valueOf(var1.replace("-", "").substring(0, 6));
            }
        }

        if (!isAdmin) {
            if (dto.getAccountUnitId() == null || dto.getAccountUnitId() <= 0) {
                throw new BizException("科室id不能为空");
            }
        }

        List<KpiItemEquivalentVO> result = new ArrayList<>();

        List<KpiItemEquivalent> list = this.list(Wrappers.<KpiItemEquivalent>lambdaQuery()
                .eq(KpiItemEquivalent::getPeriod, period)
                .eq(ObjectUtils.isNotEmpty(dto.getAccountUnitId()), KpiItemEquivalent::getAccountUnitId, dto.getAccountUnitId())
                .in(!CollectionUtils.isEmpty(dto.getItemIds()), KpiItemEquivalent::getItemId, dto.getItemIds())
                .eq(ObjectUtils.isNotEmpty(dto.getItemId()), KpiItemEquivalent::getItemId, dto.getItemId()));
        if (CollectionUtils.isEmpty(list)) {
            return result;
        }

        String itemCodes = list.stream().map(KpiItemEquivalent::getCode).distinct().collect(Collectors.joining(","));
        KpiItemQueryDTO queryDTO = new KpiItemQueryDTO();
        queryDTO.setCodes(itemCodes);
        queryDTO.setEquivalentFlag(YesNoEnum.YES.getValue());

        List<Long> userIds = list.stream()
                .filter(x -> x.getUserId() != null && x.getUserId() > 0)
                .map(KpiItemEquivalent::getUserId).distinct().collect(Collectors.toList());
        Map<Long, KpiUserAttendance> userMap = kpiUserAttendanceService.list(Wrappers.<KpiUserAttendance>lambdaQuery()
                        .eq(KpiUserAttendance::getPeriod, period)
                        .eq(KpiUserAttendance::getDelFlag, YesNoEnum.NO.getValue())
                        .eq(KpiUserAttendance::getBusiType, "1")
                        .in(!CollectionUtils.isEmpty(userIds), KpiUserAttendance::getUserId, userIds))
                .stream().collect(Collectors.toMap(KpiUserAttendance::getUserId, Function.identity(), (user1, user2) -> user1));

        List<Long> unitIds = list.stream()
                .filter(x -> x.getAccountUnitId() != null && x.getAccountUnitId() > 0)
                .map(KpiItemEquivalent::getAccountUnitId).distinct().collect(Collectors.toList());
        List<KpiItemEquivalentConfig> configList = kpiItemEquivalentConfigService.list(
                Wrappers.<KpiItemEquivalentConfig>lambdaQuery()
                        .in(!CollectionUtils.isEmpty(unitIds), KpiItemEquivalentConfig::getAccountUnitId, unitIds));

        Map<Long, KpiAccountUnit> unitMap = kpiAccountUnitService.list(Wrappers.<KpiAccountUnit>lambdaQuery()
                        .in(!CollectionUtils.isEmpty(unitIds), KpiAccountUnit::getId, unitIds))
                .stream().collect(Collectors.toMap(KpiAccountUnit::getId, Function.identity(), (k1, k2) -> k1));

        KpiConfig kpiConfig = kpiConfigService.getOne(Wrappers.<KpiConfig>lambdaQuery().eq(KpiConfig::getPeriod, period));

        List<Long> equivalentIds = list.stream().map(KpiItemEquivalent::getId).collect(Collectors.toList());
        List<KpiItemEquivalentChangeRecord> changeRecords = kpiItemEquivalentChangeRecordMapper.selectList(Wrappers.<KpiItemEquivalentChangeRecord>lambdaQuery()
                .ne(KpiItemEquivalentChangeRecord::getStatus, EquivalentTaskStatusEnum.APPROVED.getCode())
                .and(x -> x.in(KpiItemEquivalentChangeRecord::getPEquivalentId, equivalentIds)
                        .or().in(KpiItemEquivalentChangeRecord::getEquivalentId, equivalentIds)));

        Map<Long, String> distributeTypeMap = changeRecords.stream()
                .filter(y -> y.getPEquivalentId() != null && y.getDistributeType() != null)
                .collect(Collectors.toMap(KpiItemEquivalentChangeRecord::getPEquivalentId, KpiItemEquivalentChangeRecord::getDistributeType, (v1, v2) -> v1));

        Map<Long, BigDecimal> coefficientMap = changeRecords.stream()
                .filter(y -> y.getCoefficient() != null)
                .collect(Collectors.toMap(KpiItemEquivalentChangeRecord::getEquivalentId, KpiItemEquivalentChangeRecord::getCoefficient, (v1, v2) -> v1));


        List<KpiItemEquivalentTask> taskList;
        if (!isAdmin) {
            taskList = kpiItemEquivalentTaskMapper.selectList(Wrappers.<KpiItemEquivalentTask>lambdaQuery()
                    .eq(KpiItemEquivalentTask::getPeriod, period)
                    .eq(KpiItemEquivalentTask::getAccountUnitId, dto.getAccountUnitId()));
        } else {
            taskList = null;
        }

        list.stream().filter(x -> CaliberEnum.DEPT.getType().equals(x.getEquivalentType()))
                .forEach(x -> {
                    KpiItemEquivalentVO vo = new KpiItemEquivalentVO();
                    BeanUtils.copyProperties(x, vo);

                    KpiAccountUnit unit = unitMap.get(x.getAccountUnitId());
                    if (unit != null) {
                        vo.setAccountUnitName(unit.getName());
                    }

                    if (!CollectionUtils.isEmpty(taskList)) {
                        taskList.stream().filter(t -> t.getCode().equals(x.getCode()))
                                .findFirst()
                                .ifPresent(task -> vo.setStatus(task.getStatus()));
                    }

                    if (!YesNoEnum.YES.getCode().equals(kpiConfig.getEquivalentFlag())) {
                        KpiItemEquivalentConfig config = configList.stream()
                                .filter(t -> t.getAccountUnitId().equals(x.getAccountUnitId()) && t.getItemCode().equals(x.getCode()))
                                .findFirst().orElse(null);

                        if (config != null) {
                            vo.setStdEquivalent(config.getStdEquivalent());
                            BigDecimal totalWorkload;
                            if (isAdmin) {
                                totalWorkload = vo.getTotalWorkloadAdmin() != null ? vo.getTotalWorkloadAdmin() : vo.getTotalWorkload();
                            } else {
                                totalWorkload = vo.getNewTotalWorkload() != null ? vo.getNewTotalWorkload() : vo.getTotalWorkloadAdmin() != null ? vo.getTotalWorkloadAdmin() : vo.getTotalWorkload();
                            }
                            vo.setTotalEquivalent(totalWorkload.multiply(config.getStdEquivalent()).setScale(6, RoundingMode.HALF_UP));
                        } else {
                            vo.setStdEquivalent(BigDecimal.ZERO);
                            vo.setTotalEquivalent(BigDecimal.ZERO);
                        }

                        if (!isAdmin && distributeTypeMap.containsKey(vo.getId())) {
                            vo.setDistributeType(distributeTypeMap.get(vo.getId()));
                        }
                    }

                    result.add(vo);
                });

        List<KpiItemEquivalentVO> resultVOList = result;
        if (!isAdmin) {
            resultVOList = Linq.of(result).orderBy(x -> x.getStatus() == null ? "999" : x.getStatus()).toList();
        }

        Map<String, KpiItemEquivalentVO> resultMap = resultVOList.stream()
                .collect(Collectors.toMap(t -> t.getAccountUnitId() + "_" + t.getCode(), Function.identity()));

        list.stream()
                .filter(x -> CaliberEnum.PEOPLE.getType().equals(x.getEquivalentType()))
                .forEach(x -> {
                    KpiUserAttendance user = userMap.get(x.getUserId());
                    if (user == null) {
                        return;
                    }
                    KpiItemEquivalentChildVO childVO = new KpiItemEquivalentChildVO();
                    BeanUtils.copyProperties(x, childVO);
                    childVO.setEmpName(user.getEmpName());

                    String key = x.getAccountUnitId() + "_" + x.getCode();
                    KpiItemEquivalentVO t = resultMap.get(key);
                    if (t != null) {
                        if (!YesNoEnum.YES.getCode().equals(kpiConfig.getEquivalentFlag())) {
                            BigDecimal totalWorkload;
                            if (isAdmin) {
                                totalWorkload = childVO.getTotalWorkloadAdmin() != null ? childVO.getTotalWorkloadAdmin() : childVO.getTotalWorkload();
                            } else {
                                totalWorkload = childVO.getNewTotalWorkload() != null ? childVO.getNewTotalWorkload() : childVO.getTotalWorkloadAdmin() != null ? childVO.getTotalWorkloadAdmin() : childVO.getTotalWorkload();

                                if (coefficientMap.containsKey(childVO.getId())) {
                                    childVO.setCoefficient(coefficientMap.get(childVO.getId()));
                                }
                            }
                            childVO.setTotalEquivalent(totalWorkload.multiply(t.getStdEquivalent()).setScale(6, RoundingMode.HALF_UP));
                        }

                        if (t.getChildVOList() == null) {
                            t.setChildVOList(new ArrayList<>());
                        }
                        t.getChildVOList().add(childVO);
                    }
                });
        return resultVOList;
    }

    @Override
    public KpiItemEquivalentVO getParentVO(KpiItemEquivalentDTO dto) {
        KpiItemEquivalent equivalent = this.getOne(Wrappers.<KpiItemEquivalent>lambdaQuery()
                .eq(KpiItemEquivalent::getEquivalentType, CaliberEnum.DEPT.getType())
                .eq(ObjectUtils.isNotEmpty(dto.getId()), KpiItemEquivalent::getId, dto.getId())
                .eq(ObjectUtils.isNotEmpty(dto.getCode()), KpiItemEquivalent::getCode, dto.getCode())
                .eq(ObjectUtils.isNotEmpty(dto.getAccountUnitId()), KpiItemEquivalent::getAccountUnitId, dto.getAccountUnitId())
                .eq(ObjectUtils.isNotEmpty(dto.getPeriod()), KpiItemEquivalent::getPeriod, dto.getPeriod())
                .eq(ObjectUtils.isNotEmpty(dto.getItemId()), KpiItemEquivalent::getItemId, dto.getItemId()));

        if (equivalent == null) {
            return null;
        }

        KpiItem kpiItem = kpiItemService.getById(equivalent.getItemId());
        if (kpiItem == null) {
            return null;
        }

        KpiItemEquivalentConfig config = kpiItemEquivalentConfigService.getOne(Wrappers.<KpiItemEquivalentConfig>lambdaQuery()
                .eq(KpiItemEquivalentConfig::getItemCode, equivalent.getCode())
                .eq(KpiItemEquivalentConfig::getAccountUnitId, equivalent.getAccountUnitId()));

        KpiAccountUnit accountUnit = kpiAccountUnitService.getById(equivalent.getAccountUnitId());

        KpiConfig kpiConfig = kpiConfigService.getOne(Wrappers.<KpiConfig>lambdaQuery()
                .eq(KpiConfig::getPeriod, equivalent.getPeriod()));

        KpiItemEquivalentVO vo = new KpiItemEquivalentVO();
        BeanUtils.copyProperties(equivalent, vo);
        vo.setItemName(kpiItem.getItemName());
        vo.setAcqMethod(kpiItem.getAcqMethod());
        vo.setCaliber(kpiItem.getCaliber());
        if (!YesNoEnum.YES.getCode().equals(kpiConfig.getEquivalentFlag())) {
            if (config != null) {
                vo.setStdEquivalent(config.getStdEquivalent());
                BigDecimal totalWorkload = vo.getNewTotalWorkload() != null ? vo.getNewTotalWorkload() : vo.getTotalWorkloadAdmin() != null ? vo.getTotalWorkloadAdmin() : vo.getTotalWorkload();
                vo.setTotalEquivalent(totalWorkload.multiply(config.getStdEquivalent()));
            } else {
                vo.setStdEquivalent(BigDecimal.ZERO);
                vo.setTotalEquivalent(BigDecimal.ZERO);
            }
        }

        if (accountUnit != null) {
            vo.setAccountUnitName(accountUnit.getName());
        }

        List<KpiItemEquivalent> list = this.list(Wrappers.<KpiItemEquivalent>lambdaQuery()
                .eq(KpiItemEquivalent::getCode, equivalent.getCode())
                .eq(KpiItemEquivalent::getEquivalentType, CaliberEnum.PEOPLE.getType())
                .eq(KpiItemEquivalent::getAccountUnitId, equivalent.getAccountUnitId())
                .eq(KpiItemEquivalent::getPeriod, equivalent.getPeriod()));

        List<Long> userIds = list.stream()
                .filter(x -> x.getUserId() != null && x.getUserId() > 0)
                .map(KpiItemEquivalent::getUserId).collect(Collectors.toList());

        Map<Long, KpiUserAttendance> userMap = kpiUserAttendanceService.list(Wrappers.<KpiUserAttendance>lambdaQuery()
                        .eq(KpiUserAttendance::getPeriod, equivalent.getPeriod())
                        .eq(KpiUserAttendance::getDelFlag, YesNoEnum.NO.getValue())
                        .eq(KpiUserAttendance::getBusiType, "1")
                        .in(!CollectionUtils.isEmpty(userIds), KpiUserAttendance::getUserId, userIds))
                .stream().collect(Collectors.toMap(KpiUserAttendance::getUserId, Function.identity(), (user1, user2) -> user1));

        list.forEach(x -> {
                    KpiItemEquivalentChildVO childVO = new KpiItemEquivalentChildVO();
                    BeanUtils.copyProperties(x, childVO);
                    if (userMap.containsKey(x.getUserId())) {
                        childVO.setEmpName(userMap.get(x.getUserId()).getEmpName());
                    }

                    if (!YesNoEnum.YES.getCode().equals(kpiConfig.getEquivalentFlag())) {
                        BigDecimal totalWorkload = childVO.getNewTotalWorkload() == null ?
                                childVO.getTotalWorkload() : childVO.getNewTotalWorkload();
                        childVO.setTotalEquivalent(totalWorkload.multiply(vo.getStdEquivalent()));
                    }

                    if (vo.getChildVOList() == null) {
                        vo.setChildVOList(new ArrayList<>());
                    }
                    vo.getChildVOList().add(childVO);
                }
        );

        return vo;
    }

    @Override
    public List<KpiAccountUnitVO> getEquivalentUnitList(KpiAccountUnitQueryDTO dto, Long period) {
        if (null == period) {
            String var1 = iKpiConfigService.getLastCycle(true);
            if (!StringUtils.hasLength(var1)) {
                throw new BizException("未查询到计算周期");
            } else {
                period = Long.valueOf(var1.replace("-", "").substring(0, 6));
            }
        }

        if (!StringUtils.hasText(dto.getStatus())) {
            dto.setStatus(EnableEnum.ENABLE.getType());
        }
        List<KpiAccountUnitVO> unitList = kpiAccountUnitService.getUnitList(dto);

        List<KpiItemEquivalent> equivalentList = this.list(Wrappers.<KpiItemEquivalent>lambdaQuery()
                .eq(KpiItemEquivalent::getPeriod, period)
                .eq(KpiItemEquivalent::getEquivalentType, CaliberEnum.DEPT.getType()));
        Map<Long, List<KpiItemEquivalent>> equivalentMap = equivalentList.stream()
                .collect(Collectors.groupingBy(KpiItemEquivalent::getAccountUnitId));

        KpiConfigSearchDto kpiConfigSearchDto = new KpiConfigSearchDto();
        kpiConfigSearchDto.setPeriod(period);
        KpiConfigVO configVO = iKpiConfigService.getConfig(kpiConfigSearchDto);

        List<KpiItemEquivalentConfig> configList = kpiItemEquivalentConfigService.list();

        unitList.forEach(unitVO -> {
            List<KpiItemEquivalentConfig> unitEquivalentConfigs = configList.stream()
                    .filter(x -> x.getAccountUnitId().equals(unitVO.getId()))
                    .collect(Collectors.toList());
            unitVO.setEquivalentConfigNum(unitEquivalentConfigs.size());

            List<KpiItemEquivalent> list = equivalentMap.getOrDefault(unitVO.getId(), Collections.emptyList());
            unitVO.setEquivalentNum(list.size());

            if (list.isEmpty()) {
                unitVO.setTotalEquivalent(BigDecimal.ZERO);
                return;
            }

            if (!YesNoEnum.YES.getCode().equals(configVO.getEquivalentFlag())) {
                list.forEach(equivalentVO -> processEquivalent(equivalentVO, unitEquivalentConfigs));
            }

            unitVO.setTotalEquivalent(list.stream()
                    .map(KpiItemEquivalent::getTotalEquivalent)
                    .reduce(BigDecimal.ZERO, BigDecimal::add));
        });

        return unitList;
    }

    @Override
    public List<KpiItemCoefficientVO> getCoefficientList(Long accountUnitId, Long period, Long itemId) {
        if (accountUnitId == null || accountUnitId <= 0) {
            throw new BizException("科室id不能为空");
        }

        if (itemId == null || itemId <= 0) {
            throw new BizException("核算项id不能为空");
        }

        if (period == null) {
            throw new BizException("周期不能为空");
        }

        List<KpiItemCoefficientVO> result = new ArrayList<>();

        List<KpiItemEquivalent> equivalentList = this.list(Wrappers.<KpiItemEquivalent>lambdaQuery()
                .eq(KpiItemEquivalent::getAccountUnitId, accountUnitId)
                .eq(KpiItemEquivalent::getItemId, itemId)
                .eq(KpiItemEquivalent::getPeriod, period));
        if (CollectionUtils.isEmpty(equivalentList)) {
            return result;
        }

        KpiItemEquivalent deptEquivalent = Linq.of(equivalentList)
                .where(x -> x.getEquivalentType().equals(CaliberEnum.DEPT.getType()))
                .firstOrDefault();

        Map<Long, String> userMap = kpiUserAttendanceService.list(Wrappers.<KpiUserAttendance>lambdaQuery()
                        .eq(KpiUserAttendance::getAccountUnit, accountUnitId)
                        .eq(KpiUserAttendance::getPeriod, period)).stream()
                .collect(Collectors.toMap(KpiUserAttendance::getUserId, KpiUserAttendance::getEmpName));

        if (deptEquivalent != null && EquivalentDistributeEnum.COEFFICIENT.getCode().equals(deptEquivalent.getDistributeType())) {
            equivalentList.stream().filter(x -> x.getEquivalentType().equals(CaliberEnum.PEOPLE.getType()))
                    .forEach(equivalent -> {
                        KpiItemCoefficientVO vo = new KpiItemCoefficientVO();
                        vo.setAccountUnitId(accountUnitId);
                        vo.setUserId(equivalent.getUserId());
                        vo.setCoefficient(equivalent.getCoefficient());
                        if (userMap.containsKey(equivalent.getUserId())) {
                            vo.setEmpName(userMap.get(equivalent.getUserId()));
                        }
                        result.add(vo);
                    });
        }

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void lock(Long period) {
        if (period == null) {
            throw new BizException("周期不能为空");
        }

        List<KpiItemEquivalentTask> tasks = kpiItemEquivalentTaskMapper.selectList(Wrappers.<KpiItemEquivalentTask>lambdaQuery()
                .eq(KpiItemEquivalentTask::getPeriod, period)
                .ne(KpiItemEquivalentTask::getStatus, EquivalentTaskStatusEnum.APPROVED.getCode()));

        KpiConfig config = kpiConfigService.getOne(Wrappers.<KpiConfig>lambdaQuery()
                .eq(KpiConfig::getPeriod, period));

        if (YesNoEnum.YES.getCode().equals(config.getEquivalentFlag())) {
            config.setEquivalentFlag(YesNoEnum.NO.getCode());
            config.setEquivalentUpdateDate(null);
        } else {
            if (!CollectionUtils.isEmpty(tasks)) {
                Set<Long> unitIds = tasks.stream().map(KpiItemEquivalentTask::getAccountUnitId).collect(Collectors.toSet());
                List<KpiAccountUnit> enableUnitList = kpiAccountUnitService.list(Wrappers.<KpiAccountUnit>lambdaQuery()
                        .eq(KpiAccountUnit::getStatus, EnableEnum.ENABLE.getType())
                        .in(KpiAccountUnit::getId, unitIds));

                if (!CollectionUtils.isEmpty(enableUnitList)) {
                    throw new BizException("存在未通过的核验任务，无法锁定");
                }
            }
            config.setEquivalentFlag(YesNoEnum.YES.getCode());
            config.setEquivalentUpdateDate(new Date());
        }
        kpiConfigService.updateById(config);

        if (YesNoEnum.NO.getCode().equals(config.getEquivalentFlag())) {
            return;
        }

        Set<Long> disableUnitIds = kpiAccountUnitService.list(Wrappers.<KpiAccountUnit>lambdaQuery()
                        .eq(KpiAccountUnit::getStatus, EnableEnum.DISABLE.getType()))
                .stream().map(KpiAccountUnit::getId).collect(Collectors.toSet());
        if (!CollectionUtils.isEmpty(disableUnitIds)) {
            Set<Long> delEquivalentIds = this.list(Wrappers.<KpiItemEquivalent>lambdaUpdate()
                            .eq(KpiItemEquivalent::getPeriod, period)
                            .in(KpiItemEquivalent::getAccountUnitId, disableUnitIds))
                    .stream().map(KpiItemEquivalent::getId).collect(Collectors.toSet());
            if (!CollectionUtils.isEmpty(delEquivalentIds)) {
                this.remove(Wrappers.<KpiItemEquivalent>lambdaUpdate()
                        .in(KpiItemEquivalent::getId, delEquivalentIds));
                log.info("当量锁定：删除已停用科室的当量数据数量：{}", delEquivalentIds.size());

                kpiItemEquivalentChangeRecordMapper.delete(Wrappers.<KpiItemEquivalentChangeRecord>lambdaUpdate()
                        .in(KpiItemEquivalentChangeRecord::getEquivalentId, delEquivalentIds));
                log.info("当量锁定：删除已停用科室的当量调整记录");
            }

            kpiItemEquivalentTaskMapper.delete(Wrappers.<KpiItemEquivalentTask>lambdaUpdate()
                    .eq(KpiItemEquivalentTask::getPeriod, period)
                    .in(KpiItemEquivalentTask::getAccountUnitId, disableUnitIds));
            log.info("当量锁定：删除已停用科室的当量核验任务");
        }

        log.info("开始锁定当量数据，周期：{}", period);

        kpiItemEquivalentMapper.updateStdEquivalent(period);

        kpiItemEquivalentMapper.updateTotalEquivalent(period);

        log.info("锁定当量数据完成");
    }

    private void processEquivalent(KpiItemEquivalent equivalentVO, List<KpiItemEquivalentConfig> configList) {
        BigDecimal totalWorkload = Optional.ofNullable(equivalentVO.getTotalWorkloadAdmin())
                .orElse(equivalentVO.getTotalWorkload());

        KpiItemEquivalentConfig config = Linq.of(configList)
                .where(x -> x.getItemCode().equals(equivalentVO.getCode()))
                .firstOrDefault();

        equivalentVO.setStdEquivalent(Optional.ofNullable(config)
                .map(KpiItemEquivalentConfig::getStdEquivalent)
                .orElse(BigDecimal.ZERO));

        equivalentVO.setTotalEquivalent(equivalentVO.getStdEquivalent().multiply(totalWorkload).setScale(6, RoundingMode.HALF_UP));
    }
}
