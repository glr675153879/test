package com.hscloud.hs.cost.account.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hscloud.hs.cost.account.constant.CacheConstants;
import com.hscloud.hs.cost.account.constant.enums.AccountTaskStatus;
import com.hscloud.hs.cost.account.constant.enums.SnapShotEnum;
import com.hscloud.hs.cost.account.constant.enums.UnitMapEnum;
import com.hscloud.hs.cost.account.listener.SecondDistributionTaskCalculateEvent;
import com.hscloud.hs.cost.account.listener.TaskCalculateEvent;
import com.hscloud.hs.cost.account.mapper.*;
import com.hscloud.hs.cost.account.model.dto.*;
import com.hscloud.hs.cost.account.model.entity.*;
import com.hscloud.hs.cost.account.model.pojo.*;
import com.hscloud.hs.cost.account.model.vo.*;
import com.hscloud.hs.cost.account.service.*;
import com.hscloud.hs.cost.account.utils.ExpressionCheckHelper;
import com.hscloud.hs.cost.account.utils.LocalCacheUtils;
import com.hscloud.hs.cost.account.utils.SqlUtil;
import com.pig4cloud.pigx.admin.api.feign.RemoteUserService;
import com.pig4cloud.pigx.admin.api.vo.UserVO;
import com.pig4cloud.pigx.common.security.service.PigxUser;
import com.pig4cloud.pigx.common.security.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.dom4j.util.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static com.hscloud.hs.cost.account.utils.TimeUtils.parseCostTime;

/**
 * @author Admin
 */
@Service
@RequiredArgsConstructor
public class CostAccountTaskServiceImpl extends ServiceImpl<CostAccountTaskMapper, CostAccountTask> implements CostAccountTaskService {

    private final ApplicationEventPublisher publisher;

    private final CostTaskSnapshotService costTaskSnapshotService;

    private final RemoteUserService remoteUserService;

    private final CostAccountUnitService costAccountUnitService;

    private final CostAccountTaskMapper costAccountTaskMapper;

    private final CostAccountPlanConfigService costAccountPlanConfigService;

    private final CostTaskExecuteResultMapper costTaskExecuteResultMapper;

    private final CostTaskExecuteResultItemMapper costTaskExecuteResultItemMapper;

    private final CostTaskExecuteResultIndexMapper costTaskExecuteResultIndexMapper;

    private final CostAccountIndexMapper costAccountIndexMapper;

    private final SqlUtil sqlUtil;

    private final LocalCacheUtils cacheUtils;

    private final CostAccountPlanConfigFormulaMapper configFormulaMapper;

    private final ICostAccountTaskNewService taskNewService;

    private final ICostAccountTaskConfigService taskConfigService;

    private final ICostAccountTaskConfigIndexService taskConfigIndexService;

    private final CostAccountTaskNewMapper taskNewMapper;

    private final StringRedisTemplate redisTemplate;

    private final Gson gson;

    @Value("${mock.flag:false}")
    private Boolean mockFlag;

    @Override
    public Boolean saveTask(CostAccountTask costAccountTask) {
//        if (!mockFlag) {
//            if (YesNoEnum.YES.getValue().equals(costAccountTask.getSupportStatistics())) {
//                //维度不能重复
//                CostAccountTask task = this.getOne(Wrappers.<CostAccountTask>lambdaQuery()
//                        .eq(CostAccountTask::getAccountType, costAccountTask.getAccountType())
//                        .eq(StrUtil.isNotBlank(costAccountTask.getDimension()), CostAccountTask::getDimension, costAccountTask.getDimension())
//                        .eq(StrUtil.isNotBlank(costAccountTask.getDetailDim()), CostAccountTask::getDetailDim, costAccountTask.getDetailDim())
//                        .last(" limit 1"));
//                if (task != null) {
//                    throw new BizException("维度不能重复");
//                }
//            }
//        }
        if (costAccountTask.getSupportStatistics().equals("0")) {
            costAccountTask.setDetailDim(null);
            costAccountTask.setDimension(null);
        }
        // 保存任务
        //核算周期处理
        if (costAccountTask.getSupportStatistics().equals("1")) {
            try {
                parseCostTime(costAccountTask);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }
        costAccountTask.setStatus(AccountTaskStatus.CALCULATING.getCode());
        this.save(costAccountTask);


        // 保存快照
        costTaskSnapshotService.saveBatch(generateTaskSnapshot(costAccountTask));
        // 发布事件
        publisher.publishEvent(new TaskCalculateEvent(costAccountTask));
        return true;
    }

    /**
     * 任务详情
     *
     * @param id
     * @return
     */
    @Override
    public TaskDetailVo getDetail(Long id) {

        TaskDetailVo vo = new TaskDetailVo();
        CostAccountTask task = this.getById(id);
        BeanUtil.copyProperties(task, vo);
        //从快照获取核算单元数据
        List<CostTaskSnapshot> unitList = costTaskSnapshotService.list(Wrappers.<CostTaskSnapshot>lambdaQuery()
                .eq(CostTaskSnapshot::getTaskId, id)
                .eq(CostTaskSnapshot::getSnapshotType, SnapShotEnum.UNIT.getCode()));
        //转为核算单元对象
        List<CostAccountUnit> units = unitList.stream().
                map(snapshot -> JSON.parseObject(snapshot.getContext(), CostAccountUnit.class)).
                collect(Collectors.toList());
        vo.setAccountUnitList(units);
        //从快照获取核算方案数据
        CostTaskSnapshot plan = costTaskSnapshotService.getOne(Wrappers.<CostTaskSnapshot>lambdaQuery().
                eq(CostTaskSnapshot::getTaskId, id)
                .eq(CostTaskSnapshot::getSnapshotType, SnapShotEnum.PLAN.getCode()));

        if (plan == null) {
            return vo;
        }
        //转为核算方案对象
        CostAccountPlanSnapshotVo costAccountPlanSnapshotVo = JSON.parseObject(plan.getContext(), CostAccountPlanSnapshotVo.class);
        //vo.setCostAccountPlanSnapshotVo(costAccountPlanSnapshotVo);
        return vo;
    }

    @Override
    public IPage<CostAccountTaskVo> listAccountTask(CostAccountTaskQueryDto queryDto) {

//        if (queryDto.getAccountTime() != null) {
//            String[] split = queryDto.getAccountTime().split("~");
//            DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
//            LocalDateTime startTime = LocalDateTime.parse(split[0], df);
//            LocalDateTime endTime = LocalDateTime.parse(split[1], df);
//            queryDto.setAccountStartTime(startTime);
//            queryDto.setAccountEndTime(endTime);
//        }
//        if (queryDto.getCreateTime() != null) {
//            String[] split = queryDto.getCreateTime().split("~");
//            DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
//            LocalDateTime startTime = LocalDateTime.parse(split[0], df);
//            LocalDateTime endTime = LocalDateTime.parse(split[1], df);
//            queryDto.setCreateStartTime(startTime);
//            queryDto.setCreateEndTime(endTime);
//        }
//        Page objectPage = new Page<>(queryDto.getCurrent(), queryDto.getSize());
//       // IPage<CostAccountTaskNew> costAccountTaskPage = taskNewMapper.listByQueryDto(objectPage, queryDto);

//        Page<CostAccountTask> costAccountTaskPage = baseMapper.selectPage(new Page<>(queryDto.getCurrent(), queryDto.getSize()),
//                Wrappers.<CostAccountTask>lambdaQuery()
//                        .eq(StrUtil.isNotBlank(queryDto.getAccountTaskName()), CostAccountTask::getAccountTaskName, queryDto.getAccountTaskName())
//                        .ge(queryDto.getAccountStartTime() != null, CostAccountTask::getAccountStartTime, queryDto.getAccountStartTime())
//                        .le(queryDto.getAccountEndTime() != null, CostAccountTask::getAccountEndTime, queryDto.getAccountEndTime())
//        );

//        return new Page(queryDto.getCurrent(), queryDto.getSize()).setTotal(costAccountTaskPage.getTotal()).setRecords(
//                costAccountTaskPage.getRecords().stream().map(r -> {
//                    CostAccountTaskVo costAccountTaskVo = new CostAccountTaskVo();
//                    BeanUtil.copyProperties(r, costAccountTaskVo);
//                    setUserDetail(Long.parseLong(r.getCreateBy()), costAccountTaskVo);
//                    return costAccountTaskVo;
//                }).collect(Collectors.toList()));
        return null;
    }

    /**
     * 设置创建人详细信息
     *
     * @param id
     * @param costAccountTaskVo
     * @return
     */
    private CostAccountTaskVo setUserDetail(Long id, CostAccountTaskVo costAccountTaskVo) {
        //查询信息
        UserVO userVO = remoteUserService.details(id).getData();
        costAccountTaskVo.setName(userVO.getName());
        costAccountTaskVo.setJobNumber(userVO.getJobNumber());
        return costAccountTaskVo;
    }


    /**
     * 保存快照
     *
     * @param costAccountTask
     * @return
     */
    private List<CostTaskSnapshot> generateTaskSnapshot(CostAccountTask costAccountTask) {
        //生成快照
        List<CostTaskSnapshot> list = new ArrayList<CostTaskSnapshot>();
        //获取核算单元的数据
        String[] unitIds = costAccountTask.getUnitIds().split(",");
        for (String unitId : unitIds) {
            CostTaskSnapshot snapshot = new CostTaskSnapshot();
            snapshot.setSnapshotType(SnapShotEnum.UNIT.getCode());
            snapshot.setContext(JSONUtil.toJsonStr(costAccountUnitService.getById(unitId)));
            list.add(snapshot);
        }
        //获取方案详细的数据
        list.add(accountPlanSnapshot(costAccountTask.getId(), costAccountTask.getPlanId()));
        //设置任务id
        list.stream().forEach(t -> {
            t.setTaskId(costAccountTask.getId());
        });
        return list;
    }

    /**
     * 解析核算方案
     */
    private CostTaskSnapshot accountPlanSnapshot(Long taskId, Long planId) {
        //调用方案方法，获取方案详情
        CostAccountPlanReviewVo costAccountPlanReviewVo = costAccountPlanConfigService.parsePlanConfig(planId);
        String context = null;
        if (costAccountPlanReviewVo != null) {
            Gson gson = new Gson();
            context = gson.toJson(costAccountPlanReviewVo);
        }
        //封装实体插入数据库
        CostTaskSnapshot costTaskSnapshot = new CostTaskSnapshot();
        costTaskSnapshot.setTaskId(taskId);
        costTaskSnapshot.setSnapshotType(SnapShotEnum.PLAN.getCode());
        costTaskSnapshot.setContext(context);
        return costTaskSnapshot;
    }


    /**
     * 计算过程总核算值
     *
     * @param dto
     * @return
     */
    @Override
    public CostAccountTaskResultTotalValueVo getTotalProcess(CostAccountTaskCalculateTotalProcessDto dto) {
        //根据任务id和核算单元id获取总核算值
        CostTaskExecuteResult costTaskExecuteResult = costTaskExecuteResultMapper.getExecuteResult(dto.getTaskId(), dto.getAccountUnitId());
        String calculateDetail = (String) costTaskExecuteResult.getCalculateDetail();
        Gson gson = new Gson();
        CostAccountTaskResultTotalValueVo resultTotalValueVo = gson.fromJson(calculateDetail, CostAccountTaskResultTotalValueVo.class);
        return resultTotalValueVo;
    }


    //数据小组数据展示单元总核算值
    @Override
    public CostAccountTaskResultTotalValueVo getNewTotalProcess(CostAccountTaskCalculateTotalProcessDto dto) {
        //取出核算任务
        CostAccountTask costAccountTask = new CostAccountTask().selectById(dto.getTaskId());
        //封装返回对象
        CostAccountTaskResultTotalValueVo taskResultTotalValueVo = new CostAccountTaskResultTotalValueVo();

        //获取核算单元id
        Long unitId = dto.getAccountUnitId();
//        List<String> relateIds = new CostUnitRelateInfo().selectList(new LambdaQueryWrapper<CostUnitRelateInfo>()
//                .eq(CostUnitRelateInfo::getAccountUnitId, unitId)
//                .select(CostUnitRelateInfo::getRelateId)).stream().map(CostUnitRelateInfo::getRelateId).collect(Collectors.toList());
//        //查询核算单元的code
//        Map<Long, String> deptCodesByDeptIds = sqlUtil.getDeptCodesByDeptIds(relateIds.stream().map(Long::parseLong).collect(Collectors.toList()));
//        //如果不是科室，查询人员
//        if (CollUtil.isEmpty(deptCodesByDeptIds)) {
//            Map<Long, String> deptCodesByUserIds = sqlUtil.getDeptCodesByUserIds(relateIds.stream().map(Long::parseLong).collect(Collectors.toList()));
//            deptCodesByDeptIds.putAll(deptCodesByUserIds);
//        }
//        List<String> deptCodes = new ArrayList<>(deptCodesByDeptIds.values());
        String detailDim = costAccountTask.getDetailDim().replaceAll("[年月]", "");
//        String countField = IndexCorrespondField.AMOUNT_DEDUCTION.getField() + "+" + IndexCorrespondField.AMOUNT_UTILITY.getField();

        List<Long> unitIdList = new ArrayList<>();
        unitIdList.add(unitId);
        BigDecimal totalCost = sqlUtil.geTotalCount(unitIdList, detailDim);
        //填充总值
        taskResultTotalValueVo.setTotalValue(totalCost);
        //根据科室单元id取出核算对象类型
        CostAccountUnit costAccountUnit = cacheUtils.getCostAccountUnit(unitId);
        CostAccountPlanConfigFormula costAccountPlanConfigFormula = new CostAccountPlanConfigFormula();
        LambdaQueryWrapper<CostAccountPlanConfigFormula> lambdaQueryWrapper = new LambdaQueryWrapper<CostAccountPlanConfigFormula>()
                .eq(CostAccountPlanConfigFormula::getPlanId, costAccountTask.getPlanId())
                .eq(CostAccountPlanConfigFormula::getCustomUnitId, unitId);
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
            formulaConfigs = objectMapper.readValue(costAccountPlanConfigFormula.getConfig(), new TypeReference<List<PlanConfigFormulaConfig>>() {
            });
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
            BigDecimal indexCount = sqlUtil.getIndexCount(detailDim, unitIdList, formulaConfig.getName());
            // TODO 取出指标的总值 目前置为 0
            indexVo.setIndexTotalValue(indexCount);
            configIndexList.add(indexVo);

        }
        //填充总公式
        taskResultTotalValueVo.setOverAllFormula(costAccountPlanConfigFormula.getPlanCostFormula());
        taskResultTotalValueVo.setConfigIndexList(configIndexList);

        return taskResultTotalValueVo;
    }

