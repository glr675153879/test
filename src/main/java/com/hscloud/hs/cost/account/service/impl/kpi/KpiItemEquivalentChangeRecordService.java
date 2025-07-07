package com.hscloud.hs.cost.account.service.impl.kpi;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bestvike.linq.Linq;
import com.hscloud.hs.cost.account.constant.enums.YesNoEnum;
import com.hscloud.hs.cost.account.constant.enums.kpi.CaliberEnum;
import com.hscloud.hs.cost.account.constant.enums.kpi.ComputeOperatorEnum;
import com.hscloud.hs.cost.account.constant.enums.kpi.EquivalentDistributeEnum;
import com.hscloud.hs.cost.account.constant.enums.kpi.EquivalentTaskStatusEnum;
import com.hscloud.hs.cost.account.mapper.kpi.KpiItemEquivalentChangeRecordMapper;
import com.hscloud.hs.cost.account.mapper.kpi.KpiItemEquivalentMapper;
import com.hscloud.hs.cost.account.model.dto.kpi.*;
import com.hscloud.hs.cost.account.model.entity.kpi.*;
import com.hscloud.hs.cost.account.model.vo.kpi.KpiConfigVO;
import com.hscloud.hs.cost.account.model.vo.kpi.KpiItemEquivalentChangeRecordVO;
import com.hscloud.hs.cost.account.model.vo.kpi.KpiItemEquivalentChildVO;
import com.hscloud.hs.cost.account.model.vo.kpi.KpiItemEquivalentVO;
import com.hscloud.hs.cost.account.service.kpi.IKpiConfigService;
import com.hscloud.hs.cost.account.service.kpi.IKpiItemEquivalentChangeRecordService;
import com.hscloud.hs.cost.account.service.kpi.KpiAccountUnitService;
import com.pig4cloud.pigx.admin.api.entity.SysUser;
import com.pig4cloud.pigx.admin.api.feign.RemoteUserService;
import com.pig4cloud.pigx.common.core.exception.BizException;
import com.pig4cloud.pigx.common.core.util.R;
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
import java.math.RoundingMode;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


