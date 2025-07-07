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
import com.hscloud.hs.cost.account.model.dto.kpi.KpiAccountUnitQueryDTO;
import com.hscloud.hs.cost.account.model.dto.kpi.KpiConfigSearchDto;
import com.hscloud.hs.cost.account.model.dto.kpi.KpiItemEquivalentDTO;
import com.hscloud.hs.cost.account.model.dto.kpi.KpiItemEquivalentTaskDTO;
import com.hscloud.hs.cost.account.model.entity.kpi.*;
import com.hscloud.hs.cost.account.model.vo.kpi.*;
import com.hscloud.hs.cost.account.service.kpi.IKpiConfigService;
import com.hscloud.hs.cost.account.service.kpi.IKpiItemEquivalentTaskService;
import com.hscloud.hs.cost.account.service.kpi.KpiAccountUnitService;
import com.pig4cloud.pigx.admin.api.feign.RemoteUserService;
import com.pig4cloud.pigx.admin.api.vo.UserCoreVo;
import com.pig4cloud.pigx.common.core.constant.SecurityConstants;
import com.pig4cloud.pigx.common.core.exception.BizException;
import com.pig4cloud.pigx.common.security.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 当量核验任务 服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KpiItemEquivalentTaskService extends ServiceImpl<KpiItemEquivalentTaskMapper, KpiItemEquivalentTask> implements IKpiItemEquivalentTaskService {
    private final IKpiConfigService iKpiConfigService;
    private final KpiItemEquivalentService kpiItemEquivalentService;
    private final KpiAccountUnitService kpiAccountUnitService;
    private final RemoteUserService remoteUserService;
    private final KpiItemEquivalentTaskMapper kpiItemEquivalentTaskMapper;
    private final KpiItemEquivalentChangeRecordMapper kpiItemEquivalentChangeRecordMapper;
    private final KpiItemEquivalentMapper kpiItemEquivalentMapper;
    private final KpiItemEquivalentConfigService kpiItemEquivalentConfigService;
    private final KpiItemService kpiItemService;
    private final KpiConfigService kpiConfigService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void issueTask(KpiItemEquivalentTaskDTO dto) {
        Long period = dto.getPeriod();
        if (null == period) {
            String var1 = iKpiConfigService.getLastCycle(true);
            if (!StringUtils.hasLength(var1)) {
                throw new BizException("未查询到周期");
            } else {
                period = Long.valueOf(var1.replace("-", "").substring(0, 6));
            }
        }

        KpiConfigSearchDto searchDto = new KpiConfigSearchDto();
        searchDto.setPeriod(period);
        KpiConfigVO config = kpiConfigService.getConfig(searchDto);
        if (YesNoEnum.YES.getCode().equals(config.getEquivalentFlag())) {
            throw new BizException("当量已锁定，无法下发核验任务");
        }

        List<Long> accountUnitIds = dto.getAccountUnitIds();
        if (CollectionUtils.isEmpty(accountUnitIds)) {
            KpiAccountUnitQueryDTO unitQueryDTO = new KpiAccountUnitQueryDTO();
            unitQueryDTO.setStatus("0");
            unitQueryDTO.setBusiType("1");
            accountUnitIds = kpiAccountUnitService.getUnitList(unitQueryDTO).stream()
                    .map(KpiAccountUnitVO::getId).collect(Collectors.toList());
        }
        /*List<Long> itemIds = kpiItemService.list(Wrappers.<KpiItem>lambdaQuery()
                .eq(KpiItem::getEquivalentFlag, YesNoEnum.YES.getValue())
                .eq(KpiItem::getDelFlag, YesNoEnum.NO.getValue())
                .eq(KpiItem::getStatus, EnableEnum.ENABLE.getType())
                .eq(KpiItem::getBusiType, "1")).stream().map(KpiItem::getId).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(itemIds)) {
            log.info("下发当量核验任务：未查询到当量核算项");
            delTaskData(period, accountUnitIds);
            return;
        }*/

        List<Long> configItemIds = kpiItemEquivalentConfigService.list(Wrappers.<KpiItemEquivalentConfig>lambdaQuery()
//                        .in(KpiItemEquivalentConfig::getItemId, itemIds)
                        .in(!CollectionUtils.isEmpty(accountUnitIds), KpiItemEquivalentConfig::getAccountUnitId, accountUnitIds))
                .stream().map(KpiItemEquivalentConfig::getItemId).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(configItemIds)) {
            log.info("下发当量核验任务：未查询到当量配置");
            delTaskData(period, accountUnitIds);
            return;
        }

        List<KpiItemEquivalent> equivalentList = kpiItemEquivalentService.list(Wrappers.<KpiItemEquivalent>lambdaQuery()
                .eq(KpiItemEquivalent::getPeriod, period)
//                .in(KpiItemEquivalent::getItemId, configItemIds)
                .in(!CollectionUtils.isEmpty(accountUnitIds), KpiItemEquivalent::getAccountUnitId, accountUnitIds));
        if (CollectionUtils.isEmpty(equivalentList)) {
            log.info("下发当量核验任务：未查询到当量数据");
            delTaskData(period, accountUnitIds);
            return;
        }

        delTaskData(period, accountUnitIds);

        equivalentList.forEach(equivalent -> equivalent.setNewTotalWorkload(null));
        kpiItemEquivalentMapper.updateBatchById(equivalentList);
        log.info("下发当量核验任务：更新当量数据数量: {}", equivalentList.size());

        List<KpiItemEquivalentTask> taskList = new ArrayList<>();
        List<KpiItemEquivalent> list = equivalentList.stream()
                .filter(x -> x.getEquivalentType().equals(CaliberEnum.DEPT.getType()))
                .collect(Collectors.toList());
        for (KpiItemEquivalent equivalent : list) {
            KpiItemEquivalentTask task = new KpiItemEquivalentTask();
            BeanUtils.copyProperties(equivalent, task);
            task.setStatus(EquivalentTaskStatusEnum.PENDING_SUBMIT.getCode());
            task.setId(null);
            task.setCreatedDate(null);
            task.setUpdatedDate(null);
            task.setCreatedId(null);
            task.setUpdatedId(null);
            taskList.add(task);
        }

        if (!CollectionUtils.isEmpty(taskList)) {
            this.saveBatch(taskList);
        }
        log.info("下发当量核验任务：插入任务数量: {}", taskList.size());
    }

    private void delTaskData(Long period, List<Long> accountUnitIds) {
        List<Long> taskIds = this.list(Wrappers.<KpiItemEquivalentTask>lambdaQuery()
                        .eq(KpiItemEquivalentTask::getPeriod, period)
                        .in(!CollectionUtils.isEmpty(accountUnitIds), KpiItemEquivalentTask::getAccountUnitId, accountUnitIds))
                .stream().map(KpiItemEquivalentTask::getId).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(taskIds)) {
            this.remove(Wrappers.<KpiItemEquivalentTask>lambdaQuery().in(KpiItemEquivalentTask::getId, taskIds));
            log.info("下发当量核验任务：删除任务数量: {}", taskIds.size());

            kpiItemEquivalentChangeRecordMapper.delete(Wrappers.<KpiItemEquivalentChangeRecord>lambdaQuery()
                    .ne(KpiItemEquivalentChangeRecord::getStatus, EquivalentTaskStatusEnum.APPROVED.getCode())
                    .in(KpiItemEquivalentChangeRecord::getTaskId, taskIds));
            log.info("下发当量核验任务：删除变更记录数量: {}", taskIds.size());
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void commitTask(Long accountUnitId, Long period) {
        if (null == period) {
            String var1 = iKpiConfigService.getLastCycle(true);
            if (!StringUtils.hasLength(var1)) {
                throw new BizException("未查询到周期");
            } else {
                period = Long.valueOf(var1.replace("-", "").substring(0, 6));
            }
        }
        List<KpiItemEquivalentTask> taskList = this.list(Wrappers.<KpiItemEquivalentTask>lambdaQuery()
                .eq(KpiItemEquivalentTask::getAccountUnitId, accountUnitId)
                .eq(KpiItemEquivalentTask::getPeriod, period));
        if (CollectionUtils.isEmpty(taskList)) {
            throw new BizException("未查询到待提交的任务");
        }

        KpiItemEquivalentDTO equivalentDTO = new KpiItemEquivalentDTO();
        equivalentDTO.setPeriod(period);
        equivalentDTO.setAccountUnitId(accountUnitId);
        equivalentDTO.setItemIds(taskList.stream().map(KpiItemEquivalentTask::getItemId).collect(Collectors.toList()));
        List<KpiItemEquivalentVO> equivalentVOList = kpiItemEquivalentService.getList(equivalentDTO, false);
        equivalentVOList.stream()
                .filter(x -> EquivalentDistributeEnum.CUSTOM.getCode().equals(x.getDistributeType()))
                .forEach(equivalentVO -> {
                    BigDecimal childTotalWorkload = BigDecimal.ZERO;
                    for (KpiItemEquivalentChildVO childVO : equivalentVO.getChildVOList()) {
                        if (childVO.getNewTotalWorkload() != null) {
                            childTotalWorkload = childTotalWorkload.add(childVO.getNewTotalWorkload());
                        } else if (childVO.getTotalWorkloadAdmin() != null) {
                            childTotalWorkload = childTotalWorkload.add(childVO.getTotalWorkloadAdmin());
                        } else {
                            childTotalWorkload = childTotalWorkload.add(childVO.getTotalWorkload());
                        }
                    }
                    BigDecimal parentTotalWorkload = equivalentVO.getNewTotalWorkload() != null
                            ? equivalentVO.getNewTotalWorkload()
                            : equivalentVO.getTotalWorkloadAdmin() != null
                            ? equivalentVO.getTotalWorkloadAdmin()
                            : equivalentVO.getTotalWorkload();

                    if (childTotalWorkload.compareTo(parentTotalWorkload) != 0) {
                        throw new BizException("[" + equivalentVO.getItemName() + "]的当量数据分配总量与子项分配总量不一致，请检查！");
                    }
                });

        taskList.forEach(task -> {
            task.setStatus(EquivalentTaskStatusEnum.PENDING_APPROVE.getCode());
            task.setCommittedDate(new Date());
        });
        this.updateBatchById(taskList);

        List<Long> changeRecordIds = kpiItemEquivalentChangeRecordMapper.selectList(Wrappers.<KpiItemEquivalentChangeRecord>lambdaQuery()
                        .ne(KpiItemEquivalentChangeRecord::getStatus, EquivalentTaskStatusEnum.APPROVED.getCode())
                        .in(KpiItemEquivalentChangeRecord::getTaskId, taskList.stream().map(KpiItemEquivalentTask::getId).collect(Collectors.toList())))
                .stream().map(KpiItemEquivalentChangeRecord::getId).collect(Collectors.toList());

        if (!CollectionUtils.isEmpty(changeRecordIds)) {
            kpiItemEquivalentChangeRecordMapper.update(Wrappers.<KpiItemEquivalentChangeRecord>lambdaUpdate()
                    .set(KpiItemEquivalentChangeRecord::getStatus, EquivalentTaskStatusEnum.PENDING_APPROVE.getCode())
                    .in(KpiItemEquivalentChangeRecord::getId, changeRecordIds));
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void approveTask(KpiItemEquivalentTaskDTO dto) {
        Long accountUnitId = dto.getAccountUnitId();
        if (accountUnitId == null || accountUnitId <= 0) {
            throw new BizException("科室id不能为空");
        }

        Long period = dto.getPeriod();
        if (period == null || period <= 0) {
            String var1 = iKpiConfigService.getLastCycle(true);
            if (!StringUtils.hasLength(var1)) {
                throw new BizException("未查询到周期");
            } else {
                period = Long.valueOf(var1.replace("-", "").substring(0, 6));
            }
        }

        String status = dto.getStatus();
        if (!EquivalentTaskStatusEnum.APPROVED.getCode().equals(status) && !EquivalentTaskStatusEnum.REJECTED.getCode().equals(status)) {
            throw new BizException("审批状态不正确");
        }

        List<KpiItemEquivalentTask> taskList = this.list(Wrappers.<KpiItemEquivalentTask>lambdaQuery()
                .eq(KpiItemEquivalentTask::getAccountUnitId, accountUnitId)
                .eq(KpiItemEquivalentTask::getPeriod, period)
                .eq(KpiItemEquivalentTask::getStatus, EquivalentTaskStatusEnum.PENDING_APPROVE.getCode()));
        if (CollectionUtils.isEmpty(taskList)) {
            throw new BizException("未查询到待审核的任务");
        }

        taskList.forEach(task -> {
            task.setStatus(status);
            task.setReason(dto.getReason());
        });

        List<KpiItemEquivalentChangeRecord> changeRecords = kpiItemEquivalentChangeRecordMapper.selectList(Wrappers.<KpiItemEquivalentChangeRecord>lambdaQuery()
                .eq(KpiItemEquivalentChangeRecord::getStatus, EquivalentTaskStatusEnum.PENDING_APPROVE.getCode())
                .in(KpiItemEquivalentChangeRecord::getTaskId, taskList.stream().map(KpiItemEquivalentTask::getId).collect(Collectors.toList())));

        if (EquivalentTaskStatusEnum.APPROVED.getCode().equals(status)) {
            List<Long> itemIds = taskList.stream().map(KpiItemEquivalentTask::getItemId).collect(Collectors.toList());
            List<KpiItemEquivalent> equivalentList = kpiItemEquivalentService.list(Wrappers.<KpiItemEquivalent>lambdaQuery()
                    .eq(KpiItemEquivalent::getAccountUnitId, dto.getAccountUnitId())
                    .eq(KpiItemEquivalent::getPeriod, period)
                    .in(KpiItemEquivalent::getItemId, itemIds));

            equivalentList.forEach(equivalent -> {
                if (equivalent.getNewTotalWorkload() != null) {
                    equivalent.setTotalWorkloadAdmin(equivalent.getNewTotalWorkload());
                    equivalent.setNewTotalWorkload(null);
                }

                KpiItemEquivalentChangeRecord pRecord = Linq.of(changeRecords)
                        .where(x -> x.getPEquivalentId().equals(equivalent.getId())).firstOrDefault();
                if (pRecord != null) {
                    equivalent.setDistributeType(pRecord.getDistributeType());
                }

                KpiItemEquivalentChangeRecord record = Linq.of(changeRecords)
                        .where(x -> x.getEquivalentId().equals(equivalent.getId())).firstOrDefault();
                if (record != null) {
                    equivalent.setDistributeType(record.getDistributeType());
                    equivalent.setCoefficient(record.getCoefficient());
                }
            });

            if (!CollectionUtils.isEmpty(equivalentList)) {
                kpiItemEquivalentService.updateBatchById(equivalentList);
            }
            log.info("当量核验任务审核通过：更新当量数据数量: {}", equivalentList.size());
        }

        List<Long> changeRecordIds = changeRecords.stream().map(KpiItemEquivalentChangeRecord::getId).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(changeRecordIds)) {
            kpiItemEquivalentChangeRecordMapper.update(Wrappers.<KpiItemEquivalentChangeRecord>lambdaUpdate()
                    .set(KpiItemEquivalentChangeRecord::getStatus, status)
                    .in(KpiItemEquivalentChangeRecord::getId, changeRecordIds));
        }
        log.info("当量核验任务审核通过：更新变更记录数量: {}", changeRecordIds.size());

        if (!CollectionUtils.isEmpty(taskList)) {
            this.updateBatchById(taskList);
        }
        log.info("当量核验任务审核通过：更新任务数量: {}", taskList.size());
    }

    @Override
    public List<KpiItemEquivalentTaskVO> getList(KpiItemEquivalentTaskDTO dto, boolean isAdmin) {
        List<KpiAccountUnit> unitList = kpiAccountUnitService.list(Wrappers.<KpiAccountUnit>lambdaQuery()
                .eq(KpiAccountUnit::getStatus, EnableEnum.ENABLE.getType())
                .like(!isAdmin, KpiAccountUnit::getResponsiblePersonId, String.valueOf(SecurityUtils.getUser().getId())));
        List<Long> unitIds = unitList.stream().map(KpiAccountUnit::getId).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(unitIds)) {
            return Collections.emptyList();
        }

        List<KpiItemEquivalentTask> taskList = this.list(Wrappers.<KpiItemEquivalentTask>lambdaQuery()
                .eq(ObjectUtils.isNotEmpty(dto.getPeriod()), KpiItemEquivalentTask::getPeriod, dto.getPeriod())
                .in(KpiItemEquivalentTask::getAccountUnitId, unitIds));
        if (CollectionUtils.isEmpty(taskList)) {
            return Collections.emptyList();
        }

        Map<Long, KpiAccountUnit> unitMap = unitList.stream()
                .collect(Collectors.toMap(KpiAccountUnit::getId, Function.identity(), (k1, k2) -> k1));

        Map<Long, String> userMap = remoteUserService.listMainDetails(SecurityConstants.FROM_IN).getData()
                .stream().collect(Collectors.toMap(UserCoreVo::getUserId, UserCoreVo::getName, (k1, k2) -> k1));

        List<KpiItemEquivalentTaskVO> result = new ArrayList<>();
        taskList.stream()
                .collect(Collectors.groupingBy(KpiItemEquivalentTask::getPeriod))
                .forEach((period, tasks) -> tasks.stream()
                        .collect(Collectors.groupingBy(KpiItemEquivalentTask::getAccountUnitId))
                        .forEach((unitId, list) -> list.stream()
                                .min(Comparator.comparing(KpiItemEquivalentTask::getStatus)
                                        .thenComparing(KpiItemEquivalentTask::getAutoIssue, Comparator.reverseOrder())
                                        .thenComparing(KpiItemEquivalentTask::getId, Comparator.reverseOrder()))
                                .filter(task -> !StringUtils.hasText(dto.getStatus()) || dto.getStatus().equals(task.getStatus()))
                                .ifPresent(task -> {
                                    KpiAccountUnit unit = unitMap.get(unitId);
                                    if (unit == null) {
                                        return;
                                    }

                                    KpiItemEquivalentTaskVO taskVO = new KpiItemEquivalentTaskVO();
                                    BeanUtils.copyProperties(task, taskVO);
                                    taskVO.setAccountUnitName(unit.getName());
                                    taskVO.setTaskName(unit.getName() + "基础当量核验任务");

                                    if (unit.getResponsiblePersonId() != null) {
                                        String responsiblePersonName = Arrays.stream(unit.getResponsiblePersonId().split(","))
                                                .map(Long::valueOf)
                                                .filter(userMap::containsKey)
                                                .map(userMap::get)
                                                .collect(Collectors.joining(","));
                                        taskVO.setResponsiblePersonName(responsiblePersonName);
                                    }

                                    result.add(taskVO);
                                })
                        )
                );
        result.sort(Comparator.comparing(KpiItemEquivalentTaskVO::getAccountUnitId));

        return result;
    }

    @Override
    public Map<String, Long> statusCount(Long period) {
        Map<String, Long> result = new HashMap<>();
        result.put(EquivalentTaskStatusEnum.PENDING_SUBMIT.getCode(), 0L);
        result.put(EquivalentTaskStatusEnum.PENDING_APPROVE.getCode(), 0L);
        result.put(EquivalentTaskStatusEnum.APPROVED.getCode(), 0L);
        result.put(EquivalentTaskStatusEnum.REJECTED.getCode(), 0L);

        List<String> statusList = kpiItemEquivalentTaskMapper.statusCount(period);
        if (CollectionUtils.isEmpty(statusList)) {
            return result;
        }

        for (String status : statusList) {
            if (result.containsKey(status)) {
                result.put(status, result.get(status) + 1);
            }
        }

        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void reIssueTask(Long period, List<Long> itemIds, Long accountUnitId) {
        if (period == null) {
            throw new BizException("周期不能为空");
        }

        List<KpiItemEquivalentTask> taskList = this.list(Wrappers.<KpiItemEquivalentTask>lambdaQuery()
                .eq(KpiItemEquivalentTask::getPeriod, period)
                .eq(ObjectUtils.isNotEmpty(accountUnitId), KpiItemEquivalentTask::getAccountUnitId, accountUnitId));
        if (CollectionUtils.isEmpty(taskList)) {
            return;
        }

        KpiItemEquivalentDTO equivalentDTO = new KpiItemEquivalentDTO();
        equivalentDTO.setPeriod(period);
        equivalentDTO.setAccountUnitId(accountUnitId);
        List<KpiItemEquivalentVO> equivalentVOList = kpiItemEquivalentService.getList(equivalentDTO, true);
        log.info("重新下发核验任务：查询到到当量核算项数据数量: {}", equivalentVOList.size());

        Map<Long, List<Long>> unitItemIdMap = equivalentVOList.stream()
                .collect(Collectors.groupingBy(KpiItemEquivalentVO::getAccountUnitId, Collectors.mapping(KpiItemEquivalentVO::getItemId, Collectors.toList())));

        List<Long> delTaskIds = new ArrayList<>();
        List<Long> accountUnitIds = taskList.stream().map(KpiItemEquivalentTask::getAccountUnitId).distinct().collect(Collectors.toList());
        taskList.stream().collect(Collectors.groupingBy(KpiItemEquivalentTask::getAccountUnitId))
                .forEach((unitId, tasks) -> {
                    if (unitItemIdMap.containsKey(unitId)) {
                        List<Long> itemIdsForUnit = unitItemIdMap.get(unitId);
                        // 删除不在当量数据中的任务
                        List<Long> toDeleteTaskIds = tasks.stream()
                                .filter(task -> !itemIdsForUnit.contains(task.getItemId()))
                                .map(KpiItemEquivalentTask::getId)
                                .collect(Collectors.toList());

                        if (!CollectionUtils.isEmpty(toDeleteTaskIds)) {
                            delTaskIds.addAll(toDeleteTaskIds);
                        }
                    } else {
                        delTaskIds.addAll(tasks.stream().map(KpiItemEquivalentTask::getId).collect(Collectors.toList()));
                    }
                });

        if (!CollectionUtils.isEmpty(itemIds)) {
            // 删除在itemIds中的任务
            delTaskIds.addAll(taskList.stream()
                    .filter(task -> itemIds.contains(task.getItemId()))
                    .map(KpiItemEquivalentTask::getId)
                    .collect(Collectors.toList()));
        }
        List<Long> finalDelTaskIds = delTaskIds.stream().distinct().collect(Collectors.toList());

        List<KpiItemEquivalentTask> tasks = new ArrayList<>();
        List<KpiItemEquivalent> updateEquivalentList = new ArrayList<>();
        equivalentVOList.stream()
                .filter(x -> (CollectionUtils.isEmpty(itemIds) || itemIds.contains(x.getItemId())
                        && accountUnitIds.contains(x.getAccountUnitId())))
                .forEach(equivalentVO -> {
                    KpiItemEquivalent equivalent = new KpiItemEquivalent();
                    equivalent.setId(equivalentVO.getId());
                    equivalent.setNewTotalWorkload(null);
                    updateEquivalentList.add(equivalent);

                    if (!CollectionUtils.isEmpty(equivalentVO.getChildVOList())) {
                        for (KpiItemEquivalentChildVO childVO : equivalentVO.getChildVOList()) {
                            KpiItemEquivalent childEquivalent = new KpiItemEquivalent();
                            childEquivalent.setId(childVO.getId());
                            childEquivalent.setNewTotalWorkload(null);
                            updateEquivalentList.add(childEquivalent);
                        }
                    }

                    KpiItemEquivalentTask task = new KpiItemEquivalentTask();
                    BeanUtils.copyProperties(equivalentVO, task);
                    task.setId(null);
                    task.setStatus(EquivalentTaskStatusEnum.PENDING_SUBMIT.getCode());
                    task.setAutoIssue(YesNoEnum.YES.getValue());
                    task.setCreatedDate(null);
                    task.setUpdatedDate(null);
                    task.setCreatedId(null);
                    task.setUpdatedId(null);
                    tasks.add(task);
                });

        if (!CollectionUtils.isEmpty(updateEquivalentList)) {
            kpiItemEquivalentMapper.updateBatchById(updateEquivalentList);
            log.info("重新下发核验任务：更新当量数据数量: {}", updateEquivalentList.size());
        } else {
            log.info("重新下发核验任务：没有需要更新的当量数据");
        }

        if (!CollectionUtils.isEmpty(finalDelTaskIds)) {
            kpiItemEquivalentChangeRecordMapper.delete(Wrappers.<KpiItemEquivalentChangeRecord>lambdaUpdate()
                    .ne(KpiItemEquivalentChangeRecord::getStatus, EquivalentTaskStatusEnum.APPROVED.getCode())
                    .in(KpiItemEquivalentChangeRecord::getTaskId, finalDelTaskIds));
            log.info("重新下发核验任务：删除变更记录数量: {}", finalDelTaskIds.size());

            this.remove(Wrappers.<KpiItemEquivalentTask>lambdaQuery().in(KpiItemEquivalentTask::getId, finalDelTaskIds));
            log.info("重新下发核验任务：删除任务数量: {}", finalDelTaskIds.size());
        }

        log.info("重新下发核验任务：准备插入新任务数量: {}", tasks.size());
        if (CollectionUtils.isEmpty(tasks)) {
            log.info("没有需要重新下发的任务");
            return;
        }
        this.saveBatch(tasks);
    }

    @Override
    public Map<String, Long> unitTaskStatusCount(Long period, Long accountUnitId) {
        if (period == null || accountUnitId == null) {
            throw new BizException("周期和科室ID不能为空");
        }

        Map<String, Long> result = new HashMap<>();
        result.put(EquivalentTaskStatusEnum.PENDING_SUBMIT.getCode(), 0L);
        result.put(EquivalentTaskStatusEnum.PENDING_APPROVE.getCode(), 0L);
        result.put(EquivalentTaskStatusEnum.APPROVED.getCode(), 0L);
        result.put(EquivalentTaskStatusEnum.REJECTED.getCode(), 0L);

        List<KpiItemEquivalentTask> taskList = this.list(Wrappers.<KpiItemEquivalentTask>lambdaQuery()
                .eq(KpiItemEquivalentTask::getPeriod, period)
                .eq(KpiItemEquivalentTask::getAccountUnitId, accountUnitId));

        if (!CollectionUtils.isEmpty(taskList)) {
            for (KpiItemEquivalentTask task : taskList) {
                String status = task.getStatus();
                if (result.containsKey(status)) {
                    result.put(status, result.get(status) + 1);
                }
            }
        }

        return result;
    }
}