    @Override
    public CostAccountTaskResultIndexProcessVo getNewCalculationProcess(CostAccountTaskCalculateProcessDto dto) {
        CostAccountTaskResultIndexProcessVo resultIndexProcessVo = new CostAccountTaskResultIndexProcessVo();
        CostAccountIndex costAccountIndex = new CostAccountIndex().selectById(dto.getBizId());
        if (costAccountIndex != null) {
            String indexFormula = costAccountIndex.getIndexFormula();
            //先封装一部分数据
            resultIndexProcessVo.setIndexId(costAccountIndex.getId());
            resultIndexProcessVo.setConfigIndexName(costAccountIndex.getName());
            resultIndexProcessVo.setIndexFormula(indexFormula);
            List<CostAccountTaskResultIndexProcessVo> resultIndexProcessList = new ArrayList<>();
            //看该核算指标下是否包含子集，包含子集的话就进行递归查询
            List<CostIndexConfigIndex> costIndexConfigIndexList = new CostIndexConfigIndex().selectList(new LambdaQueryWrapper<CostIndexConfigIndex>().eq(CostIndexConfigIndex::getIndexId, dto.getBizId()));
            if (CollUtil.isNotEmpty(costIndexConfigIndexList)) {
                for (CostIndexConfigIndex costIndexConfigIndex : costIndexConfigIndexList) {
                    if (costIndexConfigIndex.getConfigIndexId() != null) {
                        //封装子集核算指标进行递归查询
                        CostAccountTaskCalculateProcessDto recursionDto = new CostAccountTaskCalculateProcessDto();
                        recursionDto.setParentId(costIndexConfigIndex.getIndexId());
                        recursionDto.setTaskId(dto.getTaskId());
                        recursionDto.setAccountUnitId(dto.getAccountUnitId());
                        recursionDto.setBizId(costIndexConfigIndex.getConfigIndexId());
                        CostAccountTaskResultIndexProcessVo costAccountTaskResultIndexProcessVo = getNewCalculationProcess(recursionDto);
                        resultIndexProcessList.add(costAccountTaskResultIndexProcessVo);
                    }
                }
            }
            List<CostAccountTaskItemVo> configItemList = new ArrayList<>();
            //获取该指标下的所有核算项
            List<CostIndexConfigItem> costIndexConfigItemList = new CostIndexConfigItem().selectList(new LambdaQueryWrapper<CostIndexConfigItem>()
                    .eq(CostIndexConfigItem::getIndexId, dto.getBizId()));
//            List<Long> itemIds = costIndexConfigItemList.stream().map(CostIndexConfigItem::getConfigId).collect(Collectors.toList());
//            List<String> relateIds = new CostUnitRelateInfo().selectList(new LambdaQueryWrapper<CostUnitRelateInfo>()
//                    .eq(CostUnitRelateInfo::getAccountUnitId, dto.getAccountUnitId())
//                    .select(CostUnitRelateInfo::getRelateId)).stream().map(CostUnitRelateInfo::getRelateId).collect(Collectors.toList());
//            //查询核算单元的code
//            Map<Long, String> deptCodesByDeptIds = sqlUtil.getDeptCodesByDeptIds(relateIds.stream().map(Long::parseLong).collect(Collectors.toList()));
//            //如果不是科室，查询人员
//            if (CollUtil.isEmpty(deptCodesByDeptIds)) {
//                Map<Long, String> deptCodesByUserIds = sqlUtil.getDeptCodesByUserIds(relateIds.stream().map(Long::parseLong).collect(Collectors.toList()));
//                deptCodesByDeptIds.putAll(deptCodesByUserIds);
//            }
//            List<String> deptCodes = new ArrayList<>(deptCodesByDeptIds.values());
            String detailDim = new CostAccountTask().selectById(dto.getTaskId()).getDetailDim().replaceAll("[年月]", "");
            List<Long> unitIds = new ArrayList<>();
            unitIds.add(dto.getAccountUnitId());
            for (CostIndexConfigItem costIndexConfigItem : costIndexConfigItemList) {
//                String columnName = sqlUtil.getColumnNameByItemId(costIndexConfigItem.getConfigId());
//                BigDecimal itemCount = BigDecimal.ZERO;
//                if (StrUtil.isNotEmpty(columnName)) {
//                    itemCount = sqlUtil.getIndexCount(detailDim, deptCodes, columnName);
//                }
                BigDecimal itemCount = sqlUtil.getItemCount(unitIds, costAccountIndex.getName(), detailDim, costIndexConfigItem.getConfigId());
                CostAccountTaskItemVo costAccountTaskItemVo = new CostAccountTaskItemVo();
                costAccountTaskItemVo.setItemId(costIndexConfigItem.getConfigId());
                costAccountTaskItemVo.setConfigId(costIndexConfigItem.getConfigId());
                costAccountTaskItemVo.setConfigKey(costIndexConfigItem.getConfigKey());
                costAccountTaskItemVo.setConfigName(costIndexConfigItem.getConfigName());
                costAccountTaskItemVo.setConfigDesc(costIndexConfigItem.getConfigDesc());
                costAccountTaskItemVo.setItemTotalValue(itemCount);
                configItemList.add(costAccountTaskItemVo);
            }

            //获取指标总值
            BigDecimal indexCount = sqlUtil.getIndexCount(detailDim, unitIds, costAccountIndex.getName());
            //最后封装返回的vo
            resultIndexProcessVo.setConfigItemList(configItemList);
            String bedBorrowName = "借床分摊";
            String EndemicAreaName = "病区分摊";
            String setOutpatientName = "门诊共用分摊";
            String docNurseName = "医护分摊";

//            sqlUtil.getSharedCost(detailDim,costAccountIndex.getName(),bedBorrowName);
            List<CostAccountTaskMedicalAllocationVo> medicalAllocation = new ArrayList<>();
            CostAccountTaskMedicalAllocationVo allocationVo = new CostAccountTaskMedicalAllocationVo();
            allocationVo.setMedicalAllocationValue(sqlUtil.getSharedCost(dto.getAccountUnitId(), detailDim, costAccountIndex.getName(), docNurseName));
            medicalAllocation.add(allocationVo);
            resultIndexProcessVo.setMedicalAllocation(medicalAllocation);
            resultIndexProcessVo.setBedBorrow(sqlUtil.getSharedCost(dto.getAccountUnitId(), detailDim, costAccountIndex.getName(), bedBorrowName));
            resultIndexProcessVo.setEndemicArea(sqlUtil.getSharedCost(dto.getAccountUnitId(), detailDim, costAccountIndex.getName(), EndemicAreaName));
            resultIndexProcessVo.setOutpatientShard(sqlUtil.getSharedCost(dto.getAccountUnitId(), detailDim, costAccountIndex.getName(), setOutpatientName));
            resultIndexProcessVo.setConfigIndexList(resultIndexProcessList);

            // TODO 没有分摊的暂时和分摊的保持一致
            resultIndexProcessVo.setNoExtraIndexCount(indexCount
                    .subtract(sqlUtil.getSharedCost(dto.getAccountUnitId(), detailDim, costAccountIndex.getName(), docNurseName))
                    .subtract(sqlUtil.getSharedCost(dto.getAccountUnitId(), detailDim, costAccountIndex.getName(), bedBorrowName))
                    .subtract(sqlUtil.getSharedCost(dto.getAccountUnitId(), detailDim, costAccountIndex.getName(), EndemicAreaName))
                    .subtract(sqlUtil.getSharedCost(dto.getAccountUnitId(), detailDim, costAccountIndex.getName(), setOutpatientName))
            );
            resultIndexProcessVo.setIndexTotalValue(indexCount);

        }
        return resultIndexProcessVo;
    }

