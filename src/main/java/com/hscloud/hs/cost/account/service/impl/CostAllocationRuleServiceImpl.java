package com.hscloud.hs.cost.account.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.hscloud.hs.cost.account.mapper.CostAccountProportionRelationMapper;
import com.hscloud.hs.cost.account.mapper.CostAllocationRuleMapper;
import com.hscloud.hs.cost.account.model.dto.*;
import com.hscloud.hs.cost.account.model.entity.*;
import com.hscloud.hs.cost.account.model.vo.CostAllocationRuleVo;
import com.hscloud.hs.cost.account.model.vo.ValidatorResultVo;
import com.hscloud.hs.cost.account.service.*;
import com.hscloud.hs.cost.account.utils.ExpressionCheckHelper;
import com.hscloud.hs.cost.account.utils.LocalCacheUtils;
import com.pig4cloud.pigx.common.core.exception.BizException;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 分摊规则表 服务实现类
 * </p>
 *
 * @author
 * @since 2023-09-11
 */
@Service
@Slf4j
public class CostAllocationRuleServiceImpl extends ServiceImpl<CostAllocationRuleMapper, CostAllocationRule> implements ICostAllocationRuleService {

    @Autowired
    private ICostIndexConfigItemService costIndexConfigItemService;

    @Autowired
    private ICostAccountIndexService costAccountIndexService;

    @Autowired
    private ICostIndexConfigIndexService costIndexConfigIndexService;

    @Autowired
    private ICostAllocationRuleConfigIndexService costAllocationRuleConfigIndexService;

    @Autowired
    private ICostAllocationRuleConfigItemService costAllocationRuleConfigItemService;

    @Autowired
    private CostAccountProportionRelationMapper costAccountProportionRelationMapper;
    @Autowired
    private CostAccountIndexServiceImpl costAccountIndexServiceImpl;
    @Autowired
    private CostVerificationResultRuleService costVerificationResultRuleService;
    @Autowired
    private LocalCacheUtils cacheUtils;

    private final ExecutorService executorService = new ThreadPoolExecutor(8, 8,
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>(1), r -> {
        Thread thread = new Thread(r);
        thread.setName("task-calculate");
        return thread;
    }, new ThreadPoolExecutor.CallerRunsPolicy());


    private final ExecutorService rulePool = new ThreadPoolExecutor(
            8, // 核心线程数
            8, // 最大线程数
            0L, // 空闲线程存活时间
            TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>(1), // 有界队列
            r -> {
                Thread thread = new Thread(r);
                thread.setName("rule-calculate-" + thread.getName());
                return thread;
            },
            new ThreadPoolExecutor.CallerRunsPolicy() // 当队列已满时的拒绝策略
    );


    /**
     * 保存或更新分摊规则
     *
     * @param dto
     */
    @Override

    public void saveOrUpdateAccountRule(CostAllocationRuleDto dto) {
        LambdaQueryWrapper<CostAllocationRule> queryWrapper = new LambdaQueryWrapper<CostAllocationRule>();
        queryWrapper.eq(CostAllocationRule::getName, dto.getName());
        CostAllocationRule oldCostAllocationRule = getOne(queryWrapper);

        if (dto.getId() == null && oldCostAllocationRule != null && oldCostAllocationRule.getName().equals(dto.getName())) {
            throw new BizException("该分摊规则已存在");
        }
        CostAllocationRule costAllocationRule = BeanUtil.copyProperties(dto, CostAllocationRule.class);

        FormulaDto formulaDto = dto.getFormulaDto();
        costAllocationRule.setAllocationRuleFormula(formulaDto.getExpression());
        saveOrUpdate(costAllocationRule);
        if (dto.getId() != null) {
            costAllocationRuleConfigIndexService.removeByAllocationRuleId(dto.getId());
            costAllocationRuleConfigItemService.removeByAllocationRuleId(dto.getId());
        }
        //插入中间表数据
        List<FormulaDto.FormulaParam> params = (formulaDto.getParams());
        List<CostAllocationRuleConfigIndex> costAllocationRuleConfigIndexList = new ArrayList<>();
        List<CostAllocationRuleConfigItem> costAllocationRuleConfigItemList = new ArrayList<>();
        for (FormulaDto.FormulaParam param : params) {
            if (param.getType().equals("item")) {
                CostAllocationRuleConfigItem costAllocationRuleConfigItem = new CostAllocationRuleConfigItem();
                costAllocationRuleConfigItem.setConfigKey(param.getKey());
                costAllocationRuleConfigItem.setDimension(param.getDimension());
                costAllocationRuleConfigItem.setConfigName(param.getName());
                costAllocationRuleConfigItem.setConfigDesc(param.getDesc());
                costAllocationRuleConfigItem.setAccountProportionDesc(param.getAccountProportionDesc());
                if (StrUtil.isNotEmpty(param.getValue())) {
                    CostAllocationRuleConfigItemDto costAllocationRuleConfigItemDto = JSON.parseObject(param.getValue(), CostAllocationRuleConfigItemDto.class);
                    BeanUtil.copyProperties(costAllocationRuleConfigItemDto, costAllocationRuleConfigItem);
                    costAllocationRuleConfigItem.setAllocationRuleId(costAllocationRule.getId());
                    costAllocationRuleConfigItemList.add(costAllocationRuleConfigItem);
                }
            }
            if (param.getType().equals("index")) {
                JSONObject configIndex = new JSONObject(param.getValue());
                CostAllocationRuleConfigIndex costAllocationRuleConfigIndex = new CostAllocationRuleConfigIndex();
                costAllocationRuleConfigIndex.setConfigKey(param.getKey());
                costAllocationRuleConfigIndex.setConfigIndexId(configIndex.getLong("id"));
                costAllocationRuleConfigIndex.setConfigIndexName(configIndex.getStr("name"));
                costAllocationRuleConfigIndex.setAllocationRuleId(costAllocationRule.getId());
                costAllocationRuleConfigIndexList.add(costAllocationRuleConfigIndex);
            }
        }
        costAllocationRuleConfigItemService.saveOrUpdateBatch(costAllocationRuleConfigItemList);
        costAllocationRuleConfigIndexService.saveOrUpdateBatch(costAllocationRuleConfigIndexList);
        if (dto.getId() != null) {
            getTimeRule(dto);
        }
    }

