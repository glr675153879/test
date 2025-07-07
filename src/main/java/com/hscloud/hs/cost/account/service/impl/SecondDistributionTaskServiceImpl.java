package com.hscloud.hs.cost.account.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hscloud.hs.cost.account.constant.enums.SecondDistributionEnum;
import com.hscloud.hs.cost.account.constant.enums.SecondDistributionIndexEnum;
import com.hscloud.hs.cost.account.mapper.SecondDistributionTaskMapper;
import com.hscloud.hs.cost.account.model.dto.SecondDistributionManagementQueryDto;
import com.hscloud.hs.cost.account.model.dto.SecondTaskDistributionDto;
import com.hscloud.hs.cost.account.model.dto.SecondTaskQueryDto;
import com.hscloud.hs.cost.account.model.entity.*;
import com.hscloud.hs.cost.account.model.pojo.*;
import com.hscloud.hs.cost.account.model.vo.*;
import com.hscloud.hs.cost.account.service.ISecondDistributionAccountPlanService;
import com.hscloud.hs.cost.account.service.ISecondDistributionIndexCalcService;
import com.hscloud.hs.cost.account.service.ISecondDistributionTaskService;
import com.hscloud.hs.cost.account.service.ISecondDistributionTaskUnitInfoService;
import com.hscloud.hs.cost.account.utils.EasyExcelUtil;
import com.pig4cloud.pigx.common.core.exception.BizException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * <p>
 * 二次分配任务表 服务实现类
 * </p>
 *
 * @author
 * @since 2023-11-17
 */
@Service
@RequiredArgsConstructor
public class SecondDistributionTaskServiceImpl extends ServiceImpl<SecondDistributionTaskMapper, SecondDistributionTask> implements ISecondDistributionTaskService {

    private final ISecondDistributionAccountPlanService secondDistributionAccountPlanService;


    private final ISecondDistributionIndexCalcService secondDistributionIndexCalcService;

    @Autowired
    @Lazy
    private ISecondDistributionTaskUnitInfoService secondDistributionTaskUnitInfoService;


    private List<String> baseTitleList = Arrays.asList("姓名", "工号", "二次分配总绩效金额");

