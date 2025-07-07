package com.hscloud.hs.cost.account.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hscloud.hs.cost.account.constant.CacheConstants;
import com.hscloud.hs.cost.account.constant.enums.TaskTypeEnum;
import com.hscloud.hs.cost.account.constant.enums.UnitMapEnum;
import com.hscloud.hs.cost.account.mapper.*;
import com.hscloud.hs.cost.account.model.dto.CostAccountTaskCalculateProcessDto;
import com.hscloud.hs.cost.account.model.dto.CostAccountTaskCalculateProcessNewDto;
import com.hscloud.hs.cost.account.model.dto.TaskResultQueryDto;
import com.hscloud.hs.cost.account.model.dto.TaskResultQueryNewDto;
import com.hscloud.hs.cost.account.model.entity.*;
import com.hscloud.hs.cost.account.model.pojo.*;
import com.hscloud.hs.cost.account.model.vo.*;
import com.hscloud.hs.cost.account.service.CostAccountTaskService;
import com.hscloud.hs.cost.account.service.CostTaskExecuteResultService;
import com.hscloud.hs.cost.account.utils.ExpressionCheckHelper;
import com.hscloud.hs.cost.account.utils.LocalCacheUtils;
import com.hscloud.hs.cost.account.utils.SqlUtil;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;


/**
 * @author Admin
 */
@Service
@RequiredArgsConstructor
public class CostTaskExecuteResultServiceImpl extends ServiceImpl<CostTaskExecuteResultMapper, CostTaskExecuteResult> implements CostTaskExecuteResultService {


    private final CostTaskExecuteResultIndexMapper costTaskExecuteResultIndexMapper;

    private final CostTaskExecuteResultItemMapper costTaskExecuteResultItemMapper;

    private final SqlUtil sqlUtil;


    private final StringRedisTemplate redisTemplate;

    private final LocalCacheUtils cacheUtils;

    private final AdsIncomePerformanceScoreDocMapper scoreDocMapper;

    private final AdsIncomePerformanceScoreNurMapper scoreNurMapper;

    private final AdsIncomePerformanceScoreDocHeadMapper scoreDocHeadMapper;

    private final AdsIncomePerformanceScoreNurHeadMapper scoreNurHeadMapper;

    private final CostAccountPlanConfigFormulaMapper configFormulaMapper;

    private final CostAccountTaskService taskService;

    private final Gson gson;

//    private final ExecutorService executorService = new ThreadPoolExecutor(8, 8,
//            0L, TimeUnit.MILLISECONDS,
//            new LinkedBlockingQueue<>(1), r -> {
//        Thread thread = new Thread(r);
//        thread.setName("task-calculate" + thread.getId());
//        return thread;
//    }, new ThreadPoolExecutor.CallerRunsPolicy());


    /**
     * 返回核算任务结果页数据
     *
     * @param taskResultQueryDto 查询条件
     * @return
     */
    @Override
    public CostAccountTaskResultDetailVo listResult(TaskResultQueryDto taskResultQueryDto) {
        //根据任务id获取到核算单元集
        Long taskId = taskResultQueryDto.getTaskId();

        String redisKey = CacheConstants.COST_TASK_RESULT + taskId;
        String resultJson = redisTemplate.opsForValue().get(redisKey);

        //定义返回vo
        CostAccountTaskResultDetailVo costAccountTaskResultDetailVo = new CostAccountTaskResultDetailVo();
        //获取任务
        CostAccountTask costAccountTask = new CostAccountTask().selectById(taskId);

        if (resultJson != null) {
            //根据查询出的数据进行分页处理
            CostAccountTaskResultDetailVo redisResultVo = JSON.parseObject(resultJson, CostAccountTaskResultDetailVo.class);
            costAccountTaskResultDetailVo.setTaskName(redisResultVo.getTaskName());
            costAccountTaskResultDetailVo.setAccountStartTime(redisResultVo.getAccountStartTime());
            costAccountTaskResultDetailVo.setAccountEndTime(redisResultVo.getAccountEndTime());
            costAccountTaskResultDetailVo.setWholeAccountInfo(redisResultVo.getWholeAccountInfo());
            Page<UnitAccountInfo> unitAccountInfoPage = new Page<>(taskResultQueryDto.getCurrent(), taskResultQueryDto.getSize());
            List<UnitAccountInfo> records = redisResultVo.getUnitAccountInfoList().getRecords();
            //条件查询--核算单元
            if (taskResultQueryDto.getAccountUnitId() != null) {
                List<Long> unitIdList = Arrays.stream(taskResultQueryDto.getAccountUnitId().split(",")).map(Long::valueOf).collect(Collectors.toList());
                records = records.stream()
                        .filter(unitAccountInfo -> unitIdList.contains(unitAccountInfo.getUnitId()))
                        .collect(Collectors.toList());
            }
            //条件查询--核算分组
            if (taskResultQueryDto.getAccountGroupId() != null) {
                records = records.stream()
                        .filter(unitAccountInfo -> unitAccountInfo.getGroupId().equals(taskResultQueryDto.getAccountGroupId()))
                        .collect(Collectors.toList());
            }
            int startIndex = (int) ((taskResultQueryDto.getCurrent() - 1) * taskResultQueryDto.getSize());
            int endIndex = (int) Math.min(startIndex + taskResultQueryDto.getSize(), records.size());
            unitAccountInfoPage.setTotal(records.size());
            unitAccountInfoPage.setRecords(records.subList(startIndex, endIndex));
            costAccountTaskResultDetailVo.setUnitAccountInfoList(unitAccountInfoPage);

            return costAccountTaskResultDetailVo;
        }
        //总的计算结果
        WholeAccountInfo wholeAccountInfo = new WholeAccountInfo();

        Page<UnitAccountInfo> unitAccountInfoPage = new Page<>(taskResultQueryDto.getCurrent(), taskResultQueryDto.getSize());
        //核算单元核算信息
        List<UnitAccountInfo> unitAccountInfoList = new ArrayList<>();
        //根据任务id获取总核算值
        BigDecimal totalCost = baseMapper.getTotalCostSum(taskId);
        if (costAccountTask != null) {
            //填充任务名称和核算周期
            costAccountTaskResultDetailVo.setTaskName(costAccountTask.getAccountTaskName());
            costAccountTaskResultDetailVo.setAccountStartTime(costAccountTask.getAccountStartTime());
            costAccountTaskResultDetailVo.setAccountEndTime(costAccountTask.getAccountEndTime());

            List<CostTaskExecuteResultIndex> wholeResultIndexList = new CostTaskExecuteResultIndex().selectList(new LambdaQueryWrapper<CostTaskExecuteResultIndex>()
                    .eq(CostTaskExecuteResultIndex::getTaskId, taskId)
                    .eq(CostTaskExecuteResultIndex::getParentId, 0)
                    .select(CostTaskExecuteResultIndex::getIndexId, CostTaskExecuteResultIndex::getIndexName)
                    .groupBy(CostTaskExecuteResultIndex::getIndexId, CostTaskExecuteResultIndex::getIndexName));

            //查询总的核算信息
            List<AccountIndexCalculateInfo> accountIndexCalculateInfoList = totalRecursion(taskId, null, wholeResultIndexList);
            //封装总的计算结果
            wholeAccountInfo.setTotalCost(totalCost);
            wholeAccountInfo.setAccountIndexCalculateInfoList(accountIndexCalculateInfoList);

            //        Page<CostTaskExecuteResult> page = new Page<>(taskResultQueryDto.getCurrent(), taskResultQueryDto.getSize());
            //        LambdaQueryWrapper<CostTaskExecuteResult> queryWrapper = new LambdaQueryWrapper<CostTaskExecuteResult>()
            //                .eq(CostTaskExecuteResult::getTaskId, taskId)
            //                .eq(taskResultQueryDto.getAccountUnitId() != null, CostTaskExecuteResult::getUnitId, taskResultQueryDto.getAccountUnitId())
            //                .eq(!"null".equals(taskResultQueryDto.getAccountGroupId()) && !"".equals(taskResultQueryDto.getAccountGroupId()) && taskResultQueryDto.getAccountGroupId() != null, CostTaskExecuteResult::getGroupId, taskResultQueryDto.getAccountGroupId());
            //
            //        List<CostTaskExecuteResult> taskExecuteResult = new CostTaskExecuteResult().selectPage(page, queryWrapper).getRecords();
            List<CostTaskExecuteResult> taskExecuteResult = new CostTaskExecuteResult().selectList(
                    new LambdaQueryWrapper<CostTaskExecuteResult>()
                            .eq(CostTaskExecuteResult::getTaskId, taskId)
                            .eq(taskResultQueryDto.getAccountUnitId() != null, CostTaskExecuteResult::getUnitId, taskResultQueryDto.getAccountUnitId())
                            .eq(!"null".equals(taskResultQueryDto.getAccountGroupId()) && !"".equals(taskResultQueryDto.getAccountGroupId()) && taskResultQueryDto.getAccountGroupId() != null, CostTaskExecuteResult::getGroupId, taskResultQueryDto.getAccountGroupId()));
            if (CollUtil.isNotEmpty(taskExecuteResult)) {
                unitAccountInfoList = taskExecuteResult.parallelStream()
                        .map(costTaskExecuteResult -> {
                            Long unitId = costTaskExecuteResult.getUnitId();
                            UnitAccountInfo unitAccountInfo = new UnitAccountInfo();
                            unitAccountInfo.setUnitId(unitId);
                            unitAccountInfo.setUnitName(costTaskExecuteResult.getUnitName());
                            unitAccountInfo.setGroupId(costTaskExecuteResult.getGroupId());
                            unitAccountInfo.setGroupName(costTaskExecuteResult.getGroupName());
                            unitAccountInfo.setTotalCost(costTaskExecuteResult.getTotalCount());

                            List<CostTaskExecuteResultIndex> resultIndexList = costTaskExecuteResultIndexMapper.getIndexIds(taskId, unitId);
                            List<CalculateInfo> calculateInfo = detailRecursion(taskId, unitId, resultIndexList);
                            unitAccountInfo.setCalculateInfo(calculateInfo);

                            return unitAccountInfo;
                        })
                        .collect(Collectors.toList());
            }
            //封装返回分页对象
            unitAccountInfoPage.setRecords(unitAccountInfoList);
            unitAccountInfoPage.setTotal(unitAccountInfoList.size());
            //封装核算任务结果详情Vo
            costAccountTaskResultDetailVo.setWholeAccountInfo(wholeAccountInfo);
            costAccountTaskResultDetailVo.setUnitAccountInfoList(unitAccountInfoPage);
        }
        return costAccountTaskResultDetailVo;
    }