    /**
     * 修改核算方案(每一小步保存)
     *
     * @param dto
     * @return
     */
    @Override
    public List<TaskGroupIdsVo> updateTask(CostAccountTaskNewDto dto) {
        CostAccountTaskNew costAccountTaskNew = new CostAccountTaskNew().selectById(dto.getTaskId());
        //修改任务名称时间等
        updateBasicsInfo(dto);
        //修改保存任务分组
        final List<TaskGroupIdsVo> taskGroupIdsVos = updateOrSaveTaskGroup(dto);
        if ("5".equals(dto.getStep())) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy年MM月");
            DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("yyyyMM");
            String accountTime = dto.getAccountStartTime().format(formatter2);
            // 保存快照
            //costTaskSnapshotService.saveBatch(generateTaskSnapshotNew(dto));
            new CostTaskSnapshot().delete(new LambdaQueryWrapper<CostTaskSnapshot>().eq(CostTaskSnapshot::getTaskId,dto.getTaskId()));
            costTaskSnapshotService.saveBatch(generateTaskSnapshotTemporaryNew(dto, taskGroupIdsVos));

            //二次分配
            String formattedDateTime = dto.getAccountStartTime().format(formatter);
            SecondDistributionGenerateTaskDto generateTaskDto = new SecondDistributionGenerateTaskDto();
            generateTaskDto.setTaskPeriod(formattedDateTime);

            publisher.publishEvent(new SecondDistributionTaskCalculateEvent(generateTaskDto));

            final List<DistributionTaskGroup> taskGroups = dto.getTaskGroupInfoList().stream().map(taskGroupInfo -> {
                return new DistributionTaskGroup().selectById(taskGroupInfo.getTaskGroupId());
            }).collect(Collectors.toList());
            for (DistributionTaskGroup taskGroup : taskGroups) {
                if ("TT001".equals(new JSONObject(taskGroup.getType()).getStr("value"))) {
                    //成本绩效结果查询数据小组数据放入redis
                    final TaskResultQueryNewDto taskResultQueryNewDto = new TaskResultQueryNewDto();
                    taskResultQueryNewDto.setAccountTaskName(taskGroup.getName());
                    taskResultQueryNewDto.setAccountTime(accountTime);
                    taskResultQueryNewDto.setAccountTaskId(dto.getTaskId());
                    taskResultQueryNewDto.setAccountTaskGroupId(taskGroup.getId());
                    getDistributionList(taskResultQueryNewDto);
                }
            }
            //修改任务状态
            costAccountTaskNew.setStatus(AccountTaskStatus.COMPLETED.getCode());
            costAccountTaskNew.updateById();
        }
        return taskGroupIdsVos;
    }

    private void getDistributionList(TaskResultQueryNewDto dto) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMM");
        CostAccountTaskResultDetailNewVo vo = new CostAccountTaskResultDetailNewVo();
        //获取核算方案配置的指标
        CostAccountTaskNew costAccountTaskNew = new CostAccountTaskNew().selectById(dto.getAccountTaskId());
        final LocalDateTime accountStartTime = costAccountTaskNew.getAccountStartTime();
        String detailDim = accountStartTime.format(formatter);
        String redisKey = CacheConstants.COST_TASK_RESULT + dto.getAccountTaskId() + ":" + dto.getAccountTaskGroupId();
        //总的计算结果
        WholeAccountInfo wholeAccountInfo = new WholeAccountInfo();
        //核算单元核算信息
        List<UnitAccountInfo> unitAccountInfoList = new ArrayList<>();
        //根据任务id,任务分组id获取到核算单元集
        CostAccountTaskConfig taskConfig = new CostAccountTaskConfig().selectOne(new LambdaQueryWrapper<CostAccountTaskConfig>()
                .eq(CostAccountTaskConfig::getTaskId, dto.getAccountTaskId())
                .eq(CostAccountTaskConfig::getTaskGroupId, dto.getAccountTaskGroupId()));
        //根据任务id获取总核算值
        List<String> idList = new ArrayList<>();
        if (taskConfig != null) {
            idList = ExpressionCheckHelper.getIds(taskConfig.getAccountObjectIds());
        }
        List<Long> unitIds = idList.stream().map(Long::parseLong).collect(Collectors.toList());
        List<AccountIndexCalculateInfo> accountIndexCalculateInfoList = new ArrayList<>();
        List<CostAccountPlanConfig> costAccountPlanConfigs = new CostAccountPlanConfig().selectList(new LambdaQueryWrapper<CostAccountPlanConfig>()
                .eq(CostAccountPlanConfig::getPlanId, taskConfig.getPlanId())
                .select(CostAccountPlanConfig::getIndexId, CostAccountPlanConfig::getConfigIndexName)
                .groupBy(CostAccountPlanConfig::getIndexId, CostAccountPlanConfig::getConfigIndexName));

//            BigDecimal totalCost = BigDecimal.ZERO;
        AtomicReference<BigDecimal> totalCost = new AtomicReference<>(BigDecimal.ZERO);
        //查询关联的id
        List<AccountIndexCalculateInfo> accountIndexCalculateInfo = newTotalRecursion(totalCost, unitIds, costAccountPlanConfigs, detailDim);
        //指标总值 水电费+抵扣
        //获取任务总值