    @Override
    public Page<SecondDistributionTaskVo> getTaskAllocationPreview(SecondTaskQueryDto queryDto) {
//        HsUser user = SecurityUtils.getUser();
        HashSet<String> nameSet = new HashSet<>();
        //管理绩效中本科室人员
        List<SecondDistributionTaskManagement> managementList = new SecondDistributionTaskManagement().selectList(new LambdaQueryWrapper<SecondDistributionTaskManagement>()
                .eq(SecondDistributionTaskManagement::getTaskUnitRelateId, queryDto.getTaskUnitInfoId())
        );
        if (CollUtil.isNotEmpty(managementList)) {
            Set<String> nameList = managementList.stream().map(SecondDistributionTaskManagement::getName).collect(Collectors.toSet());
            nameSet.addAll(nameList);
        }
        //平均绩效本科室人员
        List<SecondDistributionTaskAverage> averageList = new SecondDistributionTaskAverage().selectList(new LambdaQueryWrapper<SecondDistributionTaskAverage>()
                .eq(SecondDistributionTaskAverage::getTaskUnitRelateId, queryDto.getTaskUnitInfoId())
        );
        if (CollUtil.isNotEmpty(averageList)) {
            Set<String> nameList = averageList.stream().map(SecondDistributionTaskAverage::getName).collect(Collectors.toSet());
            nameSet.addAll(nameList);
        }
        //工作量绩效本科室人员
        List<SecondDistributionTaskWorkload> workloadList = new SecondDistributionTaskWorkload().selectList(new LambdaQueryWrapper<SecondDistributionTaskWorkload>()
                .eq(SecondDistributionTaskWorkload::getTaskUnitRelateId, queryDto.getTaskUnitInfoId())
        );
        if (CollUtil.isNotEmpty(workloadList)) {
            Set<String> nameList = workloadList.stream().map(SecondDistributionTaskWorkload::getName).collect(Collectors.toSet());
            nameSet.addAll(nameList);
        }
        //个人职称绩效本科室人员
        List<SecondDistributionTaskIndividualPost> individualPostList = new SecondDistributionTaskIndividualPost().selectList(new LambdaQueryWrapper<SecondDistributionTaskIndividualPost>()
                .eq(SecondDistributionTaskIndividualPost::getTaskUnitRelateId, queryDto.getTaskUnitInfoId())
        );
        if (CollUtil.isNotEmpty(individualPostList)) {
            Set<String> nameList = individualPostList.stream().map(SecondDistributionTaskIndividualPost::getName).collect(Collectors.toSet());
            nameSet.addAll(nameList);
        }
        //单项绩效本科室人员
        List<SecondDistributionTaskSingle> singleList = new SecondDistributionTaskSingle().selectList(new LambdaQueryWrapper<SecondDistributionTaskSingle>()
                .eq(SecondDistributionTaskSingle::getTaskUnitRelateId, queryDto.getTaskUnitInfoId())
        );
        if (CollUtil.isNotEmpty(singleList)) {
            Set<String> nameList = singleList.stream().map(SecondDistributionTaskSingle::getName).collect(Collectors.toSet());
            nameSet.addAll(nameList);
        }
        Page<SecondDistributionTaskVo> taskVoPage = new Page<>(queryDto.getCurrent(), queryDto.getSize());
        if (!nameSet.isEmpty()) {
            // 将总的nameSet转换为List
            List<String> nameList = new ArrayList<>(nameSet);
            // 定义返回的vo
            List<SecondDistributionTaskVo> taskVoList = new ArrayList<>();

            for (String name : nameList) {
                //封装对象
                SecondDistributionTaskVo secondDistributionTaskVo = new SecondDistributionTaskVo();
                secondDistributionTaskVo.setName(name);
                //封装管理绩效对象
                SecondDistributionTaskManagement management = managementList.stream()
                        .filter(m -> m.getName().equals(name))
                        .findFirst().orElse(null);
                if (management != null) {
                    SecondTaskManagementVo secondTaskManagementVo = new SecondTaskManagementVo();
                    secondTaskManagementVo.setAmount(management.getAmount());
                    secondTaskManagementVo.setType(management.getType());
                    secondDistributionTaskVo.setManagementVo(secondTaskManagementVo);
                }
                //封装单项绩效对象
                List<SecondDistributionTaskSingle> taskSingleList = singleList.stream()
                        .filter(s -> s.getName().equals(name))
                        .collect(Collectors.toList());
                if (!taskSingleList.isEmpty()) {
                    SecondTaskSingleVo secondTaskSingleVo = new SecondTaskSingleVo();
                    List<SecondTaskSingleVo.SecondSingle> secondSingles = taskSingleList.stream()
                            .map(single -> {
                                SecondTaskSingleVo.SecondSingle secondSingle = new SecondTaskSingleVo.SecondSingle();
                                secondSingle.setName(single.getName());
                                secondSingle.setAmount(single.getAmount());
                                return secondSingle;
                            })
                            .collect(Collectors.toList());
                    BigDecimal singleTotalAmount = taskSingleList.stream()
                            .map(SecondDistributionTaskSingle::getAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    secondTaskSingleVo.setTotalAmount(singleTotalAmount);
                    secondTaskSingleVo.setSingleList(secondSingles);
                    secondDistributionTaskVo.setSingleVo(secondTaskSingleVo);
                }

                // 个人职称绩效对象
                SecondDistributionTaskIndividualPost individualPost = individualPostList.stream()
                        .filter(p -> p.getName().equals(name))
                        .findFirst().orElse(null);
                if (individualPost != null) {
                    // TODO 数据后期填充
                    SecondTaskIndividualPostVo secondTaskIndividualPostVo = new SecondTaskIndividualPostVo();
                    secondTaskIndividualPostVo.setTotalAmount(individualPost.getAmount());
                    secondTaskIndividualPostVo.setIndividualPostList(Collections.emptyList());
                    secondTaskIndividualPostVo.setExpression("");
                    secondDistributionTaskVo.setIndividualPostVo(secondTaskIndividualPostVo);
                }
                //封装工作量绩效对象
                List<SecondDistributionTaskWorkload> workload = workloadList.stream()
                        .filter(w -> w.getName().equals(name))
                        .collect(Collectors.toList());
                if (!workload.isEmpty()) {
                    SecondTaskWorkloadVo secondTaskWorkloadVo = new SecondTaskWorkloadVo();
                    List<SecondTaskWorkloadVo.SecondWorkload> workloads = workload.stream()
                            .map(taskWorkload -> {
                                SecondTaskWorkloadVo.SecondWorkload secondWorkload = new SecondTaskWorkloadVo.SecondWorkload();
                                secondWorkload.setName(taskWorkload.getName());
                                secondWorkload.setAmount(taskWorkload.getAmount());
                                return secondWorkload;
                            })
                            .collect(Collectors.toList());
                    BigDecimal workLoadTotalAmount = workload.stream()
                            .map(SecondDistributionTaskWorkload::getAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    secondTaskWorkloadVo.setTotalAmount(workLoadTotalAmount);
                    secondTaskWorkloadVo.setWorkloadList(workloads);
                    secondDistributionTaskVo.setWorkloadVo(secondTaskWorkloadVo);
                }
                //封装平均绩效对象
                SecondDistributionTaskAverage average = averageList.stream()
                        .filter(a -> a.getName().equals(name))
                        .findFirst().orElse(null);
                if (average != null) {
                    //TODO 封装逻辑后续补充
                    SecondTaskAverageVo secondTaskAverageVo = new SecondTaskAverageVo();
                    secondTaskAverageVo.setTotalAmount(BigDecimal.ZERO);
                    secondTaskAverageVo.setIndividualPostList(Collections.emptyList());
                    secondTaskAverageVo.setExpression("");
                    secondDistributionTaskVo.setAverageVo(secondTaskAverageVo);
                }

                //员工工号
                secondDistributionTaskVo.setJobNumber(management != null ? management.getJobNumber() : null);
                BigDecimal secondAmount = secondDistributionTaskVo.getSingleVo().getTotalAmount()
                        .add(secondDistributionTaskVo.getManagementVo().getAmount())
                        .add(secondDistributionTaskVo.getWorkloadVo().getTotalAmount())
                        .add(secondDistributionTaskVo.getIndividualPostVo().getTotalAmount());
                secondDistributionTaskVo.setSecondAmount(secondAmount);
                taskVoList.add(secondDistributionTaskVo);
            }

            taskVoPage.setRecords(taskVoList);
        }
        return taskVoPage;
    }

    /**
     * 获取分配管理审核列表
     *
     * @param dto
     * @return
     */
    @Override
    public Page<SecondDistributionManagementVo> getList(SecondDistributionManagementQueryDto dto) {
        //根据条件查询出所有的任务列表
        Page<SecondDistributionTask> page = new SecondDistributionTask().selectPage(new Page<>(dto.getCurrent(), dto.getSize()), new LambdaQueryWrapper<SecondDistributionTask>()
                .like(StrUtil.isNotBlank(dto.getTaskName()), SecondDistributionTask::getName, dto.getTaskName())
                .ge(dto.getStartTime() != null, SecondDistributionTask::getCreateTime, dto.getStartTime())
                .le(dto.getEndTime() != null, SecondDistributionTask::getCreateTime, dto.getEndTime()));
        // 封装返回结果
        List<SecondDistributionTask> taskList = page.getRecords();

        List<SecondDistributionManagementVo> recordVoList = new ArrayList<>();

        for (SecondDistributionTask secondDistributionTask : taskList) {
            //取出一次分配任务下数据该科室单元的子任务
            List<SecondDistributionTaskUnitInfo> secondDistributionTaskUnitInfoList = new SecondDistributionTaskUnitInfo().selectList(new LambdaQueryWrapper<SecondDistributionTaskUnitInfo>()
                    .eq(SecondDistributionTaskUnitInfo::getTaskId, secondDistributionTask.getId())
                    .eq(SecondDistributionTaskUnitInfo::getUnitId, dto.getUnitId())
                    .eq(StrUtil.isNotBlank(dto.getStatus()), SecondDistributionTaskUnitInfo::getStatus, dto.getStatus()));
            if (CollUtil.isNotEmpty(secondDistributionTaskUnitInfoList)) {
                //封装vo
                for (SecondDistributionTaskUnitInfo taskUnitInfo : secondDistributionTaskUnitInfoList) {
                    SecondDistributionManagementVo recordVo = new SecondDistributionManagementVo();
                    recordVo.setTaskUnitId(taskUnitInfo.getId());
                    recordVo.setTaskName(secondDistributionTask.getName());
                    recordVo.setTaskType(secondDistributionTask.getType());
                    recordVo.setTaskPeriod(secondDistributionTask.getTaskPeriod());
                    recordVo.setReceiptDate(secondDistributionTask.getCreateTime());
                    recordVo.setPlanId(taskUnitInfo.getPlanId());
                    recordVo.setStatus(taskUnitInfo.getStatus());
                    recordVoList.add(recordVo);
                }

            }

        }

        Page<SecondDistributionManagementVo> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        voPage.setRecords(recordVoList);
        return voPage;
    }

    @Override
    public SecondTaskDistributionDetailVo getTaskAllocationDetail(SecondTaskDistributionDto secondTaskDistributionDto) {
        Long relatePlanId = secondTaskDistributionDto.getRelatePlanId();
        SecondTaskDistributionDetailVo secondTaskDistributionDetailVo = new SecondTaskDistributionDetailVo();
        //获取总金额
        SecondDistributionTaskUnitDetail detail = secondDistributionTaskUnitInfoService.getTaskUnitInfoById(secondTaskDistributionDto.getRelateTaskId());
        if (detail == null) {
            return secondTaskDistributionDetailVo;
        }
        secondTaskDistributionDetailVo.setSubmitTime(detail.getSubmitTime());
        secondTaskDistributionDetailVo.setTotalAmount(detail.getTotalAmount());
        SecondDistributionGetAccountPlanDetailsVo distributionAccountPlanDetails = secondDistributionAccountPlanService.getDistributionAccountPlanDetails(relatePlanId, secondTaskDistributionDto.getUnitId());
        secondTaskDistributionDetailVo.setAccountPlanDetailsVo(distributionAccountPlanDetails);
        Map<String, Map<String, BigDecimal>> configMap = new HashMap<>();
        distributionAccountPlanDetails.getAccountIndexInfoList().stream().filter(secondDistributionAccountIndexInfoVo -> SecondDistributionIndexEnum.PJFPJX.getItem().equals(secondDistributionAccountIndexInfoVo.getAccountIndex())).
                map(SecondDistributionAccountIndexInfoVo::getId).forEach(accountIndexId -> configMap.put(SecondDistributionEnum.AVERAGE.getCode(), secondDistributionIndexCalcService.analysisPJJX(relatePlanId, accountIndexId, secondTaskDistributionDto.getRelateTaskId(), detail.getPeriod())));
        secondTaskDistributionDetailVo.setConfigMap(configMap);
        Map<Long, String> jobNameMap = new HashMap<>();
        //获取管理绩效
        List<SecondDistributionTaskManagement> secondDistributionTaskManagements = new SecondDistributionTaskManagement().selectList(new LambdaQueryWrapper<SecondDistributionTaskManagement>()
                .eq(SecondDistributionTaskManagement::getTaskUnitRelateId, secondTaskDistributionDto.getRelateTaskId())
                .eq(SecondDistributionTaskManagement::getPlanId, relatePlanId)
                .eq(SecondDistributionTaskManagement::getUnitId, secondTaskDistributionDto.getUnitId()));

        jobNameMap.putAll(secondDistributionTaskManagements.stream().collect(Collectors.toMap(SecondDistributionTaskManagement::getJobNumber, SecondDistributionTaskManagement::getName, (k1, k2) -> k1)));
        //以jobNumber分组
        Map<Long, List<SecondDistributionTaskManagement>> managementMap = secondDistributionTaskManagements.stream().collect(Collectors.groupingBy(SecondDistributionTaskManagement::getJobNumber));

        //获取单项绩效
        List<SecondDistributionTaskSingle> secondDistributionTaskSingles = new SecondDistributionTaskSingle().selectList(new LambdaQueryWrapper<SecondDistributionTaskSingle>()
                .eq(SecondDistributionTaskSingle::getTaskUnitRelateId, secondTaskDistributionDto.getRelateTaskId())
                .eq(SecondDistributionTaskSingle::getPlanId, relatePlanId)
                .eq(SecondDistributionTaskSingle::getUnitId, secondTaskDistributionDto.getUnitId()));
        jobNameMap.putAll(secondDistributionTaskSingles.stream().collect(Collectors.toMap(SecondDistributionTaskSingle::getJobNumber, SecondDistributionTaskSingle::getName, (k1, k2) -> k1)));
        //以jobNumber分组
        Map<Long, List<SecondDistributionTaskSingle>> singleMap = secondDistributionTaskSingles.stream().collect(Collectors.groupingBy(SecondDistributionTaskSingle::getJobNumber));
        //获取工作量绩效
        List<SecondDistributionTaskWorkload> secondDistributionTaskWorkloads = new SecondDistributionTaskWorkload().selectList(new LambdaQueryWrapper<SecondDistributionTaskWorkload>()
                .eq(SecondDistributionTaskWorkload::getTaskUnitRelateId, secondTaskDistributionDto.getRelateTaskId())
                .eq(SecondDistributionTaskWorkload::getPlanId, relatePlanId)
                .eq(SecondDistributionTaskWorkload::getUnitId, secondTaskDistributionDto.getUnitId()));
        jobNameMap.putAll(secondDistributionTaskWorkloads.stream().collect(Collectors.toMap(SecondDistributionTaskWorkload::getJobNumber, SecondDistributionTaskWorkload::getName, (k1, k2) -> k1)));
        //以jobNumber分组
        Map<Long, List<SecondDistributionTaskWorkload>> workloadMap = secondDistributionTaskWorkloads.stream().collect(Collectors.groupingBy(SecondDistributionTaskWorkload::getJobNumber));
        //获取个人职称绩效
        List<SecondDistributionTaskIndividualPost> secondDistributionTaskIndividualPosts = new SecondDistributionTaskIndividualPost().selectList(new LambdaQueryWrapper<SecondDistributionTaskIndividualPost>()
                .eq(SecondDistributionTaskIndividualPost::getTaskUnitRelateId, secondTaskDistributionDto.getRelateTaskId())
                .eq(SecondDistributionTaskIndividualPost::getPlanId, relatePlanId)
                .eq(SecondDistributionTaskIndividualPost::getUnitId, secondTaskDistributionDto.getUnitId()));
        jobNameMap.putAll(secondDistributionTaskIndividualPosts.stream().collect(Collectors.toMap(SecondDistributionTaskIndividualPost::getJobNumber, SecondDistributionTaskIndividualPost::getName, (k1, k2) -> k1)));
        //以jobNumber分组
        Map<Long, List<SecondDistributionTaskIndividualPost>> individualPostMap = secondDistributionTaskIndividualPosts.stream().collect(Collectors.groupingBy(SecondDistributionTaskIndividualPost::getJobNumber));
        List<SecondUserDistributionDetail> secondUserDistributionDetailList = new ArrayList<>(jobNameMap.size());
        //获取平均绩效
        List<SecondDistributionTaskAverage> secondDistributionTaskAverages = new SecondDistributionTaskAverage().selectList(new LambdaQueryWrapper<SecondDistributionTaskAverage>()
                .eq(SecondDistributionTaskAverage::getTaskUnitRelateId, secondTaskDistributionDto.getRelateTaskId())
                .eq(SecondDistributionTaskAverage::getPlanId, relatePlanId)
                .eq(SecondDistributionTaskAverage::getUnitId, secondTaskDistributionDto.getUnitId()));
        jobNameMap.putAll(secondDistributionTaskAverages.stream().collect(Collectors.toMap(SecondDistributionTaskAverage::getJobNumber, SecondDistributionTaskAverage::getName, (k1, k2) -> k1)));
        //以jobNumber分组
        Map<Long, List<SecondDistributionTaskAverage>> averageMap = secondDistributionTaskAverages.stream().collect(Collectors.groupingBy(SecondDistributionTaskAverage::getJobNumber));
        //遍历jobNameMap
        for (Map.Entry<Long, String> entry : jobNameMap.entrySet()) {
            SecondUserDistributionDetail secondUserDistributionDetail = new SecondUserDistributionDetail();
            secondUserDistributionDetail.setJobNumber(entry.getKey());
            secondUserDistributionDetail.setName(entry.getValue());
            List<SecondUserDistributionUnitMulti> secondUserDistributionUnitMultis = new ArrayList<>();
            List<SecondUserDistributionUnitSingle> secondUserDistributionUnitSingleList = new ArrayList<>();
            //管理绩效
            List<SecondDistributionTaskManagement> managementList = managementMap.get(entry.getKey());
            if (CollUtil.isNotEmpty(managementList)) {
                //以指标id分组
                Map<Long, List<SecondDistributionTaskManagement>> indexMap = managementList.stream().collect(Collectors.groupingBy(SecondDistributionTaskManagement::getIndexId));
                for (Map.Entry<Long, List<SecondDistributionTaskManagement>> indexEntry : indexMap.entrySet()) {
                    SecondUserDistributionUnitMulti secondUserDistributionUnitMulti = new SecondUserDistributionUnitMulti();
                    secondUserDistributionUnitMulti.setType(SecondDistributionEnum.MANAGEMENT.getCode());
                    secondUserDistributionUnitMulti.setIndexId(indexEntry.getKey());
                    secondUserDistributionUnitMulti.setIndexName(indexEntry.getValue().get(0).getIndexName());
                    secondUserDistributionUnitMulti.setPlanId(relatePlanId);
                    secondUserDistributionUnitMulti.setSecondUserDistributionSmallUnitList(managementList.stream().map(
                            secondDistributionTaskManagement -> {
                                SecondUserDistributionSmallUnit secondUserDistributionSmallUnit = new SecondUserDistributionSmallUnit();
                                secondUserDistributionSmallUnit.setAmount(secondDistributionTaskManagement.getAmount());
                                secondUserDistributionSmallUnit.setId(secondDistributionTaskManagement.getPositionId());
                                secondUserDistributionSmallUnit.setName(secondDistributionTaskManagement.getName());
                                return secondUserDistributionSmallUnit;
                            }
                    ).collect(Collectors.toList()));
                    secondUserDistributionUnitMultis.add(secondUserDistributionUnitMulti);
                }
            }
            //单项绩效
            List<SecondDistributionTaskSingle> singleList = singleMap.get(entry.getKey());
            if (CollUtil.isNotEmpty(singleList)) {
                //以指标id分组
                Map<Long, List<SecondDistributionTaskSingle>> indexMap = singleList.stream().collect(Collectors.groupingBy(SecondDistributionTaskSingle::getIndexId));
                for (Map.Entry<Long, List<SecondDistributionTaskSingle>> indexEntry : indexMap.entrySet()) {
                    SecondUserDistributionUnitMulti secondUserDistributionUnitMulti = new SecondUserDistributionUnitMulti();
                    secondUserDistributionUnitMulti.setType(SecondDistributionEnum.SINGLE.getCode());
                    secondUserDistributionUnitMulti.setIndexId(indexEntry.getKey());
                    secondUserDistributionUnitMulti.setIndexName(indexEntry.getValue().get(0).getIndexName());
                    secondUserDistributionUnitMulti.setPlanId(relatePlanId);
                    secondUserDistributionUnitMulti.setSecondUserDistributionSmallUnitList(indexEntry.getValue().stream().map(
                            secondDistributionTaskSingle -> {
                                SecondUserDistributionSmallUnit secondUserDistributionSmallUnit = new SecondUserDistributionSmallUnit();
                                secondUserDistributionSmallUnit.setAmount(secondDistributionTaskSingle.getAmount());
                                secondUserDistributionSmallUnit.setId(secondDistributionTaskSingle.getSingleId());
                                secondUserDistributionSmallUnit.setName(secondDistributionTaskSingle.getName());
                                return secondUserDistributionSmallUnit;
                            }
                    ).collect(Collectors.toList()));
                    secondUserDistributionUnitMultis.add(secondUserDistributionUnitMulti);
                }
            }
            //工作量绩效
            List<SecondDistributionTaskWorkload> workloadList = workloadMap.get(entry.getKey());
            if (CollUtil.isNotEmpty(workloadList)) {
                //以指标id分组
                Map<Long, List<SecondDistributionTaskWorkload>> indexMap = workloadList.stream().collect(Collectors.groupingBy(SecondDistributionTaskWorkload::getIndexId));
                for (Map.Entry<Long, List<SecondDistributionTaskWorkload>> indexEntry : indexMap.entrySet()) {
                    SecondUserDistributionUnitMulti secondUserDistributionUnitMulti = new SecondUserDistributionUnitMulti();
                    secondUserDistributionUnitMulti.setType(SecondDistributionEnum.WORKLOAD.getCode());
                    secondUserDistributionUnitMulti.setIndexId(indexEntry.getKey());
                    secondUserDistributionUnitMulti.setIndexName(indexEntry.getValue().get(0).getIndexName());
                    secondUserDistributionUnitMulti.setPlanId(relatePlanId);
                    secondUserDistributionUnitMulti.setSecondUserDistributionSmallUnitList(indexEntry.getValue().stream().map(
                            secondDistributionTaskWorkload -> {
                                SecondUserDistributionSmallUnit secondUserDistributionSmallUnit = new SecondUserDistributionSmallUnit();
                                secondUserDistributionSmallUnit.setAmount(secondDistributionTaskWorkload.getAmount());
                                secondUserDistributionSmallUnit.setId(secondDistributionTaskWorkload.getWorkloadId());
                                secondUserDistributionSmallUnit.setName(secondDistributionTaskWorkload.getName());
                                return secondUserDistributionSmallUnit;
                            }
                    ).collect(Collectors.toList()));
                    secondUserDistributionUnitMultis.add(secondUserDistributionUnitMulti);
                }
            }
            //个人职称绩效
            List<SecondDistributionTaskIndividualPost> individualPostList = individualPostMap.get(entry.getKey());
            if (CollUtil.isNotEmpty(individualPostList)) {
                //以指标id分组
                Map<Long, List<SecondDistributionTaskIndividualPost>> indexMap = individualPostList.stream().collect(Collectors.groupingBy(SecondDistributionTaskIndividualPost::getIndexId));
                for (Map.Entry<Long, List<SecondDistributionTaskIndividualPost>> indexEntry : indexMap.entrySet()) {
                    SecondUserDistributionUnitSingle secondUserDistributionUnitSingle = new SecondUserDistributionUnitSingle();
                    secondUserDistributionUnitSingle.setType(SecondDistributionEnum.INDIVIDUAL_POST.getCode());
                    secondUserDistributionUnitSingle.setIndexId(indexEntry.getKey());
                    secondUserDistributionUnitSingle.setIndexName(indexEntry.getValue().get(0).getIndexName());
                    secondUserDistributionUnitSingle.setPlanId(relatePlanId);
                    secondUserDistributionUnitSingle.setEducation(indexEntry.getValue().get(0).getEducation());
                    secondUserDistributionUnitSingle.setTitleLevel(indexEntry.getValue().get(0).getTitleLevel());
                    secondUserDistributionUnitSingle.setCoefficient(indexEntry.getValue().get(0).getCoefficient());
                    secondUserDistributionUnitSingle.setAmount(indexEntry.getValue().stream().map(SecondDistributionTaskIndividualPost::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add));
                    secondUserDistributionUnitSingleList.add(secondUserDistributionUnitSingle);
                }
            }
            //平均绩效
            List<SecondDistributionTaskAverage> averageList = averageMap.get(entry.getKey());
            if (CollUtil.isNotEmpty(averageList)) {
                //以指标id分组
                Map<Long, List<SecondDistributionTaskAverage>> indexMap = averageList.stream().collect(Collectors.groupingBy(SecondDistributionTaskAverage::getIndexId));
                for (Map.Entry<Long, List<SecondDistributionTaskAverage>> indexEntry : indexMap.entrySet()) {
                    SecondUserDistributionUnitSingle secondUserDistributionUnitSingle = new SecondUserDistributionUnitSingle();
                    secondUserDistributionUnitSingle.setType(SecondDistributionEnum.AVERAGE.getCode());
                    secondUserDistributionUnitSingle.setIndexId(indexEntry.getKey());
                    secondUserDistributionUnitSingle.setIndexName(indexEntry.getValue().get(0).getIndexName());
                    secondUserDistributionUnitSingle.setPlanId(relatePlanId);
                    secondUserDistributionUnitSingle.setAmount(indexEntry.getValue().stream().map(SecondDistributionTaskAverage::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add));
                    secondUserDistributionUnitSingleList.add(secondUserDistributionUnitSingle);
                }
            }


            secondUserDistributionDetail.setSecondUserDistributionUnitMultiList(secondUserDistributionUnitMultis);
            secondUserDistributionDetail.setSecondUserDistributionUnitSingleList(secondUserDistributionUnitSingleList);
            secondUserDistributionDetailList.add(secondUserDistributionDetail);
        }
        secondTaskDistributionDetailVo.setSecondUserDistributionDetailList(secondUserDistributionDetailList);
        return secondTaskDistributionDetailVo;
    }


    @Override
    public void export(HttpServletResponse response, SecondTaskDistributionDto secondTaskDistributionDto) {
        //获取当前核算指标下的所有人员数据
        List<SecondDistributionUnitUserDetail> records = getSecondDistributionUnitUserDetail(secondTaskDistributionDto);
        if (null != records && records.size() > 0) {
            List<String> head = new ArrayList<>();
            List<List<String>> body = new ArrayList<>();
            for (SecondDistributionUnitUserDetail detail : records) {
                head.add(detail.getTitle());
                body.add(detail.getData());
            }
            try {
                EasyExcelUtil.importExcel(response, "二次分配明细表", head, body);
            } catch (Exception e) {
                log.error("导出二次分配明细表失败", e);
                throw new BizException("导出二次分配明细表失败");
            }

        }

    }

    public List<SecondDistributionUnitUserDetail> getSecondDistributionUnitUserDetail(SecondTaskDistributionDto secondTaskDistributionDto) {

        //获取当前方案下的核算指标
        Long relatePlanId = secondTaskDistributionDto.getRelatePlanId();
        SecondDistributionGetAccountPlanDetailsVo distributionAccountPlanDetails = secondDistributionAccountPlanService.getDistributionAccountPlanDetails(relatePlanId, secondTaskDistributionDto.getUnitId());
        List<SecondDistributionAccountIndexInfoVo> accountIndexInfoList = distributionAccountPlanDetails.getAccountIndexInfoList();
        Map<Long, String> indexMap = accountIndexInfoList.stream().collect(Collectors.toMap(SecondDistributionAccountIndexInfoVo::getId, SecondDistributionAccountIndexInfoVo::getAccountIndex, (k1, k2) -> k1));
        //获取当前核算指标下的所有人员数据

        Map<Long, String> jobNameMap = new HashMap<>();
        //获取管理绩效
        List<SecondDistributionTaskManagement> secondDistributionTaskManagements = new SecondDistributionTaskManagement().selectList(new LambdaQueryWrapper<SecondDistributionTaskManagement>()
                .eq(SecondDistributionTaskManagement::getTaskUnitRelateId, secondTaskDistributionDto.getRelateTaskId())
                .eq(SecondDistributionTaskManagement::getPlanId, relatePlanId)
                .eq(SecondDistributionTaskManagement::getUnitId, secondTaskDistributionDto.getUnitId())
                .select(SecondDistributionTaskManagement::getJobNumber, SecondDistributionTaskManagement::getName, SecondDistributionTaskManagement::getAmount, SecondDistributionTaskManagement::getIndexId));

        jobNameMap.putAll(secondDistributionTaskManagements.stream().collect(Collectors.toMap(SecondDistributionTaskManagement::getJobNumber, SecondDistributionTaskManagement::getName, (k1, k2) -> k1)));
        //以jobNumber分组
        Map<Long, List<SecondDistributionTaskManagement>> managementMap = secondDistributionTaskManagements.stream().collect(Collectors.groupingBy(SecondDistributionTaskManagement::getJobNumber));
        //获取单项绩效
        List<SecondDistributionTaskSingle> secondDistributionTaskSingles = new SecondDistributionTaskSingle().selectList(new LambdaQueryWrapper<SecondDistributionTaskSingle>()
                .eq(SecondDistributionTaskSingle::getTaskUnitRelateId, secondTaskDistributionDto.getRelateTaskId())
                .eq(SecondDistributionTaskSingle::getPlanId, relatePlanId)
                .eq(SecondDistributionTaskSingle::getUnitId, secondTaskDistributionDto.getUnitId())
                .select(SecondDistributionTaskSingle::getJobNumber, SecondDistributionTaskSingle::getName, SecondDistributionTaskSingle::getAmount, SecondDistributionTaskSingle::getIndexId));
        jobNameMap.putAll(secondDistributionTaskSingles.stream().collect(Collectors.toMap(SecondDistributionTaskSingle::getJobNumber, SecondDistributionTaskSingle::getName, (k1, k2) -> k1)));
        //以jobNumber分组
        Map<Long, List<SecondDistributionTaskSingle>> singleMap = secondDistributionTaskSingles.stream().collect(Collectors.groupingBy(SecondDistributionTaskSingle::getJobNumber));
        //获取工作量绩效
        List<SecondDistributionTaskWorkload> secondDistributionTaskWorkloads = new SecondDistributionTaskWorkload().selectList(new LambdaQueryWrapper<SecondDistributionTaskWorkload>()
                .eq(SecondDistributionTaskWorkload::getTaskUnitRelateId, secondTaskDistributionDto.getRelateTaskId())
                .eq(SecondDistributionTaskWorkload::getPlanId, relatePlanId)
                .eq(SecondDistributionTaskWorkload::getUnitId, secondTaskDistributionDto.getUnitId())
                .select(SecondDistributionTaskWorkload::getJobNumber, SecondDistributionTaskWorkload::getName, SecondDistributionTaskWorkload::getAmount, SecondDistributionTaskWorkload::getIndexId));
        jobNameMap.putAll(secondDistributionTaskWorkloads.stream().collect(Collectors.toMap(SecondDistributionTaskWorkload::getJobNumber, SecondDistributionTaskWorkload::getName, (k1, k2) -> k1)));
        //以jobNumber分组
        Map<Long, List<SecondDistributionTaskWorkload>> workloadMap = secondDistributionTaskWorkloads.stream().collect(Collectors.groupingBy(SecondDistributionTaskWorkload::getJobNumber));
        //获取个人职称绩效
        List<SecondDistributionTaskIndividualPost> secondDistributionTaskIndividualPosts = new SecondDistributionTaskIndividualPost().selectList(new LambdaQueryWrapper<SecondDistributionTaskIndividualPost>()
                .eq(SecondDistributionTaskIndividualPost::getTaskUnitRelateId, secondTaskDistributionDto.getRelateTaskId())
                .eq(SecondDistributionTaskIndividualPost::getPlanId, relatePlanId)
                .eq(SecondDistributionTaskIndividualPost::getUnitId, secondTaskDistributionDto.getUnitId())
                .select(SecondDistributionTaskIndividualPost::getJobNumber, SecondDistributionTaskIndividualPost::getName, SecondDistributionTaskIndividualPost::getAmount, SecondDistributionTaskIndividualPost::getIndexId));
        jobNameMap.putAll(secondDistributionTaskIndividualPosts.stream().collect(Collectors.toMap(SecondDistributionTaskIndividualPost::getJobNumber, SecondDistributionTaskIndividualPost::getName, (k1, k2) -> k1)));
        //以jobNumber分组
        Map<Long, List<SecondDistributionTaskIndividualPost>> individualPostMap = secondDistributionTaskIndividualPosts.stream().collect(Collectors.groupingBy(SecondDistributionTaskIndividualPost::getJobNumber));
        //获取平均绩效
        List<SecondDistributionTaskAverage> secondDistributionTaskAverages = new SecondDistributionTaskAverage().selectList(new LambdaQueryWrapper<SecondDistributionTaskAverage>()
                .eq(SecondDistributionTaskAverage::getTaskUnitRelateId, secondTaskDistributionDto.getRelateTaskId())
                .eq(SecondDistributionTaskAverage::getPlanId, relatePlanId)
                .eq(SecondDistributionTaskAverage::getUnitId, secondTaskDistributionDto.getUnitId())
                .select(SecondDistributionTaskAverage::getJobNumber, SecondDistributionTaskAverage::getName, SecondDistributionTaskAverage::getAmount, SecondDistributionTaskAverage::getIndexId));
        jobNameMap.putAll(secondDistributionTaskAverages.stream().collect(Collectors.toMap(SecondDistributionTaskAverage::getJobNumber, SecondDistributionTaskAverage::getName, (k1, k2) -> k1)));
        //以jobNumber分组
        Map<Long, List<SecondDistributionTaskAverage>> averageMap = secondDistributionTaskAverages.stream().collect(Collectors.groupingBy(SecondDistributionTaskAverage::getJobNumber));

        //固定列
        List<String> titles = new ArrayList<>(baseTitleList);
        //动态列
        indexMap.forEach((indexId, indexName) -> titles.add(String.valueOf(indexId)));

        return fillData(indexMap, jobNameMap, managementMap, singleMap, workloadMap, individualPostMap, averageMap, titles);

    }

    private List<SecondDistributionUnitUserDetail> fillData(Map<Long, String> indexMap, Map<Long, String> jobNameMap, Map<Long, List<SecondDistributionTaskManagement>> managementMap, Map<Long, List<SecondDistributionTaskSingle>> singleMap, Map<Long, List<SecondDistributionTaskWorkload>> workloadMap, Map<Long, List<SecondDistributionTaskIndividualPost>> individualPostMap, Map<Long, List<SecondDistributionTaskAverage>> averageMap, List<String> titles) {
        List<SecondDistributionUnitUserDetail> records = new ArrayList<>(jobNameMap.size());
        //遍历titles
        titles.forEach(title -> {
            SecondDistributionUnitUserDetail secondDistributionUnitUserDetail = new SecondDistributionUnitUserDetail();
            if (baseTitleList.contains(title)) {
                secondDistributionUnitUserDetail.setTitle(title);
            } else {
                JSONObject jsonObject = JSONUtil.parseObj(indexMap.get(Long.valueOf(title)));
                if (Objects.nonNull(jsonObject.get("label"))) {
                    secondDistributionUnitUserDetail.setTitle(jsonObject.get("label").toString());
                }
            }
            List<String> data = new ArrayList<>();
            AtomicReference<String> value = new AtomicReference<>("");
            //遍历jobMap
            jobNameMap.forEach((jobNumber, name) -> {
                if (!baseTitleList.contains(title)) {
                    long indexId = Long.parseLong(title);
                    JSONObject jsonObject = JSONUtil.parseObj(indexMap.get(Long.valueOf(title)));
                    if (Objects.nonNull(jsonObject.get("label"))) {
                        String label = jsonObject.get("label").toString();
                        if (label.contains("管理绩效")) {
                            List<SecondDistributionTaskManagement> managementList = managementMap.get(jobNumber);
                            if (CollUtil.isNotEmpty(managementList)) {
                                //根据indexId sum总值
                                BigDecimal amount = managementList.stream().filter(management -> management.getIndexId().equals(indexId)).map(SecondDistributionTaskManagement::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
                                value.set(amount.toString());
                            }
                        } else if (label.contains("单项绩效")) {
                            List<SecondDistributionTaskSingle> singleList = singleMap.get(jobNumber);
                            if (CollUtil.isNotEmpty(singleList)) {
                                //根据indexId sum总值
                                BigDecimal amount = singleList.stream().filter(single -> single.getIndexId().equals(indexId)).map(SecondDistributionTaskSingle::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
                                value.set(amount.toString());
                            }
                        } else if (label.contains("工作量绩效")) {
                            List<SecondDistributionTaskWorkload> workloadList = workloadMap.get(jobNumber);
                            if (CollUtil.isNotEmpty(workloadList)) {
                                //根据indexId sum总值
                                BigDecimal amount = workloadList.stream().filter(workload -> workload.getIndexId().equals(indexId)).map(SecondDistributionTaskWorkload::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
                                value.set(amount.toString());
                            }
                        } else if (label.contains("个人职称绩效")) {
                            List<SecondDistributionTaskIndividualPost> individualPostList = individualPostMap.get(jobNumber);
                            if (CollUtil.isNotEmpty(individualPostList)) {
                                //根据indexId sum总值
                                BigDecimal amount = individualPostList.stream().filter(individualPost -> individualPost.getIndexId().equals(indexId)).map(SecondDistributionTaskIndividualPost::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
                                value.set(amount.toString());
                            }
                        } else if (label.contains("平均绩效")) {
                            List<SecondDistributionTaskAverage> averageList = averageMap.get(jobNumber);
                            if (CollUtil.isNotEmpty(averageList)) {
                                //根据indexId sum总值
                                BigDecimal amount = averageList.stream().filter(average -> average.getIndexId().equals(indexId)).map(SecondDistributionTaskAverage::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
                                value.set(amount.toString());
                            }
                        }
                    }
                } else {
                    switch (title) {
                        case "工号":
                            value.set(String.valueOf(jobNumber));
                            break;
                        case "姓名":
                            value.set(name);
                            break;

                        case "二次分配总绩效金额":
                            //获取所有与当前工号匹配的数据的汇总和
                            BigDecimal totalAmount = BigDecimal.ZERO;
                            List<SecondDistributionTaskManagement> managementList = managementMap.get(jobNumber);
                            if (CollUtil.isNotEmpty(managementList)) {
                                totalAmount = totalAmount.add(managementList.stream().map(SecondDistributionTaskManagement::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add));
                            }
                            List<SecondDistributionTaskSingle> singleList = singleMap.get(jobNumber);
                            if (CollUtil.isNotEmpty(singleList)) {
                                totalAmount = totalAmount.add(singleList.stream().map(SecondDistributionTaskSingle::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add));
                            }
                            List<SecondDistributionTaskWorkload> workloadList = workloadMap.get(jobNumber);
                            if (CollUtil.isNotEmpty(workloadList)) {
                                totalAmount = totalAmount.add(workloadList.stream().map(SecondDistributionTaskWorkload::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add));
                            }
                            List<SecondDistributionTaskIndividualPost> individualPostList = individualPostMap.get(jobNumber);
                            if (CollUtil.isNotEmpty(individualPostList)) {
                                totalAmount = totalAmount.add(individualPostList.stream().map(SecondDistributionTaskIndividualPost::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add));
                            }
                            List<SecondDistributionTaskAverage> averageList = averageMap.get(jobNumber);
                            if (CollUtil.isNotEmpty(averageList)) {
                                totalAmount = totalAmount.add(averageList.stream().map(SecondDistributionTaskAverage::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add));
                            }
                            value.set(totalAmount.toString());
                            break;
                        default:
                            value.set("");
                            break;
                    }
                }

            });
        });
        return records;
    }
}