    private List<CalculateInfo> detailRecursion(Long taskId, Long unitId, List<CostTaskExecuteResultIndex> resultIndexList) {
        List<CalculateInfo> calculateInfoList = new ArrayList<>();
        for (CostTaskExecuteResultIndex costTaskExecuteResultIndex : resultIndexList) {
            CalculateInfo indexCalculateInfo = new CalculateInfo();
            List<CalculateInfo> child = new ArrayList<>();
            // 封装核算指标计算信息
            Long indexId = costTaskExecuteResultIndex.getIndexId();// 核算指标id
            String indexName = costTaskExecuteResultIndex.getIndexName();// 核算指标名称
            BigDecimal indexCount = costTaskExecuteResultIndex.getIndexCount();// 指标核算值
//            Gson gson = new Gson();
            IndexFormulaObject indexFormulaObject = gson.fromJson(costTaskExecuteResultIndex.getCalculateFormulaDesc(), IndexFormulaObject.class);
            // 取出核算指标的信息
            indexCalculateInfo.setTotalCost(indexCount);
            indexCalculateInfo.setName(indexName);
            indexCalculateInfo.setType("index");
            indexCalculateInfo.setId(indexId);
            indexCalculateInfo.setConfigKey(indexFormulaObject.getConfigKey());
            SharedCost indexSharedCost = new SharedCost();

            // 查询该核算指标下的子集指标
            List<CostTaskExecuteResultIndex> resultIndexChildrenList = costTaskExecuteResultIndexMapper.getIndexListChildren(taskId, unitId, indexId);
            if (!resultIndexChildrenList.isEmpty()) {
                // 递归调用
                List<CalculateInfo> children = detailRecursion(taskId, unitId, resultIndexChildrenList);
                child.addAll(children);
            }

            // 获取核算项的计算结果
            List<CalculateInfo> itemCalculateInfoList = new ArrayList<>();
            String resultItemIds = costTaskExecuteResultIndexMapper.getItems(taskId, unitId, indexId, costTaskExecuteResultIndex.getParentId());
            if (StrUtil.isNotBlank(resultItemIds)) {
                BigDecimal indexDivideCountAfter = BigDecimal.ZERO;
                BigDecimal indexBedBorrowCountAfter = BigDecimal.ZERO;
                BigDecimal indexEndemicAreaCountAfter = BigDecimal.ZERO;
                BigDecimal indexOutpatientShardCountAfter = BigDecimal.ZERO;

                for (String resultItemId : ExpressionCheckHelper.getIds(resultItemIds)) {
                    // 封装核算项计算信息
                    if (resultItemId != null && !resultItemId.isEmpty()) {
                        if (redisTemplate.opsForHash().get(CacheConstants.COST_TASK_EXECUTE_RESULT_ITEM, resultItemId.toString()) != null) {
                            CostTaskExecuteResultItem resultItem = gson.fromJson(redisTemplate.opsForHash().get(CacheConstants.COST_TASK_EXECUTE_RESULT_ITEM, resultItemId.toString()).toString(), CostTaskExecuteResultItem.class);

//                        }
////                        if (cacheUtils.getCostTaskExecuteResultItem(Long.valueOf(resultItemId)) == null) {
////                            cacheUtils.initCostTaskExecuteResultItem();
////                        }
//////                        CostTaskExecuteResultItem resultItem = new CostTaskExecuteResultItem().selectById(Long.valueOf(resultItemId));
////                        CostTaskExecuteResultItem resultItem = cacheUtils.getCostTaskExecuteResultItem(Long.valueOf(resultItemId));
//                        if (resultItem != null) {
                            LambdaQueryWrapper<CostTaskExecuteResultRule> queryWrapper = new LambdaQueryWrapper<CostTaskExecuteResultRule>().eq(CostTaskExecuteResultRule::getTaskId, taskId)
                                    .eq(CostTaskExecuteResultRule::getUnitId, unitId)
                                    .eq(CostTaskExecuteResultRule::getItemId, resultItem.getId());
                            if (resultItem.getPath() != null) {
                                queryWrapper.eq(CostTaskExecuteResultRule::getPath, resultItem.getPath());
                            } else {
                                queryWrapper.isNull(CostTaskExecuteResultRule::getPath).eq(CostTaskExecuteResultRule::getIndexId, resultItem.getIndexId());
                            }
                            CostTaskExecuteResultRule costTaskExecuteResultRule = new CostTaskExecuteResultRule().selectOne(queryWrapper);
                            // 核算规则值
                            BigDecimal ruleCount = costTaskExecuteResultRule.getRuleCount();
                            String itemName = resultItem.getItemName();// 核算项名称
                            BigDecimal calculateCount = resultItem.getCalculateCount().multiply(ruleCount);// 本单元核算值
                            // 将核算项的其他分摊封装到核算指标中
                            // 取出医护分摊的值
                            String divideType = "yh";
                            indexDivideCountAfter = indexDivideCountAfter.add(getCountAfter(taskId, unitId, resultItem, divideType));
                            // 取出门诊共用分摊数据
                            String outpatientShard = "mz";
                            indexOutpatientShardCountAfter = indexOutpatientShardCountAfter.add(getCountAfter(taskId, unitId, resultItem, outpatientShard));
                            // 取出借床分摊数据
                            String bedBorrow = "jc";
                            indexBedBorrowCountAfter = indexBedBorrowCountAfter.add(getCountAfter(taskId, unitId, resultItem, bedBorrow));
                            // 获取病区分摊数据
                            String endemicArea = "bq";
                            indexEndemicAreaCountAfter = indexEndemicAreaCountAfter.add(getCountAfter(taskId, unitId, resultItem, endemicArea));
                            // 核算项的核算信息
                            CalculateInfo itemCalculateInfo = new CalculateInfo();
                            itemCalculateInfo.setTotalCost(calculateCount);
                            itemCalculateInfo.setName(itemName);
                            itemCalculateInfo.setType("item");
                            itemCalculateInfo.setId(resultItem.getItemId());
                            itemCalculateInfoList.add(itemCalculateInfo);
                        }
                    }
                }
                // 核算指标包含的分摊成本对象
                indexSharedCost.setDivideCount(indexDivideCountAfter);
                indexSharedCost.setBedBorrowCount(indexBedBorrowCountAfter);
                indexSharedCost.setEndemicAreaCount(indexEndemicAreaCountAfter);
                indexSharedCost.setOutpatientShardCount(indexOutpatientShardCountAfter);
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


    /**
     * 用来核算指标的递归查询
     *
     * @param
     * @param taskId
     * @param unitId
     * @param resultIndexList
     */
    private List<AccountIndexCalculateInfo> totalRecursion(Long taskId, Long unitId, List<CostTaskExecuteResultIndex> resultIndexList) {
        List<AccountIndexCalculateInfo> accountIndexCalculateInfoList = new ArrayList<>();
        for (CostTaskExecuteResultIndex costTaskExecuteResultIndex : resultIndexList) {
            Long indexId = costTaskExecuteResultIndex.getIndexId();//核算指标id
            BigDecimal indexCount = costTaskExecuteResultIndexMapper.getIndexCount(taskId, indexId);//指标核算值
            //封装核算指标计算信息
            AccountIndexCalculateInfo accountIndexCalculateInfo = new AccountIndexCalculateInfo();
            String indexName = costTaskExecuteResultIndex.getIndexName();//核算指标名称
            accountIndexCalculateInfo.setIndexName(indexName);
            accountIndexCalculateInfo.setIndexCalculateValue(indexCount);
            //包含子集指标,封装到返回的集合中
            List<AccountItemCalculateInfo> accountItemCalculateInfoList = new ArrayList<>();
            //查询该核算指标下的子集指标，如果该核算指标下存在子集指标，则查询子集指标的信息
            List<CostTaskExecuteResultIndex> resultIndexChildrenList = costTaskExecuteResultIndexMapper.getIndexListChildren(taskId, unitId, indexId);
            if (CollUtil.isNotEmpty(resultIndexChildrenList)) {
                for (CostTaskExecuteResultIndex taskExecuteResultIndex : resultIndexChildrenList) {
                    //查询指标的总核算值并封装起来
                    AccountItemCalculateInfo accountItemCalculateInfo = new AccountItemCalculateInfo();
                    accountItemCalculateInfo.setBizType("index");
                    accountItemCalculateInfo.setBizName(taskExecuteResultIndex.getIndexName());
                    accountItemCalculateInfo.setBizCalculateValue(taskExecuteResultIndex.getIndexCount());
                    accountItemCalculateInfoList.add(accountItemCalculateInfo);
                }
            }
            //取出核算指标集中包含的核算项id
            List<CostTaskExecuteResultIndex> costTaskExecuteResultIndices = new CostTaskExecuteResultIndex().selectList(
                    new LambdaQueryWrapper<CostTaskExecuteResultIndex>().eq(CostTaskExecuteResultIndex::getTaskId, taskId)
                            .eq(CostTaskExecuteResultIndex::getIndexId, indexId)
                            .eq(CostTaskExecuteResultIndex::getParentId, 0));
            //获取去重后的itemIds
            Set<String> resultItems = costTaskExecuteResultIndices.stream()
                    .map(CostTaskExecuteResultIndex::getItems)
                    .flatMap(items -> Arrays.stream(items.substring(1, items.length() - 1).split(",")))
                    .collect(Collectors.toSet());
            Set<Long> itemIds = new CostTaskExecuteResultItem().selectList(Wrappers.<CostTaskExecuteResultItem>lambdaQuery()
                    .in(CostTaskExecuteResultItem::getId, resultItems)).stream().map(CostTaskExecuteResultItem::getItemId).collect(Collectors.toSet());
            for (Long itemId : itemIds) {
                AccountItemCalculateInfo accountItemCalculateInfo = new AccountItemCalculateInfo();
                BigDecimal itemCount = costTaskExecuteResultItemMapper.getItemCount(taskId, indexId, itemId);
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

    /**
     * 获取分摊值
     *
     * @param taskId
     * @param unitId
     * @param resultItem
     * @param divideType
     * @return
     */
    private BigDecimal getCountAfter(Long taskId, Long unitId, CostTaskExecuteResultItem resultItem, String divideType) {
        BigDecimal countAfter = BigDecimal.ZERO;
        List<CostTaskExecuteResultExtra> outpatientShardCountResultExtra = new CostTaskExecuteResultExtra().selectList(new LambdaQueryWrapper<CostTaskExecuteResultExtra>()
                .eq(CostTaskExecuteResultExtra::getTaskId, taskId)
                .eq(CostTaskExecuteResultExtra::getDivideUnitId, unitId)
                .eq(CostTaskExecuteResultExtra::getDivideType, divideType)
        );
        for (CostTaskExecuteResultExtra costTaskExecuteResultExtra : outpatientShardCountResultExtra) {
            CostTaskExecuteResultItem costTaskExecuteResultItem = gson.fromJson(redisTemplate.opsForHash().get(CacheConstants.COST_TASK_EXECUTE_RESULT_ITEM, costTaskExecuteResultExtra.getItemId().toString()).toString(), CostTaskExecuteResultItem.class);
            if (costTaskExecuteResultItem.getItemId().equals(resultItem.getItemId())) {
                BigDecimal divideCountAfter1 = costTaskExecuteResultExtra.getDivideCountAfter();
                if (divideCountAfter1 != null) {
                    countAfter = countAfter.add(divideCountAfter1);
                }
            }
        }
        return countAfter;
    }


    //获取结果列表--数据小组数据
    @Override
    public CostAccountTaskResultDetailVo newListResult(TaskResultQueryDto taskResultQueryDto) {
        CostAccountTaskResultDetailVo costAccountTaskResultDetailVo = new CostAccountTaskResultDetailVo();
        //判断核算类型是否是成本绩效
        DistributionTaskGroup taskGroup = new DistributionTaskGroup().selectById(taskResultQueryDto.getTaskGroupId());
        //todo 类型判断
        if (!"TT001".equals(new JSONObject(taskGroup.getType()).getStr("value"))) {
            costAccountTaskResultDetailVo = getOtherTaskResult(taskResultQueryDto);
        }
        //绩效核算
        else if ("TT004".equals(new JSONObject(taskGroup.getType()).getStr("value"))) {
            costAccountTaskResultDetailVo = getPerformanceResult(taskResultQueryDto);
        } else {
            costAccountTaskResultDetailVo = getCostResult(taskResultQueryDto);
        }
        return costAccountTaskResultDetailVo;
    }

    /**
     * 一次分配结果展示(数据小组数据)
     *
     * @param dto
     * @return
     */
    @Override
    public CostAccountTaskResultDetailNewVo getDistributionList(TaskResultQueryNewDto dto) {
        CostAccountTaskResultDetailNewVo vo = new CostAccountTaskResultDetailNewVo();
        //根据名称去拿到核算任务类型
        //护理收入绩效
        if (TaskTypeEnum.NURSE_REVENUE.getDescription().equals(dto.getAccountTaskName())) {
            vo = getNurseRevenueList(dto);
        }
        //医生医技收入绩效
        else if (TaskTypeEnum.DOCTOR_TECH_REVENUE.getDescription().equals(dto.getAccountTaskName())) {
            vo = getDoctorTechRevenueList(dto);
        }
        //临床医生医技绩效
        else if (TaskTypeEnum.CLINICAL_DOCTOR_TECH_PERFORMANCE.getDescription().equals(dto.getAccountTaskName())) {
            vo = clinicalDoctorTechPerformanceList(dto);
        }
        //临床护士绩效
        else if (TaskTypeEnum.CLINICAL_NURSE_PERFORMANCE.getDescription().equals(dto.getAccountTaskName())) {
            vo = clinicalNursePerformanceList(dto);
        }
        //医生医技业绩绩效
        else if (TaskTypeEnum.DOCTOR_TECH_ACHIEVEMENT.getDescription().equals(dto.getAccountTaskName())) {
            vo = doctorTechAchievementList(dto);
        }
        //护理业绩绩效
        else if (TaskTypeEnum.NURSE_ACHIEVEMENT.getDescription().equals(dto.getAccountTaskName())) {
            vo = nurseAchievementList(dto);
        }
        //医院奖罚明细
        else if (TaskTypeEnum.HOSPITAL_REWARD_PUNISHMENT_DETAIL.getDescription().equals(dto.getAccountTaskName())) {
            vo = hospitalRewardPunishmentDetaillist(dto);
        }
        //医生医技工作量绩效
        else if (TaskTypeEnum.DOCTOR_TECH_WORKLOAD.getDescription().equals(dto.getAccountTaskName())) {
            vo = doctorTechWorkloadList(dto);
        }
        //护理工作量绩效
        else if (TaskTypeEnum.NURSE_WORKLOAD.getDescription().equals(dto.getAccountTaskName())) {
            vo = nurseWorkloadList(dto);
        } else if (TaskTypeEnum.DEPARTMENT_HEAD_PERFORMANCE.getDescription().equals(dto.getAccountTaskName())) {
            //科主任绩效
            vo = getDepartmentHeadPerformanceList(dto);
        } else if (TaskTypeEnum.NURSE_CHIEF_PERFORMANCE.getDescription().equals(dto.getAccountTaskName())) {
            //护士长绩效
            vo = getNurseChiefPerformanceList(dto);
        } else if (TaskTypeEnum.ADMIN_MIDDLE_HIGH_PERFORMANCE.getDescription().equals(dto.getAccountTaskName())) {
            //行政中高层
            vo = getAdminMiddleHighPerformanceList(dto);
        } else if (TaskTypeEnum.ADMIN_GENERAL_PERFORMANCE.getDescription().equals(dto.getAccountTaskName())) {
            //行政普通职工绩效
            vo = getAdminGeneralPerformanceList(dto);
        } else if (TaskTypeEnum.ADMIN_NON_STAFF_PERFORMANCE.getDescription().equals(dto.getAccountTaskName())) {
            //行政编外绩效
            vo = getAdminNonStaffPerformanceList(dto);
        }
        //其他核算单元绩效
        else if (TaskTypeEnum.OTHER_ACCOUNTING_UNIT_PERFORMANCE.getDescription().equals(dto.getAccountTaskName())) {
            vo = otherAccountingUnitPerformanceList(dto);
        }
        //成本绩效
        else if (TaskTypeEnum.COST_PERFORMANCE.getDescription().equals(dto.getAccountTaskName())) {
            vo = costPerformanceList(dto);
        }
        return vo;
    }

    /**
     * 成本绩效
     *
     * @param dto
     * @return
     */
    private CostAccountTaskResultDetailNewVo costPerformanceList(TaskResultQueryNewDto dto) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMM");
        CostAccountTaskResultDetailNewVo vo = new CostAccountTaskResultDetailNewVo();
        //获取核算方案配置的指标
        CostAccountTaskNew costAccountTaskNew = new CostAccountTaskNew().selectById(dto.getAccountTaskId());
        final LocalDateTime accountStartTime = costAccountTaskNew.getAccountStartTime();
        String detailDim = accountStartTime.format(formatter);
        String redisKey = CacheConstants.COST_TASK_RESULT + dto.getAccountTaskId() + ":" + dto.getAccountTaskGroupId();
        String resultJson = redisTemplate.opsForValue().get(redisKey);
        if (resultJson != null) {
            // 如果结果存在于Redis中，则直接使用
            CostAccountTaskResultDetailNewVo redisResultVo = JSON.parseObject(resultJson, CostAccountTaskResultDetailNewVo.class);
            vo.setTaskName(redisResultVo.getTaskName());
            vo.setAccountStartTime(redisResultVo.getAccountStartTime());
            vo.setAccountEndTime(redisResultVo.getAccountEndTime());
            vo.setWholeAccountInfo(redisResultVo.getWholeAccountInfo());

            Object unitAccountInfoList = redisResultVo.getUnitAccountInfoList();
//            com.alibaba.fastjson.JSONObject jsonObject = (com.alibaba.fastjson.JSONObject) unitAccountInfoList;
            JSONArray jsonArray = (JSONArray) unitAccountInfoList;
//            List<UnitAccountInfo> records = (List<UnitAccountInfo>)  unitAccountInfoList;
            List<UnitAccountInfo> records = gson.fromJson(jsonArray.toString(), new TypeToken<List<UnitAccountInfo>>() {
            }.getType());
            //条件查询--核算单元模糊匹配
            Page<UnitAccountInfo> unitAccountInfoPage = getUnitAccountInfoPage(dto, records);
            vo.setUnitAccountInfoList(unitAccountInfoPage);
            return vo;
        } else {
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
            Page<UnitAccountInfo> unitAccountInfoPage = getUnitAccountInfoPage(dto, unitAccountInfoList);
            vo.setUnitAccountInfoList(unitAccountInfoPage);
        }
        return vo;

    }

    /**
     * 将所有数据进行分页查询
     *
     * @param dto
     * @param records
     * @return
     */
    @NotNull
    private Page<UnitAccountInfo> getUnitAccountInfoPage(TaskResultQueryNewDto dto, List<UnitAccountInfo> records) {
        Page<UnitAccountInfo> unitAccountInfoPage = new Page<>(dto.getCurrent(), dto.getSize());

        if (StrUtil.isNotBlank(dto.getAccountUnitName())) {
            records = records.stream()
                    .filter(unitAccountInfo -> unitAccountInfo.getUnitName().contains(dto.getAccountUnitName()))
                    .collect(Collectors.toList());
        }
        if (Objects.nonNull(dto.getAccountUnitId())) {
            records = records.stream()
                    .filter(unitAccountInfo -> unitAccountInfo.getUnitId().equals(dto.getAccountUnitId()))
                    .collect(Collectors.toList());
        }
        //条件查询--核算分组
        if (StrUtil.isNotBlank(dto.getAccountGroupId())) {
            records = records.stream()
                    .filter(unitAccountInfo -> unitAccountInfo.getGroupId().equals(dto.getAccountGroupId()))
                    .collect(Collectors.toList());
        }
        int startIndex = (int) ((dto.getCurrent() - 1) * dto.getSize());
        int endIndex = (int) Math.min(startIndex + dto.getSize(), records.size());
        unitAccountInfoPage.setTotal(records.size());
        unitAccountInfoPage.setRecords(records.subList(startIndex, endIndex));
        return unitAccountInfoPage;
    }

    /**
     * 其他核算单元绩效
     *
     * @param dto
     * @return
     */
    private CostAccountTaskResultDetailNewVo otherAccountingUnitPerformanceList(TaskResultQueryNewDto dto) {
        CostAccountTaskResultDetailNewVo resultDetailNewVo = new CostAccountTaskResultDetailNewVo();
        //查询所有的
        List<AdsIncomePerformanceOther> allList = new AdsIncomePerformanceOther().selectList(new LambdaQueryWrapper<AdsIncomePerformanceOther>()
                .eq(AdsIncomePerformanceOther::getAccountPeriod, dto.getAccountTime())
                .like(StrUtil.isNotBlank(dto.getAccountUnitName()), AdsIncomePerformanceOther::getKs, dto.getAccountUnitName()));
        List<AdsIncomePerformanceOther> deduplicatedList = allList.stream()
                .collect(Collectors.toMap(AdsIncomePerformanceOther::getAccountUnitId, item -> item, (existing, replacement) -> existing))
                .values()
                .stream()
                .collect(Collectors.toList());
        //分页
        Page<OtherAccountingUnitPerformanceVo> page = new Page<>(dto.getCurrent(), dto.getSize());
        List<OtherAccountingUnitPerformanceVo> voList = new ArrayList<>();
        for (AdsIncomePerformanceOther performanceOther : deduplicatedList) {
            OtherAccountingUnitPerformanceVo performanceVo = new OtherAccountingUnitPerformanceVo();
            performanceVo.setAccountUnitId(performanceOther.getAccountUnitId());
            performanceVo.setAccountUnitName(performanceOther.getKs());
            performanceVo.setKhdf(performanceOther.getKhdf());
            performanceVo.setYyjf(performanceOther.getYyjc());
            performanceVo.setTotal(performanceOther.getXj());
            //查询总成本,总收入
            AdsIncomePerformanceScoreDoc performanceScoreDoc = new AdsIncomePerformanceScoreDoc().selectOne(new LambdaQueryWrapper<AdsIncomePerformanceScoreDoc>()
                    .eq(AdsIncomePerformanceScoreDoc::getAccountPeriod, dto.getAccountTime())
                    .eq(AdsIncomePerformanceScoreDoc::getAccountDocId, performanceOther.getAccountUnitId()));
            if (performanceScoreDoc != null) {
                performanceVo.setIncomeTotal(performanceScoreDoc.getIncomeTotal());
                performanceVo.setCostTotal(performanceScoreDoc.getCostTotal());
            }
            for (AdsIncomePerformanceOther other : allList) {
                if (performanceOther.getAccountUnitId().equals(other.getAccountUnitId())) {
                    switch (other.getKhxm()) {
                        case "中药代煎收入":
                            performanceVo.setZyfZydjsr(other.getSl());
                            break;
                        case "膏方人次":
                            performanceVo.setZyfZgl(other.getSl());
                            break;
                        case "中药味数":
                            performanceVo.setZyfZyws(other.getSl());
                            break;
                        case "中药帖数":
                            performanceVo.setZyfZyts(other.getSl());
                            break;
                        case "临床药师会诊":
                            performanceVo.setLcyxLcyshz(other.getSl());
                            break;
                        case "临床药师查房":
                            performanceVo.setLcyxLcyscf(other.getSl());
                            break;
                        case "住院处方打回":
                            performanceVo.setLcyxZycfdh(other.getSl());
                            break;
                        case "用药监护":
                            performanceVo.setLcyxYyjh(other.getSl());
                            break;
                        case "事前审方":
                            performanceVo.setLcyxSqsf(other.getSl());
                            break;
                        case "点评门诊处方":
                            performanceVo.setLcyxDpmzcf(other.getSl());
                            break;
                        case "人工设置处方规则":
                            performanceVo.setLcyxRgszcfgz(other.getSl());
                            break;
                        case "住院病历":
                            performanceVo.setLcyxZybl(other.getSl());
                            break;
                        case "协定方加工":
                            performanceVo.setLfpzsXdfjg(other.getSl());
                            break;
                        case "临时加工":
                            performanceVo.setLfpzsLsjg(other.getSl());
                            break;
                        case "固定绩效":
                            if ("伦理秘书".equals(other.getKs())) {
                                performanceVo.setLlmsGdjx(other.getSl());
                            }
                            if ("制剂室".equals(other.getKs())) {
                                performanceVo.setZjsGdjx(other.getSl());
                            }
                            break;
                        case "服务人次":
                            performanceVo.setYykFwrc(other.getSl());
                            break;
                        case "膏方":
                            performanceVo.setYzmzGf(other.getSl());
                            break;
                        case "住院床日":
                            performanceVo.setXyfZycr(other.getSl());
                            break;
                        case "门诊西药处方量":
                            performanceVo.setXyfMzxycfl(other.getSl());
                            break;
                        case "西药平均绩效":
                            performanceVo.setXyfXypjjx(other.getSl());
                            break;
                        case "基础绩效":
                            performanceVo.setYykJcjx(other.getSl());
                            break;
                        case "门诊非药物治疗开单":
                            performanceVo.setYzmzMzfywzlkd(other.getSl());
                            break;
                        case "门诊非药物治疗执行":
                            performanceVo.setYzmzMzfywzlzx(other.getSl());
                            break;
                        case "临方加工":
                            if ("鄞州门诊".equals(other.getKs())) {
                                performanceVo.setYzmzLfjg(other.getSl());
                            }
                            if ("临方炮制室".equals(other.getKs())) {
                                performanceVo.setLfpzsLsjg(other.getSl());
                            }
                            break;
                        case "门诊诊察":
                            performanceVo.setYzmzMzzc(other.getSl());
                            break;
                        case "门诊中药帖数":
                            performanceVo.setYzmzMzzyts(other.getSl());
                            break;
                        case "退休业绩分":
                            performanceVo.setYzmzTxyjf(other.getSl());
                            break;
                        case "业绩分":
                            performanceVo.setYzmzYjf(other.getSl());
                            break;
                    }
                }
            }
            voList.add(performanceVo);
        }
        page.setRecords(voList);
        //封装总值
        BigDecimal totalSum = voList.stream()
                .map(OtherAccountingUnitPerformanceVo::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        resultDetailNewVo.setTotalCost(totalSum);
        resultDetailNewVo.setUnitAccountInfoList(page);
        return resultDetailNewVo;
    }

    /**
     * 护理工作量绩效
     *
     * @param dto
     * @return
     */
    private CostAccountTaskResultDetailNewVo nurseWorkloadList(TaskResultQueryNewDto dto) {
        CostAccountTaskResultDetailNewVo vo = new CostAccountTaskResultDetailNewVo();
        //封装护理对象信息
        Page<AdsNurWorkloadPerformanceAccounting> page = new Page<>(dto.getCurrent(), dto.getSize());
        //查询返回分页结果
        LambdaQueryWrapper<AdsNurWorkloadPerformanceAccounting> wrapper = new LambdaQueryWrapper<AdsNurWorkloadPerformanceAccounting>()
                .eq(AdsNurWorkloadPerformanceAccounting::getAccountPeriod, dto.getAccountTime())
                .like(StrUtil.isNotBlank(dto.getAccountUnitName()), AdsNurWorkloadPerformanceAccounting::getAccountUnitName, dto.getAccountUnitName());
        Page<AdsNurWorkloadPerformanceAccounting> docPage = new AdsNurWorkloadPerformanceAccounting().selectPage(page, wrapper);
        vo.setUnitAccountInfoList(docPage);
        //封装总值
        List<AdsNurWorkloadPerformanceAccounting> docList = new AdsNurWorkloadPerformanceAccounting().selectList(new LambdaQueryWrapper<AdsNurWorkloadPerformanceAccounting>()
                .eq(AdsNurWorkloadPerformanceAccounting::getAccountPeriod, dto.getAccountTime()));
        BigDecimal reduce = docList.stream()
                .map(AdsNurWorkloadPerformanceAccounting::getWorkloadPerformanceTotal)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        vo.setTotalCost(reduce);
        return vo;

    }

    /**
     * 医生医技工作量绩效
     *
     * @param dto
     * @return
     */
    private CostAccountTaskResultDetailNewVo doctorTechWorkloadList(TaskResultQueryNewDto dto) {

        CostAccountTaskResultDetailNewVo vo = new CostAccountTaskResultDetailNewVo();
        //封装医生医技对象信息
        Page<AdsDocWorkloadPerformanceAccounting> page = new Page<>(dto.getCurrent(), dto.getSize());
        //查询返回分页结果
        LambdaQueryWrapper<AdsDocWorkloadPerformanceAccounting> wrapper = new LambdaQueryWrapper<AdsDocWorkloadPerformanceAccounting>()
                .eq(AdsDocWorkloadPerformanceAccounting::getAccountPeriod, dto.getAccountTime())
                .like(StrUtil.isNotBlank(dto.getAccountUnitName()), AdsDocWorkloadPerformanceAccounting::getAccountUnitName, dto.getAccountUnitName());
        Page<AdsDocWorkloadPerformanceAccounting> docPage = new AdsDocWorkloadPerformanceAccounting().selectPage(page, wrapper);
        vo.setUnitAccountInfoList(docPage);
        //封装总值
        List<AdsDocWorkloadPerformanceAccounting> docList = new AdsDocWorkloadPerformanceAccounting().selectList(new LambdaQueryWrapper<AdsDocWorkloadPerformanceAccounting>()
                .eq(AdsDocWorkloadPerformanceAccounting::getAccountPeriod, dto.getAccountTime()));
        BigDecimal reduce = docList.stream()
                .map(AdsDocWorkloadPerformanceAccounting::getWorkloadPerformanceTotal)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        vo.setTotalCost(reduce);
        return vo;
    }

    /**
     * 行政编外绩效(ADMIN_NON_STAFF_PERFORMANCE)
     *
     * @param dto
     * @return
     */
    public CostAccountTaskResultDetailNewVo getAdminNonStaffPerformanceList(TaskResultQueryNewDto dto) {
        //出参
        CostAccountTaskResultDetailNewVo vo = new CostAccountTaskResultDetailNewVo();
        //条件构造器(联查 + 时间过滤)
        LambdaQueryWrapper wrapper = Wrappers.<AdsIncomePerformanceLogisticsExtra>lambdaQuery()
                .eq(AdsIncomePerformanceLogisticsExtra::getAccountPeriod, dto.getAccountTime())
                .like(StrUtil.isNotBlank(dto.getAccountUnitName()), AdsIncomePerformanceLogisticsExtra::getAccountUnitName, dto.getAccountUnitName());
        //查询数据（分页）
        Page<AdsIncomePerformanceLogisticsExtra> adsIncomePerformanceLogisticsExtraPage = new AdsIncomePerformanceLogisticsExtra()
                .selectPage(new Page<>(dto.getCurrent(), dto.getSize()), wrapper);

        //封装核算单元核算信息
        vo.setUnitAccountInfoList(adsIncomePerformanceLogisticsExtraPage);

        //封装总值（绩效工资合计）
        List<AdsIncomePerformanceLogisticsExtra> adsIncomePerformanceLogisticsExtra = new AdsIncomePerformanceLogisticsExtra()
                .selectList(new LambdaQueryWrapper<AdsIncomePerformanceLogisticsExtra>().eq(AdsIncomePerformanceLogisticsExtra::getAccountPeriod, dto.getAccountTime()));
        BigDecimal reduce = adsIncomePerformanceLogisticsExtra.stream()
                .map(AdsIncomePerformanceLogisticsExtra::getJxgzhj)
                .filter(Objects::nonNull)
                .map(BigDecimal::new)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        vo.setTotalCost(reduce);
        return vo;
    }


    /**
     * 行政普通职工绩效(ADMIN_GENERAL_PERFORMANCE)
     *
     * @param dto
     * @return
     */
    public CostAccountTaskResultDetailNewVo getAdminGeneralPerformanceList(TaskResultQueryNewDto dto) {
        //出参
        CostAccountTaskResultDetailNewVo vo = new CostAccountTaskResultDetailNewVo();
        //条件构造器(联查 + 时间过滤)
        LambdaQueryWrapper wrapper = Wrappers.<AdsIncomePerformanceLogistics>lambdaQuery()
                .eq(AdsIncomePerformanceLogistics::getAccountPeriod, dto.getAccountTime())
                .like(StrUtil.isNotBlank(dto.getAccountUnitName()), AdsIncomePerformanceLogistics::getAccountUnitName, dto.getAccountUnitName());
        //查询数据（分页）
        Page<AdsIncomePerformanceLogistics> adsIncomePerformanceLogisticsPage = new AdsIncomePerformanceLogistics()
                .selectPage(new Page<>(dto.getCurrent(), dto.getSize()), wrapper);

        //封装核算单元核算信息
        vo.setUnitAccountInfoList(adsIncomePerformanceLogisticsPage);

        //封装总值（绩效工资合计）
        List<AdsIncomePerformanceLogistics> adsIncomePerformanceLogistics = new AdsIncomePerformanceLogistics()
                .selectList(new LambdaQueryWrapper<AdsIncomePerformanceLogistics>().eq(AdsIncomePerformanceLogistics::getAccountPeriod, dto.getAccountTime()));
        BigDecimal reduce = adsIncomePerformanceLogistics.stream()
                .map(AdsIncomePerformanceLogistics::getJxgzhj)
                .filter(Objects::nonNull)
                .map(BigDecimal::new)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        vo.setTotalCost(reduce);
        return vo;
    }

    /**
     * 行政中高层(ADMIN_MIDDLE_HIGH_PERFORMANCE)
     *
     * @param dto
     * @return
     */
    public CostAccountTaskResultDetailNewVo getAdminMiddleHighPerformanceList(TaskResultQueryNewDto dto) {
        //出参
        CostAccountTaskResultDetailNewVo vo = new CostAccountTaskResultDetailNewVo();
        //条件构造器(联查 + 时间过滤)
        LambdaQueryWrapper wrapper = Wrappers.<AdsIncomePerformanceMidhigh>lambdaQuery()
                .eq(AdsIncomePerformanceMidhigh::getAccountPeriod, dto.getAccountTime())
                .like(StrUtil.isNotBlank(dto.getAccountUnitName()), AdsIncomePerformanceMidhigh::getAccountUnitName, dto.getAccountUnitName());
        //查询数据（分页）
        Page<AdsIncomePerformanceMidhigh> adminMiddleHighPerformancePage = new AdsIncomePerformanceMidhigh()
                .selectPage(new Page<>(dto.getCurrent(), dto.getSize()), wrapper);

        //封装核算单元核算信息
        vo.setUnitAccountInfoList(adminMiddleHighPerformancePage);

        //封装总值（绩效工资合计）
        List<AdsIncomePerformanceMidhigh> adsIncomePerformanceMidhigh = new AdsIncomePerformanceMidhigh()
                .selectList(new LambdaQueryWrapper<AdsIncomePerformanceMidhigh>().eq(AdsIncomePerformanceMidhigh::getAccountPeriod, dto.getAccountTime()));
        BigDecimal reduce = adsIncomePerformanceMidhigh.stream()
                .map(AdsIncomePerformanceMidhigh::getJxgzhj)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        vo.setTotalCost(reduce);
        return vo;
    }

    /**
     * 医院奖罚明细
     *
     * @param dto
     * @return
     */
    private CostAccountTaskResultDetailNewVo hospitalRewardPunishmentDetaillist(TaskResultQueryNewDto dto) {
        CostAccountTaskResultDetailNewVo vo = new CostAccountTaskResultDetailNewVo();
        //封装护理组对象信息
        Page<AdsHospitalRewardsPunishmentsDetails> page = new Page<>(dto.getCurrent(), dto.getSize());
        // 查询数据并返回分页结果
        final LambdaQueryWrapper<AdsHospitalRewardsPunishmentsDetails> wrapper = new LambdaQueryWrapper<AdsHospitalRewardsPunishmentsDetails>()
                .eq(AdsHospitalRewardsPunishmentsDetails::getAccountPeriod, dto.getAccountTime())
                .like(StrUtil.isNotBlank(dto.getAccountUnitName()), AdsHospitalRewardsPunishmentsDetails::getAccountUnitDoc, dto.getAccountUnitName());
        final Page<AdsHospitalRewardsPunishmentsDetails> detailsPagePage = new AdsHospitalRewardsPunishmentsDetails().selectPage(page, wrapper);
        //合并支援,规培
        List<AdsHospitalRewardsPunishmentsDetailsVo> detailsVoList = new ArrayList<>();
        for (AdsHospitalRewardsPunishmentsDetails details : detailsPagePage.getRecords()) {
            AdsHospitalRewardsPunishmentsDetailsVo detailsVo = new AdsHospitalRewardsPunishmentsDetailsVo();
            BeanUtil.copyProperties(details, detailsVo);
            BigDecimal support = details.getSupportDirector().add(details.getSupportDoc()).add(details.getSupportDoc()).add(details.getSupportEmp()).add(details.getSupportNur()).add(details.getSupportNurHead());
            BigDecimal training = details.getOtherDirector().add(details.getOtherDoc()).add(details.getOtherEmp()).add(details.getOtherNur()).add(details.getOtherNurHead());
            detailsVo.setSupport(support);
            detailsVo.setTraining(training);
            detailsVoList.add(detailsVo);
        }
        Page<AdsHospitalRewardsPunishmentsDetailsVo> detailsVoPagePage = new Page<>();
        BeanUtil.copyProperties(detailsPagePage, detailsVoPagePage);
        detailsVoPagePage.setRecords(detailsVoList);
        vo.setUnitAccountInfoList(detailsVoPagePage);
        //封装总值
        List<AdsHospitalRewardsPunishmentsDetails> performanceScoreNurStream = new AdsHospitalRewardsPunishmentsDetails().selectList(new LambdaQueryWrapper<AdsHospitalRewardsPunishmentsDetails>()
                .eq(AdsHospitalRewardsPunishmentsDetails::getAccountPeriod, dto.getAccountTime()));
        BigDecimal reduce = performanceScoreNurStream.stream()
                .map(AdsHospitalRewardsPunishmentsDetails::getDeptTotal)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        vo.setTotalCost(reduce);
        return vo;
    }

    /**
     * 护理业绩绩效
     *
     * @param dto
     * @return
     */
    private CostAccountTaskResultDetailNewVo nurseAchievementList(TaskResultQueryNewDto dto) {

        CostAccountTaskResultDetailNewVo vo = new CostAccountTaskResultDetailNewVo();

        //封装page对象返回
        Page<NurseAchievementVo> page = new Page<>(dto.getCurrent(), dto.getSize());

        Page<NurseAchievementVo> voPage = scoreNurMapper.selectNurseAchievementVo(page, dto.getAccountTime(), null, dto.getAccountUnitName());
        vo.setUnitAccountInfoList(voPage);
        BigDecimal totalCost = scoreNurMapper.selectTotalCost(dto.getAccountTime());
        vo.setTotalCost(totalCost);
        return vo;
    }

    /**
     * 护士长绩效
     *
     * @param dto
     * @return
     */
    public CostAccountTaskResultDetailNewVo getNurseChiefPerformanceList(TaskResultQueryNewDto dto) {
        //出参
        CostAccountTaskResultDetailNewVo vo = new CostAccountTaskResultDetailNewVo();
        //条件构造器(联查 + 时间过滤)
        MPJLambdaWrapper<AdsIncomePerformanceScoreNurHead> wrapper = new MPJLambdaWrapper<>();
        wrapper.selectAll(AdsIncomePerformanceScoreNurHead.class)
                .leftJoin(AdsIncomePerformanceClinicalNur.class,
                        AdsIncomePerformanceClinicalNur::getAccountUnitId,
                        AdsIncomePerformanceScoreNurHead::getAccountUnitId)
                .select(AdsIncomePerformanceClinicalNur::getSupportPerf,
                        AdsIncomePerformanceClinicalNur::getPerforPerf,
                        AdsIncomePerformanceClinicalNur::getWorkloadPerf,
                        AdsIncomePerformanceClinicalNur::getNurVerticalPerf)
                .eq(AdsIncomePerformanceScoreNurHead::getAccountPeriod, dto.getAccountTime())
                .eq(AdsIncomePerformanceClinicalNur::getAccountPeriod, AdsIncomePerformanceScoreNurHead::getAccountPeriod)
                .like(StrUtil.isNotBlank(dto.getAccountUnitName()), AdsIncomePerformanceScoreNurHead::getAccountUnitName, dto.getAccountUnitName());

        //查询数据（分页）
        Page<NurseChiefPerformanceListVo> nurseChiefPerformanceListVoPage = scoreNurHeadMapper
                .selectJoinPage(new Page<>(dto.getCurrent(), dto.getSize()),
                        NurseChiefPerformanceListVo.class, wrapper);
        //封装核算单元核算信息
        vo.setUnitAccountInfoList(nurseChiefPerformanceListVoPage);

        MPJLambdaWrapper<AdsIncomePerformanceScoreNurHead> newWrapper = new MPJLambdaWrapper<>();
        newWrapper.selectAll(AdsIncomePerformanceScoreNurHead.class)
                .leftJoin(AdsIncomePerformanceClinicalNur.class,
                        AdsIncomePerformanceClinicalNur::getAccountUnitId,
                        AdsIncomePerformanceScoreNurHead::getAccountUnitId)
                .select(AdsIncomePerformanceClinicalNur::getSupportPerf,
                        AdsIncomePerformanceClinicalNur::getPerforPerf,
                        AdsIncomePerformanceClinicalNur::getWorkloadPerf,
                        AdsIncomePerformanceClinicalNur::getNurVerticalPerf)
                .eq(AdsIncomePerformanceClinicalNur::getAccountPeriod, AdsIncomePerformanceScoreNurHead::getAccountPeriod)
                .eq(AdsIncomePerformanceScoreNurHead::getAccountPeriod, dto.getAccountTime());
        //封装总值（绩效工资合计）
        List<AdsIncomePerformanceScoreNurHead> adsIncomePerformanceScoreDocHeads = scoreNurHeadMapper
                .selectJoinList(AdsIncomePerformanceScoreNurHead.class, newWrapper);
        BigDecimal reduce = adsIncomePerformanceScoreDocHeads.stream()
                .map(AdsIncomePerformanceScoreNurHead::getJxgzhj) // 直接获取BigDecimal类型的Jxgzhj
                .filter(Objects::nonNull) // 过滤掉null值
                .reduce(BigDecimal.ZERO, BigDecimal::add); // 使用初始值为0的reduce方法进行累加
        vo.setTotalCost(reduce);

        return vo;
    }

    /**
     * 科主任绩效
     *
     * @param dto
     * @return
     */
    private CostAccountTaskResultDetailNewVo getDepartmentHeadPerformanceList(TaskResultQueryNewDto dto) {
        //出参
        CostAccountTaskResultDetailNewVo vo = new CostAccountTaskResultDetailNewVo();
        Page<AdsIncomePerformanceScoreDocHead> page = new Page<>(dto.getCurrent(), dto.getSize());

        final Page<AdsIncomePerformanceScoreDocHead> headPage = new AdsIncomePerformanceScoreDocHead().selectPage(page, new LambdaQueryWrapper<AdsIncomePerformanceScoreDocHead>()
                .eq(AdsIncomePerformanceScoreDocHead::getAccountPeriod, dto.getAccountTime())
                .like(AdsIncomePerformanceScoreDocHead::getAccountUnitName,dto.getAccountUnitName()));

        final List<AdsIncomePerformanceScoreDocHead> records = headPage.getRecords();
        //封装其他数据
        final List<DepartmentHeadPerformanceListVo> performanceListVos = records.stream().map(headDoc -> {
            DepartmentHeadPerformanceListVo listVo = new DepartmentHeadPerformanceListVo();
            BeanUtil.copyProperties(headDoc, listVo);
            AdsIncomePerformanceClinicalDoc clinicalDoc = new AdsIncomePerformanceClinicalDoc();
            if ("杜学宏".equals(headDoc.getEmpName())) {
                clinicalDoc = new AdsIncomePerformanceClinicalDoc().selectOne(new LambdaQueryWrapper<AdsIncomePerformanceClinicalDoc>()
                        .eq(AdsIncomePerformanceClinicalDoc::getAccountPeriod, dto.getAccountTime())
                        .eq(AdsIncomePerformanceClinicalDoc::getAccountUnitDoc, "急诊科"));
            } else {
                clinicalDoc = new AdsIncomePerformanceClinicalDoc().selectOne(new LambdaQueryWrapper<AdsIncomePerformanceClinicalDoc>()
                        .eq(AdsIncomePerformanceClinicalDoc::getAccountPeriod, dto.getAccountTime())
                        .eq(AdsIncomePerformanceClinicalDoc::getAccountUnitId, headDoc.getAccountUnitId()));
            }
            listVo.setSupportPerf(clinicalDoc.getSupportPerf());
            listVo.setPerforPerf(clinicalDoc.getPerforPerf());
            listVo.setWorkloadPerf(clinicalDoc.getWorkloadPerf());
            return listVo;
        }).collect(Collectors.toList());
        //封装核算单元核算信息
        Page<DepartmentHeadPerformanceListVo> performanceListVoPage=new Page<>();
        performanceListVoPage.setRecords(performanceListVos);
        performanceListVoPage.setSize(headPage.getSize());
        performanceListVoPage.setTotal(headPage.getTotal());
        vo.setUnitAccountInfoList(performanceListVoPage);

        List<AdsIncomePerformanceScoreDocHead> adsIncomePerformanceScoreDocHeads = new AdsIncomePerformanceScoreDocHead().selectList(new LambdaQueryWrapper<AdsIncomePerformanceScoreDocHead>()
                .eq(AdsIncomePerformanceScoreDocHead::getAccountPeriod, dto.getAccountTime()));
        BigDecimal reduce = adsIncomePerformanceScoreDocHeads.stream()
                .map(AdsIncomePerformanceScoreDocHead::getJxgzhj) // 直接获取BigDecimal类型的Jxgzhj
                .filter(Objects::nonNull) // 过滤掉null值
                .reduce(BigDecimal.ZERO, BigDecimal::add); // 使用初始值为0的reduce方法进行累加
        vo.setTotalCost(reduce);
        return vo;
    }

    /**
     * 医生医技业绩绩效
     *
     * @param dto
     * @return
     */
    private CostAccountTaskResultDetailNewVo doctorTechAchievementList(TaskResultQueryNewDto dto) {
        CostAccountTaskResultDetailNewVo vo = new CostAccountTaskResultDetailNewVo();


//        多表联查返回vo对象
//        MPJLambdaWrapper<AdsIncomePerformanceScoreDoc> wrapper = new MPJLambdaWrapper();
//        wrapper
//                .selectAs(AdsIncomePerformanceScoreDoc::getIncomeTotal, DoctorTechAchievementVo::getIncomeTotal)
//                .selectAs(AdsIncomePerformanceScoreDoc::getCostTotal, DoctorTechAchievementVo::getCostTotal)
//                .selectAs(AdsIncomePerformanceScoreDoc::getInpatIncome, DoctorTechAchievementVo::getInpatIncome)
//                .selectAs(AdsIncomePerformanceScoreDoc::getInsuranceIncome, DoctorTechAchievementVo::getInsuranceIncome)
//                .selectAs(AdsIncomePerformanceScoreDoc::getInpatExamIndex, DoctorTechAchievementVo::getInpatExamIndex)
//                .selectAs(AdsIncomePerformanceScoreDoc::getPerformanceScore, DoctorTechAchievementVo::getPerformanceScore)
//                .selectAs(AdsIncomePerformanceClinicalDoc::getDepPerfPoint, DoctorTechAchievementVo::getDepPerfPoint)
//                .selectAs(AdsIncomePerformanceClinicalDoc::getInpatPerfPoint, DoctorTechAchievementVo::getInpatPerfPoint)
//                .selectAs(AdsIncomePerformanceClinicalDoc::getAccountUnitId, DoctorTechAchievementVo::getAccountUnitId)
//                .selectAs(AdsIncomePerformanceClinicalDoc::getAccountUnitDoc, DoctorTechAchievementVo::getAccountUnitDoc)
//                .leftJoin(AdsIncomePerformanceClinicalDoc.class, AdsIncomePerformanceClinicalDoc::getAccountUnitId, AdsIncomePerformanceScoreDoc::getAccountDocId)// 进行左连接，连接条件是Department的id字段和User的deptId字段
//                .eq(AdsIncomePerformanceClinicalDoc::getAccountUnitId, AdsIncomePerformanceScoreDoc::getAccountDocId)
//                .eq(AdsIncomePerformanceClinicalDoc::getDt, dto.getAccountTime());
//        //封装page对象返回
        Page<DoctorTechAchievementVo> page = new Page<>(dto.getCurrent(), dto.getSize());
        Page<DoctorTechAchievementVo> doctorTechAchievementVoPage = scoreDocMapper.selectDoctorTechAchievementVo(page, dto.getAccountTime(), null, dto.getAccountUnitName());

//        Page<DoctorTechAchievementVo> doctorTechAchievementVoPage = scoreDocMapper.selectJoinPage(page, DoctorTechAchievementVo.class, wrapper);
        vo.setUnitAccountInfoList(doctorTechAchievementVoPage);

//        scoreDocMapper.selectJoinList(DoctorTechAchievementVo.class, wrapper);
//        填充总值
//        BigDecimal totalCost = doctorTechAchievementVoPage.getRecords().stream()
//                .map(DoctorTechAchievementVo::getPerformanceScore)
//                .filter(Objects::nonNull)
//                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalCost = scoreDocMapper.selectTotalCost(dto.getAccountTime());
        vo.setTotalCost(totalCost);
        return vo;
    }

    /**
     * 临床护士绩效
     *
     * @param dto
     * @return
     */
    private CostAccountTaskResultDetailNewVo clinicalNursePerformanceList(TaskResultQueryNewDto dto) {
        CostAccountTaskResultDetailNewVo vo = new CostAccountTaskResultDetailNewVo();
        //封装医生医技对象信息
        Page<AdsIncomePerformanceClinicalNur> page = new Page<>(dto.getCurrent(), dto.getSize());
        //查询返回分页结果
        LambdaQueryWrapper<AdsIncomePerformanceClinicalNur> wrapper = new LambdaQueryWrapper<AdsIncomePerformanceClinicalNur>()
                .eq(AdsIncomePerformanceClinicalNur::getAccountPeriod, dto.getAccountTime())
                .like(StrUtil.isNotBlank(dto.getAccountUnitName()), AdsIncomePerformanceClinicalNur::getAccountUnitNur, dto.getAccountUnitName());
        Page<AdsIncomePerformanceClinicalNur> docPage = new AdsIncomePerformanceClinicalNur().selectPage(page, wrapper);
        vo.setUnitAccountInfoList(docPage);
        //封装总值
        List<AdsIncomePerformanceClinicalNur> docList = new AdsIncomePerformanceClinicalNur().selectList(new LambdaQueryWrapper<AdsIncomePerformanceClinicalNur>()
                .eq(AdsIncomePerformanceClinicalNur::getAccountPeriod, dto.getAccountTime()));
        BigDecimal sum = docList.stream()
                .map(AdsIncomePerformanceClinicalNur::getPerfTotal)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        vo.setTotalCost(sum);
        return vo;
    }

    /**
     * 临床医生医技绩效
     *
     * @param dto
     * @return
     */
    private CostAccountTaskResultDetailNewVo clinicalDoctorTechPerformanceList(TaskResultQueryNewDto dto) {
        CostAccountTaskResultDetailNewVo vo = new CostAccountTaskResultDetailNewVo();
        //封装医生医技对象信息
        Page<AdsIncomePerformanceClinicalDoc> page = new Page<>(dto.getCurrent(), dto.getSize());
        //查询返回分页结果
        LambdaQueryWrapper<AdsIncomePerformanceClinicalDoc> wrapper = new LambdaQueryWrapper<AdsIncomePerformanceClinicalDoc>()
                .eq(AdsIncomePerformanceClinicalDoc::getAccountPeriod, dto.getAccountTime())
                .like(StrUtil.isNotBlank(dto.getAccountUnitName()), AdsIncomePerformanceClinicalDoc::getAccountUnitDoc, dto.getAccountUnitName());
        Page<AdsIncomePerformanceClinicalDoc> docPage = new AdsIncomePerformanceClinicalDoc().selectPage(page, wrapper);
        vo.setUnitAccountInfoList(docPage);
        //封装总值
        List<AdsIncomePerformanceClinicalDoc> docList = new AdsIncomePerformanceClinicalDoc().selectList(new LambdaQueryWrapper<AdsIncomePerformanceClinicalDoc>()
                .eq(AdsIncomePerformanceClinicalDoc::getAccountPeriod, dto.getAccountTime()));
        BigDecimal sum = docList.stream()
                .map(AdsIncomePerformanceClinicalDoc::getPerfTotal)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        vo.setTotalCost(sum);
        return vo;
    }

    /**
     * 医生医技收入绩效
     *
     * @param dto
     * @return
     */
    private CostAccountTaskResultDetailNewVo getDoctorTechRevenueList(TaskResultQueryNewDto dto) {
        CostAccountTaskResultDetailNewVo vo = new CostAccountTaskResultDetailNewVo();
        //封装护理组对象信息
        Page<AdsIncomePerformanceScoreDoc> page = new Page<>(dto.getCurrent(), dto.getSize());
        // 查询数据并返回分页结果
        final LambdaQueryWrapper<AdsIncomePerformanceScoreDoc> wrapper = new LambdaQueryWrapper<AdsIncomePerformanceScoreDoc>()
                .eq(AdsIncomePerformanceScoreDoc::getAccountPeriod, dto.getAccountTime())
                .like(StrUtil.isNotBlank(dto.getAccountUnitName()), AdsIncomePerformanceScoreDoc::getAccountDocUnitName, dto.getAccountUnitName());
        final Page<AdsIncomePerformanceScoreDoc> docPage = new AdsIncomePerformanceScoreDoc().selectPage(page, wrapper);
        vo.setUnitAccountInfoList(docPage);
        //封装总值
        List<AdsIncomePerformanceScoreDoc> performanceScoreDocStream = new AdsIncomePerformanceScoreDoc().selectList(new LambdaQueryWrapper<AdsIncomePerformanceScoreDoc>()
                .eq(AdsIncomePerformanceScoreDoc::getAccountPeriod, dto.getAccountTime()));
        BigDecimal reduce = performanceScoreDocStream.stream()
                .map(AdsIncomePerformanceScoreDoc::getPerformanceScore)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        vo.setTotalCost(reduce);
        return vo;
    }

    /**
     * 护理组收入绩效结果展示
     *
     * @param dto
     * @return
     */
    private CostAccountTaskResultDetailNewVo getNurseRevenueList(TaskResultQueryNewDto dto) {
        CostAccountTaskResultDetailNewVo vo = new CostAccountTaskResultDetailNewVo();
        //封装护理组对象信息
        Page<AdsIncomePerformanceScoreNur> page = new Page<>(dto.getCurrent(), dto.getSize());
        // 查询数据并返回分页结果
        final LambdaQueryWrapper<AdsIncomePerformanceScoreNur> wrapper = new LambdaQueryWrapper<AdsIncomePerformanceScoreNur>()
                .eq(AdsIncomePerformanceScoreNur::getAccountPeriod, dto.getAccountTime())
                .like(StrUtil.isNotBlank(dto.getAccountUnitName()), AdsIncomePerformanceScoreNur::getAccountUnitName, dto.getAccountUnitName());
        final Page<AdsIncomePerformanceScoreNur> nurPage = new AdsIncomePerformanceScoreNur().selectPage(page, wrapper);
        vo.setUnitAccountInfoList(nurPage);
        //封装总值
        List<AdsIncomePerformanceScoreNur> performanceScoreNurStream = new AdsIncomePerformanceScoreNur().selectList(new LambdaQueryWrapper<AdsIncomePerformanceScoreNur>()
                .eq(AdsIncomePerformanceScoreNur::getAccountPeriod, dto.getAccountTime()));
        BigDecimal reduce = performanceScoreNurStream.stream()
                .map(AdsIncomePerformanceScoreNur::getPerformanceScore)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        vo.setTotalCost(reduce);
        return vo;
    }

    /**
     * 数据下转明细(数据小组)
     *
     * @param dto
     * @return
     */
    @Override
    public CostAccountTaskResultIndexProcessNewVo getDistributionNewList(CostAccountTaskCalculateProcessNewDto dto) {
        CostAccountTaskResultIndexProcessNewVo vo = new CostAccountTaskResultIndexProcessNewVo();
        //判断是那种类型的明细
        //护理组收入绩效明细
        if (TaskTypeEnum.NURSE_REVENUE.getDescription().equals(dto.getAccountTaskName())) {
            if ("住院收入".equals(dto.getIndexName())) {
                //获取该科室单元下的所有明细
                List<DwsFinanceInpatNurseFee30d> list = new DwsFinanceInpatNurseFee30d().selectList(new LambdaQueryWrapper<DwsFinanceInpatNurseFee30d>()
                        .eq(DwsFinanceInpatNurseFee30d::getDt, dto.getAccountTime())
                        .eq(DwsFinanceInpatNurseFee30d::getAccountUnitNurId, dto.getUnitId()));
                DwsFinanceInpatNurseFee30d details = new DwsFinanceInpatNurseFee30d();
                //合并所有相同费用的值
                for (DwsFinanceInpatNurseFee30d fee30d : list) {
                    details.setAmountWesMed(details.getAmountWesMed().add(fee30d.getAmountWesMed()));
                    details.setAmountPatMed(details.getAmountPatMed().add(fee30d.getAmountPatMed()));
                    details.setAmountChiMed(details.getAmountChiMed().add(fee30d.getAmountChiMed()));
                    details.setAmountTreat(details.getAmountTreat().add(fee30d.getAmountTreat()));
                    details.setAmountRadiology(details.getAmountRadiology().add(fee30d.getAmountRadiology()));
                    details.setAmountB(details.getAmountB().add(fee30d.getAmountB()));
                    details.setAmountCheck(details.getAmountCheck().add(fee30d.getAmountCheck()));
                    details.setAmountAcupuncture(details.getAmountAcupuncture().add(fee30d.getAmountAcupuncture()));
                    details.setAmountMassage(details.getAmountMassage().add(fee30d.getAmountMassage()));
                    details.setAmountNurse(details.getAmountNurse().add(fee30d.getAmountNurse()));
                    details.setAmountDecoct(details.getAmountDecoct().add(fee30d.getAmountDecoct()));
                    details.setAmountInjection(details.getAmountInjection().add(fee30d.getAmountInjection()));
                    details.setAmountPhysio(details.getAmountPhysio().add(fee30d.getAmountPhysio()));
                    details.setAmountTest(details.getAmountTest().add(fee30d.getAmountTest()));
                    details.setAmountRadiology2(details.getAmountRadiology2().add(fee30d.getAmountRadiology2()));
                    details.setAmountTransfuse(details.getAmountTransfuse().add(fee30d.getAmountTransfuse()));
                    details.setAmountOxygenate(details.getAmountOxygenate().add(fee30d.getAmountOxygenate()));
                    details.setAmountTreatment(details.getAmountTreatment().add(fee30d.getAmountTreatment()));
                    details.setAmountBed(details.getAmountBed().add(fee30d.getAmountBed()));
                    details.setAmountOther(details.getAmountOther().add(fee30d.getAmountOther()));
                    details.setAmountMaterial(details.getAmountMaterial().add(fee30d.getAmountMaterial()));
                    details.setAmountSurgery(details.getAmountSurgery().add(fee30d.getAmountSurgery()));
                    details.setAmountCheckup(details.getAmountCheckup().add(fee30d.getAmountCheckup()));
                    details.setAmountRegister(details.getAmountRegister().add(fee30d.getAmountRegister()));
                    details.setAmountCard(details.getAmountCard().add(fee30d.getAmountCard()));
                    details.setAmountRecord(details.getAmountRecord().add(fee30d.getAmountRecord()));
                    details.setAmountEndoscope(details.getAmountEndoscope().add(fee30d.getAmountEndoscope()));
                    details.setAmountNarcotism(details.getAmountNarcotism().add(fee30d.getAmountNarcotism()));
                    details.setAmountEmergency(details.getAmountEmergency().add(fee30d.getAmountEmergency()));
                    details.setAmountFood(details.getAmountFood().add(fee30d.getAmountFood()));
                }
                vo.setDetails(details);
            }
            if ("日间病房".equals(dto.getIndexName())) {
                //获取该科室单元下的所有明细
                DwsFinanceOtherIncome30d dwsFinanceOtherIncome30d = new DwsFinanceOtherIncome30d().selectOne(new LambdaQueryWrapper<DwsFinanceOtherIncome30d>()
                        .eq(DwsFinanceOtherIncome30d::getDt, dto.getAccountTime())
                        .eq(DwsFinanceOtherIncome30d::getIncomeTypeCode, "105")
                        .eq(DwsFinanceOtherIncome30d::getAccountUnitNurId, dto.getUnitId()));
                vo.setDetails(dwsFinanceOtherIncome30d);
            }
            if ("总核算值".equals(dto.getIndexName())) {
                //查询
                AdsIncomePerformanceScoreNur adsIncomePerformanceScoreNur = new AdsIncomePerformanceScoreNur().selectOne(new LambdaQueryWrapper<AdsIncomePerformanceScoreNur>()
                        .eq(AdsIncomePerformanceScoreNur::getAccountPeriod, dto.getAccountTime())
                        .eq(AdsIncomePerformanceScoreNur::getAccountUnitId, dto.getUnitId()));
                if (adsIncomePerformanceScoreNur != null) {
                    vo.setTotalCount(adsIncomePerformanceScoreNur.getTotalCost().toString());
                    vo.setDetails(adsIncomePerformanceScoreNur);
                }
            }
        }
        //医生医技收入绩效
        else if (TaskTypeEnum.DOCTOR_TECH_REVENUE.getDescription().equals(dto.getAccountTaskName())) {
            if ("总核算值".equals(dto.getIndexName())) {
                //查询
                AdsIncomePerformanceScoreDoc adsIncomePerformanceScoreDoc = new AdsIncomePerformanceScoreDoc().selectOne(new LambdaQueryWrapper<AdsIncomePerformanceScoreDoc>()
                        .eq(AdsIncomePerformanceScoreDoc::getAccountPeriod, dto.getAccountTime())
                        .eq(AdsIncomePerformanceScoreDoc::getAccountDocId, dto.getUnitId()));
                vo.setTotalCount(adsIncomePerformanceScoreDoc.getCostTotal().toString());
                vo.setDetails(adsIncomePerformanceScoreDoc);
            }
            if ("住院收入".equals(dto.getIndexName())) {
                //获取该科室单元下的所有明细
                List<DwsFinanceInpatFee30d> list = new DwsFinanceInpatFee30d().selectList(new LambdaQueryWrapper<DwsFinanceInpatFee30d>()
                        .eq(DwsFinanceInpatFee30d::getDt, dto.getAccountTime())
                        .eq(DwsFinanceInpatFee30d::getAccountUnitDocId, dto.getUnitId()));
                DwsFinanceInpatFee30d details = new DwsFinanceInpatFee30d();
                //合并所有相同费用的值
                for (DwsFinanceInpatFee30d fee30d : list) {
                    details.setAmountWesMed(details.getAmountWesMed().add(fee30d.getAmountWesMed()));
                    details.setAmountPatMed(details.getAmountPatMed().add(fee30d.getAmountPatMed()));
                    details.setAmountChiMed(details.getAmountChiMed().add(fee30d.getAmountChiMed()));
                    details.setAmountTreat(details.getAmountTreat().add(fee30d.getAmountTreat()));
                    details.setAmountRadiology(details.getAmountRadiology().add(fee30d.getAmountRadiology()));
                    details.setAmountB(details.getAmountB().add(fee30d.getAmountB()));
                    details.setAmountCheck(details.getAmountCheck().add(fee30d.getAmountCheck()));
                    details.setAmountAcupuncture(details.getAmountAcupuncture().add(fee30d.getAmountAcupuncture()));
                    details.setAmountMassage(details.getAmountMassage().add(fee30d.getAmountMassage()));
                    details.setAmountNurse(details.getAmountNurse().add(fee30d.getAmountNurse()));
                    details.setAmountDecoct(details.getAmountDecoct().add(fee30d.getAmountDecoct()));
                    details.setAmountInjection(details.getAmountInjection().add(fee30d.getAmountInjection()));
                    details.setAmountPhysio(details.getAmountPhysio().add(fee30d.getAmountPhysio()));
                    details.setAmountTest(details.getAmountTest().add(fee30d.getAmountTest()));
                    details.setAmountRadiology2(details.getAmountRadiology2().add(fee30d.getAmountRadiology2()));
                    details.setAmountTransfuse(details.getAmountTransfuse().add(fee30d.getAmountTransfuse()));
                    details.setAmountOxygenate(details.getAmountOxygenate().add(fee30d.getAmountOxygenate()));
                    details.setAmountTreatment(details.getAmountTreatment().add(fee30d.getAmountTreatment()));
                    details.setAmountBed(details.getAmountBed().add(fee30d.getAmountBed()));
                    details.setAmountOther(details.getAmountOther().add(fee30d.getAmountOther()));
                    details.setAmountMaterial(details.getAmountMaterial().add(fee30d.getAmountMaterial()));
                    details.setAmountSurgery(details.getAmountSurgery().add(fee30d.getAmountSurgery()));
                    details.setAmountCheckup(details.getAmountCheckup().add(fee30d.getAmountCheckup()));
                    details.setAmountRegister(details.getAmountRegister().add(fee30d.getAmountRegister()));
                    details.setAmountCard(details.getAmountCard().add(fee30d.getAmountCard()));
                    details.setAmountRecord(details.getAmountRecord().add(fee30d.getAmountRecord()));
                    details.setAmountEndoscope(details.getAmountEndoscope().add(fee30d.getAmountEndoscope()));
                    details.setAmountNarcotism(details.getAmountNarcotism().add(fee30d.getAmountNarcotism()));
                    details.setAmountEmergency(details.getAmountEmergency().add(fee30d.getAmountEmergency()));
                    details.setAmountFood(details.getAmountFood().add(fee30d.getAmountFood()));
                    details.setAmountMed(details.getAmountMed().add(fee30d.getAmountMed()));
                    details.setAmountNoneMed(details.getAmountNoneMed().add(fee30d.getAmountNoneMed()));
                }
                vo.setDetails(details);
            }
            if ("总院门诊收入".equals(dto.getIndexName())) {
                //获取该科室单元下的所有明细
                DwsFinanceOutFee30d dwsFinanceOutFee30d = new DwsFinanceOutFee30d().selectOne(new LambdaQueryWrapper<DwsFinanceOutFee30d>()
                        .eq(DwsFinanceOutFee30d::getDt, dto.getAccountTime())
                        .eq(DwsFinanceOutFee30d::getBranchCode, "00")
                        .eq(DwsFinanceOutFee30d::getAccountUnitDocId, dto.getUnitId()));
                vo.setDetails(dwsFinanceOutFee30d);
            }
            if ("名医馆门诊收入".equals(dto.getIndexName())) {
                //获取该科室单元下的所有明细
                DwsFinanceOutFee30d dwsFinanceOutFee30d = new DwsFinanceOutFee30d().selectOne(new LambdaQueryWrapper<DwsFinanceOutFee30d>()
                        .eq(DwsFinanceOutFee30d::getDt, dto.getAccountTime())
                        .eq(DwsFinanceOutFee30d::getBranchCode, "02")
                        .eq(DwsFinanceOutFee30d::getAccountUnitDocId, dto.getUnitId()));
                vo.setDetails(dwsFinanceOutFee30d);
            }
        }
        //临床医生医技绩效明细
        else if (TaskTypeEnum.CLINICAL_DOCTOR_TECH_PERFORMANCE.getDescription().equals(dto.getAccountTaskName())) {
            if ("总核算值".equals(dto.getIndexName())) {
                //查询
                AdsIncomePerformanceClinicalDoc clinicalDoc = new AdsIncomePerformanceClinicalDoc().selectOne(new LambdaQueryWrapper<AdsIncomePerformanceClinicalDoc>()
                        .eq(AdsIncomePerformanceClinicalDoc::getAccountPeriod, dto.getAccountTime())
                        .eq(AdsIncomePerformanceClinicalDoc::getAccountUnitId, dto.getUnitId()));
                if (clinicalDoc != null) {
                    vo.setTotalCount(clinicalDoc.getPerfTotal().toString());
                    vo.setDetails(clinicalDoc);
                }
            }
        }
        //临床护士绩效明细
        else if (TaskTypeEnum.CLINICAL_NURSE_PERFORMANCE.getDescription().equals(dto.getAccountTaskName())) {
            if ("总核算值".equals(dto.getIndexName())) {
                //查询
                AdsIncomePerformanceClinicalNur clinicalNur = new AdsIncomePerformanceClinicalNur().selectOne(new LambdaQueryWrapper<AdsIncomePerformanceClinicalNur>()
                        .eq(AdsIncomePerformanceClinicalNur::getAccountPeriod, dto.getAccountTime())
                        .eq(AdsIncomePerformanceClinicalNur::getAccountUnitId, dto.getUnitId()));
                if (clinicalNur != null) {
                    vo.setTotalCount(clinicalNur.getPerfTotal().toString());
                    vo.setDetails(clinicalNur);
                }
            }
        }
        // 医生医技业绩绩效
        else if (TaskTypeEnum.DOCTOR_TECH_ACHIEVEMENT.getDescription().equals(dto.getAccountTaskName())) {
            if ("总核算值".equals(dto.getIndexName())) {
                Page<DoctorTechAchievementVo> doctorTechAchievementVoPage = scoreDocMapper.selectDoctorTechAchievementVo(new Page<>(1, 1, 1), dto.getAccountTime(), dto.getUnitId(), null);
                if (CollUtil.isNotEmpty(doctorTechAchievementVoPage.getRecords())) {
                    vo.setTotalCount(doctorTechAchievementVoPage.getRecords().get(0).getPerformanceScore().toString());
                    vo.setDetails(doctorTechAchievementVoPage.getRecords().get(0));
                }
            }

        }
        // 护理业绩绩效
        else if (TaskTypeEnum.NURSE_ACHIEVEMENT.getDescription().equals(dto.getAccountTaskName())) {
            if ("总核算值".equals(dto.getIndexName())) {
                Page<NurseAchievementVo> voPage = scoreNurMapper.selectNurseAchievementVo(new Page<>(1, 1, 1), dto.getAccountTime(), dto.getUnitId(), null);
                if (CollUtil.isNotEmpty(voPage.getRecords())) {
                    vo.setTotalCount(voPage.getRecords().get(0).getPerformanceScore().toString());
                    vo.setDetails(voPage.getRecords().get(0));
                }
            }

        }
        //医院奖罚明细
        else if (TaskTypeEnum.HOSPITAL_REWARD_PUNISHMENT_DETAIL.getDescription().equals(dto.getAccountTaskName())) {
            if ("医院单项奖罚".equals(dto.getIndexName())) {
                //获取该科室单元下的所有明细
                AdsHospitalRewardsPunishments adsHospitalRewardsPunishments = new AdsHospitalRewardsPunishments().selectOne(new LambdaQueryWrapper<AdsHospitalRewardsPunishments>()
                        .eq(AdsHospitalRewardsPunishments::getAccountPeriod, dto.getAccountTime())
                        .eq(AdsHospitalRewardsPunishments::getAccountUnitId, dto.getUnitId()));
                vo.setDetails(adsHospitalRewardsPunishments);
            }
            if ("总核算值".equals(dto.getIndexName())) {
                //查询
                AdsHospitalRewardsPunishmentsDetails details = new AdsHospitalRewardsPunishmentsDetails().selectOne(new LambdaQueryWrapper<AdsHospitalRewardsPunishmentsDetails>()
                        .eq(AdsHospitalRewardsPunishmentsDetails::getAccountPeriod, dto.getAccountTime())
                        .eq(AdsHospitalRewardsPunishmentsDetails::getAccountUnitId, dto.getUnitId()));
                if (details != null) {
                    AdsHospitalRewardsPunishmentsDetailsVo detailsVo = new AdsHospitalRewardsPunishmentsDetailsVo();
                    BeanUtil.copyProperties(details, detailsVo);
                    BigDecimal support = details.getSupportDirector().add(details.getSupportDoc()).add(details.getSupportDoc()).add(details.getSupportEmp()).add(details.getSupportNur()).add(details.getSupportNurHead());
                    BigDecimal training = details.getOtherDirector().add(details.getOtherDoc()).add(details.getOtherEmp()).add(details.getOtherNur()).add(details.getOtherNurHead());
                    detailsVo.setSupport(support);
                    detailsVo.setTraining(training);
                    vo.setTotalCount(detailsVo.getDeptTotal().toString());
                    vo.setDetails(detailsVo);
                }
            }

        }
        //科主任绩效
        else if (TaskTypeEnum.DEPARTMENT_HEAD_PERFORMANCE.getDescription().equals(dto.getAccountTaskName())) {
            if ("总核算值".equals(dto.getIndexName())) {
                //条件联查构造器
                MPJLambdaWrapper<AdsIncomePerformanceScoreDocHead> wrapper = new MPJLambdaWrapper<>();
                wrapper.selectAll(AdsIncomePerformanceScoreDocHead.class)
                        .leftJoin(AdsIncomePerformanceClinicalDoc.class,
                                AdsIncomePerformanceClinicalDoc::getAccountUnitId,
                                AdsIncomePerformanceScoreDocHead::getAccountUnitId)
                        .select(AdsIncomePerformanceClinicalDoc::getSupportPerf,
                                AdsIncomePerformanceClinicalDoc::getPerforPerf,
                                AdsIncomePerformanceClinicalDoc::getWorkloadPerf)
                        .eq(AdsIncomePerformanceScoreDocHead::getAccountPeriod, dto.getAccountTime())
                        .eq(AdsIncomePerformanceScoreDocHead::getAccountPeriod, AdsIncomePerformanceClinicalDoc::getAccountPeriod)
                        .eq(AdsIncomePerformanceScoreDocHead::getAccountUnitId, dto.getUnitId())
                        .eq(AdsIncomePerformanceScoreDocHead::getEmpId, dto.getUserId());

                DepartmentHeadPerformanceListVo departmentHeadPerformanceListVo = scoreDocHeadMapper
                        .selectJoinOne(DepartmentHeadPerformanceListVo.class, wrapper);

                vo.setTotalCount(departmentHeadPerformanceListVo.getJxgzhj().toString());
                vo.setDetails(departmentHeadPerformanceListVo);
            }
        }
        //护士长绩效
        else if (TaskTypeEnum.NURSE_CHIEF_PERFORMANCE.getDescription().equals(dto.getAccountTaskName())) {
            if ("总核算值".equals(dto.getIndexName())) {
                //条件联查构造器
                MPJLambdaWrapper<AdsIncomePerformanceScoreNurHead> wrapper = new MPJLambdaWrapper<>();
                wrapper.selectAll(AdsIncomePerformanceScoreNurHead.class)
                        .leftJoin(AdsIncomePerformanceClinicalNur.class,
                                AdsIncomePerformanceClinicalNur::getAccountUnitId,
                                AdsIncomePerformanceScoreNurHead::getAccountUnitId)
                        .select(AdsIncomePerformanceClinicalNur::getSupportPerf,
                                AdsIncomePerformanceClinicalNur::getPerforPerf,
                                AdsIncomePerformanceClinicalNur::getWorkloadPerf,
                                AdsIncomePerformanceClinicalNur::getNurVerticalPerf)
                        .eq(AdsIncomePerformanceScoreNurHead::getAccountPeriod, dto.getAccountTime())
                        .eq(AdsIncomePerformanceClinicalNur::getAccountPeriod, AdsIncomePerformanceScoreNurHead::getAccountPeriod)
                        .eq(AdsIncomePerformanceScoreNurHead::getAccountUnitId, dto.getUnitId())
                        .eq(AdsIncomePerformanceScoreNurHead::getEmpId, dto.getUserId());

                NurseChiefPerformanceListVo nurseChiefPerformanceListVo = scoreNurHeadMapper
                        .selectJoinOne(NurseChiefPerformanceListVo.class, wrapper);

                vo.setTotalCount(nurseChiefPerformanceListVo.getJxgzhj());
                vo.setDetails(nurseChiefPerformanceListVo);
            }
        }
        //行政中高层绩效
        else if (TaskTypeEnum.ADMIN_MIDDLE_HIGH_PERFORMANCE.getDescription().equals(dto.getAccountTaskName())) {
            if ("总核算值".equals(dto.getIndexName())) {
                //条件联查构造器
                LambdaQueryWrapper wrapper = Wrappers.<AdsIncomePerformanceMidhigh>lambdaQuery()
                        .eq(AdsIncomePerformanceMidhigh::getAccountPeriod, dto.getAccountTime())
                        .eq(AdsIncomePerformanceMidhigh::getAccountUnitId, dto.getUnitId())
                        .eq(AdsIncomePerformanceMidhigh::getEmpId, dto.getUserId());

                AdsIncomePerformanceMidhigh adsIncomePerformanceMidhigh = new AdsIncomePerformanceMidhigh().selectOne(wrapper);

                vo.setTotalCount(adsIncomePerformanceMidhigh.getJxgzhj().toString());
                vo.setDetails(adsIncomePerformanceMidhigh);
            }
        }
        //行政普通职工绩效
        else if (TaskTypeEnum.ADMIN_GENERAL_PERFORMANCE.getDescription().equals(dto.getAccountTaskName())) {
            if ("总核算值".equals(dto.getIndexName())) {
                //条件联查构造器
                LambdaQueryWrapper wrapper = Wrappers.<AdsIncomePerformanceLogistics>lambdaQuery()
                        .eq(AdsIncomePerformanceLogistics::getAccountPeriod, dto.getAccountTime())
                        .eq(AdsIncomePerformanceLogistics::getAccountUnitId, dto.getUnitId());

                AdsIncomePerformanceLogistics adsIncomePerformanceLogistics = new AdsIncomePerformanceLogistics().selectOne(wrapper);

                vo.setTotalCount(adsIncomePerformanceLogistics.getJxgzhj().toString());
                vo.setDetails(adsIncomePerformanceLogistics);
            }
        }
        //行政编外绩效
        else if (TaskTypeEnum.ADMIN_NON_STAFF_PERFORMANCE.getDescription().equals(dto.getAccountTaskName())) {
            if ("总核算值".equals(dto.getIndexName())) {
                //条件联查构造器
                LambdaQueryWrapper wrapper = Wrappers.<AdsIncomePerformanceLogisticsExtra>lambdaQuery()
                        .eq(AdsIncomePerformanceLogisticsExtra::getAccountPeriod, dto.getAccountTime())
                        .eq(AdsIncomePerformanceLogisticsExtra::getAccountUnitId, dto.getUnitId())
                        .eq(AdsIncomePerformanceLogisticsExtra::getEmpId, dto.getUserId());

                AdsIncomePerformanceLogisticsExtra adsIncomePerformanceLogisticsExtra = new AdsIncomePerformanceLogisticsExtra().selectOne(wrapper);

                vo.setTotalCount(adsIncomePerformanceLogisticsExtra.getJxgzhj().toString());
                vo.setDetails(adsIncomePerformanceLogisticsExtra);
            }
        }
        //医生医技工作量绩效明细
        else if (TaskTypeEnum.DOCTOR_TECH_WORKLOAD.getDescription().equals(dto.getAccountTaskName())) {
            if ("总核算值".equals(dto.getIndexName())) {
                //查询
                AdsDocWorkloadPerformanceAccounting docWorkloadPerformanceAccounting = new AdsDocWorkloadPerformanceAccounting().selectOne(new LambdaQueryWrapper<AdsDocWorkloadPerformanceAccounting>()
                        .eq(AdsDocWorkloadPerformanceAccounting::getAccountPeriod, dto.getAccountTime())
                        .eq(AdsDocWorkloadPerformanceAccounting::getAccountUnitId, dto.getUnitId()));
                if (docWorkloadPerformanceAccounting != null) {
                    vo.setTotalCount(docWorkloadPerformanceAccounting.getWorkloadPerformanceTotal().toString());
                    vo.setDetails(docWorkloadPerformanceAccounting);
                }
            }
        }
        //护理工作量绩效明细
        else if (TaskTypeEnum.NURSE_WORKLOAD.getDescription().equals(dto.getAccountTaskName())) {
            if ("总核算值".equals(dto.getIndexName())) {
                //查询
                AdsNurWorkloadPerformanceAccounting nurWorkloadPerformanceAccounting = new AdsNurWorkloadPerformanceAccounting().selectOne(new LambdaQueryWrapper<AdsNurWorkloadPerformanceAccounting>()
                        .eq(AdsNurWorkloadPerformanceAccounting::getAccountPeriod, dto.getAccountTime())
                        .eq(AdsNurWorkloadPerformanceAccounting::getAccountUnitId, dto.getUnitId()));
                if (nurWorkloadPerformanceAccounting != null) {
                    vo.setTotalCount(nurWorkloadPerformanceAccounting.getWorkloadPerformanceTotal().toString());
                    vo.setDetails(nurWorkloadPerformanceAccounting);
                }
            }
        }
        //其他核算单元绩效
        else if (TaskTypeEnum.OTHER_ACCOUNTING_UNIT_PERFORMANCE.getDescription().equals(dto.getAccountTaskName())) {
            if ("总核算值".equals(dto.getIndexName())) {
                //查询所有的
                List<AdsIncomePerformanceOther> allList = new AdsIncomePerformanceOther().selectList(new LambdaQueryWrapper<AdsIncomePerformanceOther>()
                        .eq(AdsIncomePerformanceOther::getAccountPeriod, dto.getAccountTime())
                        .eq(AdsIncomePerformanceOther::getAccountUnitId, dto.getUnitId()));
                OtherAccountingUnitPerformanceVo performanceVo = new OtherAccountingUnitPerformanceVo();
                AdsIncomePerformanceOther performanceOther = allList.get(0);
                performanceVo.setAccountUnitId(performanceOther.getAccountUnitId());
                performanceVo.setAccountUnitName(performanceOther.getKs());
                performanceVo.setKhdf(performanceOther.getKhdf());
                performanceVo.setYyjf(performanceOther.getYyjc());
                performanceVo.setTotal(performanceOther.getXj());
                //查询总成本,总收入
                AdsIncomePerformanceScoreDoc performanceScoreDoc = new AdsIncomePerformanceScoreDoc().selectOne(new LambdaQueryWrapper<AdsIncomePerformanceScoreDoc>()
                        .eq(AdsIncomePerformanceScoreDoc::getAccountPeriod, dto.getAccountTime())
                        .eq(AdsIncomePerformanceScoreDoc::getAccountDocId, performanceOther.getAccountUnitId()));
                if (performanceScoreDoc != null) {
                    performanceVo.setIncomeTotal(performanceScoreDoc.getIncomeTotal());
                    performanceVo.setCostTotal(performanceScoreDoc.getCostTotal());
                }
                for (AdsIncomePerformanceOther other : allList) {
                    switch (other.getKhxm()) {
                        case "中药代煎收入":
                            performanceVo.setZyfZydjsr(other.getSl());
                            break;
                        case "制膏量":
                            performanceVo.setZyfZgl(other.getSl());
                            break;
                        case "中药味数":
                            performanceVo.setZyfZyws(other.getSl());
                            break;
                        case "中药贴数":
                            performanceVo.setZyfZyts(other.getSl());
                            break;
                        case "临床药师会诊":
                            performanceVo.setLcyxLcyshz(other.getSl());
                            break;
                        case "临床药师查房":
                            performanceVo.setLcyxLcyscf(other.getSl());
                            break;
                        case "住院处方打回":
                            performanceVo.setLcyxZycfdh(other.getSl());
                            break;
                        case "用药监护":
                            performanceVo.setLcyxYyjh(other.getSl());
                            break;
                        case "事前审方":
                            performanceVo.setLcyxSqsf(other.getSl());
                            break;
                        case "点评门诊处方":
                            performanceVo.setLcyxDpmzcf(other.getSl());
                            break;
                        case "人工设置处方规则":
                            performanceVo.setLcyxRgszcfgz(other.getSl());
                            break;
                        case "住院病历":
                            performanceVo.setLcyxZybl(other.getSl());
                            break;
                        case "协定方加工":
                            performanceVo.setLfpzsXdfjg(other.getSl());
                            break;
                        case "临时加工":
                            performanceVo.setLfpzsLsjg(other.getSl());
                            break;
                        case "固定绩效":
                            if ("伦理秘书".equals(other.getKs())) {
                                performanceVo.setLlmsGdjx(other.getSl());
                            }
                            if ("制剂室".equals(other.getKs())) {
                                performanceVo.setZjsGdjx(other.getSl());
                            }
                            break;
                        case "服务人次":
                            performanceVo.setYykFwrc(other.getSl());
                            break;
                        case "住院床日":
                            performanceVo.setXyfZycr(other.getSl());
                            break;
                        case "门诊西药处方量":
                            performanceVo.setXyfMzxycfl(other.getSl());
                            break;
                        case "西药平均绩效80%":
                            performanceVo.setXyfXypjjx(other.getSl());
                            break;
                        case "基础绩效":
                            performanceVo.setYykJcjx(other.getSl());
                            break;
                        case "门诊非药物治疗开单":
                            performanceVo.setYzmzMzfywzlkd(other.getSl());
                            break;
                        case "门诊非药物治疗执行":
                            performanceVo.setYzmzMzfywzlzx(other.getSl());
                            break;
                        case "临方加工":
                            performanceVo.setYzmzLfjg(other.getSl());
                            break;
                        case "门诊诊察":
                            performanceVo.setYzmzMzzc(other.getSl());
                            break;
                        case "门诊中药贴数":
                            performanceVo.setYzmzMzzyts(other.getSl());
                            break;
                        case "退休业绩分":
                            performanceVo.setYzmzTxyjf(other.getSl());
                            break;
                        case "业绩分":
                            performanceVo.setYzmzYjf(other.getSl());
                            break;
                    }
                }
                if (performanceVo != null) {
                    vo.setTotalCount(performanceVo.getTotal().toString());
                    vo.setDetails(performanceVo);
                }
            }
        }
        //成本绩效
        else if (TaskTypeEnum.COST_PERFORMANCE.getDescription().equals(dto.getAccountTaskName())) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMM");
            //获取核算方案配置的指标
            CostAccountTaskNew costAccountTaskNew = new CostAccountTaskNew().selectById(dto.getAccountTaskId());
            final LocalDateTime accountStartTime = costAccountTaskNew.getAccountStartTime();
            String detailDim = accountStartTime.format(formatter);
            //获取核算单元id
            Long unitId = dto.getUnitId();
            List<Long> unitIdList = new ArrayList<>();
            unitIdList.add(unitId);

            if ("总核算值".equals(dto.getIndexName())) {
                //封装返回对象
                CostAccountTaskResultTotalValueVo taskResultTotalValueVo = new CostAccountTaskResultTotalValueVo();
                //根据任务id,任务分组id获取到核算单元集
                CostAccountTaskConfig taskConfig = new CostAccountTaskConfig().selectOne(new LambdaQueryWrapper<CostAccountTaskConfig>()
                        .eq(CostAccountTaskConfig::getTaskId, dto.getAccountTaskId())
                        .eq(CostAccountTaskConfig::getTaskGroupId, dto.getAccountTaskGroupId()));

                CostAccountPlanConfigFormula costAccountPlanConfigFormula = new CostAccountPlanConfigFormula();
                //根据科室单元id取出核算对象类型
                CostAccountUnit costAccountUnit = cacheUtils.getCostAccountUnit(unitId);
                LambdaQueryWrapper<CostAccountPlanConfigFormula> lambdaQueryWrapper = new LambdaQueryWrapper<CostAccountPlanConfigFormula>()
                        .eq(CostAccountPlanConfigFormula::getPlanId, taskConfig.getPlanId())
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
                            .eq(CostAccountPlanConfigFormula::getPlanId, taskConfig.getPlanId())
                            .eq(CostAccountPlanConfigFormula::getAccountObject, value)
                            .isNull(CostAccountPlanConfigFormula::getCustomUnitId);
                    //根据方案id取出公式
                    //CostAccountPlanConfigFormula costAccountPlanConfigFormula = new CostAccountPlanConfigFormula().selectOne(queryWrapper);
                    costAccountPlanConfigFormula = configFormulaMapper.selectOne(queryWrapper);
                }
                if (costAccountPlanConfigFormula != null) {
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
                    BigDecimal totalCost = BigDecimal.ZERO;
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
                        totalCost = totalCost.add(indexCount);

                    }
                    //                BigDecimal totalCost = sqlUtil.geTotalCount(unitIdList, detailDim);
                    //填充总值
                    taskResultTotalValueVo.setTotalValue(totalCost);
                    //填充总公式
                    taskResultTotalValueVo.setOverAllFormula(costAccountPlanConfigFormula.getPlanCostFormula());
                    taskResultTotalValueVo.setConfigIndexList(configIndexList);
                }
                vo.setDetails(taskResultTotalValueVo);
            } else {
                CostAccountTaskResultIndexProcessVo resultIndexProcessVo = new CostAccountTaskResultIndexProcessVo();
                CostAccountIndex costAccountIndex = new CostAccountIndex().selectById(dto.getIndexId());
                if (costAccountIndex != null) {
                    String indexFormula = costAccountIndex.getIndexFormula();
                    //先封装一部分数据
                    resultIndexProcessVo.setIndexId(costAccountIndex.getId());
                    resultIndexProcessVo.setConfigIndexName(costAccountIndex.getName());
                    resultIndexProcessVo.setIndexFormula(indexFormula);
                    List<CostAccountTaskResultIndexProcessVo> resultIndexProcessList = new ArrayList<>();
                    //看该核算指标下是否包含子集，包含子集的话就进行递归查询
                    List<CostIndexConfigIndex> costIndexConfigIndexList = new CostIndexConfigIndex().selectList(new LambdaQueryWrapper<CostIndexConfigIndex>().eq(CostIndexConfigIndex::getIndexId, dto.getIndexId()));
                    if (CollUtil.isNotEmpty(costIndexConfigIndexList)) {
                        for (CostIndexConfigIndex costIndexConfigIndex : costIndexConfigIndexList) {
                            if (costIndexConfigIndex.getConfigIndexId() != null) {
                                //封装子集核算指标进行递归查询
                                CostAccountTaskCalculateProcessDto recursionDto = new CostAccountTaskCalculateProcessDto();
                                recursionDto.setParentId(costIndexConfigIndex.getIndexId());
                                recursionDto.setTaskId(dto.getAccountTaskId());
                                recursionDto.setAccountUnitId(dto.getUnitId());
                                recursionDto.setBizId(costIndexConfigIndex.getConfigIndexId());
                                CostAccountTaskResultIndexProcessVo costAccountTaskResultIndexProcessVo = taskService.getNewCalculationProcess(recursionDto);
                                resultIndexProcessList.add(costAccountTaskResultIndexProcessVo);
                            }
                        }
                    }
                    List<CostAccountTaskItemVo> configItemList = new ArrayList<>();
                    //获取该指标下的所有核算项
                    List<CostIndexConfigItem> costIndexConfigItemList = new CostIndexConfigItem().selectList(new LambdaQueryWrapper<CostIndexConfigItem>()
                            .eq(CostIndexConfigItem::getIndexId, dto.getIndexId()));

                    for (CostIndexConfigItem costIndexConfigItem : costIndexConfigItemList) {
                        BigDecimal itemCount = sqlUtil.getItemCount(unitIdList, costAccountIndex.getName(), detailDim, costIndexConfigItem.getConfigId());
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
                    BigDecimal indexCount = sqlUtil.getIndexCount(detailDim, unitIdList, costAccountIndex.getName());
                    //最后封装返回的vo
                    resultIndexProcessVo.setConfigItemList(configItemList);
                    String bedBorrowName = "借床分摊";
                    String EndemicAreaName = "病区分摊";
                    String setOutpatientName = "门诊共用分摊";
                    String docNurseName = "医护分摊";

                    List<CostAccountTaskMedicalAllocationVo> medicalAllocation = new ArrayList<>();
                    CostAccountTaskMedicalAllocationVo allocationVo = new CostAccountTaskMedicalAllocationVo();
                    allocationVo.setMedicalAllocationValue(sqlUtil.getSharedCost(unitId, detailDim, costAccountIndex.getName(), docNurseName));
                    medicalAllocation.add(allocationVo);
                    resultIndexProcessVo.setMedicalAllocation(medicalAllocation);
                    resultIndexProcessVo.setBedBorrow(sqlUtil.getSharedCost(unitId, detailDim, costAccountIndex.getName(), bedBorrowName));
                    resultIndexProcessVo.setEndemicArea(sqlUtil.getSharedCost(unitId, detailDim, costAccountIndex.getName(), EndemicAreaName));
                    resultIndexProcessVo.setOutpatientShard(sqlUtil.getSharedCost(unitId, detailDim, costAccountIndex.getName(), setOutpatientName));
                    resultIndexProcessVo.setConfigIndexList(resultIndexProcessList);

                    //减去分摊的值，是原本的值
                    resultIndexProcessVo.setNoExtraIndexCount(indexCount
                            .subtract(sqlUtil.getSharedCost(unitId, detailDim, costAccountIndex.getName(), docNurseName))
                            .subtract(sqlUtil.getSharedCost(unitId, detailDim, costAccountIndex.getName(), bedBorrowName))
                            .subtract(sqlUtil.getSharedCost(unitId, detailDim, costAccountIndex.getName(), EndemicAreaName))
                            .subtract(sqlUtil.getSharedCost(unitId, detailDim, costAccountIndex.getName(), setOutpatientName))
                    );
                    resultIndexProcessVo.setIndexTotalValue(indexCount);

                }
                vo.setDetails(resultIndexProcessVo);
            }

        }
        return vo;
    }

    /**
     * 绩效核算结果展示(数据小组)
     *
     * @param taskResultQueryDto
     * @return
     */
    private CostAccountTaskResultDetailVo getPerformanceResult(TaskResultQueryDto taskResultQueryDto) {
        CostAccountTaskResultDetailVo costAccountTaskResultDetailVo = new CostAccountTaskResultDetailVo();
        //总的计算结果
        WholeAccountInfo wholeAccountInfo = new WholeAccountInfo();
        //String all = sqlUtil.executeSql(unitConfig, map);
        Page<UnitAccountInfo> unitAccountInfoPage = new Page<>(taskResultQueryDto.getCurrent(), taskResultQueryDto.getSize());
        //核算单元核算信息
        List<UnitAccountInfo> unitAccountInfoList = new ArrayList<>();
        //根据任务id获取到核算单元集
        Long taskId = taskResultQueryDto.getTaskId();
        Long taskGroupId = taskResultQueryDto.getTaskId();
        final CostAccountTaskConfig costAccountTaskConfig = new CostAccountTaskConfig().selectOne(new LambdaQueryWrapper<CostAccountTaskConfig>()
                .eq(CostAccountTaskConfig::getTaskId, taskId)
                .eq(CostAccountTaskConfig::getTaskGroupId, taskGroupId));
        //获取任务配置的方案
        CostAccountPlan costAccountPlan = new CostAccountPlan().selectById(costAccountTaskConfig.getPlanId());
        //获取方案的配置(核算对象类型:医生组/护理组)
        List<CostAccountPlanConfig> planConfigList = new CostAccountPlanConfig().selectList(new LambdaQueryWrapper<CostAccountPlanConfig>()
                .eq(CostAccountPlanConfig::getPlanId, costAccountPlan.getId()));
        return costAccountTaskResultDetailVo;

    }

    /**
     * 获取成本核算的结果
     *
     * @param taskResultQueryDto
     * @return
     */
    private CostAccountTaskResultDetailVo getCostResult(TaskResultQueryDto taskResultQueryDto) {
        CostAccountTaskResultDetailVo costAccountTaskResultDetailVo = new CostAccountTaskResultDetailVo();
        //获取核算方案配置的指标
        CostAccountTask costAccountTask = new CostAccountTask().selectById(taskResultQueryDto.getTaskId());
        String detailDim = costAccountTask.getDetailDim().replaceAll("[年月]", "");
        String redisKey = "KPI_" + detailDim + "_RESULT";

        String resultJson = redisTemplate.opsForValue().get(redisKey);
        if (resultJson != null) {
            // 如果结果存在于Redis中，则直接使用
            CostAccountTaskResultDetailVo result = JSON.parseObject(resultJson, CostAccountTaskResultDetailVo.class);
            return result;
        } else {
            //总的计算结果
            WholeAccountInfo wholeAccountInfo = new WholeAccountInfo();
            Page<UnitAccountInfo> unitAccountInfoPage = new Page<>(taskResultQueryDto.getCurrent(), taskResultQueryDto.getSize());
            //核算单元核算信息
            List<UnitAccountInfo> unitAccountInfoList = new ArrayList<>();
            //根据任务id获取到核算单元集
            Long taskId = taskResultQueryDto.getTaskId();
            //根据任务id获取总核算值
            List<Long> unitIds = Arrays.stream(new CostAccountTask().selectById(taskId).getUnitIds().split(",")).map(Long::parseLong).collect(Collectors.toList());


            List<AccountIndexCalculateInfo> accountIndexCalculateInfoList = new ArrayList<>();
            List<CostAccountPlanConfig> costAccountPlanConfigs = new CostAccountPlanConfig().selectList(new LambdaQueryWrapper<CostAccountPlanConfig>()
                    .eq(CostAccountPlanConfig::getPlanId, costAccountTask.getPlanId())
                    .select(CostAccountPlanConfig::getIndexId, CostAccountPlanConfig::getConfigIndexName)
                    .groupBy(CostAccountPlanConfig::getIndexId, CostAccountPlanConfig::getConfigIndexName));

            //查询关联的id
            //定义总值，递归查询获取任务总值
            AtomicReference<BigDecimal> totalCost = new AtomicReference<>(BigDecimal.ZERO);
            List<AccountIndexCalculateInfo> accountIndexCalculateInfo = newTotalRecursion(totalCost, unitIds, costAccountPlanConfigs, detailDim);
            accountIndexCalculateInfoList.addAll(accountIndexCalculateInfo);
            //填充任务名称和核算周期
            costAccountTaskResultDetailVo.setTaskName(costAccountTask.getAccountTaskName());
            costAccountTaskResultDetailVo.setAccountStartTime(costAccountTask.getAccountStartTime());
            costAccountTaskResultDetailVo.setAccountEndTime(costAccountTask.getAccountEndTime());
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
                //定义总值字段，用于递增
                AtomicReference<BigDecimal> unitCount = new AtomicReference<>(BigDecimal.ZERO);
                //根据任务id和核算单元id获取核算指标集合
                List<CalculateInfo> calculateInfo = newDetailRecursion(unitCount, unitId, deptCodeList, costAccountPlanConfigs, detailDim);
                unitAccountInfo.setCalculateInfo(calculateInfo);
                unitAccountInfo.setTotalCost(unitCount.get());
                unitAccountInfoList.add(unitAccountInfo);
            }
            //封装返回分页对象

            unitAccountInfoPage.setTotal(unitAccountInfoList.size());
            unitAccountInfoPage.setRecords(unitAccountInfoList);
            //封装核算任务结果详情Vo
            costAccountTaskResultDetailVo.setWholeAccountInfo(wholeAccountInfo);
            costAccountTaskResultDetailVo.setUnitAccountInfoList(unitAccountInfoPage);

            redisTemplate.opsForValue().set(redisKey, JSON.toJSONString(costAccountTaskResultDetailVo));
        }
        return costAccountTaskResultDetailVo;
    }

    /**
     * 获取成本核算外其他核算结果
     *
     * @param taskResultQueryDto
     * @return
     */
    private CostAccountTaskResultDetailVo getOtherTaskResult(TaskResultQueryDto taskResultQueryDto) {
        CostAccountTaskResultDetailVo costAccountTaskResultDetailVo = new CostAccountTaskResultDetailVo();
        //总的计算结果
        WholeAccountInfo wholeAccountInfo = new WholeAccountInfo();
        Page<UnitAccountInfo> unitAccountInfoPage = new Page<>(taskResultQueryDto.getCurrent(), taskResultQueryDto.getSize());
        //核算单元核算信息
        List<UnitAccountInfo> unitAccountInfoList = new ArrayList<>();
        //根据任务id获取到核算单元集
        Long taskId = taskResultQueryDto.getTaskId();
        //根据任务id获取总核算值
        List<Long> unitIds = Arrays.stream(new CostAccountTask().selectById(taskId).getUnitIds().split(",")).map(Long::parseLong).collect(Collectors.toList());

        return costAccountTaskResultDetailVo;
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

    //获取结果列表--数据小组数据
    @NotNull
    private List<String> getDeptCodeList(List<Long> unitIds) {
        List<String> relateIds = new CostUnitRelateInfo().selectList(new LambdaQueryWrapper<CostUnitRelateInfo>()
                .in(CostUnitRelateInfo::getAccountUnitId, unitIds)
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

}