    /**
     * 此方法用于计算增量分摊规则的值
     *
     * @param dto
     */
    private void getTimeRule(CostAllocationRuleDto dto) {
        BigDecimal result = new BigDecimal(0.0);
        List<CostVerificationResultRule> resultRuleList = new ArrayList<>();
        CostAllocationRule costAllocationRule = new CostAllocationRule().selectById(dto.getId());
        //获取当月时间时间
        //String formattedYearMonth = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        String formattedYearMonth = "202309";
        //先查表,有数据不计算,跳过
        final List<CostVerificationResultRule> list = new CostVerificationResultRule().selectList(new LambdaQueryWrapper<CostVerificationResultRule>()
                .eq(CostVerificationResultRule::getRuleId, costAllocationRule.getId())
                .eq(CostVerificationResultRule::getAccountDate, formattedYearMonth));
        final List<Long> idList = list.stream().map(CostVerificationResultRule::getId).collect(Collectors.toList());
        //删除
        costAccountProportionRelationMapper.deleteBatchIds(idList);
        //新增计算
        List<CostAccountUnit> costAccountUnitList = new CostAccountUnit().selectList(new LambdaQueryWrapper<CostAccountUnit>()
                .eq(CostAccountUnit::getDelFlag, "0")
                .eq(CostAccountUnit::getStatus, "0"));
        for (CostAccountUnit costAccountUnit : costAccountUnitList) {
            CostVerificationResultRule resultRule = new CostVerificationResultRule();
            resultRule.setRuleId(costAllocationRule.getId());
            resultRule.setUnitId(Long.valueOf(costAccountUnit.getId()));
            resultRule.setAccountDate(formattedYearMonth);
            //调用方法计算
            ValidatorResultVo  ruleVo = getTimeingAllocationRuleData(costAllocationRule, formattedYearMonth, formattedYearMonth, costAccountUnit.getId().toString());
            result = (ruleVo == null || StrUtil.isBlank(ruleVo.getResult()) ? new BigDecimal(0.0) : new BigDecimal(ruleVo.getResult()));
            //插入校验明细表
            resultRule.setRuleCount(result);
            resultRuleList.add(resultRule);
        }
        //插入表中
        costVerificationResultRuleService.saveBatch(resultRuleList);
        resultRuleList.clear();
    }

    /**
     * 启停用分摊规则
     *
     * @param dto
     * @return
     */
    @Override
    public Boolean updateStatusAllocationRule(CostAllocationRuleStatusDto dto) {
        CostAllocationRule allocationRule = getById(dto.getId());
        if (allocationRule == null) {
            throw new BizException("分摊规则不存在");
        }
        allocationRule.setStatus(dto.getStatus());
        return updateById(allocationRule);
    }

