package com.hscloud.hs.cost.account.listener;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.JsonObject;


import com.hscloud.hs.cost.account.constant.CacheConstants;
import com.hscloud.hs.cost.account.constant.enums.*;
import com.hscloud.hs.cost.account.mapper.*;
import com.hscloud.hs.cost.account.model.dto.TaskResultQueryDto;
import com.hscloud.hs.cost.account.model.entity.*;
import com.hscloud.hs.cost.account.model.pojo.*;
import com.hscloud.hs.cost.account.model.vo.*;
import com.hscloud.hs.cost.account.service.*;
import com.hscloud.hs.cost.account.utils.ExpressionCheckHelper;
import com.hscloud.hs.cost.account.utils.LocalCacheUtils;
import com.hscloud.hs.cost.account.utils.SqlUtil;
import com.pig4cloud.pigx.common.core.exception.BizException;
import com.pig4cloud.pigx.common.core.util.SpringContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Admin
 */
@Component
@Slf4j
public class TaskCalculateEventListener implements ApplicationListener<TaskCalculateEvent> {


    @Autowired
    private CostAccountUnitService costAccountUnitService;

    @Autowired
    private CostAccountPlanConfigService costAccountPlanConfigService;


    @Autowired
    private ICostAccountIndexService costAccountIndexService;

    @Autowired
    private ICostAllocationRuleService costAllocationRuleService;

    @Autowired
    private SqlUtil sqlUtil;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private CostAccountPlanConfigMapper costAccountPlanConfigMapper;

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
    private StringRedisTemplate redisTemplate;


    private final ExecutorService executorService = new ThreadPoolExecutor(8, 8,
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>(1), r -> {
        Thread thread = new Thread(r);
        thread.setName("task-calculate"+thread.getId());
        return thread;
    }, new ThreadPoolExecutor.CallerRunsPolicy());

    @Override
    @Async
    public void onApplicationEvent(@NotNull TaskCalculateEvent event) {
        Object source = event.getSource();
        long currented = System.currentTimeMillis();
        if (source instanceof CostAccountTask) {
            CostAccountTask costAccountTask = (CostAccountTask) source;
            // 任务计算
            try {
                //项计算
                CountDownLatch countDownLatch  = calculate(costAccountTask);
                countDownLatch.await();
                //指标计算
                CountDownLatch indexCountDownLatch  =  calculateIndexResult(costAccountTask);
                indexCountDownLatch.await();
                costAccountTask.setStatus(AccountTaskStatus.COMPLETED.getCode());
                //调用方法将CostTaskExecuteResultItem表缓存到redis中
                cacheRedisCostTaskExecuteResultItem(costAccountTask.getId());
            } catch (Exception e) {
                if (e instanceof BizException) {
                    BizException bizException = (BizException) e;
                    log.error("计算任务异常,任务id为{},异常原因为{}", costAccountTask.getId(), bizException.getDefaultMessage());
                    costAccountTask.setReason(bizException.getDefaultMessage());
                } else {
                    log.error("计算任务异常,任务id为{},异常原因为{}", costAccountTask.getId(), e);
                    costAccountTask.setReason(JSON.toJSONString(e));
                }
                costAccountTask.setStatus(AccountTaskStatus.EXCEPTION.getCode());
            }
            finally {
                // 修改状态
                costAccountTaskMapper.updateById(costAccountTask);
                log.info("计算任务耗时为{}", System.currentTimeMillis() - currented);
            }
        }
    }

    /**
     * 用于将CostTaskExecuteResultItem表中的数据、当前任务的展示结果放入到redis缓存中
     * @param taskId
     */
    private void cacheRedisCostTaskExecuteResultItem(Long taskId) {
        Gson gson = new Gson();
        HashMap<String, String> map = new HashMap<>();
        for (CostTaskExecuteResultItem costTaskExecuteResultItem : new CostTaskExecuteResultItem().selectAll()) {
            String value = gson.toJson(costTaskExecuteResultItem);
            String id = costTaskExecuteResultItem.getId().toString();
            map.put(id, value);
        }
        redisTemplate.opsForHash().putAll(CacheConstants.COST_TASK_EXECUTE_RESULT_ITEM, map);


        TaskResultQueryDto dto = new TaskResultQueryDto();
        dto.setTaskId(taskId);
        dto.setSize(1000L);
        dto.setCurrent(1L);
        CostAccountTaskResultDetailVo costAccountTaskResultDetailVo = costTaskExecuteResultService.listResult(dto);

        redisTemplate.opsForValue().set(CacheConstants.COST_TASK_RESULT +taskId, JSON.toJSONString(costAccountTaskResultDetailVo));
    }

    /**
     * 指标计算
     *
     * @param costAccountTask 任务信息
     * @return
     */
    public CountDownLatch calculateIndexResult(CostAccountTask costAccountTask) {

        String unitIds = costAccountTask.getUnitIds();
        List<Long> accountIds = Arrays.stream(unitIds.split(",")).map(Long::parseLong).collect(Collectors.toList());
        //根据核算单元id和核算方案id获取核算方案信息 只取与计算任务有关的配置数据
        //先取出自定义的数据
        List<CostPlanCalculateInfo> usefulConfigs = costAccountPlanConfigService.getCustomUsefulConfig(accountIds, costAccountTask.getPlanId());
        List<Long> unitIdList = usefulConfigs.stream().map(CostPlanCalculateInfo::getUnitId).collect(Collectors.toList());
        CountDownLatch countDownLatch = new CountDownLatch(accountIds.size());
        if (unitIdList.size() > 0) {
            //开始计算自定义的数据
            usefulConfigs.forEach(costPlanCalculateInfo -> {
                executorService.execute(() -> {
                    //计算指标的数据
                    SpringContextHolder.getBean(this.getClass()).doCalculateIndex(costAccountTask.getId(), costPlanCalculateInfo);
                    countDownLatch.countDown();
                });
            });

            accountIds.removeAll(unitIdList);
        }
        if (CollUtil.isNotEmpty(accountIds)) {
            //根据核算单元id获取核算单元信息以分组信息进行分组
            List<CostAccountUnit> costAccountUnits = costAccountUnitService.list(Wrappers.<CostAccountUnit>lambdaQuery().
                    in(CostAccountUnit::getId, accountIds));
            Map<String, List<CostAccountUnit>> map = costAccountUnits.stream().collect(Collectors.groupingBy(unit -> {
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    Map<String, String> jsonMap = mapper.readValue(unit.getAccountGroupCode(), Map.class);
                    return jsonMap.get("value");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }));
            List<String> groupCodes = map.keySet().stream().map(UnitMapEnum::getPlanGroup).collect(Collectors.toList());
            for (String groupCode : groupCodes) {
                if (groupCode == null) {
                    throw new BizException("暂无该组数据");
                }
            }
            //再计算组的数据
            List<CostPlanCalculateInfo> groupConfigs = costAccountPlanConfigService.getGroupUsefulConfig(groupCodes, costAccountTask.getPlanId());
            //开始计算组的数据
            groupConfigs.forEach(sourceCostPlanCalculateInfo -> {
                String group = sourceCostPlanCalculateInfo.getGroup();
                String unitGroup = UnitMapEnum.getUnitGroup(group);
                map.get(unitGroup).forEach(costAccountUnit ->
                        executorService.execute(() ->
                        {
                    CostPlanCalculateInfo costPlanCalculateInfo = BeanUtil.copyProperties(sourceCostPlanCalculateInfo, CostPlanCalculateInfo.class);
                    costPlanCalculateInfo.setUnitId(costAccountUnit.getId());
                    //计算组的数据
                    SpringContextHolder.getBean(this.getClass()).doCalculateIndex(costAccountTask.getId(), costPlanCalculateInfo);
                    countDownLatch.countDown();
                }));
            });
        }

        return countDownLatch;
    }

    /**
     * 任务计算
     *
     * @param costAccountTask 任务信息
     * @return
     */
    public CountDownLatch calculate(CostAccountTask costAccountTask) {
        String unitIds = costAccountTask.getUnitIds();
        List<Long> accountIds = Arrays.stream(unitIds.split(",")).map(Long::parseLong).collect(Collectors.toList());
        //根据核算单元id和核算方案id获取核算方案信息 只取与计算任务有关的配置数据
        //先取出自定义的数据
        CountDownLatch countDownLatch = new CountDownLatch(accountIds.size());
        List<CostPlanCalculateInfo> usefulConfigs = costAccountPlanConfigService.getCustomUsefulConfig(accountIds, costAccountTask.getPlanId());
        List<Long> unitIdList = usefulConfigs.stream().map(CostPlanCalculateInfo::getUnitId).collect(Collectors.toList());

        if (unitIdList.size() > 0) {
            usefulConfigs.forEach(costPlanCalculateInfo -> executorService.execute(() -> {
                //计算自定义的数据
                SpringContextHolder.getBean(this.getClass()).doCalculate(costAccountTask.getId(), costPlanCalculateInfo, DateUtil.format(costAccountTask.getAccountStartTime(), "YYYYMM"), DateUtil.format(costAccountTask.getAccountEndTime(), "YYYYMM"));
                countDownLatch.countDown();
            }));

            accountIds.removeAll(unitIdList);
        }
        if (CollUtil.isNotEmpty(accountIds)) {
            //根据核算单元id获取核算单元信息以分组信息进行分组
            List<CostAccountUnit> costAccountUnits = costAccountUnitService.list(Wrappers.<CostAccountUnit>lambdaQuery().
                    in(CostAccountUnit::getId, accountIds));
            Map<String, List<CostAccountUnit>> map = costAccountUnits.stream().collect(Collectors.groupingBy(unit -> {
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    Map<String, String> jsonMap = mapper.readValue(unit.getAccountGroupCode(), Map.class);
                    return jsonMap.get("value");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }));
            List<String> groupCodes = map.keySet().stream().map(UnitMapEnum::getPlanGroup).collect(Collectors.toList());
            for (String groupCode : groupCodes) {
                if (groupCode == null) {
                    throw new BizException("暂无该组数据");
                }
            }
            //再计算组的数据
            List<CostPlanCalculateInfo> groupConfigs = costAccountPlanConfigService.getGroupUsefulConfig(groupCodes, costAccountTask.getPlanId());
            //开始计算组的数据
            groupConfigs.forEach(sourceCostPlanCalculateInfo -> {
                String group = sourceCostPlanCalculateInfo.getGroup();
                String unitGroup = UnitMapEnum.getUnitGroup(group);
                map.get(unitGroup).forEach(costAccountUnit ->
                        executorService.execute(() ->
                        {
                    CostPlanCalculateInfo costPlanCalculateInfo = BeanUtil.copyProperties(sourceCostPlanCalculateInfo, CostPlanCalculateInfo.class);
                    costPlanCalculateInfo.setUnitId(costAccountUnit.getId());
                    //计算组的数据
                    SpringContextHolder.getBean(this.getClass()).doCalculate(costAccountTask.getId(), costPlanCalculateInfo, DateUtil.format(costAccountTask.getAccountStartTime(), "YYYYMM"), DateUtil.format(costAccountTask.getAccountEndTime(), "YYYYMM"));
                    countDownLatch.countDown();
                }));
            });
        }


        return countDownLatch;
    }