/**
 * 当量调整记录 服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KpiItemEquivalentChangeRecordService extends ServiceImpl<KpiItemEquivalentChangeRecordMapper,
        KpiItemEquivalentChangeRecord> implements IKpiItemEquivalentChangeRecordService {

    private final KpiItemEquivalentService kpiItemEquivalentService;
    private final KpiItemEquivalentMapper kpiItemEquivalentMapper;
    private final KpiItemService kpiItemService;
    private final IKpiConfigService iKpiConfigService;
    private final KpiUserAttendanceService kpiUserAttendanceService;
    private final KpiAccountUnitService kpiAccountUnitService;
    private final RemoteUserService remoteUserService;
    private final KpiItemEquivalentConfigService kpiItemEquivalentConfigService;
    private final KpiItemEquivalentTaskService kpiItemEquivalentTaskService;
    private final KpiConfigService kpiConfigService;

    @Override
    public List<KpiItemEquivalentChangeRecordVO> getList(KpiItemEquivalentChangeDTO dto) {
        Long equivalentId = dto.getEquivalentId();
        if (equivalentId == null || equivalentId <= 0) {
            throw new BizException("当量id不能为空");
        }

        String changeFlag = dto.getChangeFlag();
        if (!StringUtils.hasText(changeFlag)) {
            throw new BizException("修改者类型不能为空");
        }

        KpiItemEquivalent equivalent = kpiItemEquivalentService.getById(equivalentId);
        if (equivalent == null) {
            throw new BizException("未查询到当量数据");
        }

        String accountUnitName = "";
        KpiAccountUnit accountUnit = kpiAccountUnitService.getById(equivalent.getAccountUnitId());
        if (accountUnit != null) {
            accountUnitName = accountUnit.getName();
        }

        String changeName = "";
        if (equivalent.getUserId() != null && equivalent.getUserId() > 0) {
            KpiUserAttendance userAttendance = kpiUserAttendanceService.getOne(Wrappers.<KpiUserAttendance>lambdaQuery()
                    .eq(KpiUserAttendance::getPeriod, equivalent.getPeriod())
                    .eq(KpiUserAttendance::getAccountUnit, equivalent.getAccountUnitId())
                    .eq(KpiUserAttendance::getUserId, equivalent.getUserId())
                    .last("limit 1"));
            if (userAttendance != null) {
                changeName = userAttendance.getEmpName();
            }
        }

        List<KpiItemEquivalentChangeRecord> records;

        Map<Long, KpiItemEquivalentChildVO> childVOMap = null;
        if (CaliberEnum.DEPT.getType().equals(equivalent.getEquivalentType())) {
            KpiItemEquivalentDTO equivalentDTO = new KpiItemEquivalentDTO();
            equivalentDTO.setId(equivalentId);
            KpiItemEquivalentVO parentVO = kpiItemEquivalentService.getParentVO(equivalentDTO);
            if (parentVO == null) {
                throw new BizException("未查询到当量数据");
            }

            childVOMap = parentVO.getChildVOList()
                    .stream().collect(Collectors.toMap(KpiItemEquivalentChildVO::getId, Function.identity(), (x, y) -> x));

            records = this.list(Wrappers.<KpiItemEquivalentChangeRecord>lambdaQuery()
                    .eq(StringUtils.hasText(dto.getStatus()), KpiItemEquivalentChangeRecord::getStatus, dto.getStatus())
                    .eq("1".equals(changeFlag), KpiItemEquivalentChangeRecord::getStatus, EquivalentTaskStatusEnum.APPROVED.getCode())
                    .ne("0".equals(changeFlag), KpiItemEquivalentChangeRecord::getStatus, EquivalentTaskStatusEnum.APPROVED.getCode())
                    .isNull(KpiItemEquivalentChangeRecord::getDistributeType)
                    .and(x -> x.eq(KpiItemEquivalentChangeRecord::getPEquivalentId, equivalentId)
                            .or().eq(KpiItemEquivalentChangeRecord::getEquivalentId, equivalentId)));
            if (CollectionUtils.isEmpty(records)) {
                return Collections.emptyList();
            }
        } else {
            records = this.list(Wrappers.<KpiItemEquivalentChangeRecord>lambdaQuery()
                    .eq(StringUtils.hasText(dto.getStatus()), KpiItemEquivalentChangeRecord::getStatus, dto.getStatus())
                    .eq("1".equals(changeFlag), KpiItemEquivalentChangeRecord::getStatus, EquivalentTaskStatusEnum.APPROVED.getCode())
                    .ne("0".equals(changeFlag), KpiItemEquivalentChangeRecord::getStatus, EquivalentTaskStatusEnum.APPROVED.getCode())
                    .eq(KpiItemEquivalentChangeRecord::getEquivalentId, equivalentId));

            if (CollectionUtils.isEmpty(records)) {
                return Collections.emptyList();
            }
        }

        List<Long> userIds = records.stream()
                .filter(x -> x.getChangeUserId() != null && x.getChangeUserId() > 0)
                .map(KpiItemEquivalentChangeRecord::getChangeUserId)
                .collect(Collectors.toList());
        R<List<SysUser>> userList = remoteUserService.getUserListPost(userIds);
        Map<Long, String> userMap = new HashMap<>();
        if (userList.getCode() == 0) {
            List<SysUser> sysUsers = userList.getData();
            userMap = sysUsers.stream()
                    .collect(Collectors.toMap(SysUser::getUserId, SysUser::getName, (x, y) -> x));
        }

        List<KpiItemEquivalentChangeRecordVO> result = new ArrayList<>();
        for (KpiItemEquivalentChangeRecord record : records) {
            KpiItemEquivalentChangeRecordVO vo = new KpiItemEquivalentChangeRecordVO();
            BeanUtils.copyProperties(record, vo);
            if (userMap.containsKey(record.getChangeUserId())) {
                vo.setChangeUserName(userMap.get(record.getChangeUserId()));
            }

            if (vo.getPEquivalentId().equals(0L)) {
                vo.setChangeName(accountUnitName);
            } else if (CaliberEnum.PEOPLE.getType().equals(equivalent.getEquivalentType())) {
                vo.setChangeName(changeName);
            } else if (childVOMap != null && childVOMap.containsKey(vo.getEquivalentId())) {
                vo.setChangeName(childVOMap.get(vo.getEquivalentId()).getEmpName());
            }

            result.add(vo);
        }


        result = result.stream()
                .collect(Collectors.groupingBy(KpiItemEquivalentChangeRecordVO::getGroupUuid))
                .values().stream()
                .peek(group -> group.sort(
                        Comparator.comparing((KpiItemEquivalentChangeRecordVO vo) -> vo.getPEquivalentId().equals(0L) ? 0 : 1)
                                .thenComparing(KpiItemEquivalentChangeRecordVO::getCreatedDate, Comparator.reverseOrder())))
                .sorted(Comparator.comparing(group -> group.get(0).getCreatedDate(), Comparator.reverseOrder()))
                .flatMap(List::stream)
                .collect(Collectors.toList());

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveRecord(KpiItemEquivalentChangeDTO dto) {
        Long itemId = dto.getItemId();
        if (itemId == null || itemId <= 0) {
            throw new BizException("核算项id不能为空");
        }

        KpiItem kpiItem = kpiItemService.getById(itemId);
        if (kpiItem == null) {
            throw new BizException("未查询到核算项");
        }

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
            throw new BizException("当量已锁定无法校准");
        }

        if ("0".equals(dto.getChangeFlag())) {
            KpiItemEquivalentTask task = kpiItemEquivalentTaskService.getOne(Wrappers.<KpiItemEquivalentTask>lambdaQuery()
                    .eq(KpiItemEquivalentTask::getItemId, itemId)
                    .eq(KpiItemEquivalentTask::getPeriod, period)
                    .eq(KpiItemEquivalentTask::getAccountUnitId, dto.getAccountUnitId())
                    .last("limit 1"));
            if (task == null) {
                throw new BizException("未查询到当量核验任务");
            }

            if (EquivalentTaskStatusEnum.APPROVED.getCode().equals(task.getStatus())) {
                task.setStatus(EquivalentTaskStatusEnum.PENDING_SUBMIT.getCode());
                task.setCommittedDate(new Date());
                kpiItemEquivalentTaskService.updateById(task);
            }

            dto.setTaskId(task.getId());
        }

        KpiItemEquivalentDTO equivalentDTO = new KpiItemEquivalentDTO();
        if (CaliberEnum.DEPT.getType().equals(kpiItem.getCaliber())) {
            equivalentDTO.setId(dto.getEquivalentId());
        } else if (CaliberEnum.PEOPLE.getType().equals(kpiItem.getCaliber())) {
            equivalentDTO.setAccountUnitId(dto.getAccountUnitId());
            equivalentDTO.setItemId(itemId);
            equivalentDTO.setPeriod(period);
        } else {
            throw new BizException("核算项类型不支持");
        }

        KpiItemEquivalentVO pEquivalentVO = kpiItemEquivalentService.getParentVO(equivalentDTO);
        if (pEquivalentVO == null) {
            throw new BizException("未查询到当量数据");
        }

        List<KpiItemEquivalent> updateEquivalents = new ArrayList<>();
        List<KpiItemEquivalentChangeRecord> changeRecords = new ArrayList<>();

        calculate(dto, pEquivalentVO, updateEquivalents, changeRecords, kpiItem.getCaliber());

        if (!CollectionUtils.isEmpty(updateEquivalents)) {
            kpiItemEquivalentMapper.updateBatchById(updateEquivalents);
            log.info("当量数据校准：更新当量数据数量：{}", updateEquivalents.size());
        }

        List<Long> delIds = new ArrayList<>();
        if (CaliberEnum.DEPT.getType().equals(kpiItem.getCaliber())) {
            delIds.add(pEquivalentVO.getId());
            delIds.addAll(pEquivalentVO.getChildVOList().stream().map(KpiItemEquivalentChildVO::getId).collect(Collectors.toList()));
        } else {
            delIds.add(dto.getEquivalentId());
        }

        if (!CollectionUtils.isEmpty(delIds)) {
            this.remove(Wrappers.<KpiItemEquivalentChangeRecord>lambdaQuery()
                    .ne(KpiItemEquivalentChangeRecord::getStatus, EquivalentTaskStatusEnum.APPROVED.getCode())
                    .in(KpiItemEquivalentChangeRecord::getEquivalentId, delIds));
            log.info("当量数据校准：删除未通过的当量调整记录数量：{}", delIds.size());
        }

        if (!CollectionUtils.isEmpty(changeRecords)) {
            this.saveOrUpdateBatch(changeRecords);
            log.info("当量数据校准：保存当量调整记录数量：{}", changeRecords.size());
        }

        if ("1".equals(dto.getChangeFlag())) {
            log.info("当量数据校准：重新下发任务");
            kpiItemEquivalentTaskService.reIssueTask(period, Collections.singletonList(itemId), dto.getAccountUnitId());
        }
    }

    public void calculate(KpiItemEquivalentChangeDTO dto, KpiItemEquivalentVO pEquivalentVO, List<KpiItemEquivalent> updateEquivalents,
                          List<KpiItemEquivalentChangeRecord> changeRecords, String changeType) {
        String changeFlag = dto.getChangeFlag();

        KpiItemEquivalent pEquivalent = new KpiItemEquivalent();
        pEquivalent.setId(pEquivalentVO.getId());
        pEquivalent.setTotalWorkload(pEquivalentVO.getTotalWorkload());
        pEquivalent.setTotalWorkloadAdmin(pEquivalentVO.getTotalWorkloadAdmin());
        pEquivalent.setNewTotalWorkload(pEquivalentVO.getNewTotalWorkload());

        String groupUuid = UUID.randomUUID().toString();
        BigDecimal pBbeforeValue = pEquivalent.getTotalWorkloadAdmin() != null ? pEquivalent.getTotalWorkloadAdmin() : pEquivalent.getTotalWorkload();

        KpiItemEquivalentChangeRecord pRecord = new KpiItemEquivalentChangeRecord();
        BeanUtils.copyProperties(dto, pRecord);
        pRecord.setEquivalentId(pEquivalent.getId());
        pRecord.setPEquivalentId(0L);
        pRecord.setGroupUuid(groupUuid);
        pRecord.setBeforeValue(pBbeforeValue);
        pRecord.setChangeUserId(SecurityUtils.getUser().getId());
        if ("1".equals(changeFlag)) {
            pRecord.setStatus(EquivalentTaskStatusEnum.APPROVED.getCode());
        } else {
            pRecord.setStatus(EquivalentTaskStatusEnum.PENDING_SUBMIT.getCode());
        }

        List<KpiItemEquivalent> childUpdateRecords = new ArrayList<>();
        List<KpiItemEquivalentChangeRecord> childRecords = new ArrayList<>();

        BigDecimal pAfterValue;

        if (CaliberEnum.PEOPLE.getType().equals(changeType)) {
            log.info("处理核算项颗粒度为人员的当量数据");
            processPeopleItemEquivalent(dto, pEquivalentVO, pRecord, pEquivalent, groupUuid, childUpdateRecords, childRecords);
            if ("1".equals(changeFlag)) {
                pAfterValue = pEquivalent.getTotalWorkloadAdmin();
            } else {
                pAfterValue = pEquivalent.getNewTotalWorkload();
            }
        } else {
            log.info("处理核算项颗粒度为科室的当量数据");
            pAfterValue = calNewTotalWorkload(dto.getChangeValue(), dto.getOperators(), pBbeforeValue);
            if ("1".equals(changeFlag)) {
                pEquivalent.setTotalWorkloadAdmin(pAfterValue);
            } else {
                pEquivalent.setNewTotalWorkload(pAfterValue);
            }

            if (YesNoEnum.YES.getValue().equals(pEquivalentVO.getAssignFlag())) {
                log.info("需要当量分配的核算项进行分配处理");
                distributeByType(dto, pEquivalentVO, pEquivalent, childUpdateRecords);
            }
        }
        updateEquivalents.addAll(childUpdateRecords);
        changeRecords.addAll(childRecords);

        if (pBbeforeValue.compareTo(pAfterValue) != 0) {
            updateEquivalents.add(pEquivalent);
        }

        BigDecimal afterValue = calNewTotalWorkload(pRecord.getChangeValue(), pRecord.getOperators(), pRecord.getBeforeValue());
        if (afterValue.compareTo(pRecord.getBeforeValue()) != 0) {
            changeRecords.add(pRecord);
        }
    }

    private void distributeByType(KpiItemEquivalentChangeDTO dto, KpiItemEquivalentVO pEquivalentVO, KpiItemEquivalent pEquivalent,
                                  List<KpiItemEquivalent> childUpdateRecords) {
        BigDecimal pTotalWorkload = Optional.ofNullable(pEquivalent.getNewTotalWorkload()).orElse(pEquivalent.getTotalWorkloadAdmin());
        String distributeType = pEquivalentVO.getDistributeType();
        List<KpiItemEquivalentChildVO> childVOS = pEquivalentVO.getChildVOList();
        String changeFlag = dto.getChangeFlag();

        if (EquivalentDistributeEnum.AVERAGE.getCode().equals(distributeType) ||
                EquivalentDistributeEnum.COEFFICIENT.getCode().equals(distributeType)) {

            BigDecimal totalCoefficient = BigDecimal.ZERO;
            if (EquivalentDistributeEnum.COEFFICIENT.getCode().equals(distributeType)) {
                for (KpiItemEquivalentChildVO vo : childVOS) {
                    totalCoefficient = totalCoefficient.add(vo.getCoefficient());
                }
            }

            for (KpiItemEquivalentChildVO vo : childVOS) {
                KpiItemEquivalent equivalent = new KpiItemEquivalent();
                equivalent.setId(vo.getId());
                BigDecimal afterValue;
                if (EquivalentDistributeEnum.AVERAGE.getCode().equals(distributeType)) {
                    afterValue = pTotalWorkload.divide(new BigDecimal(childVOS.size()), 6, RoundingMode.HALF_UP);
                } else {
                    if (totalCoefficient.compareTo(BigDecimal.ZERO) <= 0) {
                        throw new BizException("系数之和必须为正数");
                    }
                    afterValue = pTotalWorkload.multiply(vo.getCoefficient()).divide(totalCoefficient, 6, RoundingMode.HALF_UP);
                }

                BigDecimal beforeValue = vo.getTotalWorkloadAdmin() != null ? vo.getTotalWorkloadAdmin() : vo.getTotalWorkload();
                if (beforeValue.compareTo(afterValue) == 0) {
                    continue;
                }

                if ("1".equals(changeFlag)) {
                    equivalent.setTotalWorkloadAdmin(afterValue);
                } else {
                    equivalent.setNewTotalWorkload(afterValue);
                }
                childUpdateRecords.add(equivalent);
            }
        }
    }

    private void processPeopleItemEquivalent(KpiItemEquivalentChangeDTO dto, KpiItemEquivalentVO pEquivalentVO,
                                             KpiItemEquivalentChangeRecord pRecord, KpiItemEquivalent pEquivalent,
                                             String groupUuid, List<KpiItemEquivalent> childUpdateRecords,
                                             List<KpiItemEquivalentChangeRecord> childRecords) {
        List<KpiItemEquivalentChildVO> childVOS = pEquivalentVO.getChildVOList();
        String changeFlag = dto.getChangeFlag();

        KpiItemEquivalentChildVO change = Linq.of(childVOS)
                .where(x -> x.getId().equals(dto.getEquivalentId())).firstOrDefault();
        if (change == null) {
            throw new BizException("未查询到要修改的人员当量数据");
        }

        KpiItemEquivalentChangeRecord lastPRecord = this.getOne(Wrappers.<KpiItemEquivalentChangeRecord>lambdaQuery()
                .eq("1".equals(changeFlag), KpiItemEquivalentChangeRecord::getStatus, EquivalentTaskStatusEnum.APPROVED.getCode())
                .ne("0".equals(changeFlag), KpiItemEquivalentChangeRecord::getStatus, EquivalentTaskStatusEnum.APPROVED.getCode())
                .eq(KpiItemEquivalentChangeRecord::getEquivalentId, pEquivalentVO.getId())
                .orderByDesc(KpiItemEquivalentChangeRecord::getId)
                .last("limit 1"));
        if (lastPRecord != null && changeFlag.equals(lastPRecord.getChangeFlag())) {
            groupUuid = lastPRecord.getGroupUuid();
            pRecord.setId(lastPRecord.getId());
            pRecord.setGroupUuid(groupUuid);
        }

        BigDecimal childTotalWorkload = BigDecimal.ZERO;

        for (KpiItemEquivalentChildVO vo : childVOS) {
            if (vo.getId().equals(dto.getEquivalentId())) {
                KpiItemEquivalent equivalent = new KpiItemEquivalent();
                BeanUtils.copyProperties(vo, equivalent);

                BigDecimal beforeValue = equivalent.getTotalWorkloadAdmin() != null ? equivalent.getTotalWorkloadAdmin() : equivalent.getTotalWorkload();
                BigDecimal afterValue = calNewTotalWorkload(dto.getChangeValue(), dto.getOperators(), beforeValue);
                if (beforeValue.compareTo(afterValue) == 0) {
                    continue;
                }

                if ("1".equals(changeFlag)) {
                    equivalent.setTotalWorkloadAdmin(afterValue);
                } else {
                    equivalent.setNewTotalWorkload(afterValue);
                }

                childUpdateRecords.add(equivalent);

                childTotalWorkload = childTotalWorkload.add(afterValue);

                KpiItemEquivalentChangeRecord record = new KpiItemEquivalentChangeRecord();
                BeanUtils.copyProperties(dto, record);
                record.setChangeUserId(SecurityUtils.getUser().getId());
                if ("1".equals(changeFlag)) {
                    record.setStatus(EquivalentTaskStatusEnum.APPROVED.getCode());
                } else {
                    record.setStatus(EquivalentTaskStatusEnum.PENDING_SUBMIT.getCode());
                }
                record.setGroupUuid(groupUuid);
                record.setPEquivalentId(pEquivalent.getId());
                record.setBeforeValue(beforeValue);
                childRecords.add(record);
            } else {
                BigDecimal voTotalWorkload;
                if ("1".equals(changeFlag)) {
                    voTotalWorkload = vo.getTotalWorkloadAdmin() != null ? vo.getTotalWorkloadAdmin() : vo.getTotalWorkload();
                } else {
                    voTotalWorkload = vo.getNewTotalWorkload() != null ? vo.getNewTotalWorkload() : vo.getTotalWorkloadAdmin() != null ? vo.getTotalWorkloadAdmin() : vo.getTotalWorkload();
                }
                childTotalWorkload = childTotalWorkload.add(voTotalWorkload);
            }
        }

        if ("1".equals(changeFlag)) {
            pEquivalent.setTotalWorkloadAdmin(childTotalWorkload);
        } else {
            pEquivalent.setNewTotalWorkload(childTotalWorkload);
        }

        pRecord.setOperators(ComputeOperatorEnum.EQ.getCode());
        pRecord.setChangeValue(childTotalWorkload);
        pRecord.setReason(null);
        pRecord.setGroupUuid(groupUuid);
        pRecord.setEquivalentId(pEquivalentVO.getId());
        pRecord.setCreatedDate(new Date());
    }

    public BigDecimal calNewTotalWorkload(BigDecimal changeValue, String dtoOperators, BigDecimal totalWorkload) {
        BigDecimal newTotalWorkload;
        switch (ComputeOperatorEnum.getByCode(dtoOperators)) {
            case ADD:
                newTotalWorkload = totalWorkload.add(changeValue);
                break;
            case SUB:
                newTotalWorkload = totalWorkload.subtract(changeValue);
                break;
            case MUL:
                newTotalWorkload = totalWorkload.multiply(changeValue).setScale(6, RoundingMode.HALF_UP);
                break;
            case EQ:
                newTotalWorkload = changeValue;
                break;
            default:
                throw new BizException("不支持的操作符：" + dtoOperators);
        }

        return newTotalWorkload;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateDistribute(KpiItemEquivalentDistributeDTO dto) {
        List<KpiItemEquivalentChangeRecord> changeRecords = new ArrayList<>();
        List<KpiItemEquivalent> equivalentRecords = new ArrayList<>();

        Long itemId = dto.getItemId();
        if (itemId == null || itemId <= 0) {
            throw new BizException("核算项id不能为空");
        }

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
        KpiConfigVO kpiConfigVO = kpiConfigService.getConfig(searchDto);
        if (YesNoEnum.YES.getCode().equals(kpiConfigVO.getEquivalentFlag())) {
            throw new BizException("当量已锁定，无法修改分配方式");
        }

        KpiItemEquivalentDTO equivalentDTO = new KpiItemEquivalentDTO();
        equivalentDTO.setItemId(itemId);
        equivalentDTO.setAccountUnitId(dto.getAccountUnitId());
        equivalentDTO.setPeriod(period);
        KpiItemEquivalentVO parentVO = kpiItemEquivalentService.getParentVO(equivalentDTO);
        if (parentVO == null) {
            throw new BizException("未查询到当量数据");
        }

        KpiItemEquivalent pEquivalent = new KpiItemEquivalent();
        BeanUtils.copyProperties(parentVO, pEquivalent);
        String changeFlag = dto.getChangeFlag();
        if ("1".equals(changeFlag)) {
            pEquivalent.setDistributeType(dto.getDistributeType());
        }
        equivalentRecords.add(pEquivalent);

        List<Long> delIds = parentVO.getChildVOList().stream()
                .map(KpiItemEquivalentChildVO::getId).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(delIds)) {
            this.remove(Wrappers.<KpiItemEquivalentChangeRecord>lambdaQuery()
                    .ne(KpiItemEquivalentChangeRecord::getStatus, EquivalentTaskStatusEnum.APPROVED.getCode())
                    .in(KpiItemEquivalentChangeRecord::getEquivalentId, delIds));
            log.info("修改分配方式：删除未通过的当量调整记录: {}", delIds.size());
        }

        Long taskId = 0L;
        if ("0".equals(changeFlag)) {
            KpiItemEquivalentTask task = kpiItemEquivalentTaskService.getOne(Wrappers.<KpiItemEquivalentTask>lambdaQuery()
                    .eq(KpiItemEquivalentTask::getItemId, itemId)
                    .eq(KpiItemEquivalentTask::getPeriod, period)
                    .eq(KpiItemEquivalentTask::getAccountUnitId, dto.getAccountUnitId())
                    .last("limit 1"));
            if (task == null) {
                throw new BizException("未查询到当量核验任务");
            }
            taskId = task.getId();
            if (EquivalentTaskStatusEnum.APPROVED.getCode().equals(task.getStatus())) {
                task.setStatus(EquivalentTaskStatusEnum.PENDING_SUBMIT.getCode());
                task.setCommittedDate(new Date());
                kpiItemEquivalentTaskService.updateById(task);
            }
        }

        distributeCalculate(dto.getDistributeType(), dto.getCoefficientMap(), dto.getChangeRecords(), changeFlag, parentVO, changeRecords, equivalentRecords, taskId);

        kpiItemEquivalentMapper.updateBatchById(equivalentRecords);
        log.info("修改分配方式：更新当量数量: {}", equivalentRecords.size());

        if ("1".equals(changeFlag)) {
            log.info("修改分配方式：重新发起当量核验任务");
            kpiItemEquivalentTaskService.reIssueTask(period, Collections.singletonList(itemId), dto.getAccountUnitId());
        }

        this.saveBatch(changeRecords);
        log.info("修改分配方式：保存当量调整记录: {}", changeRecords.size());
    }

    public void distributeCalculate(String distributeType, Map<Long, BigDecimal> coefficientMap, List<KpiItemEquivalentChangeDTO> childDTOs,
                                    String changeFlag, KpiItemEquivalentVO parentVO, List<KpiItemEquivalentChangeRecord> changeRecords,
                                    List<KpiItemEquivalent> equivalentRecords, Long taskId) {
        Long pid = parentVO.getId();
        String groupUuid = UUID.randomUUID().toString();

        List<KpiItemEquivalentChildVO> childVOS = parentVO.getChildVOList();
        BigDecimal pTotalWorkloadAdmin;
        if ("1".equals(changeFlag)) {
            pTotalWorkloadAdmin = parentVO.getTotalWorkloadAdmin() != null ? parentVO.getTotalWorkloadAdmin() : parentVO.getTotalWorkload();
        } else {
            if (parentVO.getNewTotalWorkload() != null) {
                pTotalWorkloadAdmin = parentVO.getNewTotalWorkload();
            } else if (parentVO.getTotalWorkloadAdmin() != null) {
                pTotalWorkloadAdmin = parentVO.getTotalWorkloadAdmin();
            } else {
                pTotalWorkloadAdmin = parentVO.getTotalWorkload();
            }
        }

        BigDecimal totalCoefficient = BigDecimal.ZERO;
        if (EquivalentDistributeEnum.AVERAGE.getCode().equals(distributeType) ||
                EquivalentDistributeEnum.COEFFICIENT.getCode().equals(distributeType)) {

            if (EquivalentDistributeEnum.COEFFICIENT.getCode().equals(distributeType)) {
                if (coefficientMap == null || coefficientMap.isEmpty()) {
                    throw new BizException("系数不能为空");
                }

                childVOS.forEach(vo -> {
                    BigDecimal coefficient = coefficientMap.get(vo.getUserId());
                    if (coefficient == null) {
                        throw new BizException("人员id: " + vo.getUserId() + "的系数不能为空");
                    }
                    vo.setCoefficient(coefficient);
                });

                for (KpiItemEquivalentChildVO vo : childVOS) {
                    totalCoefficient = totalCoefficient.add(vo.getCoefficient());
                }
            }

            for (KpiItemEquivalentChildVO vo : childVOS) {
                BigDecimal coefficient = vo.getCoefficient();
                BigDecimal beforeValue = vo.getTotalWorkloadAdmin() != null ? vo.getTotalWorkloadAdmin() : vo.getTotalWorkload();

                KpiItemEquivalent equivalent = new KpiItemEquivalent();
                equivalent.setId(vo.getId());

                KpiItemEquivalentChangeRecord record = new KpiItemEquivalentChangeRecord();
                record.setTaskId(taskId);
                record.setEquivalentId(equivalent.getId());
                record.setBeforeValue(beforeValue);
                record.setOperators(ComputeOperatorEnum.EQ.getCode());
                record.setGroupUuid(groupUuid);
                record.setPEquivalentId(pid);
                record.setChangeFlag(changeFlag);
                record.setDistributeType(distributeType);
                record.setChangeUserId(SecurityUtils.getUser().getId());

                BigDecimal afterValue;
                if (EquivalentDistributeEnum.AVERAGE.getCode().equals(distributeType)) {
                    afterValue = pTotalWorkloadAdmin.divide(new BigDecimal(childVOS.size()), 6, RoundingMode.HALF_UP);
                } else {
                    if (totalCoefficient.compareTo(BigDecimal.ZERO) <= 0) {
                        throw new BizException("系数之和必须为正数");
                    }
                    afterValue = pTotalWorkloadAdmin.multiply(coefficient).divide(totalCoefficient, 6, RoundingMode.HALF_UP);

                    record.setCoefficient(coefficient);
                }
                record.setChangeValue(afterValue);

                // 绩效办
                if ("1".equals(changeFlag)) {
                    equivalent.setDistributeType(distributeType);
                    if (beforeValue.compareTo(afterValue) != 0) {
                        equivalent.setTotalWorkloadAdmin(afterValue);
                    }
                    if (EquivalentDistributeEnum.COEFFICIENT.getCode().equals(distributeType)) {
                        equivalent.setCoefficient(coefficient);
                    }
                    record.setStatus(EquivalentTaskStatusEnum.APPROVED.getCode());
                } else {
                    if (beforeValue.compareTo(afterValue) != 0) {
                        equivalent.setNewTotalWorkload(afterValue);
                    }
                    record.setStatus(EquivalentTaskStatusEnum.PENDING_SUBMIT.getCode());
                }

                equivalentRecords.add(equivalent);

                if (beforeValue.compareTo(afterValue) != 0) {
                    changeRecords.add(record);
                }
            }
        } else if (EquivalentDistributeEnum.CUSTOM.getCode().equals(distributeType)) {
            if (CollectionUtils.isEmpty(childDTOs)) {
                throw new BizException("人员当量数据不能为空");
            }

            BigDecimal childTotalWorkload = BigDecimal.ZERO;

            for (KpiItemEquivalentChildVO vo : childVOS) {
                KpiItemEquivalentChangeDTO childDto = Linq.of(childDTOs)
                        .where(x -> x.getEquivalentId().equals(vo.getId()))
                        .firstOrDefault();
                if (childDto == null) {
                    throw new BizException("人员当量数据缺失");
                }

                KpiItemEquivalent equivalent = new KpiItemEquivalent();
                equivalent.setId(vo.getId());
                equivalent.setNewTotalWorkload(vo.getNewTotalWorkload());
                equivalent.setTotalWorkload(vo.getTotalWorkload());
                equivalent.setTotalWorkloadAdmin(vo.getTotalWorkloadAdmin());

                BigDecimal beforeAdminValue = equivalent.getTotalWorkloadAdmin() != null ? equivalent.getTotalWorkloadAdmin() : equivalent.getTotalWorkload();
                BigDecimal afterValue = childDto.getChangeValue();

                childTotalWorkload = childTotalWorkload.add(afterValue);

                if (beforeAdminValue.compareTo(afterValue) != 0) {
                    if ("1".equals(changeFlag)) {
                        equivalent.setTotalWorkloadAdmin(afterValue);
                        equivalent.setDistributeType(distributeType);
                    } else {
                        equivalent.setNewTotalWorkload(afterValue);
                    }
                } else {
                    equivalent.setNewTotalWorkload(null);
                }

                equivalentRecords.add(equivalent);

                if (beforeAdminValue.compareTo(afterValue) == 0) {
                    continue;
                }

                KpiItemEquivalentChangeRecord record = new KpiItemEquivalentChangeRecord();
                BeanUtils.copyProperties(childDto, record);
                record.setTaskId(taskId);
                record.setBeforeValue(beforeAdminValue);
                record.setChangeUserId(SecurityUtils.getUser().getId());
                record.setGroupUuid(groupUuid);
                record.setPEquivalentId(pid);
                record.setDistributeType(distributeType);

                if ("1".equals(changeFlag)) {
                    record.setStatus(EquivalentTaskStatusEnum.APPROVED.getCode());
                } else {
                    record.setStatus(EquivalentTaskStatusEnum.PENDING_SUBMIT.getCode());
                }

                changeRecords.add(record);
            }

            if (childTotalWorkload.compareTo(pTotalWorkloadAdmin) != 0) {
                throw new BizException("指标“" + parentVO.getItemName() + "”分配的数额与科室总数额不匹配，请重新填写");
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void coefficientBatchSet(KpiItemCoefficientDTO dto) {
        Long accountUnitId = dto.getAccountUnitId();
        if (accountUnitId == null || accountUnitId <= 0) {
            throw new BizException("科室id不能为空");
        }

        List<Long> itemIds = dto.getItemIds();
        if (CollectionUtils.isEmpty(itemIds)) {
            throw new BizException("核算项ids不能为空");
        }

        Map<Long, BigDecimal> coefficientMap = dto.getCoefficientMap();
        if (coefficientMap == null || coefficientMap.isEmpty()) {
            throw new BizException("系数map不能为空");
        }

        Long period = dto.getPeriod();
        if (period == null) {
            String var1 = iKpiConfigService.getLastCycle(true);
            if (!StringUtils.hasLength(var1)) {
                throw new BizException("未查询到计算周期");
            } else {
                period = Long.valueOf(var1.replace("-", "").substring(0, 6));
            }
        }

        KpiConfigSearchDto searchDto = new KpiConfigSearchDto();
        searchDto.setPeriod(period);
        KpiConfigVO kpiConfigVO = kpiConfigService.getConfig(searchDto);
        if (YesNoEnum.YES.getCode().equals(kpiConfigVO.getEquivalentFlag())) {
            throw new BizException("当量已锁定，无法修改分配系数");
        }

        if (dto.getInheritFlag() == null) {
            dto.setInheritFlag(YesNoEnum.YES.getCode());
        }

        kpiItemService.list(Wrappers.<KpiItem>lambdaQuery().in(KpiItem::getId, itemIds)).stream()
                .filter(x -> !CaliberEnum.DEPT.getType().equals(x.getCaliber()))
                .findFirst().ifPresent(x -> {
                    throw new BizException("仅科室核算项可设置分配系数");
                });

        KpiItemEquivalentDTO equivalentDTO = new KpiItemEquivalentDTO();
        equivalentDTO.setAccountUnitId(accountUnitId);
        equivalentDTO.setPeriod(period);
        equivalentDTO.setItemIds(itemIds);
        List<KpiItemEquivalentVO> equivalentVOList = kpiItemEquivalentService.getList(equivalentDTO, true);
        if (CollectionUtils.isEmpty(equivalentVOList)) {
            throw new BizException("未查询要分配的当量数据");
        }

        List<Long> delIds = new ArrayList<>();
        for (KpiItemEquivalentVO equivalentVO : equivalentVOList) {
            delIds.addAll(equivalentVO.getChildVOList().stream()
                    .map(KpiItemEquivalentChildVO::getId).collect(Collectors.toList()));
        }

        if (!CollectionUtils.isEmpty(delIds)) {
            this.remove(Wrappers.<KpiItemEquivalentChangeRecord>lambdaQuery()
                    .ne(KpiItemEquivalentChangeRecord::getStatus, EquivalentTaskStatusEnum.APPROVED.getCode())
                    .in(KpiItemEquivalentChangeRecord::getEquivalentId, delIds));
            log.info("批量设置系数：删除未通过当量变更记录成功，删除数量：{}", delIds.size());
        }

        Map<Long, KpiItemEquivalentTask> taskIdMap;
        if ("0".equals(dto.getChangeFlag())) {
            taskIdMap = kpiItemEquivalentTaskService.list(Wrappers.<KpiItemEquivalentTask>lambdaQuery()
                            .in(KpiItemEquivalentTask::getItemId, itemIds)
                            .eq(KpiItemEquivalentTask::getPeriod, period)
                            .eq(KpiItemEquivalentTask::getAccountUnitId, dto.getAccountUnitId()))
                    .stream().collect(Collectors.toMap(KpiItemEquivalentTask::getItemId, Function.identity(), (v1, v2) -> v1));
        } else {
            taskIdMap = null;
        }

        List<KpiItemEquivalentConfig> configList = kpiItemEquivalentConfigService.list(Wrappers.<KpiItemEquivalentConfig>lambdaQuery()
                .eq(KpiItemEquivalentConfig::getAccountUnitId, accountUnitId)
                .in(KpiItemEquivalentConfig::getItemId, itemIds));
        configList.forEach(config -> config.setInheritFlag(dto.getInheritFlag()));

        List<KpiItemEquivalentChangeRecord> changeRecords = new ArrayList<>();
        List<KpiItemEquivalent> equivalentRecords = new ArrayList<>();

        String distributeType = EquivalentDistributeEnum.COEFFICIENT.getCode();
        List<KpiItemEquivalentTask> updateTaskList = new ArrayList<>();
        for (KpiItemEquivalentVO parentVO : equivalentVOList) {
            KpiItemEquivalent pEquivalent = new KpiItemEquivalent();
            BeanUtils.copyProperties(parentVO, pEquivalent);
            if ("1".equals(dto.getChangeFlag())) {
                pEquivalent.setDistributeType(distributeType);
            }
            equivalentRecords.add(pEquivalent);

            Long taskId = 0L;
            if (taskIdMap != null) {
                if (taskIdMap.containsKey(parentVO.getItemId())) {
                    KpiItemEquivalentTask task = taskIdMap.get(parentVO.getItemId());
                    taskId = task.getId();
                    if (EquivalentTaskStatusEnum.APPROVED.getCode().equals(task.getStatus())) {
                        task.setStatus(EquivalentTaskStatusEnum.PENDING_SUBMIT.getCode());
                        task.setCommittedDate(new Date());
                        updateTaskList.add(task);
                    }
                } else {
                    throw new BizException("未查询到当量核验任务");
                }
            }

            distributeCalculate(distributeType, coefficientMap, null, dto.getChangeFlag(), parentVO, changeRecords, equivalentRecords, taskId);
        }
        if (!CollectionUtils.isEmpty(updateTaskList)) {
            kpiItemEquivalentTaskService.updateBatchById(updateTaskList);
            log.info("批量设置系数：更新当量核验任务成功，更新数量：{}", updateTaskList.size());
        }

        if (!CollectionUtils.isEmpty(changeRecords)) {
            kpiItemEquivalentConfigService.updateBatchById(configList);
            log.info("批量设置系数：更新当量配置成功，更新数量：{}", configList.size());
        }

        if (!CollectionUtils.isEmpty(equivalentRecords)) {
            kpiItemEquivalentMapper.updateBatchById(equivalentRecords);
            log.info("批量设置系数：更新当量数据成功，更新数量：{}", equivalentRecords.size());
        }

        if ("1".equals(dto.getChangeFlag())) {
            log.info("批量设置系数：重新发起当量核验任务，周期：{}，科室id：{}", period, accountUnitId);
            kpiItemEquivalentTaskService.reIssueTask(period, itemIds, accountUnitId);
        }

        if (!CollectionUtils.isEmpty(changeRecords)) {
            this.saveBatch(changeRecords);
        }
        log.info("批量设置系数：保存当量变更记录成功，保存数量：{}", changeRecords.size());
    }

    @Override
    public List<KpiItemEquivalentChangeRecordVO> listByUnit(KpiItemEquivalentChangeDTO dto) {
        Long accountUnitId = dto.getAccountUnitId();
        Long period = dto.getPeriod();
        if (period == null) {
            String var1 = iKpiConfigService.getLastCycle(true);
            if (!StringUtils.hasLength(var1)) {
                throw new BizException("未查询到周期");
            } else {
                period = Long.valueOf(var1.replace("-", "").substring(0, 6));
            }
        }

        if (accountUnitId == null || accountUnitId <= 0) {
            throw new BizException("科室id不能为空");
        }

        List<KpiItemEquivalent> equivalents = kpiItemEquivalentService.list(Wrappers.<KpiItemEquivalent>lambdaQuery()
                .eq(KpiItemEquivalent::getAccountUnitId, accountUnitId)
                .eq(KpiItemEquivalent::getPeriod, period));
        if (CollectionUtils.isEmpty(equivalents)) {
            return Collections.emptyList();
        }

        List<KpiItemEquivalentChangeRecord> changeRecords = this.list(Wrappers.<KpiItemEquivalentChangeRecord>lambdaQuery()
                .eq(KpiItemEquivalentChangeRecord::getChangeFlag, "0")
                .in(KpiItemEquivalentChangeRecord::getEquivalentId, equivalents.stream().map(KpiItemEquivalent::getId).collect(Collectors.toList()))
                .isNull(KpiItemEquivalentChangeRecord::getDistributeType));
        if (CollectionUtils.isEmpty(changeRecords)) {
            return Collections.emptyList();
        }

        String accountUnitName = "";
        KpiAccountUnit accountUnit = kpiAccountUnitService.getById(accountUnitId);
        if (accountUnit != null) {
            accountUnitName = accountUnit.getName();
        }

        List<Long> userIds = equivalents.stream().filter(x -> x.getUserId() != null && x.getUserId() > 0)
                .map(KpiItemEquivalent::getUserId).collect(Collectors.toList());

        Map<Long, String> userMap = kpiUserAttendanceService.list(Wrappers.<KpiUserAttendance>lambdaQuery()
                        .eq(KpiUserAttendance::getPeriod, period)
                        .eq(KpiUserAttendance::getAccountUnit, accountUnitId)
                        .in(!CollectionUtils.isEmpty(userIds), KpiUserAttendance::getUserId, userIds))
                .stream().collect(Collectors.toMap(KpiUserAttendance::getUserId, KpiUserAttendance::getEmpName));

        List<Long> changeUserIds = changeRecords.stream()
                .filter(x -> x.getChangeUserId() != null && x.getChangeUserId() > 0)
                .map(KpiItemEquivalentChangeRecord::getChangeUserId)
                .collect(Collectors.toList());
        R<List<SysUser>> userList = remoteUserService.getUserListPost(changeUserIds);
        Map<Long, String> changeUserMap = new HashMap<>();
        if (userList.getCode() == 0) {
            List<SysUser> sysUsers = userList.getData();
            changeUserMap = sysUsers.stream()
                    .collect(Collectors.toMap(SysUser::getUserId, SysUser::getName, (x, y) -> x));
        }

        List<KpiItemEquivalentChangeRecordVO> result = new ArrayList<>();
        for (KpiItemEquivalentChangeRecord record : changeRecords) {
            KpiItemEquivalentChangeRecordVO vo = new KpiItemEquivalentChangeRecordVO();
            BeanUtils.copyProperties(record, vo);

            if (changeUserMap.containsKey(record.getChangeUserId())) {
                vo.setChangeUserName(changeUserMap.get(record.getChangeUserId()));
            }

            KpiItemEquivalent equivalent = Linq.of(equivalents).where(x -> x.getId().equals(vo.getEquivalentId())).firstOrDefault();

            if (vo.getPEquivalentId().equals(0L)) {
                vo.setChangeName(accountUnitName);
            } else {
                vo.setChangeName(userMap.get(equivalent.getUserId()));
            }
            vo.setItemName(equivalent.getItemName());

            result.add(vo);
        }

        result = result.stream()
                .collect(Collectors.groupingBy(KpiItemEquivalentChangeRecordVO::getGroupUuid))
                .values().stream()
                .peek(group -> group.sort(
                        Comparator.comparing((KpiItemEquivalentChangeRecordVO vo) -> vo.getPEquivalentId().equals(0L) ? 0 : 1)
                                .thenComparing(KpiItemEquivalentChangeRecordVO::getCreatedDate, Comparator.reverseOrder())))
                .sorted(Comparator.comparing(group -> group.get(0).getCreatedDate(), Comparator.reverseOrder()))
                .flatMap(List::stream)
                .collect(Collectors.toList());


        return result;
    }

    @Override
    public void reset(KpiItemEquivalentDTO dto) {
        List<Long> itemIds = dto.getItemIds();
        if (CollectionUtils.isEmpty(itemIds)) {
            throw new BizException("核算项ids不能为空");
        }

        Long period = dto.getPeriod();
        if (period == null) {
            String var1 = iKpiConfigService.getLastCycle(true);
            if (!StringUtils.hasLength(var1)) {
                throw new BizException("未查询到周期");
            } else {
                period = Long.valueOf(var1.replace("-", "").substring(0, 6));
            }
        }

        Long accountUnitId = dto.getAccountUnitId();
        if (accountUnitId == null || accountUnitId <= 0) {
            throw new BizException("科室id不能为空");
        }

        KpiItemEquivalentDTO equivalentDTO = new KpiItemEquivalentDTO();
        equivalentDTO.setPeriod(period);
        equivalentDTO.setAccountUnitId(accountUnitId);
        equivalentDTO.setItemIds(itemIds);
        List<KpiItemEquivalentVO> equivalentVOS = kpiItemEquivalentService.getList(equivalentDTO, true);
        if (CollectionUtils.isEmpty(equivalentVOS)) {
            throw new BizException("未查询到当量数据");
        }

        List<Long> delIds = equivalentVOS.stream()
                .map(KpiItemEquivalentVO::getId)
                .collect(Collectors.toList());
        equivalentVOS.forEach(equivalent -> {
            if (equivalent.getChildVOList() != null) {
                delIds.addAll(equivalent.getChildVOList().stream()
                        .map(KpiItemEquivalentChildVO::getId)
                        .collect(Collectors.toList()));
            }
        });

        if (!CollectionUtils.isEmpty(delIds)) {
            this.remove(Wrappers.<KpiItemEquivalentChangeRecord>lambdaQuery()
                    .ne(KpiItemEquivalentChangeRecord::getStatus, EquivalentTaskStatusEnum.APPROVED.getCode())
                    .in(KpiItemEquivalentChangeRecord::getEquivalentId, delIds));
            log.info("重置核验任务：删除当量变更记录数量: {}", delIds.size());
        }

        List<KpiItemEquivalent> updateList = new ArrayList<>();
        for (KpiItemEquivalentVO equivalentVO : equivalentVOS) {
            KpiItemEquivalent equivalent = new KpiItemEquivalent();
            equivalent.setId(equivalentVO.getId());
            equivalent.setNewTotalWorkload(null);
            updateList.add(equivalent);

            for (KpiItemEquivalentChildVO childVO : equivalentVO.getChildVOList()) {
                KpiItemEquivalent childEquivalent = new KpiItemEquivalent();
                childEquivalent.setId(childVO.getId());
                childEquivalent.setNewTotalWorkload(null);
                updateList.add(childEquivalent);
            }
        }

        if (!CollectionUtils.isEmpty(updateList)) {
            kpiItemEquivalentMapper.updateBatchById(updateList);
        }
        log.info("重置核验任务：更新当量数据数量: {}", updateList.size());
    }
}