    @Override
    public IPage<CostAllocationRuleVo> getAllocationRulePage(CostAllocationRuleQueryDto queryDto) {
        Page<CostAllocationRuleVo> ruleVoPage = new Page<>(queryDto.getCurrent(), queryDto.getSize());
        LambdaQueryWrapper<CostAllocationRule> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StrUtil.isNotEmpty(queryDto.getName()), CostAllocationRule::getName, queryDto.getName())
                .eq(StrUtil.isNotEmpty(queryDto.getStatus()), CostAllocationRule::getStatus, queryDto.getStatus())
                .eq(StrUtil.isNotBlank(queryDto.getStatisticalCycle()), CostAllocationRule::getStatisticalCycle, queryDto.getStatisticalCycle());
        List<CostAllocationRule> allocationRuleList = list(queryWrapper);
        List<CostAllocationRuleVo> costAllocationRuleVoList = new ArrayList<>();
        for (CostAllocationRule costAllocationRule : allocationRuleList) {
            CostAllocationRuleVo costAllocationRuleVo = BeanUtil.copyProperties(costAllocationRule, CostAllocationRuleVo.class);
            List<CostAllocationRuleConfigIndex> costAllocationRuleConfigIndex = costAllocationRuleConfigIndexService.getByAllocationRuleId(costAllocationRule.getId());
            List<CostAllocationRuleConfigItem> costAllocationRuleConfigItem = costAllocationRuleConfigItemService.getByAllocationRuleId(costAllocationRule.getId());

            costAllocationRuleVo.setCostAllocationRuleConfigIndex(costAllocationRuleConfigIndex);
            costAllocationRuleVo.setCostAllocationRuleConfigItem(costAllocationRuleConfigItem);
            costAllocationRuleVoList.add(costAllocationRuleVo);
        }
        ruleVoPage.setRecords(costAllocationRuleVoList);
        ruleVoPage.setTotal(allocationRuleList.size());
        return ruleVoPage;
    }

    /**
     * 根据id查询分摊规则
     *
     * @param id
     * @return
     */
    @Override
    public CostAllocationRuleVo getAllocationRuleById(Long id) {
        CostAllocationRule allocationRule = getById(id);
        CostAllocationRuleVo costAllocationRuleVo = BeanUtil.copyProperties(allocationRule, CostAllocationRuleVo.class);
        List<CostAllocationRuleConfigIndex> costAllocationRuleConfigIndex = costAllocationRuleConfigIndexService.getByAllocationRuleId(allocationRule.getId());
        List<CostAllocationRuleConfigItem> costAllocationRuleConfigItem = costAllocationRuleConfigItemService.getByAllocationRuleId(allocationRule.getId());
        costAllocationRuleVo.setCostAllocationRuleConfigItem(costAllocationRuleConfigItem);
        costAllocationRuleVo.setCostAllocationRuleConfigIndex(costAllocationRuleConfigIndex);
        return costAllocationRuleVo;
    }


    /**
     * 分摊规则校验
     *
     * @param dto
     * @return
     */
    @Override
    public ValidatorResultVo verificationAllocationRule(CostAllocationRuleVerificationDto dto) {
        Long sTime = System.currentTimeMillis();
        Map<String, Double> map = new HashMap<>();
        String result = "";
        String errorMsg = "";
        ValidatorResultVo vo = new ValidatorResultVo();
        List<CostVerificationResultIndex> indexList = new ArrayList<>();
        //获取里面的内容
        final List<FormulaDto.FormulaParam> paramList = dto.getFormulaDto().getParams();
        //遍历判断求值
        for (FormulaDto.FormulaParam param : paramList) {

            BigDecimal itemNumber;
            //判断粒度和核算对象类型
            //核算项直接调用接口计算
            if (param.getType().equals("item")) {
                CostAllocationRuleConfigItemDto costAllocationRuleConfigItemDto = JSON.parseObject(param.getValue(), CostAllocationRuleConfigItemDto.class);
                BigDecimal itemResult = new BigDecimal(0.0);
                CostIndexConfigItemDto costIndexConfigItemDto = new CostIndexConfigItemDto();
                BeanUtil.copyProperties(costAllocationRuleConfigItemDto, costIndexConfigItemDto);
                CostAccountItem costAccountItem = cacheUtils.getCostAccountItem(costAllocationRuleConfigItemDto.getConfigId());
                //计算值
                final List<String> accounts = ExpressionCheckHelper.getIds(costAllocationRuleConfigItemDto.getAccounts());
                if (CollUtil.isEmpty(accounts)) {
                    final ValidatorResultVo item = costAccountIndexService.verificationRuleItem(costIndexConfigItemDto, param.getDimension(), dto.getStartTime(), dto.getEndTime(), dto.getObjectId(), costAllocationRuleConfigItemDto.getAccountObject());
                    if (StrUtil.isBlank(item.getResult())) {
                        errorMsg = param.getName() + param.getDesc() + "查不到数据" + "   原因:" + item.getErrorMsg();
                        vo.setErrorMsg(errorMsg);
                        Long eTime = System.currentTimeMillis();
                        vo.setExecuteTime(Integer.valueOf(String.valueOf(eTime - sTime)));
                        return vo;
                    }
                    itemResult.add(new BigDecimal(item.getResult()));

                } else {
                    for (String account : accounts) {
                        final ValidatorResultVo item = costAccountIndexService.verificationRuleItem(costIndexConfigItemDto, param.getDimension(), dto.getStartTime(), dto.getEndTime(), account, costAllocationRuleConfigItemDto.getAccountObject());
                        if (StrUtil.isBlank(item.getResult())) {
                            errorMsg = param.getName() + param.getDesc() + "查不到数据" + "   原因:" + item.getErrorMsg();
                            vo.setErrorMsg(errorMsg);
                            Long eTime = System.currentTimeMillis();
                            vo.setExecuteTime(Integer.valueOf(String.valueOf(eTime - sTime)));
                            return vo;
                        }
                        itemResult.add(new BigDecimal(item.getResult()));
                    }

                }


                //核算比例处理
                //判断核算比例是否为空
                if (costAllocationRuleConfigItemDto.getAccountProportion() != null) {
                    //查询核算比例的值
                    //判断是不是自定义比例
                    LambdaQueryWrapper<CostAccountProportionRelation> lqw = new LambdaQueryWrapper<CostAccountProportionRelation>()
                            .eq(CostAccountProportionRelation::getCostAccountProportionId, costAllocationRuleConfigItemDto.getAccountProportionId());
                    if (StrUtil.isBlank(costAllocationRuleConfigItemDto.getProportionBaseId().toString())) {
                        lqw.eq(CostAccountProportionRelation::getBzid, dto.getObjectId());
                    } else {
                        lqw.eq(CostAccountProportionRelation::getBzid, costAllocationRuleConfigItemDto.getProportionBaseId());
                    }
                    final CostAccountProportionRelation costAccountProportionRelation = costAccountProportionRelationMapper.selectOne(lqw);
                    if (costAccountProportionRelation == null) {
                        vo.setErrorMsg("核算项" + dto.getObjectId() + "选择的核算比例未设置");
                        return vo;
                    }
                    itemNumber = itemResult.multiply(new BigDecimal(costAccountProportionRelation.getProportion()));
                    map.put(param.getKey(), itemNumber.doubleValue());
                } else {
                    map.put(param.getKey(), itemResult.doubleValue());
                }
            }
            //指标处理
            if (param.getType().equals("index")) {
                //根据id查询指标公式
                JSONObject configIndex = new JSONObject(param.getValue());
                String indexFormula = costAccountIndexService.getById(configIndex.getLong("id")).getIndexFormula();
                final ValidatorResultVo index = getIndex(indexFormula, dto, param);
                //判断计算是否成功
                if (StrUtil.isBlank(index.getResult())) {
                    vo.setErrorMsg(index.getErrorMsg());
                    Long eTime = System.currentTimeMillis();
                    vo.setExecuteTime(Integer.valueOf(String.valueOf(eTime - sTime)));
                    return vo;
                }
                map.put(param.getKey(), Double.valueOf(index.getResult()));
            }
        }
        try {
            String newExpression = dto.getFormulaDto().getExpression().replace("%", "/100");
            result = ExpressionCheckHelper.checkAndCalculate(map,
                    newExpression,
                    null,
                    null,
                    null);
        } catch (NullPointerException e1) {
            errorMsg = "表达式校验不通过，请检查表达式。";
        } catch (Exception e2) {
            errorMsg = e2.getMessage();
        }
        //判断结果是否是数字格式
        if (result.equals("NaN")) {
            result = "0.0";
        }

        Long eTime = System.currentTimeMillis();
        //封装返回对象
        Double aDouble = Double.valueOf(result);
        Double newResult = aDouble * dto.getNumber();
        vo.setResult(newResult.toString());
        vo.setExecuteTime(Integer.valueOf(String.valueOf(eTime - sTime)));
        vo.setErrorMsg(errorMsg);
        return vo;
    }


    /**
     * 此方法用于解析分摊项为指标
     *
     * @param expression
     * @param dto
     * @return
     */
    private ValidatorResultVo getIndex(String expression, CostAllocationRuleVerificationDto dto, FormulaDto.FormulaParam param) {
        Long sTime = System.currentTimeMillis();
        ValidatorResultVo vo = new ValidatorResultVo();
//        //先查询定时任务指标计算表是否有值
//        //根据id查询指标公式
//        JSONObject configIndex = new JSONObject(param.getValue());
//        CostVerificationResultIndex costVerificationResultIndex=new CostVerificationResultIndex().selectOne(new LambdaQueryWrapper<CostVerificationResultIndex>()
//               .eq(CostVerificationResultIndex::getIndexId,costAccountIndexService.getById(configIndex.getLong("id")))
//                .eq());
        Map<String, Double> map = new HashMap<>();
        String result = "";
        String errorMsg;
        //拿到指标公式解析,获取到key集合
        Set<String> keys = ExpressionCheckHelper.expressionQuantity(expression);
        //遍历key,拿到配置项
        for (String key : keys) {
            //根据key去查询核算配置项
            CostIndexConfigItem costIndexConfigItem = costIndexConfigItemService.getOne(new LambdaQueryWrapper<CostIndexConfigItem>()
                    .eq(CostIndexConfigItem::getConfigKey, key));
            //能查到,是配置项
            if (costIndexConfigItem != null) {
                //核算项直接调用接口计算
                BigDecimal itemResult = new BigDecimal(0.0);
                CostIndexConfigItemDto costIndexConfigItemDto = BeanUtil.copyProperties(costIndexConfigItem, CostIndexConfigItemDto.class);
                final String dimension = cacheUtils.getCostAccountItem(costIndexConfigItem.getConfigId()).getDimension();
                //计算配置项的值
                //计算值
                final List<String> accounts = ExpressionCheckHelper.getIds(costIndexConfigItemDto.getAccounts());
                if (CollUtil.isEmpty(accounts)) {
                    final ValidatorResultVo item = costAccountIndexService.verificationRuleItem(costIndexConfigItemDto, dimension, dto.getStartTime(), dto.getEndTime(), dto.getObjectId(), costIndexConfigItemDto.getAccountObject());
                    if (StrUtil.isBlank(item.getResult())) {
                        errorMsg = param.getName() + param.getDesc() + "查不到数据" + "   原因:" + item.getErrorMsg();
                        vo.setErrorMsg(errorMsg);
                        Long eTime = System.currentTimeMillis();
                        vo.setExecuteTime(Integer.valueOf(String.valueOf(eTime - sTime)));
                        return vo;
                    }
                    itemResult.add(new BigDecimal(item.getResult()));

                } else {
                    for (String account : accounts) {
                        final ValidatorResultVo item = costAccountIndexService.verificationRuleItem(costIndexConfigItemDto, dimension, dto.getStartTime(), dto.getEndTime(), account, costIndexConfigItemDto.getAccountObject());
                        if (StrUtil.isBlank(item.getResult())) {
                            errorMsg = param.getName() + param.getDesc() + "查不到数据" + "   原因:" + item.getErrorMsg();
                            vo.setErrorMsg(errorMsg);
                            Long eTime = System.currentTimeMillis();
                            vo.setExecuteTime(Integer.valueOf(String.valueOf(eTime - sTime)));
                            return vo;
                        }
                        itemResult.add(new BigDecimal(item.getResult()));
                    }
                }
                map.put(key, itemResult.doubleValue());
            }
            //如果查询不到,则该key对应是为配置项指标
            if (costIndexConfigItem == null) {
                //根据key查询配置指标
                CostIndexConfigIndex costIndexConfigIndex = cacheUtils.getCostIndexConfigIndex(key);
                //根据指标配置项查询指标
                CostAccountIndex costAccountIndex = costAccountIndexService.getById(costIndexConfigIndex.getConfigIndexId());
                //递归
                final ValidatorResultVo index = getIndex(costAccountIndex.getIndexFormula(), dto, param);
                if (StrUtil.isBlank(index.getResult())) {
                    vo.setErrorMsg(index.getErrorMsg());
                    Long eTime = System.currentTimeMillis();
                    vo.setExecuteTime(Integer.valueOf(String.valueOf(eTime - sTime)));
                    return vo;
                }
                map.put(key, Double.valueOf(index.getResult()));
            }
        }
        //计算值
        try {
            String newExpression = expression.replace("%", "/100");
            result = ExpressionCheckHelper.calculate(map, newExpression);
        } catch (Exception e) {
            log.error("计算指标值失败,计算入参为{},失败原因为{}", JSON.toJSONString(dto), e);
        }
        //判断结果是否是数字格式
        if (result.equals("NaN")) {
            result = "0.0";
        }
        vo.setResult(result);
        return vo;
    }

    /**
     * * 计算分摊规则的值
     *
     * @param map       分摊规则计算参数
     * @param id        分摊规则id
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @param objectId  核算对象id
     * @param dimension 粒度
     * @return
     */
    @Override
    public ValidatorResultVo getAllocationRuleData(Map<String, Double> map, Long id, String startTime, String endTime, String objectId, String dimension) {
        ValidatorResultVo vo = new ValidatorResultVo();
        Long sTime = System.currentTimeMillis();
        //先查询表中是否有值
        final CostVerificationResultRule resultRule = costVerificationResultRuleService.getOne(new LambdaQueryWrapper<CostVerificationResultRule>()
                .eq(CostVerificationResultRule::getRuleId, id)
                .eq(CostVerificationResultRule::getAccountDate, startTime)
                .eq(CostVerificationResultRule::getUnitId, objectId));
        if (resultRule != null) {
            vo.setResult(resultRule.getRuleCount().toString());
            return vo;
        }
        Map<String, BigDecimal> resultMap = new HashMap<>();
        String result = "";
        //根据id查询分摊规则对象
        CostAllocationRule costAllocationRule = getById(id);
        //解析公式获取keys
        Set<String> keys = ExpressionCheckHelper.expressionQuantity(costAllocationRule.getAllocationRuleFormula());
        // 使用线程池提交并行任务
        List<Future<?>> futures = new ArrayList<>();
        for (String key : keys) {
            futures.add(rulePool.submit(() -> {
                calculateAllocationRulePart(key, map, resultMap, startTime, endTime, objectId);
                log.info("我是线程池中的线程,线程名为{}", Thread.currentThread().getName());
            }));
        }

        // 等待所有任务完成
        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (Exception e) {
                // 处理异常
                log.error("计算分摊规则的值失败", e);
            }
        }
        //计算值
        try {
            String newExpression = costAllocationRule.getAllocationRuleFormula().replace("%", "/100");
            result = ExpressionCheckHelper.calculate(map, newExpression);
        } catch (Exception e) {
            log.error("计算分摊规则的值失败", e);
        }
        //判断计算是否是数字
        if (result.equals("NaN") || StrUtil.isBlank(result)) {
            result = "0.0";
        }
        vo.setResult(result);
        vo.setMap(resultMap);
        log.info("getAllocationRuleData耗时为{}", System.currentTimeMillis() - sTime);
        return vo;
    }


    private void calculateAllocationRulePart(String key, Map<String, Double> map, Map<String, BigDecimal> resultMap,
                                             String startTime, String endTime,
                                             String objectId) {
        //获取配置项
        CostAllocationRuleConfigItem allocationRuleConfigItem = costAllocationRuleConfigItemService.getOne(new LambdaQueryWrapper<CostAllocationRuleConfigItem>()
                .eq(CostAllocationRuleConfigItem::getConfigKey, key));

        //如果查到了,就是配置项
        if (allocationRuleConfigItem != null) {
            ValidatorResultVo item = new ValidatorResultVo();
            String dimension = allocationRuleConfigItem.getDimension();
            CostIndexConfigItemDto costIndexConfigItemDto = BeanUtil.copyProperties(allocationRuleConfigItem, CostIndexConfigItemDto.class);
            final CostVerificationResultIndex costVerificationResultIndex = new CostVerificationResultIndex().selectOne(new LambdaQueryWrapper<CostVerificationResultIndex>()
                    .eq(CostVerificationResultIndex::getAccountDate, startTime)
                    .eq(CostVerificationResultIndex::getItemId, costIndexConfigItemDto.getConfigId())
                    .eq(CostVerificationResultIndex::getUnitId, objectId));
            if (costVerificationResultIndex != null) {
                item.setResult(costVerificationResultIndex.getItemCount() + "");
            } else {

                item = costAccountIndexServiceImpl.getItem(costIndexConfigItemDto, startTime, endTime, objectId, allocationRuleConfigItem.getAccountPeriod(), dimension, allocationRuleConfigItem.getAccountObject());
            }
            if (StrUtil.isNotBlank(item.getErrorMsg())) {
                return;
            }
            //构造计算map
            map.put(key, Double.valueOf(item.getResult()));
            resultMap.put(allocationRuleConfigItem.getConfigName(), BigDecimal.valueOf(Double.valueOf(item.getResult())));
        }
        //如果查询不到,则该key对应是为配置项指标
        if (allocationRuleConfigItem == null) {
            //根据key查询配置指标
            CostAllocationRuleConfigIndex costAllocationRuleConfigIndex = new CostAllocationRuleConfigIndex().selectOne(new LambdaQueryWrapper<CostAllocationRuleConfigIndex>()
                    .eq(CostAllocationRuleConfigIndex::getConfigKey, key));
            //根据指标配置项查询指标
            CostAccountIndex costAccountIndex = costAccountIndexService.getById(costAllocationRuleConfigIndex.getConfigIndexId());
            //计算值
            ValidatorResultVo index = new ValidatorResultVo();

            //先查询定时任务计算指标表中的是否有数据
            CostVerificationResultIndex costVerificationResultIndex = new CostVerificationResultIndex().selectOne(new LambdaQueryWrapper<CostVerificationResultIndex>()
                    .eq(CostVerificationResultIndex::getIndexId, costAllocationRuleConfigIndex.getConfigIndexId())
                    .eq(CostVerificationResultIndex::getUnitId, objectId));
            if (costVerificationResultIndex != null) {
                index.setResult(costVerificationResultIndex.getIndexCount() + "");
            } else {
                try {
                    index = costAccountIndexServiceImpl.getIndex(costAccountIndex, startTime, endTime, objectId, costAccountIndex.getStatisticalCycle() + "");
                } catch (Exception e) {
                    log.error("计算指标值失败,计算入参为{},失败原因为{}", JSON.toJSONString(costAccountIndex), e.getMessage());
                }
            }
            if (StrUtil.isBlank(index.getResult())) {
                index.setResult("0.0");
            }
            map.put(key, Double.valueOf(index.getResult()));
            resultMap.put(costAllocationRuleConfigIndex.getConfigIndexName(), BigDecimal.valueOf(Double.valueOf(index.getResult())));
        }
    }


    /**
     * * 定时计算分摊规则的值
     *
     * @param
     * @param
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @param objectId  核算对象id
     * @return
     */
    public ValidatorResultVo getTimeingAllocationRuleData(CostAllocationRule costAllocationRule, String startTime, String endTime, String objectId) {
        ValidatorResultVo vo = new ValidatorResultVo();
        Map<String, Double> map = new HashMap<>();
        Map<String, BigDecimal> resultMap = new HashMap<>();
        String result = "";
        //解析公式获取keys
        Set<String> keys = ExpressionCheckHelper.expressionQuantity(costAllocationRule.getAllocationRuleFormula());
        //遍历查询是否为配置项/配置指标
        for (String key : keys) {
            //获取配置项
            CostAllocationRuleConfigItem allocationRuleConfigItem = costAllocationRuleConfigItemService.getOne(new LambdaQueryWrapper<CostAllocationRuleConfigItem>()
                    .eq(CostAllocationRuleConfigItem::getConfigKey, key));
            //如果查到了,就是配置项
            if (allocationRuleConfigItem != null) {
                ValidatorResultVo item = new ValidatorResultVo();
                String dimension = allocationRuleConfigItem.getDimension();
                CostIndexConfigItemDto costIndexConfigItemDto = BeanUtil.copyProperties(allocationRuleConfigItem, CostIndexConfigItemDto.class);
                final CostVerificationResultIndexNew costVerificationResultIndexNew = new CostVerificationResultIndexNew().selectOne(new LambdaQueryWrapper<CostVerificationResultIndexNew>()
                        .eq(CostVerificationResultIndexNew::getAccountDate, startTime)
                        .eq(CostVerificationResultIndexNew::getItemId, allocationRuleConfigItem.getConfigId())
                        .eq(CostVerificationResultIndexNew::getUnitId, objectId));
                if (costVerificationResultIndexNew != null) {
                    item.setResult(costVerificationResultIndexNew.getItemCount() + "");
                } else {
                    item = costAccountIndexServiceImpl.getItem(costIndexConfigItemDto, startTime, endTime, objectId, allocationRuleConfigItem.getAccountPeriod(), dimension, allocationRuleConfigItem.getAccountObject());
                }
                if (StrUtil.isNotBlank(item.getErrorMsg())) {
                    return item;
                }
                //构造计算map
                map.put(key, Double.valueOf(item.getResult()));
                resultMap.put(allocationRuleConfigItem.getConfigName(), new BigDecimal(item.getResult()));
            }
            //如果查询不到,则该key对应是为配置项指标
            if (allocationRuleConfigItem == null) {
                //根据key查询配置指标
                CostAllocationRuleConfigIndex costAllocationRuleConfigIndex = new CostAllocationRuleConfigIndex().selectOne(new LambdaQueryWrapper<CostAllocationRuleConfigIndex>()
                        .eq(CostAllocationRuleConfigIndex::getConfigKey, key));
                //根据指标配置项查询指标
                CostAccountIndex costAccountIndex = new CostAccountIndex().selectById(costAllocationRuleConfigIndex.getConfigIndexId());
                //计算值
                ValidatorResultVo index = new ValidatorResultVo();
                //先查询定时任务计算指标表中的是否有数据
                CostVerificationResultIndexNew costVerificationResultIndexNew = new CostVerificationResultIndexNew().selectOne(new LambdaQueryWrapper<CostVerificationResultIndexNew>()
                        .eq(CostVerificationResultIndexNew::getIndexId, costAllocationRuleConfigIndex.getConfigIndexId())
                        .eq(CostVerificationResultIndexNew::getUnitId, objectId)
                        .eq(CostVerificationResultIndexNew::getOuterMostIndexId, costAllocationRuleConfigIndex.getConfigIndexId())
                        .eq(CostVerificationResultIndexNew::getAccountDate, startTime));
                if (costVerificationResultIndexNew != null) {
                    index.setResult(costVerificationResultIndexNew.getIndexCount() + "");
                } else {
                    try {
                        index = costAccountIndexServiceImpl.getIndex(costAccountIndex, startTime, endTime, objectId, costAccountIndex.getStatisticalCycle() + "");
                    } catch (Exception e) {
                        log.info("计算指标值失败,计算入参为{},失败原因为{}", JSON.toJSONString(costAccountIndex), e);
                    }
                }
                if (StrUtil.isBlank(index.getResult())) {
                    index.setResult("0.0");
                }
                map.put(key, Double.valueOf(index.getResult()));
                resultMap.put(costAllocationRuleConfigIndex.getConfigIndexName(), BigDecimal.valueOf(Double.valueOf(index.getResult())));
            }
        }
        //计算值
        try {
            String newExpression = costAllocationRule.getAllocationRuleFormula().replace("%", "/100");
            result = ExpressionCheckHelper.calculate(map, newExpression);
        } catch (Exception e) {
            log.error("计算分摊规则的值失败", e);
            log.info("计算分摊规则的值失败", e);
        }
        //判断计算是否是数字
        if (result.equals("NaN") || StrUtil.isBlank(result)) {
            result = "0.0";
        }
        vo.setResult(result);
        vo.setMap(resultMap);
        return vo;
    }

    /**
     * 定时插入分摊规则计算结果明细
     */
    @XxlJob("saveRuleResultDetail")
    public void saveRuleResultDetail() {
        ValidatorResultVo vo = new ValidatorResultVo();
        BigDecimal result = new BigDecimal(0.0);
        List<CostVerificationResultRule> resultRuleList = new ArrayList<>();
        //自定义传参
        String jobParam = XxlJobHelper.getJobParam();
        //查询所有可用的分摊规则
        List<CostAllocationRule> costAllocationRuleList = baseMapper.selectList(new LambdaQueryWrapper<CostAllocationRule>()
                .eq(CostAllocationRule::getDelFlag, "0")
                .eq(CostAllocationRule::getStatus, "0"));
        //查询所有可用的核算单元
        List<CostAccountUnit> costAccountUnitList = new CostAccountUnit().selectList(new LambdaQueryWrapper<CostAccountUnit>()
                .eq(CostAccountUnit::getDelFlag, "0")
                .eq(CostAccountUnit::getStatus, "0"));
        //过滤获取核算单元id
        List<Long> unitIdList = costAccountUnitList.stream().map(CostAccountUnit::getId).collect(Collectors.toList());
        for (CostAllocationRule costAllocationRule : costAllocationRuleList) {
            //遍历执行计算
            for (Long unitId : unitIdList) {
                Map<String, Double> map = new HashMap<>();
                String startTime = "";
                String endTime = "";
                //设置时间
                if (StrUtil.isNotEmpty(jobParam) && jobParam != "") {
                    final List<String> times = ExpressionCheckHelper.getIds(jobParam);
                    startTime = times.get(0);
                    endTime = times.get(1);
                    log.info("自定义传入参数开始时间为：" + startTime + "结束时间为：" + endTime);
                } else {
                    //获取当前时间的上一个月
                    String formattedYearMonth = LocalDateTime.now().minusMonths(1).format(DateTimeFormatter.ofPattern("yyyyMM"));
                    startTime = formattedYearMonth;
                    endTime = formattedYearMonth;
                    log.info("默认定时任务开始时间为:" + startTime + "结束时间为：" + endTime);
                }
                //先查表,有数据不计算,跳过
                final CostVerificationResultRule costVerificationResultRule = new CostVerificationResultRule().selectOne(new LambdaQueryWrapper<CostVerificationResultRule>()
                        .eq(CostVerificationResultRule::getRuleId, costAllocationRule.getId())
                        .eq(CostVerificationResultRule::getUnitId, unitId)
                        .eq(CostVerificationResultRule::getAccountDate, startTime));
                if (costVerificationResultRule != null) {
                    continue;
                }
                CostVerificationResultRule resultRule = new CostVerificationResultRule();
                resultRule.setRuleId(costAllocationRule.getId());
                resultRule.setUnitId(Long.valueOf(unitId));
                resultRule.setAccountDate(startTime);
                //创建辅助final变量
                final String sTime = startTime;
                final String eTime = endTime;
                //调用方法计算
                ValidatorResultVo ruleVo = null;
                try {
                    ruleVo = getTimeingAllocationRuleData(costAllocationRule, sTime, eTime, unitId.toString());
                    result = (ruleVo == null || StrUtil.isBlank(ruleVo.getResult()) ? new BigDecimal(0.0) : new BigDecimal(ruleVo.getResult()));
                } catch (Exception e) {
                    log.info("分摊计算异常", e);
                    result = new BigDecimal(0.0);
                }
                //插入校验明细表
                resultRule.setRuleCount(result);
                resultRuleList.add(resultRule);
            }
            //插入表中
            costVerificationResultRuleService.saveBatch(resultRuleList);
            resultRuleList.clear();
        }
    }

}