    @SuppressWarnings("SimplifyForEach")
    @Transactional(rollbackFor = Exception.class)
    public void doCalculateIndex(Long taskId, CostPlanCalculateInfo costPlanCalculateInfo) {
        Long startTime = System.currentTimeMillis();
        Long unitId = costPlanCalculateInfo.getUnitId();
        if (unitId != null) {
            List<CostFormulaInfo> newCostFormulaInfos = costPlanCalculateInfo.getCostFormulaInfos();
            newCostFormulaInfos.forEach(costFormulaInfo -> {
                //方案的配置id
                Long planConfigId = costFormulaInfo.getId();
                //根据方案的配置id获取方案的配置信息
                //方案的配置信息
                List<CostAccountPlanConfigIndexNew> costAccountPlanConfigIndexNews = new CostAccountPlanConfigIndexNew().selectList(Wrappers.<CostAccountPlanConfigIndexNew>lambdaQuery().
                        eq(CostAccountPlanConfigIndexNew::getPlanConfigId, planConfigId));
                //计算外层指标
                calculatePrentIndex(costAccountPlanConfigIndexNews, taskId, unitId);
            });
        }
        //任务的核算方案解析计算入库
        saveToTaskExecuteResult(taskId, unitId);
        log.info("doCalculateIndex耗时为{}",  System.currentTimeMillis() - startTime);
    }

    @SuppressWarnings("SimplifyForEach")
    @Transactional(rollbackFor = Exception.class)
    public void doCalculate(Long taskId, CostPlanCalculateInfo costPlanCalculateInfo, String accountStartTime, String accountEndTime) {
        Long startTime = System.currentTimeMillis();
        Long unitId = costPlanCalculateInfo.getUnitId();
        if (unitId != null) {
            List<CostFormulaInfo> costFormulaInfos = costPlanCalculateInfo.getCostFormulaInfos();
            costFormulaInfos.forEach(costFormulaInfo -> {
                //方案的配置id
                Long planConfigId = costFormulaInfo.getId();
                //根据方案的配置id获取方案的配置信息
                //方案的配置信息
                List<CostAccountPlanConfigIndexNew> costAccountPlanConfigIndexNews = new CostAccountPlanConfigIndexNew().selectList(Wrappers.<CostAccountPlanConfigIndexNew>lambdaQuery().
                        eq(CostAccountPlanConfigIndexNew::getPlanConfigId, planConfigId));
                //先算出所有指标项的值
                costAccountPlanConfigIndexNews.stream().map(s -> {
                    //指标项的id
                    Long itemId = s.getItemId();
                    //先获取指标项的计算维度信息
                    CostAccountItem costAccountItem = cacheUtils.getCostAccountItem(itemId);
                    String dimension = costAccountItem.getDimension();
                    try {
                        Gson gson = new Gson();
                        JsonObject jsonObject = gson.fromJson(dimension, JsonObject.class);
                        dimension = jsonObject.get("value").getAsString();
                    } catch (Exception e) {
                        log.error("核算项的核算维度解析失败,核算项id为{},失败原因为{}", costAccountItem.getId(), e.getMessage());
                    }
                    //填充配置信息 核算项自身的配置 核算项在方案里中的配置 自定义科室单元不包含各种分摊
                    CostIndexConfigItem costIndexConfigItem = new CostIndexConfigItem().selectOne(Wrappers.<CostIndexConfigItem>lambdaQuery().
                            eq(CostIndexConfigItem::getConfigKey, s.getConfigKey()).
                            eq(CostIndexConfigItem::getConfigId, itemId));
                    ConfigCorrespondenceItem configCorrespondenceItem = new ConfigCorrespondenceItem();
                    BeanUtils.copyProperties(s, configCorrespondenceItem);
                    configCorrespondenceItem.setConfigId(planConfigId);
                    configCorrespondenceItem.setReservedDecimal(costAccountItem.getRetainDecimal());
                    configCorrespondenceItem.setCarryRule(costAccountItem.getCarryRule());
                    configCorrespondenceItem.setUnit(costAccountItem.getMeasureUnit());
                    configCorrespondenceItem.setConfigDesc(costIndexConfigItem.getConfigDesc());
                    configCorrespondenceItem.setConfigName(costIndexConfigItem.getConfigName());
                    //todo 被分摊对象要根据原型来做调整
                    String accountRange = s.getAccountRange();
                    AccountRangeEnum enumValue = AccountRangeEnum.fromPlanGroup(accountRange);
                    switch (enumValue) {
                        case THIS_DEPT_UNIT:
                            //本科室单元
                            //先根据科室单元id获取科室单元关联关系

                            String accountObject = cacheUtils.getCostAccountUnit(unitId).getName();
                            calculateThisUnit(taskId, s, accountObject, costIndexConfigItem, unitId, configCorrespondenceItem, dimension, accountStartTime, accountEndTime);
                            break;
                        case ALL:
                            //全院 从数据小组那边获取数据
                            String allObject = "全院";
                            calculateAll(taskId, s, allObject, costIndexConfigItem, unitId, configCorrespondenceItem, dimension, accountStartTime, accountEndTime);
                            break;
                        case DOCTOR_NURSE_DEPT_UNIT:
                            //医护对应科室单元
                            StringBuilder docNurseObjects = new StringBuilder();
                            //获取医护对应科室单元
                            for (CostDocNRelation costDocNRelation : new CostDocNRelation().selectList(new LambdaQueryWrapper<CostDocNRelation>().eq(CostDocNRelation::getDocAccountGroupId, unitId))) {
                                String name = cacheUtils.getCostAccountUnit(costDocNRelation.getNurseAccountGroupId()).getName();
                                docNurseObjects.append(name);
                            }
                            calculateDocNurseUnit(taskId, s, docNurseObjects.toString(), costIndexConfigItem, unitId, configCorrespondenceItem, dimension, accountStartTime, accountEndTime);
                            break;
                        case CUSTOM_DEPT_UNIT:
                            //获取自定义科室单元的被分摊核算对象
                            String customDeptNames = getStringBuilder(s);
                            //自定义科室单元
                            calculateCustomUnit(taskId, s, customDeptNames, costIndexConfigItem, unitId, configCorrespondenceItem, dimension, accountStartTime, accountEndTime);
                            break;
                        case CUSTOM_DEPT:
                            //获取自定义科室的被分摊核算对象
                            String deptNames = getStringBuilder(s);
                            //自定义科室
                            calculateCustomDept(taskId, s, deptNames, costIndexConfigItem, unitId, configCorrespondenceItem, dimension, accountStartTime, accountEndTime);
                            break;
                        case CUSTOM_PEOPLE:
                            //自定义人员
                            String userNames = getStringBuilder(s);
                            calculateCustomUser(taskId, s, userNames, costIndexConfigItem, unitId, configCorrespondenceItem, dimension, accountStartTime, accountEndTime);
                            break;
                        default:
                            break;
                    }
                    //指标项的公式
                    //指标项的值
                    Double indexValue = 0.0;
                    //TODO
                    return indexValue;
                }).collect(Collectors.toList());

            });
        } else {
            //计算组的数据
            //TODO
        }
        log.info("doCalculate耗时为{}",  System.currentTimeMillis() - startTime);
    }

    /**
     * 获取自定义科室单元、自定义科室、自定义人员的被分摊核算对象
     *
     * @param s
     * @return
     */
    @NotNull
    private String getStringBuilder(CostAccountPlanConfigIndexNew s) {
        StringBuilder customNames = new StringBuilder();
        List<JSONObject> deptList = JSON.parseArray(s.getCustomInfo(), JSONObject.class);
        for (int i = 0; i < deptList.size(); i++) {
            JSONObject jsonObject = deptList.get(i); // 获取每个元素的 JSON 对象
            String name = jsonObject.getStr("name"); // 获取部门名称
            customNames.append(name);

            if (i < deptList.size() - 1) {
                customNames.append(",");
            }
        }
        return customNames.toString();
    }


    private void calculateAll(Long taskId, CostAccountPlanConfigIndexNew s, String accountObject, CostIndexConfigItem costIndexConfigItem, Long unitId, ConfigCorrespondenceItem configCorrespondenceItem, String dimension, String accountStartTime, String accountEndTime) {
        ValidatorResultVo validatorResultVo = costAccountIndexService.verificationItem(costIndexConfigItem.getId(), accountStartTime, accountEndTime, null, AccountObject.KPI_OBJECT_ALL);
        String result = validatorResultVo.getResult();
        //若计算结果为空,则表示表中没有数据(未上报等原因),结果置为0并保存到异常表中
        if (StrUtil.isBlank(result)) {
            result = "0.0";
            CostTaskExecuteResultExceptionItem costTaskExecuteResultExceptionItem=new CostTaskExecuteResultExceptionItem();
            costTaskExecuteResultExceptionItem.setTaskId(taskId);
            costTaskExecuteResultExceptionItem.setUnitId(unitId);
            costTaskExecuteResultExceptionItem.setItemId(costIndexConfigItem.getId());
            costTaskExecuteResultExceptionItem.setIndexId(s.getIndexId());
            costTaskExecuteResultExceptionItem.setItemName(costIndexConfigItem.getConfigName());
            costTaskExecuteResultExceptionItem.setPath(s.getPath());
            costTaskExecuteResultExceptionItem.setConfigKey(costIndexConfigItem.getConfigKey());
            costTaskExecuteResultExceptionItem.setAccountPeriod(costIndexConfigItem.getAccountPeriod());
            costTaskExecuteResultExceptionItem.setTimePeriod(accountStartTime + "-" + accountEndTime);
            costTaskExecuteResultExceptionItem.setExceptionReason(validatorResultVo.getErrorMsg());
            costTaskExecuteResultExceptionItem.insert();
        }

        ItemCalculateDetail itemCalculateDetail = new ItemCalculateDetail();
        itemCalculateDetail.setCalculatedValue(new BigDecimal(result));
        itemCalculateDetail.setBizId("");
        itemCalculateDetail.setBizName("全院");
        dealCommon(costIndexConfigItem, taskId, unitId, s, accountObject, new BigDecimal(result), accountStartTime, accountEndTime, configCorrespondenceItem, Lists.newArrayList(itemCalculateDetail));
    }