//            BigDecimal totalCost = sqlUtil.geTotalCount(unitIds, detailDim);
        accountIndexCalculateInfoList.addAll(accountIndexCalculateInfo);
        //填充任务名称和核算周期
        vo.setTaskName(costAccountTaskNew.getAccountTaskName());
        vo.setAccountStartTime(costAccountTaskNew.getAccountStartTime());
        vo.setAccountEndTime(costAccountTaskNew.getAccountEndTime());
        //封装总的计算结果
        wholeAccountInfo.setTotalCost(totalCost.get());
        wholeAccountInfo.setAccountIndexCalculateInfoList(accountIndexCalculateInfoList);

        for (Long unitId : unitIds) {
            //封装核算单元核算信息
            UnitAccountInfo unitAccountInfo = new UnitAccountInfo();
            unitAccountInfo.setUnitId(unitId);
            CostAccountUnit costAccountUnit = cacheUtils.getCostAccountUnit(unitId);
            unitAccountInfo.setUnitName(costAccountUnit.getName());
            unitAccountInfo.setGroupId(costAccountUnit.getAccountGroupCode());
            List<String> deptCodeList = getDeptCodes(unitId);
            List<Long> unitIdList = new ArrayList<Long>();
            unitIdList.add(unitId);


            AtomicReference<BigDecimal> unitCount = new AtomicReference<>(BigDecimal.ZERO);
            //根据任务id和核算单元id获取核算指标集合
            List<CalculateInfo> calculateInfo = newDetailRecursion(unitCount, unitId, deptCodeList, costAccountPlanConfigs, detailDim);
            unitAccountInfo.setCalculateInfo(calculateInfo);
//                BigDecimal unitCount = sqlUtil.geTotalCount(unitIdList, detailDim);
            unitAccountInfo.setTotalCost(unitCount.get());

            unitAccountInfoList.add(unitAccountInfo);
        }

        vo.setWholeAccountInfo(wholeAccountInfo);
        vo.setUnitAccountInfoList(unitAccountInfoList);

        redisTemplate.opsForValue().set(redisKey, JSON.toJSONString(vo));

    }


    //获取结果列表--数据小组数据
    private List<CalculateInfo> newDetailRecursion(AtomicReference<BigDecimal> unitCount, Long unitId, List<String> deptCodes, List<CostAccountPlanConfig> costAccountPlanConfigs, String detailDim) {

        List<CalculateInfo> calculateInfoList = new ArrayList<>();
        CostAccountUnit costAccountUnit = cacheUtils.getCostAccountUnit(unitId);
        Gson gson = new Gson();
        UnitInfo unitInfo = gson.fromJson(costAccountUnit.getAccountGroupCode(), UnitInfo.class);
        for (CostAccountPlanConfig costAccountPlanConfig : costAccountPlanConfigs) {
            CalculateInfo indexCalculateInfo = new CalculateInfo();
            List<CalculateInfo> child = new ArrayList<>();
            // 封装核算指标计算信息
            Long indexId = costAccountPlanConfig.getIndexId();// 核算指标id
            String indexName = new CostAccountIndex().selectById(indexId).getName();//核算指标名称
            String accountType = "";
            if (unitInfo != null && unitInfo.getValue().equals(UnitMapEnum.DOCKER.getUnitGroup())) {
                accountType = "医生组";
            } else if (unitInfo != null && unitInfo.getValue().equals(UnitMapEnum.NURSE.getUnitGroup())) {
                accountType = "护理组";
            } else if (unitInfo != null && unitInfo.getValue().equals(UnitMapEnum.MEDICAL_SKILL.getUnitGroup())) {
                accountType = "医技组";
            }
            List<Long> unitIds = new ArrayList<Long>();
            unitIds.add(unitId);
            BigDecimal indexCount = sqlUtil.getIndexCount(detailDim, unitIds, indexName);
            // 取出核算指标的信息
            indexCalculateInfo.setTotalCost(indexCount);
            indexCalculateInfo.setName(indexName);
            indexCalculateInfo.setType("index");
            indexCalculateInfo.setId(indexId);
            indexCalculateInfo.setConfigKey(costAccountPlanConfig.getConfigKey());


            BigDecimal currentCost = unitCount.get();
            BigDecimal newCost = currentCost.add(indexCount);
            unitCount.set(newCost);

            //封装指标分摊数据
            String bedBorrowName = "借床分摊";
            String EndemicAreaName = "病区分摊";
            String setOutpatientName = "门诊共用分摊";
            String docNurseName = "医护分摊";
            SharedCost indexSharedCost = new SharedCost();
            indexSharedCost.setDivideCount(sqlUtil.getSharedCost(unitId, detailDim, indexName, docNurseName));
            indexSharedCost.setBedBorrowCount(sqlUtil.getSharedCost(unitId, detailDim, indexName, bedBorrowName));
            indexSharedCost.setEndemicAreaCount(sqlUtil.getSharedCost(unitId, detailDim, indexName, EndemicAreaName));
            indexSharedCost.setOutpatientShardCount(sqlUtil.getSharedCost(unitId, detailDim, indexName, setOutpatientName));
            //获取子集指标
            List<CostIndexConfigIndex> costIndexConfigChildrenList = new CostIndexConfigIndex().selectList(new LambdaQueryWrapper<CostIndexConfigIndex>()
                    .eq(CostIndexConfigIndex::getIndexId, indexId));

            if (CollUtil.isNotEmpty(costIndexConfigChildrenList)) {
                List<CalculateInfo> children = newDetailRecursion(unitCount, unitId, deptCodes, costAccountPlanConfigs, detailDim);
                child.addAll(children);
            }
            //获取指标下面的核算项
            List<CostIndexConfigItem> costIndexConfigItemList = new CostIndexConfigItem().selectList(new LambdaQueryWrapper<CostIndexConfigItem>()
                    .eq(CostIndexConfigItem::getIndexId, indexId));
            List<Long> itemIds = costIndexConfigItemList.stream().map(CostIndexConfigItem::getConfigId).collect(Collectors.toList());
            // 获取核算项的计算结果
            List<CalculateInfo> itemCalculateInfoList = new ArrayList<>();
            if (CollUtil.isNotEmpty(itemIds)) {
                for (Long itemId : itemIds) {
                    CalculateInfo itemCalculateInfo = new CalculateInfo();
                    CostAccountItem costAccountItem = cacheUtils.getCostAccountItem(itemId);
                    String itemName = costAccountItem.getAccountItemName();// 核算项名称
                    BigDecimal itemCount = sqlUtil.getItemShareCount(unitId, indexName, accountType, detailDim, itemId);
                    // 核算项的核算信息
                    itemCalculateInfo.setTotalCost(itemCount);
                    itemCalculateInfo.setName(itemName);
                    itemCalculateInfo.setType("item");
                    itemCalculateInfo.setId(itemId);
                    itemCalculateInfoList.add(itemCalculateInfo);
                }

            }
            // 核算指标下的核算项
            child.addAll(itemCalculateInfoList);

            indexCalculateInfo.setChildren(child);
            // 填充核算指标的分摊成本
            indexCalculateInfo.setSharedCost(indexSharedCost);
            calculateInfoList.add(indexCalculateInfo);
        }
        return calculateInfoList;


    }

    //获取结果列表--数据小组数据
    private List<AccountIndexCalculateInfo> newTotalRecursion(AtomicReference<BigDecimal> totalCost, List<Long> unitIds, List<CostAccountPlanConfig> costAccountPlanConfigs, String detailDim) {
        List<AccountIndexCalculateInfo> accountIndexCalculateInfoList = new ArrayList<>();
        for (CostAccountPlanConfig costAccountPlanConfig : costAccountPlanConfigs) {
            Long indexId = costAccountPlanConfig.getIndexId();//核算指标id
            String indexName = new CostAccountIndex().selectById(indexId).getName();//核算指标名称
            BigDecimal indexCount = sqlUtil.getIndexCount(detailDim, unitIds, indexName);
            //累加指标值，组装总值
            BigDecimal currentCost = totalCost.get();
            BigDecimal newCost = currentCost.add(indexCount);
            totalCost.set(newCost);
            //封装核算指标计算信息
            AccountIndexCalculateInfo accountIndexCalculateInfo = new AccountIndexCalculateInfo();
            accountIndexCalculateInfo.setIndexName(indexName);
            accountIndexCalculateInfo.setIndexCalculateValue(indexCount);
            //包含子集指标,封装到返回的集合中
            List<AccountItemCalculateInfo> accountItemCalculateInfoList = new ArrayList<>();
            //查询该核算指标下的子集指标，如果该核算指标下存在子集指标，则查询子集指标的信息
            List<CostIndexConfigIndex> costIndexConfigIndexList = new CostIndexConfigIndex().selectList(new LambdaQueryWrapper<CostIndexConfigIndex>()
                    .eq(CostIndexConfigIndex::getIndexId, indexId));
            if (CollUtil.isNotEmpty(costIndexConfigIndexList)) {
                for (CostIndexConfigIndex costIndexConfigIndex : costIndexConfigIndexList) {
                    AccountItemCalculateInfo accountItemCalculateInfo = new AccountItemCalculateInfo();
                    accountItemCalculateInfo.setBizType("index");
                    accountItemCalculateInfo.setBizName(costIndexConfigIndex.getConfigIndexName());
                    BigDecimal bizCalculateValue = sqlUtil.getIndexCount(detailDim, unitIds, costIndexConfigIndex.getConfigIndexName());
                    accountItemCalculateInfo.setBizCalculateValue(bizCalculateValue);
                    accountItemCalculateInfoList.add(accountItemCalculateInfo);
                }
            }
            List<CostIndexConfigItem> costIndexConfigItemList = new CostIndexConfigItem().selectList(new LambdaQueryWrapper<CostIndexConfigItem>()
                    .eq(CostIndexConfigItem::getIndexId, indexId));
            List<Long> itemIds = costIndexConfigItemList.stream().map(CostIndexConfigItem::getConfigId).collect(Collectors.toList());

            for (Long itemId : itemIds) {
                AccountItemCalculateInfo accountItemCalculateInfo = new AccountItemCalculateInfo();
                BigDecimal itemCount = sqlUtil.getItemCount(unitIds, indexName, detailDim, itemId);
                String accountItemName = cacheUtils.getCostAccountItem(itemId).getAccountItemName();
                accountItemCalculateInfo.setBizName(accountItemName);
                accountItemCalculateInfo.setBizType("item");
                accountItemCalculateInfo.setBizCalculateValue(itemCount);
                accountItemCalculateInfoList.add(accountItemCalculateInfo);
            }
            accountIndexCalculateInfo.setAccountItemCalculateInfoList(accountItemCalculateInfoList);
            accountIndexCalculateInfoList.add(accountIndexCalculateInfo);
        }

        return accountIndexCalculateInfoList;
    }

    //获取结果列表--数据小组数据
    @NotNull
    private List<String> getDeptCodes(Long unitId) {
        List<String> relateIds = new CostUnitRelateInfo().selectList(new LambdaQueryWrapper<CostUnitRelateInfo>()
                .eq(CostUnitRelateInfo::getAccountUnitId, unitId)
                .select(CostUnitRelateInfo::getRelateId)).stream().map(CostUnitRelateInfo::getRelateId).collect(Collectors.toList());
        //查询科室的code
        Map<Long, String> deptCodesByDeptIds = sqlUtil.getDeptCodesByDeptIds(relateIds.stream().map(Long::parseLong).collect(Collectors.toList()));
        //如果不是科室，查询人员
        if (CollUtil.isEmpty(deptCodesByDeptIds)) {
            Map<Long, String> deptCodesByUserIds = sqlUtil.getDeptCodesByUserIds(relateIds.stream().map(Long::parseLong).collect(Collectors.toList()));
            deptCodesByDeptIds.putAll(deptCodesByUserIds);
        }
        List<String> deptCodes = new ArrayList<>(deptCodesByDeptIds.values());
        return deptCodes;
    }

    /**
     * 临时存储快照
     *
     * @param dto
     * @return
     */
    private Collection<CostTaskSnapshot> generateTaskSnapshotTemporaryNew(CostAccountTaskNewDto dto, List<TaskGroupIdsVo> taskGroupIdsVos) {
        //存储核算人员信息
        //生成快照
        List<CostTaskSnapshot> list = new ArrayList<CostTaskSnapshot>();
        //获取核算单元的数据
        final List<CostAccountTaskNewDto.TaskGroupInfo> taskGroupInfoList = dto.getTaskGroupInfoList();
        for (CostAccountTaskNewDto.TaskGroupInfo taskGroupInfo : taskGroupInfoList) {
            for (TaskGroupIdsVo taskGroupIdsVo : taskGroupIdsVos) {
                if (taskGroupInfo.getTaskGroupId() == taskGroupIdsVo.getTaskGroupId()) {
                    //判断核算对象类型
                    DistributionTaskGroup taskGroup = new DistributionTaskGroup().selectById(taskGroupInfo.getTaskGroupId());
                    //人员类型
                    if ("HSDX002".equals(new JSONObject(taskGroup.getAccountObject()).getStr("value"))) {
                        final List<CostAccountTaskNewDto.objectInfo> accountObjectIds = taskGroupInfo.getAccountObjectIds();
                        for (CostAccountTaskNewDto.objectInfo accountObjectId : accountObjectIds) {
                            DistributionUserInfo userInfo = new DistributionUserInfo().selectById(accountObjectId.getId());
                            CostTaskSnapshot snapshot = new CostTaskSnapshot();
                            snapshot.setSnapshotType("user");
                            snapshot.setContext(JSONUtil.toJsonStr(userInfo));
                            snapshot.setTaskId(dto.getTaskId());
                            snapshot.setTaskGroupId(taskGroupInfo.getTaskGroupId());
                            list.add(snapshot);
                        }
                    }
                    //科室单元
                    else if ("HSDX001".equals(new JSONObject(taskGroup.getAccountObject()).getStr("value"))) {
                        final List<CostAccountTaskNewDto.objectInfo> accountObjectIds = taskGroupInfo.getAccountObjectIds();
                        for (CostAccountTaskNewDto.objectInfo accountObjectId : accountObjectIds) {
                            CostAccountUnit unit = new CostAccountUnit().selectById(accountObjectId.getId());
                            CostTaskSnapshot snapshot = new CostTaskSnapshot();
                            snapshot.setSnapshotType("unit");
                            snapshot.setContext(JSONUtil.toJsonStr(unit));
                            snapshot.setTaskId(dto.getTaskId());
                            snapshot.setTaskGroupId(taskGroupInfo.getTaskGroupId());
                            list.add(snapshot);
                        }
                    }
                    //方案id
                    CostTaskSnapshot plan = new CostTaskSnapshot();
                    plan.setSnapshotType("plan");
                    plan.setContext(taskGroupInfo.getPlanId() + "");
                    plan.setTaskId(dto.getTaskId());
                    plan.setTaskGroupId(taskGroupInfo.getTaskGroupId());
                    list.add(plan);
                    //配置指标
                    final CostAccountTaskConfigIndex costAccountTaskConfigIndex = new CostAccountTaskConfigIndex().selectOne(new LambdaQueryWrapper<CostAccountTaskConfigIndex>()
                            .eq(CostAccountTaskConfigIndex::getTaskConfigId, taskGroupIdsVo.getTaskGroupInfoId()));

                    CostTaskSnapshot index = new CostTaskSnapshot();
                    index.setSnapshotType("index");
                    index.setContext(JSONUtil.toJsonStr(costAccountTaskConfigIndex));
                    index.setTaskId(dto.getTaskId());
                    index.setTaskGroupId(taskGroupIdsVo.getTaskGroupId());
                    list.add(index);
                }
            }
        }
        return list;
    }

    /**
     * 保存任务快照(新)
     *
     * @param dto
     * @return
     */
    private Collection<CostTaskSnapshot> generateTaskSnapshotNew(CostAccountTaskNewDto dto) {
        //生成快照
        List<CostTaskSnapshot> list = new ArrayList<CostTaskSnapshot>();
        //获取核算单元的数据
        final List<CostAccountTaskNewDto.TaskGroupInfo> taskGroupInfoList = dto.getTaskGroupInfoList();
        for (CostAccountTaskNewDto.TaskGroupInfo taskGroupInfo : taskGroupInfoList) {
            CostTaskSnapshot snapshot = new CostTaskSnapshot();
            snapshot.setSnapshotType(SnapShotEnum.UNIT.getCode());
            snapshot.setContext(JSONUtil.toJsonStr(taskGroupInfo));
            snapshot.setTaskId(dto.getTaskId());
            snapshot.setTaskGroupId(taskGroupInfo.getTaskGroupId());
            list.add(snapshot);
            //获取方案详细的数据
            list.add(accountPlanSnapshotNew(dto.getTaskId(), taskGroupInfo));
        }
        return list;
    }

    private CostTaskSnapshot accountPlanSnapshotNew(Long taskId, CostAccountTaskNewDto.TaskGroupInfo taskGroupInfo) {
        //调用方案方法，获取方案详情
        CostAccountPlanReviewVo costAccountPlanReviewVo = costAccountPlanConfigService.parsePlanConfig(taskGroupInfo.getPlanId());
        costAccountPlanReviewVo.setPlanId(taskGroupInfo.getPlanId());
        String context = null;
        if (costAccountPlanReviewVo != null) {
            Gson gson = new Gson();
            context = gson.toJson(costAccountPlanReviewVo);
        }
        //封装实体插入数据库
        CostTaskSnapshot costTaskSnapshot = new CostTaskSnapshot();
        costTaskSnapshot.setTaskId(taskId);
        costTaskSnapshot.setSnapshotType(SnapShotEnum.PLAN.getCode());
        costTaskSnapshot.setContext(context);
        costTaskSnapshot.setTaskGroupId(taskGroupInfo.getTaskGroupId());
        return costTaskSnapshot;
    }

    /**
     * 修改保存任务分组
     *
     * @param dto
     */
    private List<TaskGroupIdsVo> updateOrSaveTaskGroup(CostAccountTaskNewDto dto) {
        final List<CostAccountTaskNewDto.TaskGroupInfo> taskGroupInfoList = dto.getTaskGroupInfoList();
        List<TaskGroupIdsVo> list = new ArrayList<>();

        //判断是否为空
        if (CollUtil.isNotEmpty(taskGroupInfoList)) {
            //先查询表中数据,删除
            new CostAccountTaskConfig().delete(new LambdaQueryWrapper<CostAccountTaskConfig>()
                    .eq(CostAccountTaskConfig::getTaskId, dto.getTaskId()));
            //遍历插入配置表(cost_account_task_config)
            for (CostAccountTaskNewDto.TaskGroupInfo taskGroupInfo : taskGroupInfoList) {
                TaskGroupIdsVo vo = new TaskGroupIdsVo();
                CostAccountTaskConfig costAccountTaskConfig = new CostAccountTaskConfig();
                costAccountTaskConfig.setTaskId(dto.getTaskId());
                costAccountTaskConfig.setTaskGroupId(taskGroupInfo.getTaskGroupId());
                costAccountTaskConfig.setPlanId(taskGroupInfo.getPlanId());
                if (CollUtil.isNotEmpty(taskGroupInfo.getAccountObjectIds())) {
                    costAccountTaskConfig.setAccountObjectIds(new Gson().toJson(taskGroupInfo.getAccountObjectIds()));
                }
                DistributionTaskGroup distributionTaskGroup = new DistributionTaskGroup().selectById(taskGroupInfo.getTaskGroupId());
                if (distributionTaskGroup != null) {
                    costAccountTaskConfig.setAccountObjectType(distributionTaskGroup.getAccountObject());
                }
                costAccountTaskConfig.insert();
                //插入指标配置表(cost_account_task_config_index)
                updateOrSaveTaskIndex(costAccountTaskConfig.getId(), taskGroupInfo.getIndexInfoList());
                vo.setTaskGroupInfoId(costAccountTaskConfig.getId());
                vo.setTaskGroupId(taskGroupInfo.getTaskGroupId());
                list.add(vo);
            }
        }
        return list;
    }

    /**
     * 新增或修改任务配置指标
     *
     * @param taskConfigId
     * @param indexInfoList
     */
    private void updateOrSaveTaskIndex(Long taskConfigId, List<CostAccountTaskNewDto.IndexInfo> indexInfoList) {
        //判断是否为空
        if (CollUtil.isNotEmpty(indexInfoList)) {
            //先查询表中数据,删除
            new CostAccountTaskConfigIndex().delete(new LambdaQueryWrapper<CostAccountTaskConfigIndex>()
                    .eq(CostAccountTaskConfigIndex::getTaskConfigId, taskConfigId));
            final List<CostAccountTaskConfigIndex> indexList = indexInfoList.stream().map(indexInfo -> {
                CostAccountTaskConfigIndex costAccountTaskConfigIndex = new CostAccountTaskConfigIndex();
                costAccountTaskConfigIndex.setTaskConfigId(taskConfigId);
                costAccountTaskConfigIndex.setIndexId(indexInfo.getIndexId());
                //costAccountTaskConfigIndex.setAccountObjectType(indexInfo.getAccountObjectType());
                costAccountTaskConfigIndex.setIsRelevance(indexInfo.getIsRelevance());
                costAccountTaskConfigIndex.setRelevanceTaskId(indexInfo.getRelevanceTaskId());
                return costAccountTaskConfigIndex;
            }).collect(Collectors.toList());
            taskConfigIndexService.saveBatch(indexList);
        }
    }

    /**
     * 修改任务名称等基础信息
     *
     * @param dto
     */
    private void updateBasicsInfo(CostAccountTaskNewDto dto) {
        CostAccountTaskNew costAccountTaskNew = new CostAccountTaskNew().selectById(dto.getTaskId());
        costAccountTaskNew.setAccountTaskName(dto.getAccountTaskName());
        costAccountTaskNew.setAccountStartTime(dto.getAccountStartTime());
        costAccountTaskNew.setAccountEndTime(dto.getAccountEndTime());
        costAccountTaskNew.setStatus(AccountTaskStatus.PENDING.getCode());
        costAccountTaskNew.setStep(dto.getStep());
        taskNewService.updateById(costAccountTaskNew);
    }

    /**
     * 保存新任务
     *
     * @param dto
     * @return
     */
    @Override
    public Long saveTaskNew(CostAccountTaskNewDto dto) {
        final PigxUser user = SecurityUtils.getUser();
        CostAccountTaskNew costAccountTaskNew = new CostAccountTaskNew();
        costAccountTaskNew.setAccountTaskName(dto.getAccountTaskName());
        costAccountTaskNew.setAccountStartTime(dto.getAccountStartTime());
        costAccountTaskNew.setAccountEndTime(dto.getAccountEndTime());
        costAccountTaskNew.setCreateBy(user.getName());
        costAccountTaskNew.setCreateTime(LocalDateTime.now());
        costAccountTaskNew.setStatus(AccountTaskStatus.PENDING.getCode());
        costAccountTaskNew.setStep(dto.getStep());
        costAccountTaskNew.insert();
        return costAccountTaskNew.getId();
    }

    /**
     * 根据任务id获取任务分组
     *
     * @param id
     * @return
     */
    @Override
    public List<DistributionTaskGroup> listTaskGroup(Long id) {
        List<CostAccountTaskConfig> configList = new CostAccountTaskConfig().selectList(new LambdaQueryWrapper<CostAccountTaskConfig>()
                .eq(CostAccountTaskConfig::getTaskId, id));
        final List<DistributionTaskGroup> taskGroupList = configList.stream().map(config -> {
            DistributionTaskGroup taskGroup = new DistributionTaskGroup().selectById(config.getTaskGroupId());
            return taskGroup;
        }).collect(Collectors.toList());
        return taskGroupList;
    }

    /**
     * 任务详情(新)
     *
     * @param
     * @return
     */
    @Override
    public TaskDetailVo getDetailNew(Long taskId, Long taskGroupId) {
        TaskDetailVo vo = new TaskDetailVo();
        CostAccountTaskNew task = new CostAccountTaskNew().selectById(taskId);
        if (task == null) {
            return vo;
        }
        BeanUtil.copyProperties(task, vo);
        //查询任务分组,判断核算类型
        DistributionTaskGroup taskGroup = new DistributionTaskGroup().selectById(taskGroupId);
        if (taskGroup == null) {
            return vo;
        }
        //人员类型
        if ("HSDX002".equals(new JSONObject(taskGroup.getAccountObject()).getStr("value"))) {
            //List<DistributionUserInfo> userList = getUserList(taskId, taskGroupId);
            List<CostTaskSnapshot> userList = new CostTaskSnapshot().selectList(new LambdaQueryWrapper<CostTaskSnapshot>()
                    .eq(CostTaskSnapshot::getTaskId, taskId)
                    .eq(CostTaskSnapshot::getSnapshotType, "user")
                    .eq(CostTaskSnapshot::getTaskGroupId, taskGroupId));
            if (CollUtil.isNotEmpty(userList)) {
                final List<DistributionUserInfo> collect = userList.stream().map(user -> {
                    DistributionUserInfo userInfo = JSON.parseObject(user.getContext(), DistributionUserInfo.class);
                    return userInfo;
                }).collect(Collectors.toList());
                vo.setAccountType("{\"label\":\"人员\",\"value\":\"HSDX002\"}");
                vo.setAccountUserList(collect);
            }
        }
        //科室单元类型
        if ("HSDX001".equals(new JSONObject(taskGroup.getAccountObject()).getStr("value"))) {
            // List<CostAccountUnit> unitList = getUnitList(taskId, taskGroupId);
            List<CostTaskSnapshot> unitList = new CostTaskSnapshot().selectList(new LambdaQueryWrapper<CostTaskSnapshot>()
                    .eq(CostTaskSnapshot::getTaskId, taskId)
                    .eq(CostTaskSnapshot::getSnapshotType, "unit")
                    .eq(CostTaskSnapshot::getTaskGroupId, taskGroupId));

            if (CollUtil.isNotEmpty(unitList)) {
                final List<CostAccountUnit> collect = unitList.stream().map(unit -> {
                    CostAccountUnit accountUnit = JSON.parseObject(unit.getContext(), CostAccountUnit.class);
                    return accountUnit;
                }).collect(Collectors.toList());
                vo.setAccountType("{\"label\":\"科室单元\",\"value\":\"HSDX001\"}");
                vo.setAccountUnitList(collect);
            }
        }
        //从快照获取核算方案数据
        CostTaskSnapshot plan = costTaskSnapshotService.getOne(Wrappers.<CostTaskSnapshot>lambdaQuery().
                eq(CostTaskSnapshot::getTaskId, taskId)
                .eq(CostTaskSnapshot::getSnapshotType, "plan")
                .eq(CostTaskSnapshot::getTaskGroupId, taskGroupId), false);

        if (plan == null) {
            return vo;
        }
        //转为核算方案对象
        //CostAccountPlanReviewVo costAccountPlanReviewVo = JSON.parseObject(plan.getContext(), CostAccountPlanReviewVo.class);
        vo.setPlanId(Long.valueOf(plan.getContext()));
        //获取指标信息
        //从快照获取核算方案数据
        List<CostTaskSnapshot> indexs = new CostTaskSnapshot().selectList(Wrappers.<CostTaskSnapshot>lambdaQuery().
                eq(CostTaskSnapshot::getTaskId, taskId)
                .eq(CostTaskSnapshot::getSnapshotType, "index")
                .eq(CostTaskSnapshot::getTaskGroupId, taskGroupId));
        if (CollUtil.isNotEmpty(indexs)) {
            List<CostAccountTaskConfigIndex> configIndexList = new ArrayList<>();
            for (CostTaskSnapshot index : indexs) {
                CostAccountTaskConfigIndex costAccountTaskConfigIndex = JSON.parseObject(index.getContext(), CostAccountTaskConfigIndex.class);
                configIndexList.add(costAccountTaskConfigIndex);
            }
            vo.setIndexInfo(configIndexList);
        }
        return vo;
    }

    /**
     * 根据方案id查询回显
     *
     * @param id
     * @return
     */
    @Override
    public CostAccountTaskNewVo getTaskById(Long id) {
        CostAccountTaskNewVo vo = new CostAccountTaskNewVo();
        //封装方案基础信息
        CostAccountTaskNew costAccountTaskNew = new CostAccountTaskNew().selectById(id);
        vo.setCostAccountTaskNew(costAccountTaskNew);
        //判断到达那步
        switch (costAccountTaskNew.getStep()) {
            case "1":
                vo.setStep("1");
                break;
            case "2":
                List<CostAccountTaskNewVo.TaskGroupInfo> taskGroupInfoListTwo = getTwo(id);
                vo.setTaskGroupInfoList(taskGroupInfoListTwo);
                vo.setStep("2");
                break;
            case "3":
                List<CostAccountTaskNewVo.TaskGroupInfo> taskGroupInfoListThree = getThree(id);
                vo.setTaskGroupInfoList(taskGroupInfoListThree);
                vo.setStep("3");
                break;
            case "4":
                List<CostAccountTaskNewVo.TaskGroupInfo> taskGroupInfoListFour = getFour(id);
                vo.setTaskGroupInfoList(taskGroupInfoListFour);
                vo.setStep("4");
                break;
            case "5":
                List<CostAccountTaskNewVo.TaskGroupInfo> taskGroupInfoListFive = getFive(id);
                vo.setTaskGroupInfoList(taskGroupInfoListFive);
                vo.setStep("5");
                break;
        }
        return vo;
    }

    /**
     * 根据任务id获取任务下所有核算分组
     *
     * @param id
     * @return
     */
    @Override
    public List<DistributionTaskGroup> getTaskGroup(Long id) {
        List<CostAccountTaskConfig> configList = new CostAccountTaskConfig().selectList(new LambdaQueryWrapper<CostAccountTaskConfig>()
                .eq(CostAccountTaskConfig::getTaskId, id));
        final List<DistributionTaskGroup> taskGroupList = configList.stream().map(config -> {
            DistributionTaskGroup taskGroup = new DistributionTaskGroup().selectById(config.getTaskGroupId());
            return taskGroup;
        }).collect(Collectors.toList());
        return taskGroupList;
    }

    /**
     * 删除任务
     *
     * @param id
     * @return
     */
    @Override
    public Boolean deleteTask(Long id) {
        //查询任务配置
        List<CostAccountTaskConfig> taskConfigList = new CostAccountTaskConfig().selectList(new LambdaQueryWrapper<CostAccountTaskConfig>()
                .eq(CostAccountTaskConfig::getTaskId, id));
        //删除任务配置指标
        for (CostAccountTaskConfig taskConfig : taskConfigList) {
            new CostAccountTaskConfigIndex().delete(new LambdaQueryWrapper<CostAccountTaskConfigIndex>()
                    .eq(CostAccountTaskConfigIndex::getTaskConfigId, taskConfig.getId()));
        }
        taskConfigService.removeByIds(taskConfigList);
        //快照
        new CostTaskSnapshot().delete(new LambdaQueryWrapper<CostTaskSnapshot>()
                .eq(CostTaskSnapshot::getTaskId, id));
        //方案信息
        new CostAccountTaskNew().deleteById(id);
        return true;
    }

    /**
     * 任务第五步信息
     *
     * @param id
     * @return
     */
    private List<CostAccountTaskNewVo.TaskGroupInfo> getFive(Long id) {
        //封装配置核算组
        final List<CostAccountTaskConfig> configList = new CostAccountTaskConfig().selectList(new LambdaQueryWrapper<CostAccountTaskConfig>()
                .eq(CostAccountTaskConfig::getTaskId, id));
        List<CostAccountTaskNewVo.TaskGroupInfo> taskGroupInfoList = new ArrayList<>();
        for (CostAccountTaskConfig costAccountTaskConfig : configList) {
            CostAccountTaskNewVo.TaskGroupInfo taskGroupInfo = new CostAccountTaskNewVo.TaskGroupInfo();
            //查询任务分组
            DistributionTaskGroup taskGroup = new DistributionTaskGroup().selectById(costAccountTaskConfig.getTaskGroupId());
            taskGroupInfo.setTaskGroupInfoId(costAccountTaskConfig.getId());
            taskGroupInfo.setTaskGroup(taskGroup);
            //查询方案
            CostAccountPlan costAccountPlan = new CostAccountPlan().selectById(costAccountTaskConfig.getPlanId());
            taskGroupInfo.setPlan(costAccountPlan);
            //封装人员
            taskGroupInfo.setAccountObjectIds(costAccountTaskConfig.getAccountObjectIds());
            //查询关联指标
            final List<CostAccountTaskConfigIndex> costAccountTaskConfigIndices = new CostAccountTaskConfigIndex().selectList(new LambdaQueryWrapper<CostAccountTaskConfigIndex>()
                    .eq(CostAccountTaskConfigIndex::getTaskConfigId, costAccountTaskConfig.getId()));

            List<CostAccountTaskNewVo.IndexInfo> indexInfoList = new ArrayList<>();
            for (CostAccountTaskConfigIndex costAccountTaskConfigIndex : costAccountTaskConfigIndices) {
                CostAccountTaskNewVo.IndexInfo indexInfo = new CostAccountTaskNewVo.IndexInfo();
                //查询指标
                CostAccountIndex costAccountIndex = new CostAccountIndex().selectById(costAccountTaskConfigIndex.getIndexId());
                indexInfo.setIndex(costAccountIndex);
                indexInfo.setIsRelevance(costAccountTaskConfigIndex.getIsRelevance());
                indexInfo.setRelevanceTaskId(costAccountTaskConfigIndex.getRelevanceTaskId());
                indexInfoList.add(indexInfo);
            }
            taskGroupInfo.setIndexInfoList(indexInfoList);
            taskGroupInfoList.add(taskGroupInfo);
        }
        return taskGroupInfoList;
    }

    /**
     * 任务第四步信息
     *
     * @param id
     * @return
     */
    private List<CostAccountTaskNewVo.TaskGroupInfo> getFour(Long id) {
        //封装配置核算组
        final List<CostAccountTaskConfig> configList = new CostAccountTaskConfig().selectList(new LambdaQueryWrapper<CostAccountTaskConfig>()
                .eq(CostAccountTaskConfig::getTaskId, id));
        List<CostAccountTaskNewVo.TaskGroupInfo> taskGroupInfoList = new ArrayList<>();
        for (CostAccountTaskConfig costAccountTaskConfig : configList) {
            CostAccountTaskNewVo.TaskGroupInfo taskGroupInfo = new CostAccountTaskNewVo.TaskGroupInfo();
            //查询任务分组
            DistributionTaskGroup taskGroup = new DistributionTaskGroup().selectById(costAccountTaskConfig.getTaskGroupId());
            taskGroupInfo.setTaskGroupInfoId(costAccountTaskConfig.getId());
            taskGroupInfo.setTaskGroup(taskGroup);
            //查询方案
            CostAccountPlan costAccountPlan = new CostAccountPlan().selectById(costAccountTaskConfig.getPlanId());
            taskGroupInfo.setPlan(costAccountPlan);
            //查询关联指标
            final List<CostAccountTaskConfigIndex> costAccountTaskConfigIndices = new CostAccountTaskConfigIndex().selectList(new LambdaQueryWrapper<CostAccountTaskConfigIndex>()
                    .eq(CostAccountTaskConfigIndex::getTaskConfigId, costAccountTaskConfig.getId()));

            List<CostAccountTaskNewVo.IndexInfo> indexInfoList = new ArrayList<>();
            for (CostAccountTaskConfigIndex costAccountTaskConfigIndex : costAccountTaskConfigIndices) {
                CostAccountTaskNewVo.IndexInfo indexInfo = new CostAccountTaskNewVo.IndexInfo();
                //查询指标
                CostAccountPlanConfig planConfig = new CostAccountPlanConfig().selectById(costAccountTaskConfigIndex.getIndexId());
                if(planConfig!=null){
                    CostAccountIndex costAccountIndex = new CostAccountIndex().selectById(planConfig.getIndexId());
                    indexInfo.setIndexInfoId(planConfig.getId());
                    indexInfo.setIndex(costAccountIndex);
                    indexInfo.setIsRelevance(costAccountTaskConfigIndex.getIsRelevance());
                    indexInfo.setRelevanceTaskId(costAccountTaskConfigIndex.getRelevanceTaskId());
                    indexInfoList.add(indexInfo);
                }
            }
            taskGroupInfo.setIndexInfoList(indexInfoList);
            taskGroupInfoList.add(taskGroupInfo);
        }
        return taskGroupInfoList;
    }

    /**
     * 任务第三步信息
     *
     * @param id
     * @return
     */
    private List<CostAccountTaskNewVo.TaskGroupInfo> getThree(Long id) {
        //封装配置核算组
        final List<CostAccountTaskConfig> configList = new CostAccountTaskConfig().selectList(new LambdaQueryWrapper<CostAccountTaskConfig>()
                .eq(CostAccountTaskConfig::getTaskId, id));
        List<CostAccountTaskNewVo.TaskGroupInfo> taskGroupInfoList = new ArrayList<>();
        for (CostAccountTaskConfig costAccountTaskConfig : configList) {
            CostAccountTaskNewVo.TaskGroupInfo taskGroupInfo = new CostAccountTaskNewVo.TaskGroupInfo();
            //查询任务分组
            DistributionTaskGroup taskGroup = new DistributionTaskGroup().selectById(costAccountTaskConfig.getTaskGroupId());
            taskGroupInfo.setTaskGroupInfoId(costAccountTaskConfig.getId());
            taskGroupInfo.setTaskGroup(taskGroup);
            //查询方案
            CostAccountPlan costAccountPlan = new CostAccountPlan().selectById(costAccountTaskConfig.getPlanId());
            taskGroupInfo.setPlan(costAccountPlan);
            taskGroupInfoList.add(taskGroupInfo);
        }
        return taskGroupInfoList;
    }

    /**
     * 任务第二步信息
     *
     * @param id
     * @return
     */
    private List<CostAccountTaskNewVo.TaskGroupInfo> getTwo(Long id) {
        //封装配置核算组
        final List<CostAccountTaskConfig> configList = new CostAccountTaskConfig().selectList(new LambdaQueryWrapper<CostAccountTaskConfig>()
                .eq(CostAccountTaskConfig::getTaskId, id));
        List<CostAccountTaskNewVo.TaskGroupInfo> taskGroupInfoList = new ArrayList<>();
        for (CostAccountTaskConfig costAccountTaskConfig : configList) {
            CostAccountTaskNewVo.TaskGroupInfo taskGroupInfo = new CostAccountTaskNewVo.TaskGroupInfo();
            DistributionTaskGroup taskGroup = new DistributionTaskGroup().selectById(costAccountTaskConfig.getTaskGroupId());
            taskGroupInfo.setTaskGroupInfoId(costAccountTaskConfig.getId());
            taskGroupInfo.setTaskGroup(taskGroup);
            taskGroupInfoList.add(taskGroupInfo);
        }
        return taskGroupInfoList;
    }

    /**
     * 获取核算科室单元
     *
     * @param taskId
     * @param taskGroupId
     * @return
     */
    private List<CostAccountUnit> getUnitList(Long taskId, Long taskGroupId) {
        //从快照获取核算单元数据
        final CostTaskSnapshot costTaskSnapshot = costTaskSnapshotService.getOne(Wrappers.<CostTaskSnapshot>lambdaQuery()
                .eq(CostTaskSnapshot::getTaskId, taskId)
                .eq(CostTaskSnapshot::getSnapshotType, "unit")
                .eq(CostTaskSnapshot::getTaskGroupId, taskGroupId));
        //将信息转为对象
        final CostAccountTaskNewDto.TaskGroupInfo taskGroupInfo = JSON.parseObject(costTaskSnapshot.getContext(), CostAccountTaskNewDto.TaskGroupInfo.class);
        //转为核算单元对象
        final List<CostAccountTaskNewDto.objectInfo> accountObjectIds = taskGroupInfo.getAccountObjectIds();
        final List<CostAccountUnit> list = new ArrayList<>();
        for (CostAccountTaskNewDto.objectInfo accountObjectId : accountObjectIds) {
            CostAccountUnit costAccountUnit = new CostAccountUnit().selectById(accountObjectId.getId());
            list.add(costAccountUnit);
        }
        return list;
    }

    /**
     * 获取核算任务人员
     *
     * @param taskId
     * @param taskGroupId
     * @return
     */
    private List<DistributionUserInfo> getUserList(Long taskId, Long taskGroupId) {
        //从快照获取核算单元数据
        final CostTaskSnapshot costTaskSnapshot = costTaskSnapshotService.getOne(Wrappers.<CostTaskSnapshot>lambdaQuery()
                .eq(CostTaskSnapshot::getTaskId, taskId)
                .eq(CostTaskSnapshot::getSnapshotType, "unit")
                .eq(CostTaskSnapshot::getTaskGroupId, taskGroupId));
        //将信息转为对象
        final CostAccountTaskNewDto.TaskGroupInfo taskGroupInfo = JSON.parseObject(costTaskSnapshot.getContext(), CostAccountTaskNewDto.TaskGroupInfo.class);
        //转为核算单元对象
        final List<CostAccountTaskNewDto.objectInfo> accountObjectIds = taskGroupInfo.getAccountObjectIds();
        final List<DistributionUserInfo> list = new ArrayList<>();
        for (CostAccountTaskNewDto.objectInfo accountObjectId : accountObjectIds) {
            DistributionUserInfo distributionUserInfo = new DistributionUserInfo().selectById(accountObjectId.getId());
            list.add(distributionUserInfo);
        }
        return list;
    }


    /**
     * 计算过程详情
     *
     * @param dto
     * @return
     */
    @Override
    public Object getCalculationProcess(CostAccountTaskCalculateProcessDto dto) {
        if ("item".equals(dto.getType())) {
            return itemProcess(dto);
        }
        if ("index".equals(dto.getType())) {
            return indexProcess(dto);
        }
        return null;
    }


    /**
     * 核算指标详情
     *
     * @param dto
     */
    private CostAccountTaskResultIndexProcessVo indexProcess(CostAccountTaskCalculateProcessDto dto) {
        CostAccountTaskResultIndexProcessVo resultIndexProcessVo = new CostAccountTaskResultIndexProcessVo();
        //获取核算指标对象，取出核算指标计算的公式，并解析公式
        CostTaskExecuteResultIndex costTaskExecuteResultIndex = costTaskExecuteResultIndexMapper.getResultIndex(dto.getTaskId(), dto.getAccountUnitId(), dto.getBizId(), dto.getParentId());
        if (costTaskExecuteResultIndex != null) {
            String calculateFormulaDesc = costTaskExecuteResultIndex.getCalculateFormulaDesc();
            Gson gson = new Gson();
            //解析公式
            IndexFormulaObject indexFormulaObject = gson.fromJson(calculateFormulaDesc, IndexFormulaObject.class);
            //先封装一部分数据
            resultIndexProcessVo.setIndexId(costTaskExecuteResultIndex.getIndexId());
            resultIndexProcessVo.setConfigIndexName(costTaskExecuteResultIndex.getIndexName());
            resultIndexProcessVo.setConfigKey(indexFormulaObject.getConfigKey());
            resultIndexProcessVo.setIndexFormula(indexFormulaObject.getIndexFormula());

            List<CostAccountTaskResultIndexProcessVo> resultIndexProcessList = new ArrayList<>();

            //看该核算指标下是否包含子集，包含子集的话就进行递归查询
            List<CostTaskExecuteResultIndex> indexListChildren = costTaskExecuteResultIndexMapper.getIndexListChildren(dto.getTaskId(), dto.getAccountUnitId(), dto.getBizId());
            if (CollUtil.isNotEmpty(indexListChildren)) {
                for (CostTaskExecuteResultIndex indexListChild : indexListChildren) {
                    //封装子集核算指标进行递归查询
                    CostAccountTaskCalculateProcessDto recursionDto = new CostAccountTaskCalculateProcessDto();
                    recursionDto.setParentId(indexListChild.getParentId());
                    recursionDto.setPath(indexListChild.getPath());
                    recursionDto.setTaskId(dto.getTaskId());
                    recursionDto.setAccountUnitId(dto.getAccountUnitId());
                    recursionDto.setBizId(indexListChild.getIndexId());
                    CostAccountTaskResultIndexProcessVo costAccountTaskResultIndexProcessVo = indexProcess(recursionDto);
                    resultIndexProcessList.add(costAccountTaskResultIndexProcessVo);
                }
            }

            List<CostAccountTaskItemVo> configItemList = new ArrayList<>();
            //定义借床分摊和病区分摊
            BigDecimal divideCountAfter = BigDecimal.ZERO;
            BigDecimal bedBorrowCountAfter = BigDecimal.ZERO;
            BigDecimal endemicAreaCountAfter = BigDecimal.ZERO;
            BigDecimal outpatientShardCountAfter = BigDecimal.ZERO;
            List<CostAccountTaskMedicalAllocationVo> medicalAllocationList = new ArrayList<>();
            //获取该指标下的所有核算项
            String resultItemIds = costTaskExecuteResultIndex.getItems();
            List<String> ids = ExpressionCheckHelper.getIds(resultItemIds);
            List<Long> idList = ids.stream().map(Long::parseLong) // 将每个 String 转换为 Long
                    .collect(Collectors.toList());
//            resultItemIds = resultItemIds.substring(1, resultItemIds.length() - 1);
            //遍历核算项，封转核算项信息和医护分摊数据
            for (Long resultItemId : idList) {
                CostTaskExecuteResultItem resultItem = costTaskExecuteResultItemMapper.selectById(resultItemId);
                if (resultItem != null) {
                    //封装核算项信息
                    CostTaskExecuteResultRule costTaskExecuteResultRule = new CostTaskExecuteResultRule().selectOne(new LambdaQueryWrapper<CostTaskExecuteResultRule>().eq(CostTaskExecuteResultRule::getTaskId, dto.getTaskId())
                            .eq(CostTaskExecuteResultRule::getUnitId, dto.getAccountUnitId())
                            .eq(CostTaskExecuteResultRule::getItemId, resultItem.getId())
                            .eq(resultItem.getPath() != null, CostTaskExecuteResultRule::getPath, resultItem.getPath())
                            .eq(resultItem.getPath() == null, CostTaskExecuteResultRule::getIndexId, resultItem.getIndexId())
                    );
                    BigDecimal ruleCount = costTaskExecuteResultRule.getRuleCount();
                    ConfigCorrespondenceItem configCorrespondenceItem = gson.fromJson(resultItem.getConfig(), ConfigCorrespondenceItem.class);
                    CostAccountTaskItemVo costAccountTaskItemVo = BeanUtil.copyProperties(configCorrespondenceItem, CostAccountTaskItemVo.class);
                    costAccountTaskItemVo.setItemTotalValue(configCorrespondenceItem.getCalculatedValue().multiply(ruleCount));
                    configItemList.add(costAccountTaskItemVo);
                }
            }


            List<CostTaskExecuteResultItem> resultItemList = new CostTaskExecuteResultItem().selectList(new LambdaQueryWrapper<CostTaskExecuteResultItem>()
                    .in(CostTaskExecuteResultItem::getId, idList));
            List<Long> itemIdList = resultItemList.stream().map(CostTaskExecuteResultItem::getItemId).collect(Collectors.toList());
            // 根据任务id、单元id、指标id、核算项id取出医护分摊的值
            String divideType = "yh";

            List<CostTaskExecuteResultExtra> divideCountResultExtra = new CostTaskExecuteResultExtra().selectList(new LambdaQueryWrapper<CostTaskExecuteResultExtra>()
                            .eq(CostTaskExecuteResultExtra::getTaskId, dto.getTaskId())
                            .eq(CostTaskExecuteResultExtra::getDivideUnitId, dto.getAccountUnitId())
                            .eq(CostTaskExecuteResultExtra::getDivideType, divideType)
//                    .in(CostTaskExecuteResultExtra::getItemId, idList)
            );
            //遍历封装医护分摊对象
            for (CostTaskExecuteResultExtra costTaskExecuteResultExtra : divideCountResultExtra) {
                CostAccountTaskMedicalAllocationVo medicalAllocationVo = new CostAccountTaskMedicalAllocationVo();
                //获取分摊项表
                CostTaskExecuteResultItem costTaskExecuteResultItem = new CostTaskExecuteResultItem().selectById(costTaskExecuteResultExtra.getItemId());
                if (itemIdList.contains(costTaskExecuteResultItemMapper.selectById(costTaskExecuteResultExtra.getItemId()).getItemId())) {
                    medicalAllocationVo.setAccountUnit(costTaskExecuteResultItem.getAccountObject());
                    medicalAllocationVo.setAccountUnitValue(costTaskExecuteResultExtra.getDivideCountBefore());
                    medicalAllocationVo.setAccountProportion(BigDecimal.valueOf(Double.parseDouble(costTaskExecuteResultExtra.getDividePercent())));
                    medicalAllocationVo.setMedicalAllocationValue(costTaskExecuteResultExtra.getDivideCountAfter());
                    medicalAllocationList.add(medicalAllocationVo);
                    BigDecimal divideCountAfter1 = costTaskExecuteResultExtra.getDivideCountAfter();
                    if (divideCountAfter1 != null) {
                        divideCountAfter = divideCountAfter.add(divideCountAfter1);
                    }
                }
//                if (costTaskExecuteResultItemMapper.selectById(costTaskExecuteResultExtra.getItemId()).getItemId().equals(resultItem.getItemId())) {
//
//                }
//                //获取该项的详细值
//                BigDecimal divideCountAfter1 = costTaskExecuteResultExtra.getDivideCountAfter();
//                if (divideCountAfter1 != null) {
//                    divideCountAfter = divideCountAfter.add(divideCountAfter1);
//                }

            }
            // 取出借床分摊对象
            String bedBorrow = "jc";
            bedBorrowCountAfter = getShareCount(dto, bedBorrowCountAfter, ids, bedBorrow);
            // 获取病区分摊数据
            String endemicArea = "bq";
            endemicAreaCountAfter = getShareCount(dto, endemicAreaCountAfter, ids, endemicArea);
            // 获取门诊共用分摊数据
            String outpatientShard = "mz";
            outpatientShardCountAfter = getShareCount(dto, outpatientShardCountAfter, ids, outpatientShard);

            //最后封装返回的vo
            resultIndexProcessVo.setConfigItemList(configItemList);
            resultIndexProcessVo.setMedicalAllocation(medicalAllocationList);
            resultIndexProcessVo.setBedBorrow(bedBorrowCountAfter);
            resultIndexProcessVo.setEndemicArea(endemicAreaCountAfter);
            resultIndexProcessVo.setOutpatientShard(outpatientShardCountAfter);
            resultIndexProcessVo.setConfigIndexList(resultIndexProcessList);
            resultIndexProcessVo.setNoExtraIndexCount(costTaskExecuteResultIndex.getNoExtraIndexCount());
            resultIndexProcessVo.setIndexTotalValue(costTaskExecuteResultIndex.getIndexCount());

        }
        return resultIndexProcessVo;
    }

    /**
     * 获取 jc bq mz 分摊值
     *
     * @param dto
     * @param countAfter
     * @param ids
     * @param divideType
     * @return
     */
    private BigDecimal getShareCount(CostAccountTaskCalculateProcessDto dto, BigDecimal countAfter, List<String> ids, String divideType) {
        List<CostTaskExecuteResultExtra> countResultExtra = new CostTaskExecuteResultExtra().selectList(new LambdaQueryWrapper<CostTaskExecuteResultExtra>()
                .eq(CostTaskExecuteResultExtra::getTaskId, dto.getTaskId())
                .eq(CostTaskExecuteResultExtra::getDivideUnitId, dto.getAccountUnitId())
                .eq(CostTaskExecuteResultExtra::getDivideType, divideType)
                .in(CostTaskExecuteResultExtra::getItemId, ids)
        );
        for (CostTaskExecuteResultExtra costTaskExecuteResultExtra : countResultExtra) {
            BigDecimal divideCountAfter1 = costTaskExecuteResultExtra.getDivideCountAfter();
            if (divideCountAfter1 != null) {
                countAfter = countAfter.add(divideCountAfter1);
            }
        }
        return countAfter;
    }

    /**
     * 核算项详情
     *
     * @param dto
     */
    private CostAccountTaskResultItemProcessVo itemProcess(CostAccountTaskCalculateProcessDto dto) {
        CostAccountTaskResultItemProcessVo resultItemProcessVo = new CostAccountTaskResultItemProcessVo();
        //查询当前指标项
        LambdaQueryWrapper<CostTaskExecuteResultItem> queryWrapper = new LambdaQueryWrapper<CostTaskExecuteResultItem>().eq(CostTaskExecuteResultItem::getTaskId, dto.getTaskId())
                .eq(CostTaskExecuteResultItem::getUnitId, dto.getAccountUnitId())
                .eq(CostTaskExecuteResultItem::getItemId, dto.getBizId());
        if (!("".equals(dto.getPath()))) {
            queryWrapper.eq(CostTaskExecuteResultItem::getPath, dto.getPath());
        } else {
            queryWrapper.eq(CostTaskExecuteResultItem::getIndexId, dto.getParentId()).isNull(CostTaskExecuteResultItem::getPath);
        }
        CostTaskExecuteResultItem resultItem = new CostTaskExecuteResultItem().selectOne(queryWrapper);
        if (resultItem != null) {
            //查询指标项对应的规则
            LambdaQueryWrapper<CostTaskExecuteResultRule> ruleQueryWrapper = new LambdaQueryWrapper<CostTaskExecuteResultRule>().eq(CostTaskExecuteResultRule::getTaskId, dto.getTaskId())
                    .eq(CostTaskExecuteResultRule::getUnitId, dto.getAccountUnitId())
                    .eq(CostTaskExecuteResultRule::getItemId, resultItem.getId());
            if (resultItem.getPath() != null) {
                ruleQueryWrapper.eq(CostTaskExecuteResultRule::getPath, resultItem.getPath());
            } else {
                ruleQueryWrapper.isNull(CostTaskExecuteResultRule::getPath).eq(CostTaskExecuteResultRule::getIndexId, resultItem.getIndexId());
            }
            CostTaskExecuteResultRule costTaskExecuteResultRule = new CostTaskExecuteResultRule().selectOne(ruleQueryWrapper);
            //封装stepOne、stepTwo、stepThree核算项结果页返回
            resultItemProcessVo.setStepOne(getStepOne(resultItem, dto));
            resultItemProcessVo.setStepTwo(getStepTwo(costTaskExecuteResultRule, dto));
            resultItemProcessVo.setStepThree(getStepThree(resultItem, costTaskExecuteResultRule));
        }
        return resultItemProcessVo;
    }

    /**
     * 定义组装stepThree
     *
     * @param
     * @return
     */
    private CalculatedValueVo getStepThree(CostTaskExecuteResultItem resultItem, CostTaskExecuteResultRule costTaskExecuteResultRule) {
        //获取核算项的详细信息
        BigDecimal ruleCount = costTaskExecuteResultRule.getRuleCount();
        Gson gson = new Gson();
        ConfigCorrespondenceItem configCorrespondenceItem = gson.fromJson(resultItem.getConfig(), ConfigCorrespondenceItem.class);
        configCorrespondenceItem.setCalculatedValue(resultItem.getCalculateCount().multiply(ruleCount));
        CalculatedValueVo stepThree = BeanUtil.copyProperties(configCorrespondenceItem, CalculatedValueVo.class);
        return stepThree;
    }

    /**
     * 定义组装stepTwo
     *
     * @param dto
     * @return
     */
    @NotNull
    private CalculationAccountRuleVo getStepTwo(CostTaskExecuteResultRule costTaskExecuteResultRule, CostAccountTaskCalculateProcessDto dto) {
        CalculationAccountRuleVo stepTwo = new CalculationAccountRuleVo();
        Gson gson = new Gson();
        if (costTaskExecuteResultRule != null) {
            //取出规则的信息，封装到vo中
            DivideFormulaDesc divideFormulaDesc = gson.fromJson(costTaskExecuteResultRule.getDivideFormulaDesc(), DivideFormulaDesc.class);
            stepTwo.setDividerFormula(divideFormulaDesc);
            stepTwo.setAccountPeriod(costTaskExecuteResultRule.getTimePeriod());
            stepTwo.setAccountObject(costTaskExecuteResultRule.getAccountObject());
            stepTwo.setAccountProportion(costTaskExecuteResultRule.getDividePercent());
            stepTwo.setRuleCount(costTaskExecuteResultRule.getRuleCount());
            Type listType = new TypeToken<List<CalculateDetail>>() {
            }.getType();
            List<CalculateDetail> calculateDetail = gson.fromJson(costTaskExecuteResultRule.getCalculateDetail(), listType);
            stepTwo.setCalculateDetail(calculateDetail);
        }
        return stepTwo;
    }

    /**
     * 定义组装stepOne
     *
     * @param dto
     * @return
     */
    @NotNull
    private AllocationObjectCalculationVo getStepOne(CostTaskExecuteResultItem resultItem, CostAccountTaskCalculateProcessDto dto) {
        AllocationObjectCalculationVo stepOne = new AllocationObjectCalculationVo();
        List<UnitAccountValueObjectVo> unitAccountValueObjectVo = new ArrayList<>();
        //封装返回的被分摊对象核算值计算
        //TODO: 可能实现分段
        stepOne.setAccountPeriod(resultItem.getTimePeriod());
        stepOne.setAccountObject(resultItem.getAccountObject());
        UnitAccountValueObjectVo accountValueObject = new UnitAccountValueObjectVo();
        accountValueObject.setAccountPeriod(resultItem.getTimePeriod());
        accountValueObject.setUnitValue(resultItem.getCalculateCount() == null ? BigDecimal.ZERO : resultItem.getCalculateCount());
        unitAccountValueObjectVo.add(accountValueObject);
        stepOne.setUnitCountValue(resultItem.getCalculateCount());
        stepOne.setUnitAccountValueObjectVo(unitAccountValueObjectVo);
        return stepOne;
    }


}