    private void calculatePrentIndex(List<CostAccountPlanConfigIndexNew> costAccountPlanConfigIndexNews, Long taskId, Long unitId) {
        //计算指标的值
        //获取配置指标的id集合
        long current = System.currentTimeMillis();
        List<Long> indexIds = costAccountPlanConfigIndexNews
                .stream().collect(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(s -> s.getIndexId()))))
                .stream().map(CostAccountPlanConfigIndexNew::getIndexId).collect(Collectors.toList());
        //遍历求值
        indexIds.forEach(indexId -> {
            long startTime = System.currentTimeMillis();
            String result = "";
            String noExtraResult = "";
            List<Long> items = new ArrayList<>();
            Map<String, Double> map = new HashMap<>();
            Map<String, Double> noExtraMap = new HashMap<>();
            List<IndexFormulaCalculateDetail.ConfigObject> configObjectList = new ArrayList<>();
            //先获取指标的计算维度信息
            CostAccountIndex costAccountIndex = new CostAccountIndex().selectById(indexId);
            Set<String> keys = ExpressionCheckHelper.expressionQuantity(costAccountIndex.getIndexFormula());
            String path = "";
            BigDecimal itemData;
            for (String key : keys) {
                IndexFormulaCalculateDetail.ConfigObject configObject = new IndexFormulaCalculateDetail.ConfigObject();
                //根据key匹配核算项id
                CostIndexConfigItem costIndexConfigItem = new CostIndexConfigItem().selectOne(new LambdaQueryWrapper<CostIndexConfigItem>().eq(CostIndexConfigItem::getConfigKey, key));
                if (costIndexConfigItem != null) {
                    CostAccountItem costAccountItem = new CostAccountItem().selectOne(new LambdaQueryWrapper<CostAccountItem>()
                            .eq(CostAccountItem::getId, costIndexConfigItem.getConfigId()));
                    //判断是否是配置项
                    if (costAccountItem != null) {
                        //是配置项,根据任务id,核算单元id,指标id,配置项id,核算项路径path拿到唯一核算值

                        //最外层,path为null
                        CostTaskExecuteResultItem costTaskExecuteResultItem = new CostTaskExecuteResultItem().selectOne(new LambdaQueryWrapper<CostTaskExecuteResultItem>()
                                .eq(CostTaskExecuteResultItem::getItemId, costAccountItem.getId())
                                .eq(CostTaskExecuteResultItem::getTaskId, taskId)
                                .eq(CostTaskExecuteResultItem::getUnitId, unitId)
                                .isNull(CostTaskExecuteResultItem::getPath)
                                .eq(CostTaskExecuteResultItem::getIndexId, indexId));

                        //是配置项,根据任务id,核算单元id,指标id,配置项id,核算项路径path拿到分摊集合
                        CostTaskExecuteResultRule costTaskExecuteResultRule = new CostTaskExecuteResultRule().selectOne(new LambdaQueryWrapper<CostTaskExecuteResultRule>()
                                .eq(CostTaskExecuteResultRule::getItemId, costTaskExecuteResultItem.getId()));

                        if (costTaskExecuteResultRule == null) {
                            configObject.setTotalValue(costTaskExecuteResultItem.getCalculateCount());
                            itemData = costTaskExecuteResultItem.getCalculateCount();
                        } else {
                            //计算各种分摊的费用
                            configObject.setTotalValue(costTaskExecuteResultItem.getCalculateCount().multiply(costTaskExecuteResultRule.getRuleCount()));
                            itemData = costTaskExecuteResultItem.getCalculateCount().multiply(costTaskExecuteResultRule.getRuleCount());
                        }
                        noExtraMap.put(key, itemData.doubleValue());
                        //查询医护,门诊,病区,借床分摊
                        final CostAccountUnit costAccountUnit = cacheUtils.getCostAccountUnit(unitId);
                        BigDecimal resultExtra;
                        //构造查询条件
                        LambdaQueryWrapper<CostTaskExecuteResultExtra> lqw = new LambdaQueryWrapper<CostTaskExecuteResultExtra>()
                                .eq(CostTaskExecuteResultExtra::getTaskId, taskId)
                                //.eq(CostTaskExecuteResultExtra::getIndexId, indexId)
                                //.eq(CostTaskExecuteResultExtra::getItemId, costTaskExecuteResultItem.getId())
                                .eq(CostTaskExecuteResultExtra::getDivideUnitId, unitId);
                        // .isNull(CostTaskExecuteResultExtra::getPath);
//                        //如果不是护理组组查询分摊表获取医护成本分摊成本,分摊到对应的护理组
//                        if (!new JSONObject(costAccountUnit.getAccountGroupCode()).getStr("label").equals(UnitMapEnum.NURSE.getDesc())) {
//                            lqw.in(CostTaskExecuteResultExtra::getDivideType, "jc", "bq", "mz");
//                        } else {
//                            //如果是护理组查询分摊表获取借床,病区成本分摊成本,分摊到对应的医生组
//                            lqw.in(CostTaskExecuteResultExtra::getDivideType, "yh");
//                        }
                        //查询分摊对象
                        final List<CostTaskExecuteResultExtra> costTaskExecuteResultExtraList = new CostTaskExecuteResultExtra().selectList(lqw);
                        //若存在分摊,则将值累加并算入该项
                        if (costTaskExecuteResultExtraList.isEmpty()) {
                            resultExtra = new BigDecimal(0.0);
                            itemData = itemData.add(resultExtra);
                        } else {
                            for (CostTaskExecuteResultExtra costTaskExecuteResultExtra : costTaskExecuteResultExtraList) {
                                if (new CostTaskExecuteResultItem().selectById(costTaskExecuteResultExtra.getItemId()).getItemId().equals(costTaskExecuteResultItem.getItemId())) {
                                    itemData = itemData.add(costTaskExecuteResultExtra.getDivideCountAfter());
                                }
                            }
                        }
                        //封装map
                        map.put(key, itemData.doubleValue());
                        //封装明细
                        configObject.setConfigKey(key);
                        configObject.setId(costAccountItem.getId());
                        configObject.setName(costAccountItem.getAccountItemName());
                        configObject.setType(CalculateEnum.ITEM.getType());
                        configObjectList.add(configObject);
                        //添加id到集合
                        items.add(costTaskExecuteResultItem.getId());
                    }
                } else {
                    //根据key匹配核算指标
                    CostIndexConfigIndex costIndexConfigIndex = new CostIndexConfigIndex().selectOne(new LambdaQueryWrapper<CostIndexConfigIndex>().eq(CostIndexConfigIndex::getConfigKey, key));
                    CostAccountIndex accountIndex = new CostAccountIndex().selectOne(new LambdaQueryWrapper<CostAccountIndex>()
                            .eq(CostAccountIndex::getId, costIndexConfigIndex.getConfigIndexId()));
                    //不是配置项,就是指标,调用方法查询
                    long startTime1 = System.currentTimeMillis();
                    String index = calculateIndex(key, indexId, key, accountIndex, taskId, unitId, indexId);
                    log.info("计算小指标耗时为{}", System.currentTimeMillis() - startTime1);
                    //封装明细
                    configObject.setConfigKey(key);
                    configObject.setId(accountIndex.getId());
                    configObject.setName(accountIndex.getName());
                    configObject.setFormula(accountIndex.getIndexFormula());
                    configObject.setType(CalculateEnum.INDEX.getType());
                    configObject.setTotalValue(new BigDecimal(index));
                    configObjectList.add(configObject);
                    //添加map
                    map.put(key, Double.valueOf(index));
                    noExtraMap.put(key, Double.valueOf(index));
                }
            }
            //计算值
            try {
                String newExpression = costAccountIndex.getIndexFormula().replace("%", "/100");
                result = ExpressionCheckHelper.checkAndCalculate(map, newExpression, null, null, null);
                noExtraResult = ExpressionCheckHelper.checkAndCalculate(noExtraMap, newExpression, null, null, null);
            } catch (Exception e) {
                if (e instanceof BizException) {
                    BizException bizException = (BizException) e;
                    log.error("指标计算异常,指标id为{},异常原因为{}", costAccountIndex.getId(), bizException.getDefaultMessage());
                } else {
                    log.error("指标计算异常,指标id为{},异常原因为{}", costAccountIndex.getId(), e);
                }
            }
            //封装核算指标计算的公式
            IndexFormulaObject indexFormulaObject = new IndexFormulaObject();
            indexFormulaObject.setIndexFormula(costAccountIndex.getIndexFormula());
            //封装核算指标计算明细
            IndexFormulaCalculateDetail calculateDetail = new IndexFormulaCalculateDetail();
            calculateDetail.setTotalValue(result);
            calculateDetail.setOverAllFormula(costAccountIndex.getIndexFormula());
            calculateDetail.setConfigIndexList(configObjectList);


            //写入cost_task_execute_result_index表
            CostTaskExecuteResultIndex costTaskExecuteResultIndex = new CostTaskExecuteResultIndex();
            costTaskExecuteResultIndex.setIndexId(costAccountIndex.getId());
            costTaskExecuteResultIndex.setTaskId(taskId);
            costTaskExecuteResultIndex.setIndexCount(new BigDecimal(result));
            costTaskExecuteResultIndex.setNoExtraIndexCount(new BigDecimal(noExtraResult));
            costTaskExecuteResultIndex.setCalculateDetail(new Gson().toJson(calculateDetail));
            costTaskExecuteResultIndex.setIndexName(costAccountIndex.getName());
            costTaskExecuteResultIndex.setCalculateFormulaDesc(new Gson().toJson(indexFormulaObject));
            costTaskExecuteResultIndex.setItems(items.toString());
            costTaskExecuteResultIndex.setParentId(0L);
            costTaskExecuteResultIndex.setUnitId(unitId);
            costTaskExecuteResultIndex.setPath(path);
            costTaskExecuteResultIndex.insert();
            log.info("计算指标耗时为{}", System.currentTimeMillis() - startTime);
        });
        log.info("计算指标总耗时为{}", System.currentTimeMillis() - current);
    }


    @Transactional(rollbackFor = Exception.class)
    public String calculateIndex(String path, Long parentId, String parentKey, CostAccountIndex costAccountIndex, Long taskId, Long unitId, Long indexId) {
        String result = "";
        String noExtraResult = "";
        List<Long> items = new ArrayList<>();
        Map<String, Double> map = new HashMap<>();
        Map<String, Double> noExtraMap = new HashMap<>();
        List<IndexFormulaCalculateDetail.ConfigObject> configObjectList = new ArrayList<>();
        String newPath;
        BigDecimal itemData = new BigDecimal(0.0);
        //先获取指标的计算维度信息
        //解析指标
        Set<String> keys = ExpressionCheckHelper.expressionQuantity(costAccountIndex.getIndexFormula());
        for (String key : keys) {
            IndexFormulaCalculateDetail.ConfigObject configObject = new IndexFormulaCalculateDetail.ConfigObject();
            //根据key匹配核算项id
            CostIndexConfigItem costIndexConfigItem = new CostIndexConfigItem().selectOne(new LambdaQueryWrapper<CostIndexConfigItem>().eq(CostIndexConfigItem::getConfigKey, key));
            if (costIndexConfigItem != null) {
                CostAccountItem costAccountItem = new CostAccountItem().selectOne(new LambdaQueryWrapper<CostAccountItem>()
                        .eq(CostAccountItem::getId, costIndexConfigItem.getConfigId()));
                //判断是否是配置项
                if (costAccountItem != null) {
                    //是配置项,根据任务id,核算单元id,指标id,配置项id拿到唯一的值
                    LambdaQueryWrapper<CostTaskExecuteResultItem> wrapper = new LambdaQueryWrapper<CostTaskExecuteResultItem>()
                            .eq(CostTaskExecuteResultItem::getItemId, costAccountItem.getId())
                            .eq(CostTaskExecuteResultItem::getTaskId, taskId)
                            .eq(CostTaskExecuteResultItem::getUnitId, unitId);
                    if (path == null) {
                        wrapper.eq(CostTaskExecuteResultItem::getIndexId, costIndexConfigItem.getIndexId()).isNull(CostTaskExecuteResultItem::getPath);
                    } else {
                        wrapper.eq(CostTaskExecuteResultItem::getPath, path);
                    }
                    CostTaskExecuteResultItem costTaskExecuteResultItem = new CostTaskExecuteResultItem().selectOne(wrapper);

                    //是配置项,根据任务id,核算单元id,指标id,配置项id,核算项路径path拿到分摊集合
                    CostTaskExecuteResultRule costTaskExecuteResultRule = new CostTaskExecuteResultRule().selectOne(new LambdaQueryWrapper<CostTaskExecuteResultRule>()
                            .eq(CostTaskExecuteResultRule::getItemId, costTaskExecuteResultItem.getId()));

                    if (costTaskExecuteResultRule == null) {
                        configObject.setTotalValue(costTaskExecuteResultItem.getCalculateCount());
                        itemData = costTaskExecuteResultItem.getCalculateCount();
                    } else {
                        //计算各种分摊的费用
                        configObject.setTotalValue(costTaskExecuteResultItem.getCalculateCount().multiply(costTaskExecuteResultRule.getRuleCount()));
                        itemData = costTaskExecuteResultItem.getCalculateCount().multiply(costTaskExecuteResultRule.getRuleCount());
                    }
                    noExtraMap.put(key, itemData.doubleValue());
                    //查询医护,门诊,病区,借床分摊
                    BigDecimal resultExtra = new BigDecimal(0.0);
                    //构造查询条件
                    LambdaQueryWrapper<CostTaskExecuteResultExtra> lqw = new LambdaQueryWrapper<CostTaskExecuteResultExtra>()
                            .eq(CostTaskExecuteResultExtra::getTaskId, taskId)
                            //.eq(CostTaskExecuteResultExtra::getIndexId, indexId)
                            //.eq(CostTaskExecuteResultExtra::getItemId, costTaskExecuteResultItem.getId())
                            .eq(CostTaskExecuteResultExtra::getDivideUnitId, unitId);
                    // .eq(CostTaskExecuteResultExtra::getPath,path);
//                    //如果不是护理组组查询分摊表获取医护成本分摊成本,分摊到对应的护理组
//                    if (new JSONObject(costAccountUnit.getAccountGroupCode()).getStr("label").equals(UnitMapEnum.NURSE.getDesc())) {
//                        lqw.in(CostTaskExecuteResultExtra::getDivideType, "jc", "bq", "mz");
//                    } else {
//                        //如果是护理组查询分摊表获取借床,病区成本分摊成本,分摊到对应的医生组
//                        lqw.in(CostTaskExecuteResultExtra::getDivideType, "yh");
//                    }
                    //查询分摊对象
                    final List<CostTaskExecuteResultExtra> costTaskExecuteResultExtraList = new CostTaskExecuteResultExtra().selectList(lqw);
                    //若存在分摊,则将值累加并算入该项
                    if (costTaskExecuteResultExtraList.isEmpty()) {
                        resultExtra = new BigDecimal(0.0);
                        itemData = itemData.add(resultExtra);
                    } else {
                        for (CostTaskExecuteResultExtra costTaskExecuteResultExtra : costTaskExecuteResultExtraList) {
                            if (new CostTaskExecuteResultItem().selectById(costTaskExecuteResultExtra.getItemId()).getItemId().equals(costTaskExecuteResultItem.getItemId())) {
                                itemData = itemData.add(costTaskExecuteResultExtra.getDivideCountAfter());
                            }
                        }
                    }
                    //封装map
                    map.put(key, itemData.doubleValue());
                    //封装明细
                    configObject.setConfigKey(key);
                    configObject.setId(costAccountItem.getId());
                    configObject.setName(costAccountItem.getAccountItemName());
                    configObject.setType(CalculateEnum.ITEM.getType());
                    configObjectList.add(configObject);
                    //封装map
                    items.add(costTaskExecuteResultItem.getId());
                }
            } else {
                //根据key匹配核算指标
                newPath = path + "," + key;
                CostIndexConfigIndex costIndexConfigIndex = new CostIndexConfigIndex().selectOne(new LambdaQueryWrapper<CostIndexConfigIndex>().eq(CostIndexConfigIndex::getConfigKey, key));
                CostAccountIndex accountIndex = new CostAccountIndex().selectOne(new LambdaQueryWrapper<CostAccountIndex>()
                        .eq(CostAccountIndex::getId, costIndexConfigIndex.getConfigIndexId()));
                String index = calculateIndex(newPath, costAccountIndex.getId(), key, accountIndex, taskId, unitId, indexId);
                //封装明细
                configObject.setConfigKey(key);
                configObject.setId(accountIndex.getId());
                configObject.setName(accountIndex.getName());
                configObject.setFormula(accountIndex.getIndexFormula());
                configObject.setType(CalculateEnum.INDEX.getType());
                configObject.setTotalValue(new BigDecimal(index));
                configObjectList.add(configObject);
                map.put(key, Double.valueOf(index));
                noExtraMap.put(key, Double.valueOf(index));
            }
        }
        //计算值
        try {
            String newExpression = costAccountIndex.getIndexFormula().replace("%", "/100");
            result = ExpressionCheckHelper.checkAndCalculate(map, newExpression, null, null, null);
            noExtraResult = ExpressionCheckHelper.checkAndCalculate(noExtraMap, newExpression, null, null, null);
        } catch (Exception e) {
            if (e instanceof BizException) {
                BizException bizException = (BizException) e;
                log.error("指标计算异常,指标id为{},异常原因为{}", costAccountIndex.getId(), bizException.getDefaultMessage());
            }
            {
                log.error("指标计算异常,指标id为{},异常原因为{}", costAccountIndex.getId(), e.getMessage());
            }
        }
        //封装核算指标计算的公式
        IndexFormulaObject indexFormulaObject = new IndexFormulaObject();
        indexFormulaObject.setConfigKey(parentKey);
        indexFormulaObject.setIndexFormula(costAccountIndex.getIndexFormula());
        //封装核算指标计算明细
        IndexFormulaCalculateDetail calculateDetail = new IndexFormulaCalculateDetail();
        calculateDetail.setTotalValue(result);
        calculateDetail.setOverAllFormula(costAccountIndex.getIndexFormula());
        calculateDetail.setConfigIndexList(configObjectList);
        //写入cost_task_execute_result_index表
        CostTaskExecuteResultIndex costTaskExecuteResultIndex = new CostTaskExecuteResultIndex();
        costTaskExecuteResultIndex.setIndexId(costAccountIndex.getId());
        costTaskExecuteResultIndex.setTaskId(taskId);
        costTaskExecuteResultIndex.setIndexCount(new BigDecimal(result));
        costTaskExecuteResultIndex.setNoExtraIndexCount(new BigDecimal(noExtraResult));
        costTaskExecuteResultIndex.setCalculateDetail(new Gson().toJson(calculateDetail));
        costTaskExecuteResultIndex.setIndexName(costAccountIndex.getName());
        costTaskExecuteResultIndex.setCalculateFormulaDesc(new Gson().toJson(indexFormulaObject));
        costTaskExecuteResultIndex.setItems(items.toString());
        costTaskExecuteResultIndex.setParentId(parentId);
        costTaskExecuteResultIndex.setUnitId(unitId);
        costTaskExecuteResultIndex.setPath(path);
        costTaskExecuteResultIndex.insert();
        //返回结果
        return result;
    }


    private void calculateThisUnit(Long taskId, CostAccountPlanConfigIndexNew configIndexNew, String accountObject, CostIndexConfigItem costIndexConfigItem, Long unitId, ConfigCorrespondenceItem configCorrespondenceItem, String dimension, String accountStartTime, String accountEndTime) {
        //根据科室单元id和计算维度获取科室单元关联关系
        if (ItemDimensionEnum.DEPT.getCode().equals(dimension)) {
            //获取该科室单元下所有的科室
            List<CostUnitRelateInfo> deptList = new CostUnitRelateInfo().selectList(Wrappers.<CostUnitRelateInfo>lambdaQuery().
                    eq(CostUnitRelateInfo::getAccountUnitId, unitId).
                    eq(CostUnitRelateInfo::getType, "dept"));
            List<Long> deptIds = deptList.stream().map(CostUnitRelateInfo::getRelateId).map(Long::parseLong).collect(Collectors.toList());
            if (CollectionUtil.isEmpty(deptIds)) {
                log.error("该科室单元下没有科室,科室单元id为{}", unitId);
                throw new BizException("该科室单元下没有科室,科室单元id为"+unitId);
            }
            Map<Long, String> map = sqlUtil.getDeptCodesByDeptIds(deptIds);
            //获取该科室单元下所有的科室的计算明细
            List<ItemCalculateDetail> itemCalculateDetails = new ArrayList<>();
            final BigDecimal[] itemCalculateTotalValue = {new BigDecimal(0)};
            deptList.forEach(dept -> {
                ValidatorResultVo validatorResultVo = costAccountIndexService.verificationItem(costIndexConfigItem.getId(), accountStartTime, accountEndTime, map.get(Long.parseLong(dept.getRelateId())), ItemDimensionEnum.DEPT.getCode());
                String result = validatorResultVo.getResult();
                //若计算结果为空,则表示表中没有数据(未上报等原因),结果置为0并保存到异常表中
                if (StrUtil.isBlank(result)) {
                    result = "0.0";
                    //插入异常任务计算项表中
                    CostTaskExecuteResultExceptionItem costTaskExecuteResultExceptionItem=new CostTaskExecuteResultExceptionItem();
                    costTaskExecuteResultExceptionItem.setTaskId(taskId);
                    costTaskExecuteResultExceptionItem.setUnitId(unitId);
                    costTaskExecuteResultExceptionItem.setItemId(costIndexConfigItem.getId());
                    costTaskExecuteResultExceptionItem.setIndexId(configIndexNew.getIndexId());
                    costTaskExecuteResultExceptionItem.setItemName(costIndexConfigItem.getConfigName());
                    costTaskExecuteResultExceptionItem.setPath(configIndexNew.getPath());
                    costTaskExecuteResultExceptionItem.setConfigKey(costIndexConfigItem.getConfigKey());
                    costTaskExecuteResultExceptionItem.setAccountPeriod(costIndexConfigItem.getAccountPeriod());
                    costTaskExecuteResultExceptionItem.setTimePeriod(accountStartTime + "-" + accountEndTime);
                    costTaskExecuteResultExceptionItem.setExceptionReason(validatorResultVo.getErrorMsg());
                    costTaskExecuteResultExceptionItem.insert();
                }
                ItemCalculateDetail itemCalculateDetail = new ItemCalculateDetail();
                itemCalculateDetail.setCalculatedValue(new BigDecimal(result));
                itemCalculateTotalValue[0] = itemCalculateTotalValue[0].add(itemCalculateDetail.getCalculatedValue());
                itemCalculateDetail.setBizId(dept.getRelateId());
                itemCalculateDetail.setBizName(dept.getName());
                itemCalculateDetails.add(itemCalculateDetail);
            });
            dealCommon(costIndexConfigItem, taskId, unitId, configIndexNew, accountObject, itemCalculateTotalValue[0], accountStartTime, accountEndTime, configCorrespondenceItem, itemCalculateDetails);

        } else if (ItemDimensionEnum.USER.getCode().equals(dimension)) {
            //获取该科室单元下所有的部门
            dealUserDimension(unitId, costIndexConfigItem, taskId, configIndexNew, accountObject, accountStartTime, accountEndTime, configCorrespondenceItem);
        } else if (ItemDimensionEnum.DEPT_UNIT.getCode().equals(dimension)) {
            //获取该科室单元下所有的科室和人员  todo 暂不实现 有需要再实现
            ValidatorResultVo validatorResultVo = costAccountIndexService.verificationItem(costIndexConfigItem.getId(), accountStartTime, accountEndTime, null, dimension);
            String result = validatorResultVo.getResult();
            //若计算结果为空,则表示表中没有数据(未上报等原因),结果置为0并保存到异常表中
            if (StrUtil.isBlank(result)) {
                result = "0.0";
                //插入异常任务计算项表中
                CostTaskExecuteResultExceptionItem costTaskExecuteResultExceptionItem=new CostTaskExecuteResultExceptionItem();
                costTaskExecuteResultExceptionItem.setTaskId(taskId);
                costTaskExecuteResultExceptionItem.setUnitId(unitId);
                costTaskExecuteResultExceptionItem.setItemId(costIndexConfigItem.getId());
                costTaskExecuteResultExceptionItem.setIndexId(configIndexNew.getIndexId());
                costTaskExecuteResultExceptionItem.setItemName(costIndexConfigItem.getConfigName());
                costTaskExecuteResultExceptionItem.setPath(configIndexNew.getPath());
                costTaskExecuteResultExceptionItem.setConfigKey(costIndexConfigItem.getConfigKey());
                costTaskExecuteResultExceptionItem.setAccountPeriod(costIndexConfigItem.getAccountPeriod());
                costTaskExecuteResultExceptionItem.setTimePeriod(accountStartTime + "-" + accountEndTime);
                costTaskExecuteResultExceptionItem.setExceptionReason(validatorResultVo.getErrorMsg());
                costTaskExecuteResultExceptionItem.insert();
            }
            ItemCalculateDetail itemCalculateDetail = new ItemCalculateDetail();
            itemCalculateDetail.setCalculatedValue(new BigDecimal(result));
            itemCalculateDetail.setBizId(unitId + "");
            itemCalculateDetail.setBizName(cacheUtils.getCostAccountUnit(unitId).getName());
            dealCommon(costIndexConfigItem, taskId, unitId, configIndexNew, accountObject, new BigDecimal(result), accountStartTime, accountEndTime, configCorrespondenceItem, Lists.newArrayList(itemCalculateDetail));
        } else {
            throw new BizException("不支持的sql维度");
        }

    }


    private void dealUserDimension(Long unitId, CostIndexConfigItem costIndexConfigItem, Long taskId, CostAccountPlanConfigIndexNew configIndexNew, String accountObject, String accountStartTime, String accountEndTime, ConfigCorrespondenceItem configCorrespondenceItem) {
        List<CostUnitRelateInfo> deptList = new CostUnitRelateInfo().selectList(Wrappers.<CostUnitRelateInfo>lambdaQuery().
                eq(CostUnitRelateInfo::getAccountUnitId, unitId).
                eq(CostUnitRelateInfo::getType, "dept"));
        //获取部门下所有的人员
        List<CostUnitExcludedInfo> costUnitExcludedInfos = new CostUnitExcludedInfo().selectList(
                Wrappers.<CostUnitExcludedInfo>lambdaQuery().eq(CostUnitExcludedInfo::getAccountUnitId, unitId)
                        .select(CostUnitExcludedInfo::getRelateId));
        //查询不参与计算的人员信息
        List<Long> excludeUserIds = new ArrayList<>();
        if (CollectionUtil.isNotEmpty(costUnitExcludedInfos)) {
            excludeUserIds = costUnitExcludedInfos.stream().map(CostUnitExcludedInfo::getRelateId).map(Long::parseLong).collect(Collectors.toList());
        }
        List<Long> deptIds = deptList.stream().map(CostUnitRelateInfo::getRelateId).map(Long::parseLong).collect(Collectors.toList());
        Map<Long, String> map = sqlUtil.getUserIdsAndNamesByDeptIds(deptIds);
        //获取该科室单元下所有的人员
        List<CostUnitRelateInfo> userList = new CostUnitRelateInfo().selectList(Wrappers.<CostUnitRelateInfo>lambdaQuery().
                eq(CostUnitRelateInfo::getAccountUnitId, unitId).
                eq(CostUnitRelateInfo::getType, "user"));
        Map<Long, String> idNames = userList.stream()
                .collect(Collectors.toMap(info -> Long.parseLong(info.getRelateId()), CostUnitRelateInfo::getName));
        map.putAll(idNames);
        if (CollectionUtil.isNotEmpty(excludeUserIds)) {
            excludeUserIds.forEach(excludeUserId -> map.remove(excludeUserId));
        }
        final BigDecimal[] itemCalculateTotalValue = {new BigDecimal(0)};
        List<ItemCalculateDetail> itemCalculateDetails = new ArrayList<>();
        map.forEach((userId, userName) -> {
            ValidatorResultVo validatorResultVo = costAccountIndexService.verificationItem(costIndexConfigItem.getId(), accountStartTime, accountEndTime, userId + "", ItemDimensionEnum.USER.getCode());
            String result = validatorResultVo.getResult();
            //若计算结果为空,则表示表中没有数据(未上报等原因),结果置为0并保存到异常表中
            if (StrUtil.isBlank(result)) {
                result = "0.0";
                //插入异常任务计算项表中
                CostTaskExecuteResultExceptionItem costTaskExecuteResultExceptionItem=new CostTaskExecuteResultExceptionItem();
                costTaskExecuteResultExceptionItem.setTaskId(taskId);
                costTaskExecuteResultExceptionItem.setUnitId(unitId);
                costTaskExecuteResultExceptionItem.setItemId(costIndexConfigItem.getId());
                costTaskExecuteResultExceptionItem.setIndexId(configIndexNew.getIndexId());
                costTaskExecuteResultExceptionItem.setItemName(costIndexConfigItem.getConfigName());
                costTaskExecuteResultExceptionItem.setPath(configIndexNew.getPath());
                costTaskExecuteResultExceptionItem.setConfigKey(costIndexConfigItem.getConfigKey());
                costTaskExecuteResultExceptionItem.setAccountPeriod(costIndexConfigItem.getAccountPeriod());
                costTaskExecuteResultExceptionItem.setTimePeriod(accountStartTime + "-" + accountEndTime);
                costTaskExecuteResultExceptionItem.setExceptionReason(validatorResultVo.getErrorMsg());
                costTaskExecuteResultExceptionItem.insert();
            }
            ItemCalculateDetail itemCalculateDetail = new ItemCalculateDetail();
            itemCalculateDetail.setCalculatedValue(new BigDecimal(result));
            itemCalculateTotalValue[0] = itemCalculateTotalValue[0].add(itemCalculateDetail.getCalculatedValue());
            itemCalculateDetail.setBizId(userId + "");
            itemCalculateDetail.setBizName(userName);
            itemCalculateDetails.add(itemCalculateDetail);
        });
        dealCommon(costIndexConfigItem, taskId, unitId, configIndexNew, accountObject, itemCalculateTotalValue[0], accountStartTime, accountEndTime, configCorrespondenceItem, itemCalculateDetails);
    }


    private void dealCommon(CostIndexConfigItem costIndexConfigItem, Long taskId, Long unitId, CostAccountPlanConfigIndexNew configIndexNew, String accountObject, BigDecimal itemCalculateTotalValue, String accountStartTime, String accountEndTime, ConfigCorrespondenceItem configCorrespondenceItem, List<ItemCalculateDetail> itemCalculateDetails) {
        long startTime = System.currentTimeMillis();
        configCorrespondenceItem.setItemCalculateDetails(itemCalculateDetails);
        configCorrespondenceItem.setCalculatedValue(itemCalculateTotalValue);
        configCorrespondenceItem.setAccountObject(accountObject);
        //定义规则配置
        CostTaskExecuteResultRule costTaskExecuteResultRule = new CostTaskExecuteResultRule();
        //通过比例id获取比例值
        Long allocateId = configIndexNew.getAllocateId();
        if (allocateId != null) {
            CostAccountProportionRelation costAccountProportionRelation = new CostAccountProportionRelation().selectOne(new LambdaQueryWrapper<CostAccountProportionRelation>()
                    .eq(CostAccountProportionRelation::getCostAccountProportionId, allocateId)
                    .eq(CostAccountProportionRelation::getBzid, unitId));
            if (costAccountProportionRelation != null) {
                configCorrespondenceItem.setAllocate(costAccountProportionRelation.getProportion().toString());
            }
        }
        //插入核算结果
        CostTaskExecuteResultItem costTaskExecuteResultItem = new CostTaskExecuteResultItem();
        costTaskExecuteResultItem.setPath(configIndexNew.getPath());
        costTaskExecuteResultItem.setTaskId(taskId);
        costTaskExecuteResultItem.setIndexId(configIndexNew.getIndexId());
        costTaskExecuteResultItem.setUnitId(unitId);
        costTaskExecuteResultItem.setConfigKey(configIndexNew.getConfigKey());
        costTaskExecuteResultItem.setPath(configIndexNew.getPath());
        costTaskExecuteResultItem.setItemId(configCorrespondenceItem.getItemId());
        costTaskExecuteResultItem.setItemName(configCorrespondenceItem.getConfigName());
        costTaskExecuteResultItem.setTimePeriod(accountStartTime + "-" + accountEndTime);
        costTaskExecuteResultItem.setAccountPeriod(costIndexConfigItem.getAccountPeriod());
        costTaskExecuteResultItem.setCalculateCount(itemCalculateTotalValue);
        costTaskExecuteResultItem.setConfig(JSON.toJSONString(configCorrespondenceItem));
        costTaskExecuteResultItem.setAccountObject(accountObject);
        costTaskExecuteResultItem.insert();

        //获取该配置项的分摊规则明细
        CostAllocationRule costAllocationRule = new CostAllocationRule().selectById(configIndexNew.getRuleFormulaId());
        DivideFormulaDesc divideFormulaDesc = new DivideFormulaDesc();
        divideFormulaDesc.setAllocationRuleFormula(costAllocationRule.getAllocationRuleFormula());
        divideFormulaDesc.setCalculationAccountRuleConfigIndex(new CostAllocationRuleConfigIndex().selectList(Wrappers.<CostAllocationRuleConfigIndex>lambdaQuery().
                eq(CostAllocationRuleConfigIndex::getAllocationRuleId, costAllocationRule.getId())).stream().map(costAllocationRuleConfigIndex -> {
            CalculationAccountRuleConfigIndexVo calculationAccountRuleConfigIndexVo = new CalculationAccountRuleConfigIndexVo();
            BeanUtils.copyProperties(costAllocationRuleConfigIndex, calculationAccountRuleConfigIndexVo);
            return calculationAccountRuleConfigIndexVo;
        }).collect(Collectors.toList()));
        divideFormulaDesc.setCalculationAccountRuleConfigItem(new CostAllocationRuleConfigItem().selectList(Wrappers.<CostAllocationRuleConfigItem>lambdaQuery().
                eq(CostAllocationRuleConfigItem::getAllocationRuleId, costAllocationRule.getId())).stream().map(costAllocationRuleConfigItem -> {
            CalculationAccountRuleConfigItemVo calculationAccountRuleConfigItemVo = new CalculationAccountRuleConfigItemVo();
            BeanUtils.copyProperties(costAllocationRuleConfigItem, calculationAccountRuleConfigItemVo);
            return calculationAccountRuleConfigItemVo;
        }).collect(Collectors.toList()));


        Long ruleFormulaId = configIndexNew.getRuleFormulaId();
        if (ruleFormulaId != null) {
            //根据分摊规则id和核算项id和核算项key获取核算比例id
            CostAllocationRuleConfigItem costAllocationRuleConfigItem = new CostAllocationRuleConfigItem().selectOne(new LambdaQueryWrapper<CostAllocationRuleConfigItem>()
                    .eq(CostAllocationRuleConfigItem::getConfigKey, costTaskExecuteResultItem.getConfigKey())
                    .eq(CostAllocationRuleConfigItem::getAllocationRuleId, ruleFormulaId)
                    .eq(CostAllocationRuleConfigItem::getConfigId, costTaskExecuteResultItem.getItemId()));
            if (costAllocationRuleConfigItem != null) {
                Long allocationRuleId = costAllocationRuleConfigItem.getAllocationRuleId();
                if (allocationRuleId != null) {
                    //根据比例id和业务id获取到该业务的核算比例值
                    Double proportion = new CostAccountProportionRelation().selectOne(new LambdaQueryWrapper<CostAccountProportionRelation>()
                            .eq(CostAccountProportionRelation::getCostAccountProportionId, allocationRuleId)
                            .eq(CostAccountProportionRelation::getBzid, unitId)).getProportion();
                    costTaskExecuteResultRule.setDividePercent(new BigDecimal(proportion));
                }
            }
        }
        costTaskExecuteResultRule.setTaskId(taskId);
        costTaskExecuteResultRule.setIndexId(configIndexNew.getIndexId());
        costTaskExecuteResultRule.setUnitId(unitId);
        costTaskExecuteResultRule.setPath(configIndexNew.getPath());
        //存储的是item表中的主键id
        costTaskExecuteResultRule.setItemId(costTaskExecuteResultItem.getId());
        costTaskExecuteResultRule.setDivideFormulaDesc(JSON.toJSONString(divideFormulaDesc));
        costTaskExecuteResultRule.setAccountPeriod(costIndexConfigItem.getAccountPeriod());
        costTaskExecuteResultRule.setAccountObject(accountObject);

        costTaskExecuteResultRule.setTimePeriod(accountStartTime + "-" + accountEndTime);
        Map<String, Double> map = new HashMap<>();
        CostAccountItem costAccountItem = cacheUtils.getCostAccountItem(costIndexConfigItem.getConfigId());
        ValidatorResultVo allocationRuleData = costAllocationRuleService.getAllocationRuleData(map, ruleFormulaId, accountStartTime, accountEndTime, unitId + "", costAccountItem.getDimension());
        BigDecimal ruleCount = new BigDecimal(0.0);
        if (allocationRuleData.getResult() != null) {
            ruleCount = new BigDecimal(allocationRuleData.getResult());
        }
        costTaskExecuteResultRule.setRuleCount(ruleCount);
        //todo 缺计算明细
        List<CalculateDetail> details = new ArrayList<>();
        for (Map.Entry<String, Double> entry : map.entrySet()) {
            CalculateDetail calculateDetail = new CalculateDetail();
            calculateDetail.setConfigKey(entry.getKey());
            calculateDetail.setConfigValue(new BigDecimal(entry.getValue()));
            details.add(calculateDetail);
        }
        costTaskExecuteResultRule.setCalculateDetail(JSON.toJSONString(details));
        costTaskExecuteResultRule.insert();
        //更新对应的核算项最终数据
        costTaskExecuteResultItem.setFinalCount(costTaskExecuteResultItem.getCalculateCount().multiply(costTaskExecuteResultRule.getRuleCount()));
        if (StrUtil.isBlank(costTaskExecuteResultItem.getPath())) {
            costTaskExecuteResultItem.setPath(null);
        }
        costTaskExecuteResultItem.updateById();
        //其他分摊规则
        dealExtraAllocationRule(costTaskExecuteResultItem.getId(), taskId, unitId, configIndexNew, configCorrespondenceItem, itemCalculateTotalValue, allocationRuleData, accountStartTime, accountEndTime);
        log.info("dealCommon耗时{}ms", System.currentTimeMillis() - startTime);
    }


    private void dealExtraAllocationRule(Long executeResultItemId, Long taskId, Long unitId, CostAccountPlanConfigIndexNew configIndexNew, ConfigCorrespondenceItem configCorrespondenceItem, BigDecimal itemCalculateTotalValue, ValidatorResultVo allocationRuleData, String accountStartTime, String accountEndTime) {
        //医护分摊
        if (YesNoEnum.YES.getValue().equals(configCorrespondenceItem.getDocNurseAllocation())) {
            Long allocateId = configIndexNew.getAllocateId();
            CostAccountUnit costAccountUnit = cacheUtils.getCostAccountUnit(unitId);
            //查询核算对象对应的医护关系对应集合
            List<CostDocNRelation> costDocNRelations = new CostDocNRelation().selectList
                    (new LambdaQueryWrapper<CostDocNRelation>().eq(CostDocNRelation::getDocAccountGroupId, unitId)
                            .or().eq(CostDocNRelation::getNurseAccountGroupId, unitId));
            if (costDocNRelations == null) {
                throw new BizException("医护分摊配置错误");
            }
            Long divideUnitId;
            //遍历集合,计算核算对象对应的医护关系对应集合中每一项的值并落库
            for (CostDocNRelation costDocNRelation : costDocNRelations) {
                //获取医护对应关系的单元id
                if (Objects.equals(costDocNRelation.getDocAccountGroupId(), unitId)) {
                    divideUnitId = costDocNRelation.getNurseAccountGroupId();
                } else {
                    divideUnitId = costDocNRelation.getDocAccountGroupId();
                }
                //根据分摊id获取d对应医护对应关系的分摊规则
                CostAccountProportionRelation costAccountProportionRelation = new CostAccountProportionRelation().selectOne(new LambdaQueryWrapper<CostAccountProportionRelation>()
                        .eq(CostAccountProportionRelation::getCostAccountProportionId, allocateId)
                        .eq(CostAccountProportionRelation::getBzid, divideUnitId)
                        .orderByDesc(CostAccountProportionRelation::getId)
                        .last(" limit 1"));
                CostTaskExecuteResultExtra costTaskExecuteResultExtra = new CostTaskExecuteResultExtra();
                costTaskExecuteResultExtra.setTaskId(taskId);
                costTaskExecuteResultExtra.setIndexId(configIndexNew.getIndexId());
                costTaskExecuteResultExtra.setUnitId(unitId);
                costTaskExecuteResultExtra.setPath(configIndexNew.getPath());
                costTaskExecuteResultExtra.setUnitName(costAccountUnit.getName());
                costTaskExecuteResultExtra.setItemId(executeResultItemId);
                costTaskExecuteResultExtra.setDivideUnitId(divideUnitId);
                costTaskExecuteResultExtra.setDivideType("yh");
                costTaskExecuteResultExtra.setDivideCountBefore(itemCalculateTotalValue.multiply(new BigDecimal(allocationRuleData.getResult())));
                if (costAccountProportionRelation == null) {
                    costTaskExecuteResultExtra.setDividePercent("0");
                } else {
                    costTaskExecuteResultExtra.setDividePercent(Objects.nonNull(costAccountProportionRelation.getProportion()) ? costAccountProportionRelation.getProportion() + "" : "0");
                }
                costTaskExecuteResultExtra.setDivideCountAfter(costTaskExecuteResultExtra.getDivideCountBefore().multiply(new BigDecimal(costTaskExecuteResultExtra.getDividePercent())));
                costTaskExecuteResultExtra.insert();
            }
        }
        //门诊分摊
        if (YesNoEnum.YES.getValue().equals(configCorrespondenceItem.getOutpatientPublic())) {
            //数据小组取数据 医生分医生
            List<DwsFinanceWardShare> wardShares = sqlUtil.getWardShareDoc(unitId, configCorrespondenceItem.getItemId(), "门诊共用", accountStartTime, accountEndTime);
            if (CollectionUtil.isNotEmpty(wardShares)) {
                wardShares.forEach(wardShare -> {
                    CostTaskExecuteResultExtra costTaskExecuteResultExtra = new CostTaskExecuteResultExtra();
                    costTaskExecuteResultExtra.setTaskId(taskId);
                    costTaskExecuteResultExtra.setIndexId(configIndexNew.getIndexId());
                    costTaskExecuteResultExtra.setUnitId(unitId);
                    costTaskExecuteResultExtra.setDivideUnitId(unitId);
                    costTaskExecuteResultExtra.setPath(configIndexNew.getPath());
                    costTaskExecuteResultExtra.setDivideUnitId(unitId);
                    costTaskExecuteResultExtra.setItemId(executeResultItemId);
                    costTaskExecuteResultExtra.setDivideType("mz");
                    costTaskExecuteResultExtra.setDivideCountAfter(StrUtil.isNotBlank(wardShare.getShareFee()) ? new BigDecimal(wardShare.getShareFee()) : BigDecimal.ZERO);
                    costTaskExecuteResultExtra.insert();
                });
            }
        }
        //借床分摊
        if (YesNoEnum.YES.getValue().equals(configCorrespondenceItem.getBedAllocation())) {
            //数据小组取数据   护理分医生
            //根据核算单元id获取到核算单元对象
            addCostTaskExecuteResult(executeResultItemId, unitId, "借床", "jc", taskId, configIndexNew, configCorrespondenceItem, accountStartTime, accountEndTime);
        }
        //病区成本分摊
        if (YesNoEnum.YES.getValue().equals(configCorrespondenceItem.getWardCosts())) {
            //数据小组取数据  护理分医生
            addCostTaskExecuteResult(executeResultItemId, unitId, "综合", "bq", taskId, configIndexNew, configCorrespondenceItem, accountStartTime, accountEndTime);
        }
    }

    private void addCostTaskExecuteResult(Long executeResultItemId, Long unitId, String bedBorrow, String divideType, Long taskId, CostAccountPlanConfigIndexNew configIndexNew, ConfigCorrespondenceItem configCorrespondenceItem, String accountStartTime, String accountEndTime) {
        //定义costTaskExecuteResultExtra对象存储病区分摊
        CostAccountUnit costAccountUnit = cacheUtils.getCostAccountUnit(unitId);

        //根据核算单元id和核算项id以及分摊类型查询病区分摊的值
        List<DwsFinanceWardShare> dwsFinanceWardShares = sqlUtil.getWardShareNurse(unitId, configCorrespondenceItem.getItemId(), bedBorrow, accountStartTime, accountEndTime);
        if (!CollectionUtils.isEmpty(dwsFinanceWardShares)) {
            dwsFinanceWardShares.forEach(dwsFinanceWardShare -> {
                CostTaskExecuteResultExtra costTaskExecuteResultExtra = new CostTaskExecuteResultExtra();
                costTaskExecuteResultExtra.setTaskId(taskId);
                costTaskExecuteResultExtra.setIndexId(configIndexNew.getIndexId());
                costTaskExecuteResultExtra.setUnitId(unitId);
                costTaskExecuteResultExtra.setDivideUnitId(Long.parseLong(dwsFinanceWardShare.getAccountUnitId()));
                costTaskExecuteResultExtra.setPath(configIndexNew.getPath());
                costTaskExecuteResultExtra.setUnitName(costAccountUnit.getName());
                costTaskExecuteResultExtra.setItemId(executeResultItemId);
                costTaskExecuteResultExtra.setDivideType(divideType);
                costTaskExecuteResultExtra.setDivideCountAfter(StrUtil.isNotBlank(dwsFinanceWardShare.getShareFee()) ? new BigDecimal(dwsFinanceWardShare.getShareFee()) : BigDecimal.ZERO);
                costTaskExecuteResultExtra.insert();
            });
        }
    }


    /**
     * 医护对应科室单元
     *
     * @param taskId                   任务id
     * @param configIndexNew           方案的核算指标配置
     * @param unitId                   核算单元id
     * @param costIndexConfigItem      配置项
     * @param configCorrespondenceItem 方案对应配置信息
     * @param dimension                核算对象
     * @param accountStartTime         核算开始时间
     * @param accountEndTime           核算结束时间
     */

    private void calculateDocNurseUnit(Long taskId, CostAccountPlanConfigIndexNew configIndexNew, String AccountObject, CostIndexConfigItem costIndexConfigItem, Long unitId, ConfigCorrespondenceItem configCorrespondenceItem, String dimension, String accountStartTime, String accountEndTime) {
        //根据核算对象的维度获取科室单元关联关系
        if (ItemDimensionEnum.DEPT.getCode().equals(dimension)) {
            //获取该科室单元下所有的科室
            List<CostUnitRelateInfo> deptList = new CostUnitRelateInfo().selectList(Wrappers.<CostUnitRelateInfo>lambdaQuery().
                    eq(CostUnitRelateInfo::getAccountUnitId, unitId).
                    eq(CostUnitRelateInfo::getType, "dept"));
            List<Long> deptIds = deptList.stream().map(CostUnitRelateInfo::getRelateId).map(Long::parseLong).collect(Collectors.toList());
            if (CollectionUtil.isEmpty(deptIds)) {
                log.error("该科室单元下没有科室,科室单元id为{}", unitId);
                throw new BizException("该科室单元下没有科室,科室单元id为" + unitId);
            }
            Map<Long, String> map = sqlUtil.getDeptCodesByDeptIds(deptIds);
            //获取该科室单元下所有的科室的计算明细
            List<ItemCalculateDetail> itemCalculateDetails = new ArrayList<>();
            final BigDecimal[] itemCalculateTotalValue = {new BigDecimal(0)};
            deptList.forEach(dept -> {
                ValidatorResultVo validatorResultVo = costAccountIndexService.verificationItem(costIndexConfigItem.getId(), accountStartTime, accountEndTime, map.get(dept.getRelateId()), ItemDimensionEnum.DEPT.getCode());
                String result = validatorResultVo.getResult();
                //若计算结果为空,则表示表中没有数据(未上报等原因),结果置为0并保存到异常表中
                if (StrUtil.isBlank(result)) {
                    result = "0.0";
                    //插入异常任务计算项表中
                    CostTaskExecuteResultExceptionItem costTaskExecuteResultExceptionItem=new CostTaskExecuteResultExceptionItem();
                    costTaskExecuteResultExceptionItem.setTaskId(taskId);
                    costTaskExecuteResultExceptionItem.setUnitId(unitId);
                    costTaskExecuteResultExceptionItem.setItemId(costIndexConfigItem.getId());
                    costTaskExecuteResultExceptionItem.setIndexId(configIndexNew.getIndexId());
                    costTaskExecuteResultExceptionItem.setItemName(costIndexConfigItem.getConfigName());
                    costTaskExecuteResultExceptionItem.setPath(configIndexNew.getPath());
                    costTaskExecuteResultExceptionItem.setConfigKey(costIndexConfigItem.getConfigKey());
                    costTaskExecuteResultExceptionItem.setAccountPeriod(costIndexConfigItem.getAccountPeriod());
                    costTaskExecuteResultExceptionItem.setTimePeriod(accountStartTime + "-" + accountEndTime);
                    costTaskExecuteResultExceptionItem.setExceptionReason(validatorResultVo.getErrorMsg());
                    costTaskExecuteResultExceptionItem.insert();
                }
                ItemCalculateDetail itemCalculateDetail = new ItemCalculateDetail();
                itemCalculateDetail.setCalculatedValue(new BigDecimal(result));
                itemCalculateTotalValue[0] = itemCalculateTotalValue[0].add(itemCalculateDetail.getCalculatedValue());
                itemCalculateDetail.setBizId(dept.getRelateId());
                itemCalculateDetail.setBizName(dept.getName());
                itemCalculateDetails.add(itemCalculateDetail);
            });
            dealCommon(costIndexConfigItem, taskId, unitId, configIndexNew, AccountObject, itemCalculateTotalValue[0], accountStartTime, accountEndTime, configCorrespondenceItem, itemCalculateDetails);
        } else if (ItemDimensionEnum.USER.getCode().equals(dimension)) {
            dealUserDimension(unitId, costIndexConfigItem, taskId, configIndexNew, AccountObject, accountStartTime, accountEndTime, configCorrespondenceItem);
        } else if (ItemDimensionEnum.DEPT_UNIT.getCode().equals(dimension)) {
            //获取该科室单元下所有的科室单元  todo 暂不实现 有需要再实现
        } else {
            throw new BizException("不支持的sql维度");
        }

    }

    /**
     * 自定义科室单元
     *
     * @param taskId                   任务id
     * @param configIndexNew           方案的核算指标配置
     * @param unitId                   核算单元id
     * @param costIndexConfigItem      配置项
     * @param configCorrespondenceItem 方案对应配置信息
     * @param dimension                核算对象
     * @param accountStartTime         核算开始时间
     * @param accountEndTime           核算结束时间
     */

    private void calculateCustomUnit(Long taskId, CostAccountPlanConfigIndexNew configIndexNew, String AccountObject, CostIndexConfigItem costIndexConfigItem, Long unitId, ConfigCorrespondenceItem configCorrespondenceItem, String dimension, String accountStartTime, String accountEndTime) {
        //根据核算对象的维度获取科室单元关联关系
        if (ItemDimensionEnum.DEPT.getCode().equals(dimension)) {
            //获取该科室单元下所有的科室
            List<CostUnitRelateInfo> deptList = new CostUnitRelateInfo().selectList(Wrappers.<CostUnitRelateInfo>lambdaQuery().
                    eq(CostUnitRelateInfo::getAccountUnitId, unitId).
                    eq(CostUnitRelateInfo::getType, "dept"));
            List<Long> deptIds = deptList.stream().map(CostUnitRelateInfo::getRelateId).map(Long::parseLong).collect(Collectors.toList());
            if (CollectionUtil.isEmpty(deptIds)) {
                log.error("该科室单元下没有科室,科室单元id为{}", unitId);
                throw new BizException("该科室单元下没有科室,科室单元id为" + unitId);
            }
            Map<Long, String> map = sqlUtil.getDeptCodesByDeptIds(deptIds);
            //获取该科室单元下所有的科室的计算明细
            List<ItemCalculateDetail> itemCalculateDetails = new ArrayList<>();
            final BigDecimal[] itemCalculateTotalValue = {new BigDecimal(0)};
            deptList.forEach(dept -> {
                ValidatorResultVo validatorResultVo = costAccountIndexService.verificationItem(costIndexConfigItem.getId(), accountStartTime, accountEndTime, map.get(dept.getRelateId()), ItemDimensionEnum.DEPT.getCode());
                String result = validatorResultVo.getResult();
                //若计算结果为空,则表示表中没有数据(未上报等原因),结果置为0并保存到异常表中
                if (StrUtil.isBlank(result)) {
                    result = "0.0";
                    //插入异常任务计算项表中
                    CostTaskExecuteResultExceptionItem costTaskExecuteResultExceptionItem=new CostTaskExecuteResultExceptionItem();
                    costTaskExecuteResultExceptionItem.setTaskId(taskId);
                    costTaskExecuteResultExceptionItem.setUnitId(unitId);
                    costTaskExecuteResultExceptionItem.setItemId(costIndexConfigItem.getId());
                    costTaskExecuteResultExceptionItem.setIndexId(configIndexNew.getIndexId());
                    costTaskExecuteResultExceptionItem.setItemName(costIndexConfigItem.getConfigName());
                    costTaskExecuteResultExceptionItem.setPath(configIndexNew.getPath());
                    costTaskExecuteResultExceptionItem.setConfigKey(costIndexConfigItem.getConfigKey());
                    costTaskExecuteResultExceptionItem.setAccountPeriod(costIndexConfigItem.getAccountPeriod());
                    costTaskExecuteResultExceptionItem.setTimePeriod(accountStartTime + "-" + accountEndTime);
                    costTaskExecuteResultExceptionItem.setExceptionReason(validatorResultVo.getErrorMsg());
                    costTaskExecuteResultExceptionItem.insert();
                }
                ItemCalculateDetail itemCalculateDetail = new ItemCalculateDetail();
                itemCalculateDetail.setCalculatedValue(new BigDecimal(result));
                itemCalculateTotalValue[0] = itemCalculateTotalValue[0].add(itemCalculateDetail.getCalculatedValue());
                itemCalculateDetail.setBizId(dept.getRelateId());
                itemCalculateDetail.setBizName(dept.getName());
                itemCalculateDetails.add(itemCalculateDetail);
            });
            dealCommon(costIndexConfigItem, taskId, unitId, configIndexNew, AccountObject, itemCalculateTotalValue[0], accountStartTime, accountEndTime, configCorrespondenceItem, itemCalculateDetails);
        } else if (ItemDimensionEnum.USER.getCode().equals(dimension)) {
            dealUserDimension(unitId, costIndexConfigItem, taskId, configIndexNew, AccountObject, accountStartTime, accountEndTime, configCorrespondenceItem);
        } else if (ItemDimensionEnum.DEPT_UNIT.getCode().equals(dimension)) {
            //获取该科室单元下所有的科室和人员  todo 暂不实现 有需要再实现
        } else {
            throw new BizException("不支持的sql维度");
        }

    }


    private void calculateCustomDept(Long taskId, CostAccountPlanConfigIndexNew configIndexNew, String AccountObject, CostIndexConfigItem costIndexConfigItem, Long unitId, ConfigCorrespondenceItem configCorrespondenceItem, String dimension, String accountStartTime, String accountEndTime) {
        //根据科室单元id和计算维度获取科室单元关联关系
        if (ItemDimensionEnum.DEPT.getCode().equals(dimension)) {
            //获取该科室单元下所有的科室
            String[] relateIds = configIndexNew.getCustomObject().substring(2, configIndexNew.getCustomObject().length() - 2).split(",");
            for (String relateId : relateIds) {
//                CostUnitRelateInfo info = new CostUnitRelateInfo().selectOne(Wrappers.<CostUnitRelateInfo>lambdaQuery().
//                        eq(CostUnitRelateInfo::getRelateId, relateId));
                ItemCalculateDetail itemCalculateDetail = new ItemCalculateDetail();
                List<ItemCalculateDetail> itemCalculateDetails = new ArrayList<>();
                //当前入参科室
                final ValidatorResultVo validatorResultVo = costAccountIndexService.verificationItem(costIndexConfigItem.getId(), accountStartTime, accountEndTime, relateId, ItemDimensionEnum.DEPT.getCode());
                String result = validatorResultVo.getResult();
                //若计算结果为空,则表示表中没有数据(未上报等原因),结果置为0并保存到异常表中
                if (StrUtil.isBlank(result)) {
                    result = "0.0";
                    //插入异常任务计算项表中
                    CostTaskExecuteResultExceptionItem costTaskExecuteResultExceptionItem=new CostTaskExecuteResultExceptionItem();
                    costTaskExecuteResultExceptionItem.setTaskId(taskId);
                    costTaskExecuteResultExceptionItem.setUnitId(unitId);
                    costTaskExecuteResultExceptionItem.setItemId(costIndexConfigItem.getId());
                    costTaskExecuteResultExceptionItem.setIndexId(configIndexNew.getIndexId());
                    costTaskExecuteResultExceptionItem.setItemName(costIndexConfigItem.getConfigName());
                    costTaskExecuteResultExceptionItem.setPath(configIndexNew.getPath());
                    costTaskExecuteResultExceptionItem.setConfigKey(costIndexConfigItem.getConfigKey());
                    costTaskExecuteResultExceptionItem.setAccountPeriod(costIndexConfigItem.getAccountPeriod());
                    costTaskExecuteResultExceptionItem.setTimePeriod(accountStartTime + "-" + accountEndTime);
                    costTaskExecuteResultExceptionItem.setExceptionReason(validatorResultVo.getErrorMsg());
                    costTaskExecuteResultExceptionItem.insert();
                }
                itemCalculateDetail.setCalculatedValue(new BigDecimal(validatorResultVo.getResult()));
                itemCalculateDetail.setBizId(configIndexNew.getCustomObject());
                //            itemCalculateDetail.setBizName(info.getName());
                itemCalculateDetails.add(itemCalculateDetail);
                dealCommon(costIndexConfigItem, taskId, unitId, configIndexNew, AccountObject, new BigDecimal(result), accountStartTime, accountEndTime, configCorrespondenceItem, itemCalculateDetails);
            }
        } else if (ItemDimensionEnum.USER.getCode().equals(dimension)) {
            //获取该科室下所有的人员
            //创建集合接受人员id
            List<Long> depts = new ArrayList<>();
            depts.add(Long.parseLong(configIndexNew.getCustomObject()));
            Map<Long, String> users = sqlUtil.getUserIdsAndNamesByDeptIds(depts);
            List<ItemCalculateDetail> itemCalculateDetails = new ArrayList<>();
            final BigDecimal[] itemCalculateTotalValue = {new BigDecimal(0)};
            //遍历人员id集合,查询配置sql获取值
            users.forEach((userId, userName) -> {
                ValidatorResultVo validatorResultVo = costAccountIndexService.verificationItem(configCorrespondenceItem.getItemId(), accountStartTime, accountEndTime, userId.toString(), ItemDimensionEnum.USER.getCode());
                String result = validatorResultVo.getResult();
                //若计算结果为空,则表示表中没有数据(未上报等原因),结果置为0并保存到异常表中
                if (StrUtil.isBlank(result)) {
                    result = "0.0";
                    //插入异常任务计算项表中
                    CostTaskExecuteResultExceptionItem costTaskExecuteResultExceptionItem=new CostTaskExecuteResultExceptionItem();
                    costTaskExecuteResultExceptionItem.setTaskId(taskId);
                    costTaskExecuteResultExceptionItem.setUnitId(unitId);
                    costTaskExecuteResultExceptionItem.setItemId(costIndexConfigItem.getId());
                    costTaskExecuteResultExceptionItem.setIndexId(configIndexNew.getIndexId());
                    costTaskExecuteResultExceptionItem.setItemName(costIndexConfigItem.getConfigName());
                    costTaskExecuteResultExceptionItem.setPath(configIndexNew.getPath());
                    costTaskExecuteResultExceptionItem.setConfigKey(costIndexConfigItem.getConfigKey());
                    costTaskExecuteResultExceptionItem.setAccountPeriod(costIndexConfigItem.getAccountPeriod());
                    costTaskExecuteResultExceptionItem.setTimePeriod(accountStartTime + "-" + accountEndTime);
                    costTaskExecuteResultExceptionItem.setExceptionReason(validatorResultVo.getErrorMsg());
                    costTaskExecuteResultExceptionItem.insert();
                }
                ItemCalculateDetail itemCalculateDetail = new ItemCalculateDetail();
                itemCalculateDetail.setCalculatedValue(new BigDecimal(result));
                itemCalculateTotalValue[0] = itemCalculateTotalValue[0].add(itemCalculateDetail.getCalculatedValue());
                itemCalculateDetail.setBizId(userId + "");
                itemCalculateDetail.setBizName(userName);
                itemCalculateDetails.add(itemCalculateDetail);
            });
            dealCommon(costIndexConfigItem, taskId, unitId, configIndexNew, AccountObject, itemCalculateTotalValue[0], accountStartTime, accountEndTime, configCorrespondenceItem, itemCalculateDetails);
        } else {
            throw new BizException("不支持的sql维度");
        }
    }


    private void calculateCustomUser(Long taskId, CostAccountPlanConfigIndexNew configIndexNew, String accountObject, CostIndexConfigItem costIndexConfigItem, Long unitId, ConfigCorrespondenceItem configCorrespondenceItem, String dimension, String accountStartTime, String accountEndTime) {
        List<BigDecimal> resultList = new ArrayList<>();
        //根据科室单元id和计算维度获取科室单元关联关系
        if (ItemDimensionEnum.USER.getCode().equals(dimension)) {
            //当前入参人员
            List<Long> userIds = Arrays.stream(configIndexNew.getCustomObject().split(",")).map(Long::parseLong).collect(Collectors.toList());
            List<ItemCalculateDetail> itemCalculateDetails = new ArrayList<>();
            final BigDecimal[] itemCalculateTotalValue = {new BigDecimal(0)};
            for (Long userId : userIds) {
                //编辑sql语句(根据人员id查询姓名)
                String sql = "select username from `hsx`.`sys_user` where  user_id= '" + userId + "'";
                String userName = "";
                try {
                    //查询人员姓名
                    userName = jdbcTemplate.queryForObject(sql, String.class);
                } catch (Exception e) {
                    log.error("查询人员姓名失败,人员id为{},失败原因:{}", userId, e.getMessage());
                }
                //调用接口执行配置sql获取值
                ValidatorResultVo validatorResultVo = costAccountIndexService.verificationItem(configCorrespondenceItem.getItemId(), accountStartTime, accountEndTime, userId.toString(), ItemDimensionEnum.USER.getCode());
                String result = validatorResultVo.getResult();
                //若计算结果为空,则表示表中没有数据(未上报等原因),结果置为0并保存到异常表中
                if (StrUtil.isBlank(result)) {
                    result = "0.0";
                }
                //封装结果
                ItemCalculateDetail itemCalculateDetail = new ItemCalculateDetail();
                itemCalculateDetail.setCalculatedValue(new BigDecimal(result));
                itemCalculateTotalValue[0] = itemCalculateTotalValue[0].add(itemCalculateDetail.getCalculatedValue());
                itemCalculateDetail.setBizId(userId.toString());
                itemCalculateDetail.setBizName(userName);
                itemCalculateDetails.add(itemCalculateDetail);
                dealCommon(costIndexConfigItem, taskId, unitId, configIndexNew, accountObject, itemCalculateTotalValue[0], accountStartTime, accountEndTime, configCorrespondenceItem, itemCalculateDetails);
            }
        } else {
            throw new BizException("不支持的sql维度");
        }
    }

    /**
     * 任务的核算方案解析计算入库
     *
     * @param taskId 任务id
     * @param unitId 单元id
     * @return result
     */

    private void saveToTaskExecuteResult(Long taskId, Long unitId) {
        //根据unitId获取核算分组信息
        CostTaskExecuteResult result = new CostTaskExecuteResult();
        CostAccountUnit costAccountUnit = cacheUtils.getCostAccountUnit(unitId);
        result.setGroupId(costAccountUnit.getAccountGroupCode());
        result.setTaskId(taskId);
        result.setUnitId(unitId);
        result.setUnitName(costAccountUnit.getName());
        //根据taskId unitId parentId = 0获取 cost_task_execute_result_index parent_id 为0的值 totalValue 获取总值 获取totalValue
        BigDecimal bigDecimal = taskExecuteResultIndexMapper.querySum(taskId, unitId, null);
        //根据taskId获取核算任务并拿到planId
        CostAccountTask costAccountTask = costAccountTaskMapper.selectById(taskId);
        if (null != costAccountTask) {
            CostAccountTaskResultTotalValueVo taskResultTotalValueVo = new CostAccountTaskResultTotalValueVo();
            if (null != bigDecimal) {
                result.setTotalCount(bigDecimal);
                taskResultTotalValueVo.setTotalValue(bigDecimal);
            }
            //返回公式
//            returnOverAllFormula(taskResultTotalValueVo, costAccountTask, costAccountUnit);
            //返回公式和查询返回List
            returnConfigListNew(taskResultTotalValueVo, costAccountTask, taskId, unitId);
            if (!CollectionUtils.isEmpty(taskResultTotalValueVo.getConfigIndexList())) {
                Gson gson = new Gson();
                result.setCalculateDetail(gson.toJson(taskResultTotalValueVo));
            }
        }
        costTaskExecuteResultService.save(result);
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
    /**
     * 返回configList
     *
     * @param costAccountTask        costAccountTask
     * @param taskResultTotalValueVo vo
     * @param taskId                 任务id
     * @param unitId                 核算单元id
     */
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
    void returnOverAllFormula(CostAccountTaskResultTotalValueVo resVo, CostAccountTask result,CostAccountUnit costAccountUnit) {
        String accountGroupCode = costAccountUnit.getAccountGroupCode();
        Gson gson = new Gson();
        UnitInfo unitInfo = gson.fromJson(accountGroupCode, UnitInfo.class);
        String value = UnitMapEnum.getPlanGroup(unitInfo.getValue());
        //根据方案id和核算对象类型取出公式
        LambdaQueryWrapper<CostAccountPlanConfigFormula> queryWrapper = new LambdaQueryWrapper<CostAccountPlanConfigFormula>()
                .eq(CostAccountPlanConfigFormula::getPlanId, result.getPlanId())
                .eq(CostAccountPlanConfigFormula::getAccountObject, value);
        if (value.equals(UnitMapEnum.CUSTOM.getPlanGroup())) {
            queryWrapper.eq(CostAccountPlanConfigFormula::getCustomUnitId, costAccountUnit.getId());
        } else {
            queryWrapper.isNull(CostAccountPlanConfigFormula::getCustomUnitId);
        }
        CostAccountPlanConfigFormula costAccountPlanConfigFormula = configFormulaMapper.selectOne(queryWrapper);
        if (null != costAccountPlanConfigFormula) {
            resVo.setOverAllFormula(costAccountPlanConfigFormula.getPlanCostFormula());
        }
    }
}

