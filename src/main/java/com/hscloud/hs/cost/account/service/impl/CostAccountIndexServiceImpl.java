package com.hscloud.hs.cost.account.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.hscloud.hs.cost.account.constant.CacheConstants;
import com.hscloud.hs.cost.account.constant.enums.*;
import com.hscloud.hs.cost.account.mapper.CostAccountIndexMapper;
import com.hscloud.hs.cost.account.mapper.CostVerificationResultIndexMapper;
import com.hscloud.hs.cost.account.mapper.CostVerificationResultIndexNewMapper;
import com.hscloud.hs.cost.account.model.dto.*;
import com.hscloud.hs.cost.account.model.entity.*;
import com.hscloud.hs.cost.account.model.pojo.UnitInfo;
import com.hscloud.hs.cost.account.model.vo.CostAccountIndexVo;
import com.hscloud.hs.cost.account.model.vo.CostIndexConfigItemVo;
import com.hscloud.hs.cost.account.model.vo.ValidatorResultVo;
import com.hscloud.hs.cost.account.service.*;
import com.hscloud.hs.cost.account.utils.BeforeDateUtils;
import com.hscloud.hs.cost.account.utils.ExpressionCheckHelper;
import com.hscloud.hs.cost.account.utils.LocalCacheUtils;
import com.hscloud.hs.cost.account.utils.SqlUtil;
import com.pig4cloud.pigx.common.core.exception.BizException;
import com.pig4cloud.pigx.common.core.util.SpringContextHolder;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import dm.jdbc.util.StringUtil;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 核算指标表 服务实现类
 * </p>
 *
 * @author
 * @since 2023-09-04
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CostAccountIndexServiceImpl extends ServiceImpl<CostAccountIndexMapper, CostAccountIndex> implements ICostAccountIndexService {

    @Autowired
    private ICostIndexConfigItemService costIndexConfigItemService;

    @Autowired
    private ICostIndexConfigIndexService costIndexConfigIndexService;
    @Autowired
    private SqlUtil sqlUtil;
    @Autowired
    private CostAccountItemService costAccountItemService;


    private final CostAccountIndexMapper costAccountIndexMapper;
    private final CostVerificationResultIndexService costVerificationResultIndexService;
    private final CostVerificationResultIndexMapper costVerificationResultIndexMapper;
    private final CostVerificationResultIndexNewMapper costVerificationResultIndexNewMapper;
    private final LocalCacheUtils cacheUtils;


    private final ExecutorService executorService = new ThreadPoolExecutor(
            8, // 核心线程数
            8, // 最大线程数
            0L, // 空闲线程存活时间
            TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>(1), // 有界队列
            r -> {
                Thread thread = new Thread(r);
                thread.setName("index-calculate-" + thread.getId());
                return thread;
            },
            new ThreadPoolExecutor.CallerRunsPolicy() // 当队列已满时的拒绝策略
    );


    /**
     * 核算指标分页查询
     *
     * @param queryDto
     * @return
     */
    @Override
    public IPage<CostAccountIndexVo> getAccountIndexPage(CostAccountIndexQueryDto queryDto) {
//        if (queryDto.getIndexGroupId()==null) {
//            throw new BizException("指标分组为空");
//        }
        List<CostAccountIndexVo> costAccountIndexVoList = new ArrayList<>();
        Page<CostAccountIndexVo> accountIndexPage = new Page<>(queryDto.getCurrent(), queryDto.getSize());
//        LambdaQueryWrapper<CostAccountIndex> wrapper = new LambdaQueryWrapper<>();
//        wrapper.eq(queryDto.getId() != null, CostAccountIndex::getId, queryDto.getId())
//                .like(StrUtil.isNotEmpty(queryDto.getName()), CostAccountIndex::getName, queryDto.getName())
//                .ge(queryDto.getBeginTime() != null, CostAccountIndex::getCreateTime, queryDto.getBeginTime())  // 如果startTime不为null，则添加查询条件 >= startTime
//                .le(queryDto.getEndTime() != null, CostAccountIndex::getCreateTime, queryDto.getBeginTime())
//                .eq(StrUtil.isNotEmpty(queryDto.getStatus()), CostAccountIndex::getStatus, queryDto.getStatus())
//                .eq(CostAccountIndex::getDelFlag, '0')
//                .eq(queryDto.getIndexGroupId() != null, CostAccountIndex::getIndexGroupId, queryDto.getIndexGroupId());
//        List<CostAccountIndex> costAccountIndexList = list(wrapper);

        IPage<CostAccountIndex> costAccountIndexIPage = costAccountIndexMapper.listByQueryDto(accountIndexPage, queryDto);
        for (CostAccountIndex costAccountIndex : costAccountIndexIPage.getRecords()) {
            CostAccountIndexVo costAccountIndexVo = BeanUtil.copyProperties(costAccountIndex, CostAccountIndexVo.class);
            List<CostIndexConfigItem> costIndexConfigItemList = costIndexConfigItemService.getByIndexId(costAccountIndex.getId());
            List<CostIndexConfigItemVo> costIndexConfigItemListVo = new ArrayList<>();
            for (CostIndexConfigItem costIndexConfigItem : costIndexConfigItemList) {
                CostIndexConfigItemVo costIndexConfigItemVo = BeanUtil.copyProperties(costIndexConfigItem, CostIndexConfigItemVo.class);
                costIndexConfigItemListVo.add(costIndexConfigItemVo);
            }
            List<CostIndexConfigIndex> costIndexConfigIndexList = costIndexConfigIndexService.getByIndexId(costAccountIndex.getId());
            costAccountIndexVo.setCostIndexConfigItemList(costIndexConfigItemListVo);
            costAccountIndexVo.setCostIndexConfigIndexList(costIndexConfigIndexList);
            costAccountIndexVoList.add(costAccountIndexVo);
        }

        accountIndexPage.setRecords(costAccountIndexVoList);
        accountIndexPage.setTotal(costAccountIndexIPage.getTotal());
        return accountIndexPage;
    }


    /**
     * 新增或修改核算指标
     *
     * @param dto
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = CacheConstants.COST_INDEX, allEntries = true)
    public void saveOrUpdateAccountIndex(CostAccountIndexDto dto) {

        LambdaQueryWrapper<CostAccountIndex> queryWrapper = new LambdaQueryWrapper<CostAccountIndex>();
        queryWrapper.eq(CostAccountIndex::getName, dto.getName());
        CostAccountIndex one = this.getOne(queryWrapper);
        if (dto.getId() == null && one != null && one.getName().equals(dto.getName())) {
            throw new BizException("该指标名称已存在");
        }
        CostAccountIndex costAccountIndex = BeanUtil.copyProperties(dto, CostAccountIndex.class);
        FormulaDto formulaDto = dto.getFormulaDto();
        costAccountIndex.setIndexFormula(formulaDto.getExpression());
        costAccountIndex.setStatisticalCycle(dto.getStatisticalCycle());
        saveOrUpdate(costAccountIndex);
        if (dto.getId() == null) {
            cacheUtils.setIndexMap(costAccountIndex);
        }
        if (dto.getId() != null) {
            //删除中间表数据
            costIndexConfigItemService.deleteByIndexId(dto.getId());
            costIndexConfigIndexService.deleteByIndexId(dto.getId());
        }
        //插入核算指标配置项
        List<FormulaDto.FormulaParam> params = formulaDto.getParams();
        List<CostIndexConfigItem> costIndexConfigItemList = new ArrayList<CostIndexConfigItem>();
        List<CostIndexConfigIndex> costIndexConfigIndexList = new ArrayList<CostIndexConfigIndex>();
        for (FormulaDto.FormulaParam param : params) {
            if (param.getType().equals("item")) {
                String value = param.getValue();
                CostIndexConfigItem costIndexConfigItem = new CostIndexConfigItem();
                costIndexConfigItem.setConfigKey(param.getKey());
                costIndexConfigItem.setConfigName(param.getName());
                costIndexConfigItem.setConfigDesc(param.getDesc());
                if (value != null) {
                    CostIndexConfigItemDto costIndexConfigItemDto = JSON.parseObject(value, CostIndexConfigItemDto.class);
                    BeanUtil.copyProperties(costIndexConfigItemDto, costIndexConfigItem);
                    costIndexConfigItem.setIndexId(costAccountIndex.getId());
                    costIndexConfigItemList.add(costIndexConfigItem);
                }
            }
            if (param.getType().equals("index")) {
                JSONObject configIndex = new JSONObject(param.getValue());
                CostIndexConfigIndex costIndexConfigIndex = new CostIndexConfigIndex();
                costIndexConfigIndex.setConfigKey(param.getKey());
                costIndexConfigIndex.setIndexId(costAccountIndex.getId());
                costIndexConfigIndex.setConfigIndexId(configIndex.getLong("id"));
                costIndexConfigIndex.setConfigIndexName(configIndex.getStr("name"));
                costIndexConfigIndexList.add(costIndexConfigIndex);
            }
        }
        costIndexConfigItemService.saveOrUpdateBatch(costIndexConfigItemList);
        costIndexConfigIndexService.saveOrUpdateBatch(costIndexConfigIndexList);
        //同步缓存
        cacheUtils.setCostIndexConfigItemMap(costIndexConfigItemList);
        cacheUtils.setCostIndexConfigIndexMap(costIndexConfigIndexList);

        if (dto.getId() != null) {
            getTimeIndex(dto);
        }
    }

    /**
     * 此方法用于计算指标修改
     *
     * @param dto
     */
    private void getTimeIndex(CostAccountIndexDto dto) {
        BigDecimal result = new BigDecimal(0.0);
        List<CostVerificationResultIndexNew> resultIndexList = new ArrayList<>();
        CostAccountIndex costAccountIndex = new CostAccountIndex().selectById(dto.getId());
        //获取当月时间时间
        String formattedYearMonth = LocalDateTime.now().minusMonths(1).format(DateTimeFormatter.ofPattern("yyyyMM"));
        //String formattedYearMonth="202309";
        //先查询删除定时任务核算项表中的数据
        List<CostVerificationResultIndexNew> list = new CostVerificationResultIndexNew().selectList(new LambdaQueryWrapper<CostVerificationResultIndexNew>()
                .eq(CostVerificationResultIndexNew::getOuterMostIndexId, dto.getId()));
        final List<Long> idList = list.stream().map(CostVerificationResultIndexNew::getId).collect(Collectors.toList());
        //删除
        costVerificationResultIndexNewMapper.deleteBatchIds(idList);
        //新增计算
        List<CostAccountUnit> costAccountUnitList = new CostAccountUnit().selectList(new LambdaQueryWrapper<CostAccountUnit>().eq(CostAccountUnit::getDelFlag, "0").eq(CostAccountUnit::getStatus, "0"));
        for (CostAccountUnit costAccountUnit : costAccountUnitList) {
            CostVerificationResultIndexNew resultIndex = new CostVerificationResultIndexNew();
            resultIndex.setUnitId(costAccountUnit.getId());
            resultIndex.setUnitName(costAccountUnit.getName());
            resultIndex.setAccountDate(formattedYearMonth);
            resultIndex.setOuterMostIndexId(dto.getId());
            ValidatorResultVo indexVo = getVerificationIndexNew(costAccountIndex, formattedYearMonth, formattedYearMonth, costAccountUnit.getId(), resultIndex, resultIndexList);
            result = (indexVo == null || StrUtil.isBlank(indexVo.getResult()) ? new BigDecimal(0.0) : new BigDecimal(indexVo.getResult()));
            //插入校验明细表
            resultIndex.setIndexId(costAccountIndex.getId());
            resultIndex.setIndexCount(result);
            resultIndex.setIndexName(costAccountIndex.getName());
            resultIndexList.add(resultIndex);
        }
        //插入表中
        costVerificationResultIndexNewMapper.insertBatchSomeColumn(resultIndexList);
    }

    @Override
    public Boolean updateStatusAccountIndex(CostAccountIndexStatusDto dto) {
        //根据核算指标id获取核算指标对象
        CostAccountIndex accountIndex = getById(dto.getId());
        if (accountIndex == null) {
            throw new BizException("核算指标不存在");
        }
        accountIndex.setStatus(dto.getStatus());
        return updateById(accountIndex);
    }

    /**
     * 核算指标校验
     *
     * @param dto
     * @return
     */
    @Override
    public ValidatorResultVo verificationAccountIndex(CostAccountIndexVerificationDto dto) {
        Long sTime = System.currentTimeMillis();
        ValidatorResultVo vo = new ValidatorResultVo();
//        CostVerificationResultIndexNew costVerificationResultIndexNew = new CostVerificationResultIndexNew().selectOne(new LambdaQueryWrapper<CostVerificationResultIndexNew>()
//                .eq(CostVerificationResultIndexNew::getIndexId, dto.getIndexId())
//                .eq(CostVerificationResultIndexNew::getUnitId, dto.getObjectId())
//                .eq(CostVerificationResultIndexNew::getAccountDate, dto.getStartTime()));
//        if (costVerificationResultIndexNew != null) {
//            vo.setResult(costVerificationResultIndexNew.getIndexCount().toString());
//            Long eTime = System.currentTimeMillis();
//            vo.setExecuteTime(Integer.valueOf(String.valueOf(eTime - sTime)));
//            return vo;
//        }
        Map<String, Double> map = new HashMap<>();
        String result = "";
        String errorMsg = "";

//        CostVerificationResultIndex resultIndex = new CostVerificationResultIndex();
//        resultIndex.setUnitId(Long.valueOf(dto.getObjectId()));
//        resultIndex.setAccountDate(dto.getStartTime());
//        resultIndex.setOuterMostIndexId(Long.valueOf(dto.getIndexId()));
        //获取公式参数集合
        List<FormulaDto.FormulaParam> params = dto.getFormulaDto().getParams();
        //遍历求值
        for (FormulaDto.FormulaParam param : params) {
            //配置类型 item:核算项 index:核算指标
            //核算项处理
            SimpleDateFormat odf = new SimpleDateFormat("yyyyMM");
            SimpleDateFormat ndf = new SimpleDateFormat("yyyy年M月");
            if (param.getType().equals("item")) {
//                CostVerificationResultIndex costVerificationResultIndex = new CostVerificationResultIndex();
//                BeanUtil.copyProperties(resultIndex, costVerificationResultIndex);
                CostIndexConfigItemDto costIndexConfigItemDto = JSON.parseObject(param.getValue(), CostIndexConfigItemDto.class);
                CostAccountItem costAccountItem = cacheUtils.getCostAccountItem(costIndexConfigItemDto.getConfigId());
                //先查定时任务计算表中的数据
                CostVerificationResultItem costVerificationResultItem = new CostVerificationResultItem().selectOne(new LambdaQueryWrapper<CostVerificationResultItem>()
                        .eq(CostVerificationResultItem::getUnitId, dto.getObjectId())
                        .eq(CostVerificationResultItem::getAccountDate, dto.getStartTime())
                        .eq(CostVerificationResultItem::getItemId, costAccountItem.getId()));
                if (costVerificationResultItem != null) {
                    map.put(param.getKey(), costVerificationResultItem.getItemCount().doubleValue());
                    continue;
                }

                ValidatorResultVo item = getItem(costIndexConfigItemDto, dto.getStartTime(), dto.getEndTime(), dto.getObjectId(), dto.getType(), costAccountItem.getDimension(), costIndexConfigItemDto.getAccountObject());
                if (!StrUtil.isBlank(item.getErrorMsg())) {
                    try {
                        vo.setErrorMsg(item.getErrorMsg());
                        Long eTime = System.currentTimeMillis();
                        vo.setExecuteTime(Integer.valueOf(String.valueOf(eTime - sTime)));
                        return vo;
                    } catch (Exception e) {
                        log.error(param.getDesc() + "查不到数据");
                    }
                } else {
//                    costVerificationResultIndex.setItemId(costIndexConfigItemDto.getConfigId());
//                    costVerificationResultIndex.setItemKey(param.getKey());
//                    costVerificationResultIndex.setItemCount(item.getResult() == null ? new BigDecimal(0.0) : new BigDecimal(item.getResult()));
//                    resultIndexList.add(costVerificationResultIndex);
                    map.put(param.getKey(), Double.valueOf(item.getResult()));
                }
            }
            //核算指标处理
            if (param.getType().equals("index")) {
//                CostVerificationResultIndex costVerificationResultIndex = new CostVerificationResultIndex();
//                BeanUtil.copyProperties(resultIndex, costVerificationResultIndex);
                //根据id查询指标公式
                JSONObject configIndex = new JSONObject(param.getValue());
                CostAccountIndex costAccountIndex = cacheUtils.getCostAccountIndex(configIndex.getLong("id"));
                //先查定时任务计算表中的数据
                CostVerificationResultIndexNew costVerificationResultItemNew = new CostVerificationResultIndexNew().selectOne(new LambdaQueryWrapper<CostVerificationResultIndexNew>()
                        .eq(CostVerificationResultIndexNew::getUnitId, dto.getObjectId())
                        .eq(CostVerificationResultIndexNew::getAccountDate, dto.getStartTime())
                        .eq(CostVerificationResultIndexNew::getIndexId, costAccountIndex.getId()));
                if (costVerificationResultItemNew != null) {
                    map.put(param.getKey(), costVerificationResultItemNew.getIndexCount().doubleValue());
                    continue;
                }

                try {
                    vo = getIndex(costAccountIndex, dto.getStartTime(), dto.getEndTime(), dto.getObjectId(), dto.getType());
                    if (vo.getErrorMsg() != null) {
                        Long eTime = System.currentTimeMillis();
                        vo.setExecuteTime(Integer.valueOf(String.valueOf(eTime - sTime)));
                        return vo;
                    }
                    if (vo.getResult() == null) {
                        try {
                            errorMsg = param.getDesc() + ndf.format(odf.parse(dto.getStartTime())) + param.getName() + "==>查不到数据";
                            vo.setErrorMsg(errorMsg);
                            Long eTime = System.currentTimeMillis();
                            vo.setExecuteTime(Integer.valueOf(String.valueOf(eTime - sTime)));
                            return vo;
                        } catch (Exception e) {
                        }
                    }
                } catch (Exception e) {
                    log.error("核算指标校验异常", e);
                }
                //插入校验明细表
//                costVerificationResultIndex.setIndexId(configIndex.getLong("id"));
//                costVerificationResultIndex.setIndexKey(param.getKey());
//                costVerificationResultIndex.setPath(param.getKey());
//                costVerificationResultIndex.setCalculateFormulaDesc(indexFormula);
//                costVerificationResultIndex.setParentId(configIndex.getLong("id"));
//                costVerificationResultIndex.setIndexCount(vo.getResult() == null ? new BigDecimal(0.0) : new BigDecimal(vo.getResult()));
//                costVerificationResultIndex.setParentId(Long.valueOf(dto.getIndexId()));
//                resultIndexList.add(costVerificationResultIndex);
                map.put(param.getKey(), Double.valueOf(vo.getResult()));
            }
        }

        try {
            String expression = dto.getFormulaDto().getExpression().replace("%", "/100");
            result = ExpressionCheckHelper.checkAndCalculate(map,
                    expression,
                    dto.getIndexUnit(),
                    dto.getReservedDecimal(),
                    dto.getCarryRule());
        } catch (NullPointerException e1) {
            errorMsg = "表达式校验不通过，请检查表达式。";
        } catch (Exception e2) {
            errorMsg = e2.getMessage();
        }
        Long eTime = System.currentTimeMillis();
//        //插入计算明细表
//        resultIndex.setIndexCount(result == null ? new BigDecimal("0") : new BigDecimal(result));
//        resultIndex.setIndexId(Long.valueOf(dto.getIndexId()));
//        resultIndex.setCalculateFormulaDesc(dto.getFormulaDto().getExpression());
//        resultIndexList.add(resultIndex);
//        costVerificationResultIndexService.saveBatch(resultIndexList);
        //封装返回对象
        vo.setResult(result);
        vo.setExecuteTime(Integer.valueOf(String.valueOf(eTime - sTime)));
        vo.setErrorMsg(errorMsg);
        return vo;
    }

    /**
     * 此方法用于校验计算核算指标的值
     *
     * @param expression 表达式
     * @param startTime  开始时间
     * @param endTime    结束时间
     * @param objectId   核算对象id
     * @param type       核算类型
     * @return
     * @throws Exception
     */
    public ValidatorResultVo getVerificationIndex(String expression, String startTime, String endTime, String objectId, String type, String isTrue, CostVerificationResultIndex resultIndex, List<CostVerificationResultIndex> resultIndexList) {
        ValidatorResultVo vo = new ValidatorResultVo();
        Map<String, Double> map = new HashMap<>();
        //拿到指标公式解析,获取到key集合
        Set<String> keys = ExpressionCheckHelper.expressionQuantity(expression);
        //遍历key,拿到配置项
        for (String key : keys) {
            //根据key去查询核算配置项
            CostIndexConfigItem costIndexConfigItem = cacheUtils.getCostIndexConfigItem(key);
            //查询到配置项
            if (costIndexConfigItem != null) {
                CostVerificationResultIndex costVerificationResultIndex = new CostVerificationResultIndex();
                String dimension = cacheUtils.getCostAccountItem(costIndexConfigItem.getConfigId()).getDimension();
                //计算配置项的值
                CostIndexConfigItemDto costIndexConfigItemDto = BeanUtil.copyProperties(costIndexConfigItem, CostIndexConfigItemDto.class);
                if (isTrue.equals(YesNoEnum.YES.getCode())) {
                    BeanUtil.copyProperties(resultIndex, costVerificationResultIndex);
                    costVerificationResultIndex.setItemId(costIndexConfigItemDto.getConfigId());
                    costVerificationResultIndex.setItemKey(key);
                }
                ValidatorResultVo item = getVerificationItem(costIndexConfigItemDto, startTime, endTime, objectId, type, dimension, costIndexConfigItem.getAccountObject(), isTrue, costVerificationResultIndex, resultIndexList);
                String result = "";
                if (item == null || StrUtil.isBlank(item.getResult())) {
                    //计算不到数据,给出原因
                    vo.setErrorMsg("指标中的" + costIndexConfigItem.getConfigName() + costIndexConfigItem.getConfigDesc() + "==>查不到数据");
                    result = "0.0";
                } else {
                    result = item.getResult();
                }
                costVerificationResultIndex.setItemCount(new BigDecimal(result));
                resultIndexList.add(costVerificationResultIndex);
                //构造计算map
                map.put(key, Double.valueOf(result));
            } else if (costIndexConfigItem == null) {
                CostVerificationResultIndex costVerificationResultIndex = new CostVerificationResultIndex();
                //如果查询不到,则该key对应是为配置项指标
                //根据key查询配置指标
                CostIndexConfigIndex costIndexConfigIndex = cacheUtils.getCostIndexConfigIndex(key);
                //如果指标也查不到,说明不存在
                if (costIndexConfigIndex == null) {
                    //计算不到数据,给出原因
                    vo.setErrorMsg("指标中的" + key + "查询不到对应的配置项和子指标");
                    return vo;
                }
                //根据指标配置项查询指标
                CostAccountIndex costAccountIndex = cacheUtils.getCostAccountIndex(costIndexConfigIndex.getConfigIndexId());
                if (isTrue.equals(YesNoEnum.YES.getCode())) {
                    BeanUtil.copyProperties(resultIndex, costVerificationResultIndex);
                    costVerificationResultIndex.setPath(resultIndex.getPath() + "," + key);
                }
                //递归
                final ValidatorResultVo index = getVerificationIndex(costAccountIndex.getIndexFormula(), startTime, endTime, objectId, type, isTrue, costVerificationResultIndex, resultIndexList);
                //插入校验明细表
                costVerificationResultIndex.setIndexCount(index.getResult() == null ? new BigDecimal(0.0) : new BigDecimal(index.getResult()));
                costVerificationResultIndex.setIndexId(Long.valueOf(costAccountIndex.getId()));
                costVerificationResultIndex.setCalculateFormulaDesc(costAccountIndex.getIndexFormula());
                resultIndexList.add(costVerificationResultIndex);
                //构造计算map
                map.put(key, Double.valueOf(index.getResult()));
            }
        }
        //计算值
        String newExpression = expression.replace("%", "/100");
        try {
            vo.setResult(ExpressionCheckHelper.calculate(map, newExpression));
        } catch (Exception e) {
            log.error("指标计算异常");
            vo.setResult("0.0");
        }
        costVerificationResultIndexService.saveBatch(resultIndexList);
        resultIndexList.clear();
        return vo;
    }

    /**
     * 此方法用于校验计算核算项的值
     *
     * @param costIndexConfigItemDto
     * @param startTime
     * @param endTime
     * @param objectId
     * @param type
     * @return
     */
    public ValidatorResultVo getVerificationItem(CostIndexConfigItemDto costIndexConfigItemDto, String startTime, String endTime, String objectId, String type, String dimension, String objectIdType, String isTrue, CostVerificationResultIndex resultIndex, List<CostVerificationResultIndex> resultIndexList) {
        ValidatorResultVo vo = new ValidatorResultVo();
        //处理本周期
        if (costIndexConfigItemDto.getAccountPeriod().equals("current")) {
            vo = getVerificationResult(startTime, endTime, objectId, costIndexConfigItemDto, dimension, objectIdType, isTrue, resultIndex, resultIndexList);
        }
        //处理上一周期
        if (costIndexConfigItemDto.getAccountPeriod().equals("before")) {
            //环比上一周期,往前推一个月
            if ("RING_RATIO".equals(type)) {
                String newStartTime = BeforeDateUtils.getBeforeMonthDate(startTime, -01);
                String newEndTime = BeforeDateUtils.getBeforeMonthDate(endTime, -01);
                vo = getVerificationResult(newStartTime, newEndTime, objectId, costIndexConfigItemDto, dimension, objectIdType, isTrue, resultIndex, resultIndexList);
            }
            //同比上一周期,往前推一年
            if ("YEAR_ON_YEAR".equals(type)) {
                String newStartTime = BeforeDateUtils.getBeforeYearDate(startTime, -01);
                String newEndTime = BeforeDateUtils.getBeforeYearDate(endTime, -01);
                vo = getVerificationResult(newStartTime, newEndTime, objectId, costIndexConfigItemDto, dimension, objectIdType, isTrue, resultIndex, resultIndexList);
            }
        }
        return vo;
    }

    /**
     * 此方法用于校验核算配置项
     *
     * @param startTime
     * @param endTime
     * @param objectId
     * @param costIndexConfigItemDto
     * @return
     */
    public ValidatorResultVo getVerificationResult(String startTime, String endTime, String objectId, CostIndexConfigItemDto costIndexConfigItemDto, String dimension, String objectIdType, String isTrue, CostVerificationResultIndex resultIndex, List<CostVerificationResultIndex> resultIndexList) {
        ValidatorResultVo vo = new ValidatorResultVo();
        //构造计算配置
        Map<String, String> map = new HashMap<>();
        map.put("startTime", startTime);
        map.put("endTime", endTime);
        if (StrUtil.isBlank(dimension)) {
            vo.setErrorMsg("配置项" + costIndexConfigItemDto.getConfigId() + "未配置核算维度");
            return vo;
        }
        //核算粒度判断
        switch (new JSONObject(dimension).getStr("value")) {
            //科室单元
            case AccountObject.KPI_OBJECT_DEPT_UNIT:
                vo = getVerificationDeptUnit(costIndexConfigItemDto, objectIdType, objectId, map, isTrue, resultIndex, resultIndexList);
                break;
            //人员
            case AccountObject.KPI_OBJECT_PERSON:
                vo = getVerificationPerson(costIndexConfigItemDto, objectIdType, objectId, map, isTrue, resultIndex, resultIndexList);
                break;
            //科室
            case AccountObject.KPI_OBJECT_DEPT:
                vo = getVerificationDept(costIndexConfigItemDto, objectIdType, objectId, map, isTrue, resultIndex, resultIndexList);
                break;
            //全院
            case AccountObject.KPI_OBJECT_ALL:
                vo = getVerificationAll(costIndexConfigItemDto, objectIdType, objectId, map, isTrue, resultIndex, resultIndexList);
                break;
            default:
                break;
        }
        return vo;
    }

    /**
     * 此方法用于校验核算维度为全院的值
     *
     * @return
     */
    private ValidatorResultVo getVerificationAll(CostIndexConfigItemDto costIndexConfigItemDto, String objectIdType, String objectId, Map<String, String> map, String isTrue, CostVerificationResultIndex resultIndex, List<CostVerificationResultIndex> resultIndexList) {
        ValidatorResultVo vo = new ValidatorResultVo();
        //获取sql语句
        String deptConfig = cacheUtils.getCostAccountItem(costIndexConfigItemDto.getConfigId()).getConfig();
        if (StringUtil.isEmpty(deptConfig)) {
            vo.setErrorMsg("查询不到对应的sql语句");
            return vo;
        }
        String all = sqlUtil.executeSql(deptConfig, map);
        if (StrUtil.isBlank(all)) {
            all = "0.0";
        }
        //插入明细表
        if (isTrue.equals(YesNoEnum.YES.getCode())) {
            CostVerificationResultIndex costVerificationResultIndex = new CostVerificationResultIndex();
            BeanUtil.copyProperties(resultIndex, costVerificationResultIndex);
            costVerificationResultIndex.setType("All");
            costVerificationResultIndex.setObjectResult(new BigDecimal(all));
            resultIndexList.add(costVerificationResultIndex);
        }
        vo.setResult(all);
        return vo;
    }

    /**
     * 此方法用于校验核算维度为科室的值
     *
     * @return
     */
    private ValidatorResultVo getVerificationDept(CostIndexConfigItemDto costIndexConfigItemDto, String objectIdType, String objectId, Map<String, String> map, String isTrue, CostVerificationResultIndex resultIndex, List<CostVerificationResultIndex> resultIndexList) {
        ValidatorResultVo vo = new ValidatorResultVo();
        BigDecimal result = new BigDecimal(0.0);
        Map<Long, String> deptMap = new HashMap<>();
        //获取sql语句
        String deptConfig = cacheUtils.getCostAccountItem(costIndexConfigItemDto.getConfigId()).getConfig();
        if (StringUtil.isEmpty(deptConfig)) {
            vo.setErrorMsg("查询不到对应的sql语句");
            return vo;
        }
        //判断核算单元粒度
        OUT:
        switch (costIndexConfigItemDto.getAccountObject()) {
            //科室单元
            case AccountObject.KPI_OBJECT_DEPT_UNIT:
                //id类型为科室单元
                if (ItemDimensionEnum.DEPT_UNIT.getCode().equals(objectIdType)) {
                    List<Long> unitIds = new ArrayList<>();
                    //如果是医护对应
                    if (CostAccountProportionType.DOCNURSEGROUP.getGroupArrange().equals(costIndexConfigItemDto.getAccountRange())) {
                        //根据核算对象id获取核算对象
                        CostAccountUnit costAccountUnit = cacheUtils.getCostAccountUnit(Long.valueOf(objectId));
                        //如果是医生组,查询对应的护理组
                        final String unitGroup = UnitMapEnum.getUnitGroup(CostAccountProportionType.DOCGROUP.getGroupArrange());
                        final String desc = UnitMapEnum.getDesc(CostAccountProportionType.DOCGROUP.getGroupArrange());
                        UnitInfo unitInfo = new UnitInfo();
                        unitInfo.setLabel(desc);
                        unitInfo.setValue(unitGroup);
                        final String groupCode = new Gson().toJson(unitInfo).toString();
                        if (costAccountUnit.getAccountGroupCode().equals(groupCode)) {
                            //查询该医生组对应的所有护理组
                            List<CostDocNRelation> costDocNRelationList = new CostDocNRelation().selectList(new LambdaQueryWrapper<CostDocNRelation>().eq(CostDocNRelation::getDocAccountGroupId, objectId));
                            unitIds = costDocNRelationList.stream().map(CostDocNRelation::getNurseAccountGroupId).collect(Collectors.toList());
                        } else {
                            //查询该护理组组对应的所有医生组
                            List<CostDocNRelation> costDocNRelationList = new CostDocNRelation().selectList(new LambdaQueryWrapper<CostDocNRelation>().eq(CostDocNRelation::getNurseAccountGroupId, objectId));
                            unitIds = costDocNRelationList.stream().map(CostDocNRelation::getDocAccountGroupId).collect(Collectors.toList());
                        }
                    }
                    //自定义科室单元
                    else if (CostAccountProportionType.CUSTOMGROUP.getGroupArrange().equals(costIndexConfigItemDto.getAccountRange())) {
                        unitIds = ExpressionCheckHelper.getIds(costIndexConfigItemDto.getAccounts())
                                .stream()
                                .map(Long::parseLong)
                                .collect(Collectors.toList());
                        ;
                    } else {
                        unitIds.add(Long.valueOf(objectId));
                    }
                    //遍历科室单元下的科室
                    List<CostUnitRelateInfo> deptList = new CostUnitRelateInfo().selectList(Wrappers.<CostUnitRelateInfo>lambdaQuery()
                            .in(CostUnitRelateInfo::getAccountUnitId, unitIds)
                            .eq(CostUnitRelateInfo::getType, "dept"));
                    //获取科室的id并去重
                    List<Long> deptIds = deptList.stream().map(CostUnitRelateInfo::getRelateId).map(Long::parseLong).collect(Collectors.toList());
                    if (CollectionUtil.isEmpty(deptIds)) {
                        vo.setErrorMsg("科室单元" + objectId + "查不到对应的科室");
                        break OUT;
                    }
                    //获取科室code
                    deptMap = sqlUtil.getDeptCodesByDeptIds(deptIds);
                    for (Map.Entry<Long, String> entry : deptMap.entrySet()) {
                        String code = entry.getValue();
                        map.put("dept_code", code);
                        //执行查询到的sql
                        String dept = sqlUtil.executeSql(deptConfig, map);
                        map.remove("dept_code", code);
                        if (StrUtil.isBlank(dept)) {
                            dept = "0.0";
                        }
                        //插入明细表
                        if (isTrue.equals(YesNoEnum.YES.getCode())) {
                            CostVerificationResultIndex costVerificationResultIndex = new CostVerificationResultIndex();
                            BeanUtil.copyProperties(resultIndex, costVerificationResultIndex);
                            costVerificationResultIndex.setType("Dept");
                            costVerificationResultIndex.setObjectId(Long.valueOf(code));
                            costVerificationResultIndex.setObjectResult(new BigDecimal(dept));
                            resultIndexList.add(costVerificationResultIndex);
                        }
                        result = result.add(new BigDecimal(dept));
                    }
                }
                //id类型为科室
                else if (ItemDimensionEnum.DEPT.getCode().equals(objectIdType)) {
                    map.put("dept_code", objectId);
                    //执行查询到的sql
                    String dept = sqlUtil.executeSql(deptConfig, map);
                    map.remove("dept_code", objectId);
                    if (StrUtil.isBlank(dept)) {
                        dept = "0.0";
                    }
                    //插入明细表
                    if (isTrue.equals(YesNoEnum.YES.getCode())) {
                        CostVerificationResultIndex costVerificationResultIndex = new CostVerificationResultIndex();
                        BeanUtil.copyProperties(resultIndex, costVerificationResultIndex);
                        costVerificationResultIndex.setType("Dept");
                        costVerificationResultIndex.setObjectId(Long.valueOf(objectId));
                        costVerificationResultIndex.setObjectResult(new BigDecimal(dept));
                        resultIndexList.add(costVerificationResultIndex);
                    }
                    result = result.add(new BigDecimal(dept));
                } else {
                    vo.setErrorMsg("核算维度与核算对象不一致");
                    return vo;
                }
                break;
            //人员
            case AccountObject.KPI_OBJECT_PERSON:
                vo.setErrorMsg("核算维度与核算对象不一致");
                break;
            //科室
            case AccountObject.KPI_OBJECT_DEPT:
                final List<String> deptCodes = ExpressionCheckHelper.getIds(costIndexConfigItemDto.getAccounts());
                if (deptCodes.size() < 1) {
                    vo.setErrorMsg("请输入科室");
                }
                for (String deptCode : deptCodes) {
                    map.put("dept_code", deptCode);
                    //执行查询到的sql
                    String dept = sqlUtil.executeSql(deptConfig, map);
                    map.remove("dept_code", deptCode);
                    if (StrUtil.isBlank(dept)) {
                        dept = "0.0";
                    }
                    //插入明细表
                    if (isTrue.equals(YesNoEnum.YES.getCode())) {
                        CostVerificationResultIndex costVerificationResultIndex = new CostVerificationResultIndex();
                        BeanUtil.copyProperties(resultIndex, costVerificationResultIndex);
                        costVerificationResultIndex.setType("Dept");
                        costVerificationResultIndex.setObjectId(Long.valueOf(deptCode));
                        costVerificationResultIndex.setObjectResult(new BigDecimal(dept));
                        resultIndexList.add(costVerificationResultIndex);
                    }
                    result = result.add(new BigDecimal(dept));
                }
                break;
            //全院
            case AccountObject.KPI_OBJECT_ALL:
                String dept = sqlUtil.executeSql(deptConfig, map);
                if (StrUtil.isBlank(dept)) {
                    dept = "0.0";
                }
                //插入明细表
                if (isTrue.equals(YesNoEnum.YES.getCode())) {
                    CostVerificationResultIndex costVerificationResultIndex = new CostVerificationResultIndex();
                    BeanUtil.copyProperties(resultIndex, costVerificationResultIndex);
                    costVerificationResultIndex.setType("Dept");
                    costVerificationResultIndex.setObjectResult(new BigDecimal(dept));
                    resultIndexList.add(costVerificationResultIndex);
                }
                result = result.add(new BigDecimal(dept));
                break;
            default:
                break;
        }
        vo.setResult(result + "");
        return vo;
    }

    /**
     * 此方法用于校验核算维度为人员的值
     *
     * @return
     */
    private ValidatorResultVo getVerificationPerson(CostIndexConfigItemDto costIndexConfigItemDto, String objectIdType, String objectId, Map<String, String> map, String isTrue, CostVerificationResultIndex resultIndex, List<CostVerificationResultIndex> resultIndexList) {
        ValidatorResultVo vo = new ValidatorResultVo();
        BigDecimal result = new BigDecimal(0.0);
        Map<Long, String> userMap = new HashMap<>();
        //获取sql语句
        String config = cacheUtils.getCostAccountItem(costIndexConfigItemDto.getConfigId()).getConfig();
        if (StringUtil.isEmpty(config)) {
            vo.setErrorMsg("查询不到对应的sql语句");
            return vo;
        }
        //核算对象颗粒度判断
        switch (costIndexConfigItemDto.getAccountObject()) {
            //科室单元
            case AccountObject.KPI_OBJECT_DEPT_UNIT:
                //科室单元id
                if (ItemDimensionEnum.DEPT_UNIT.getCode().equals(objectIdType)) {
                    //获取科室单元下所有科室
                    List<CostUnitRelateInfo> deptList = getDeptList(objectId, costIndexConfigItemDto);
                    //判断科室单元下是否有科室,没有则直接查询科室单元下的人员
                    if (deptList.size() > 0) {
                        //获取科室下所有的人员
                        List<Long> deptIds = deptList.stream().map(CostUnitRelateInfo::getRelateId).map(Long::parseLong).collect(Collectors.toList());
                        userMap = sqlUtil.getUserIdsAndNamesByDeptIds(deptIds);
                    }
                    //获取该科室单元下所有的人员
                    List<CostUnitRelateInfo> userList = new CostUnitRelateInfo().selectList(Wrappers.<CostUnitRelateInfo>lambdaQuery().
                            eq(CostUnitRelateInfo::getAccountUnitId, objectId).
                            eq(CostUnitRelateInfo::getType, "user"));
                    //获取该科室单元下所有不参与核算的人员
                    List<CostUnitExcludedInfo> excludedUserList = new CostUnitExcludedInfo().selectList(Wrappers.<CostUnitExcludedInfo>lambdaQuery().
                            eq(CostUnitExcludedInfo::getAccountUnitId, objectId).
                            eq(CostUnitExcludedInfo::getType, "user"));
                    Map<Long, String> idNames = userList.stream().collect(Collectors.toMap(info -> Long.parseLong(info.getRelateId()), CostUnitRelateInfo::getName));
                    Map<Long, String> excludedIdNames = excludedUserList.stream().collect(Collectors.toMap(info -> Long.parseLong(info.getRelateId()), CostUnitExcludedInfo::getName));
                    //将所有关联科室的人添加到部门获取的map
                    userMap.putAll(idNames);
                    //将所有不参与核算人员删除
                    excludedIdNames.forEach(userMap::remove);
                    if (userMap.isEmpty()) {
                        vo.setErrorMsg(objectId + "查询不到人员");
                        break;
                    }
                }
                //科室id
                else if (ItemDimensionEnum.DEPT.getCode().equals(objectIdType)) {
                    List<Long> deptIds = new ArrayList<>();
                    deptIds.add(Long.valueOf(objectId));
                    userMap = sqlUtil.getUserIdsAndNamesByDeptIds(deptIds);
                }
                //人员id
                else if (ItemDimensionEnum.USER.getCode().equals(objectIdType)) {
                    vo.setErrorMsg("核算维度与核算对象不一致");
                    break;
                }
                //遍历求值
                for (Map.Entry<Long, String> entry : userMap.entrySet()) {
                    Long userId = entry.getKey();
                    map.put("user_id", userId.toString());
                    //执行查询到的sql
                    String person = sqlUtil.executeSql(config, map);
                    map.remove("user_id", userId);
                    if (StrUtil.isBlank(person)) {
                        person = "0.0";
                    }
                    //插入明细表
                    if (isTrue.equals(YesNoEnum.YES.getCode())) {
                        CostVerificationResultIndex costVerificationResultIndex = new CostVerificationResultIndex();
                        BeanUtil.copyProperties(resultIndex, costVerificationResultIndex);
                        costVerificationResultIndex.setType("User");
                        costVerificationResultIndex.setObjectId(userId);
                        costVerificationResultIndex.setObjectResult(new BigDecimal(person));
                        resultIndexList.add(costVerificationResultIndex);
                    }
                    result = result.add(new BigDecimal(person));
                }
                break;
            //人员
            case AccountObject.KPI_OBJECT_PERSON:
                final List<String> userIds = ExpressionCheckHelper.getIds(costIndexConfigItemDto.getAccounts());
                if (userIds.size() < 1) {
                    vo.setErrorMsg("未输入核算人员");
                    break;
                }
                for (String userId : userIds) {
                    map.put("user_id", userId);
                    //执行查询到的sql
                    String person = sqlUtil.executeSql(config, map);
                    map.remove("user_id", userId);
                    if (StrUtil.isBlank(person)) {
                        person = "0.0";
                    }
                    //插入明细表
                    if (isTrue.equals(YesNoEnum.YES.getCode())) {
                        CostVerificationResultIndex costVerificationResultIndex = new CostVerificationResultIndex();
                        BeanUtil.copyProperties(resultIndex, costVerificationResultIndex);
                        costVerificationResultIndex.setType("User");
                        costVerificationResultIndex.setObjectId(Long.valueOf(userId));
                        costVerificationResultIndex.setObjectResult(new BigDecimal(person));
                        resultIndexList.add(costVerificationResultIndex);
                    }
                    result = result.add(new BigDecimal(person));
                }
                break;
            //科室
            case AccountObject.KPI_OBJECT_DEPT:
                //获取科室科室id
                final List<String> deptCode = ExpressionCheckHelper.getIds(costIndexConfigItemDto.getAccounts());
                List<Long> deptCodes = new ArrayList<>();
                for (String s : deptCode) {
                    deptCodes.add(Long.valueOf(s));
                }
                //获取科室下面的人员
                userMap = sqlUtil.getUserIdsAndNamesByDeptIds(deptCodes);
                if (userMap.isEmpty()) {
                    vo.setErrorMsg("科室" + costIndexConfigItemDto.getConfigId() + "查不到人员");
                    break;
                }
                //遍历求值
                for (Map.Entry<Long, String> entry : userMap.entrySet()) {
                    Long userId = entry.getKey();
                    map.put("user_id", userId.toString());
                    //执行查询到的sql
                    String person = sqlUtil.executeSql(config, map);
                    map.remove("user_id", userId);
                    if (StrUtil.isBlank(person)) {
                        person = "0.0";
                    }
                    //插入明细表
                    if (isTrue.equals(YesNoEnum.YES.getCode())) {
                        CostVerificationResultIndex costVerificationResultIndex = new CostVerificationResultIndex();
                        BeanUtil.copyProperties(resultIndex, costVerificationResultIndex);
                        costVerificationResultIndex.setType("User");
                        costVerificationResultIndex.setObjectId(userId);
                        costVerificationResultIndex.setObjectResult(new BigDecimal(person));
                        resultIndexList.add(costVerificationResultIndex);
                    }
                    result = result.add(new BigDecimal(person));
                }
                break;
            case AccountObject.KPI_OBJECT_ALL://全院
                String person = sqlUtil.executeSql(config, map);
                if (StrUtil.isBlank(person)) {
                    person = "0.0";
                }
                //插入明细表
                if (isTrue.equals(YesNoEnum.YES.getCode())) {
                    CostVerificationResultIndex costVerificationResultIndex = new CostVerificationResultIndex();
                    BeanUtil.copyProperties(resultIndex, costVerificationResultIndex);
                    costVerificationResultIndex.setType("User");
                    costVerificationResultIndex.setObjectResult(new BigDecimal(person));
                    resultIndexList.add(costVerificationResultIndex);
                }
                result = result.add(new BigDecimal(person));
                break;
            default:
                break;
        }
        vo.setResult(result + "");
        return vo;
    }

    /**
     * 此方法用于校验核算维度为科室单元的值
     *
     * @return
     */
    private ValidatorResultVo getVerificationDeptUnit(CostIndexConfigItemDto costIndexConfigItemDto, String objectIdType, String objectId, Map<String, String> map, String isTrue, CostVerificationResultIndex resultIndex, List<CostVerificationResultIndex> resultIndexList) {
        ValidatorResultVo vo = new ValidatorResultVo();
        BigDecimal result = new BigDecimal(0.0);
        //获取sql语句
        String unitConfig = cacheUtils.getCostAccountItem(costIndexConfigItemDto.getConfigId()).getConfig();
        if (StringUtil.isEmpty(unitConfig)) {
            vo.setErrorMsg("配置项" + costIndexConfigItemDto.getConfigId() + "查询不到对应的sql语句");
            return vo;
        }
        //核算对象粒度判断
        switch (costIndexConfigItemDto.getAccountObject()) {
            //科室单元
            case AccountObject.KPI_OBJECT_DEPT_UNIT:
                //核算对象类型判断(科室单元)
//                if (ItemDimensionEnum.DEPT_UNIT.getCode().equals(objectIdType)) {
                //核算科室单元类型判断
                List<Long> unitIds = getUnitIdByAccountRange(costIndexConfigItemDto, objectId);
                if (CollUtil.isEmpty(unitIds)) {
                    vo.setErrorMsg("查询不到对应的科室单元");
                    return vo;
                }
                //遍历匹配
                for (Long id : unitIds) {
                    map.put("account_unit_id", id + "");
                    //执行查询到的sql
                    String unit = sqlUtil.executeSql(unitConfig, map);
                    map.remove("account_unit_id", id + "");
                    //如果执行结果没有,跳过
                    if (StrUtil.isBlank(unit)) {
                        unit = "0.0";
                    }
                    //插入明细表
                    if (isTrue.equals(YesNoEnum.YES.getCode())) {
                        CostVerificationResultIndex costVerificationResultIndex = new CostVerificationResultIndex();
                        BeanUtil.copyProperties(resultIndex, costVerificationResultIndex);
                        costVerificationResultIndex.setType("Unit");
                        costVerificationResultIndex.setObjectId(id);
                        costVerificationResultIndex.setObjectResult(new BigDecimal(unit));
                        resultIndexList.add(costVerificationResultIndex);
                    }
                    result = result.add(new BigDecimal(unit));
                }
//                } else {
//                    vo.setErrorMsg("核算维度与核算对象不一致");
//                    break;
//                }
                break;
            default:
                vo.setErrorMsg("核算维度与核算对象不一致");
                return vo;
        }
        vo.setResult(result + "");
        return vo;
    }


    /**
     * 指标计算
     *
     * @param id        指标id
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @param objectId  核算对象id
     * @return
     */
    @SneakyThrows
    @Override
    public ValidatorResultVo verificationIndex(Long id, String startTime, String endTime, String objectId) {
        //根据id查找指标
        CostAccountIndex costAccountIndex = cacheUtils.getCostAccountIndex(id);
        ValidatorResultVo vo = new ValidatorResultVo();
        Map<String, Double> map = new HashMap<>();
        //拿到指标公式解析,获取到key集合
        Set<String> keys = ExpressionCheckHelper.expressionQuantity(costAccountIndex.getIndexFormula());
        //遍历key,拿到配置项
        for (String key : keys) {
            //根据key去查询核算配置项
            CostIndexConfigItem costIndexConfigItem = cacheUtils.getCostIndexConfigItem(key);
            //计算配置项的值
            CostIndexConfigItemDto costIndexConfigItemDto = BeanUtil.copyProperties(costIndexConfigItem, CostIndexConfigItemDto.class);
            String dimension = cacheUtils.getCostAccountItem(costIndexConfigItem.getConfigId()).getDimension();
            final ValidatorResultVo item = getVerificationItem(costIndexConfigItemDto, startTime, endTime, objectId, costAccountIndex.getStatisticalCycle().toString(), dimension, ItemDimensionEnum.DEPT_UNIT.getCode(), "N", null, null);
            if (StrUtil.isBlank(item.getResult())) {
                try {
                    //计算不到数据,给出原因
                    vo.setErrorMsg("指标中的" + costIndexConfigItem.getConfigName() + costIndexConfigItem.getConfigDesc() + "==>查不到数据");
                    return vo;
                } catch (Exception e) {
                }
            }
            //构造计算map

            map.put(key, Double.valueOf(item.getResult()));
            //如果查询不到,则该key对应是为配置项指标
            if (costIndexConfigItem == null) {
                //根据key查询配置指标
                CostIndexConfigIndex costIndexConfigIndex = cacheUtils.getCostIndexConfigIndex(key);
                //根据指标配置项查询指标
                CostAccountIndex accountIndex = cacheUtils.getCostAccountIndex(costIndexConfigIndex.getConfigIndexId());
                //递归
                verificationIndex(accountIndex.getId(), startTime, endTime, objectId);
            }
        }
        //计算值
        String expression = costAccountIndex.getIndexFormula().replace("%", "/100");
        vo.setResult(ExpressionCheckHelper.calculate(map, expression));
        return vo;
    }

    /**
     * 核算配置项计算
     *
     * @param id        核算配置项id
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @param objectId  核算对象id
     * @return
     */
    @Override
    public ValidatorResultVo verificationItem(Long id, String startTime, String endTime, String objectId, String objectIdType) {
        Long sTime = System.currentTimeMillis();
        ValidatorResultVo vo = new ValidatorResultVo();
        //根据配置项id获取核算项
        CostIndexConfigItem costIndexConfigItem = costIndexConfigItemService.getById(id);
        //先查定时任务计算表中的数据
        CostVerificationResultItem costVerificationResultItem = new CostVerificationResultItem().selectOne(new LambdaQueryWrapper<CostVerificationResultItem>()
                .eq(CostVerificationResultItem::getUnitId, objectId)
                .eq(CostVerificationResultItem::getAccountDate, startTime)
                .eq(CostVerificationResultItem::getItemId, costIndexConfigItem.getConfigId()));
        if (costVerificationResultItem != null) {
            vo.setResult(costVerificationResultItem.getItemCount() + "");
            return vo;
        }

        CostIndexConfigItemDto costIndexConfigItemDto = BeanUtil.copyProperties(costIndexConfigItem, CostIndexConfigItemDto.class);
        String dimension = cacheUtils.getCostAccountItem(costIndexConfigItem.getConfigId()).getDimension();
        final ValidatorResultVo item = getItem(costIndexConfigItemDto, startTime, endTime, objectId, costIndexConfigItem.getAccountPeriod(), dimension, objectIdType);
        if (StrUtil.isBlank(item.getResult())) {
            try {
                //计算不到数据,给出原因
                vo.setErrorMsg(costIndexConfigItem.getConfigName() + costIndexConfigItem.getConfigDesc() + "==>查不到数据");
                return vo;
            } catch (Exception e) {
                log.error(costIndexConfigItem.getConfigName() + costIndexConfigItem.getConfigDesc() + "==>查不到数据");
            }
        }
        Long eTime = System.currentTimeMillis();
        vo.setResult(item.getResult());
        vo.setExecuteTime(Integer.valueOf(String.valueOf(eTime - sTime)));
        return vo;
    }

    /**
     * 分摊规则配置项计算
     *
     * @param costIndexConfigItemDto 配置信息
     * @param startTime              开始时间
     * @param endTime                结束时间
     * @param objectId               核算对象id
     * @param objectIdType
     * @return
     */
    @Override
    public ValidatorResultVo verificationRuleItem(CostIndexConfigItemDto costIndexConfigItemDto, String dimension, String startTime, String endTime, String objectId, String objectIdType) {
        Long sTime = System.currentTimeMillis();
        ValidatorResultVo vo = new ValidatorResultVo();
        //根据配置项id获取核算项
        final ValidatorResultVo item = getItem(costIndexConfigItemDto, startTime, endTime, objectId, costIndexConfigItemDto.getAccountPeriod(), dimension, objectIdType);
        if (StrUtil.isBlank(item.getResult())) {
            //计算不到数据,给出原因
            Long eTime = System.currentTimeMillis();
            item.setExecuteTime(Integer.valueOf(String.valueOf(eTime - sTime)));
            return item;
        }
        Long eTime = System.currentTimeMillis();
        vo.setResult(item.getResult());
        vo.setExecuteTime(Integer.valueOf(String.valueOf(eTime - sTime)));
        return vo;
    }

    /**
     * 此方法用于解析计算核算指标的值
     *
     * @param costAccountIndex vo对象
     * @param startTime        开始时间
     * @param endTime          结束时间
     * @param objectId         核算对象id
     * @param type             核算类型
     * @return
     * @throws Exception
     */
    public ValidatorResultVo getIndex(CostAccountIndex costAccountIndex, String startTime, String endTime, String objectId, String type) throws Exception {
        ValidatorResultVo vo = new ValidatorResultVo();
        //先查询定时任务计算指标表中的是否有数据
        CostVerificationResultIndexNew costVerificationResultIndexNew = new CostVerificationResultIndexNew().selectOne(new LambdaQueryWrapper<CostVerificationResultIndexNew>()
                .eq(CostVerificationResultIndexNew::getIndexId, costAccountIndex.getId())
                .eq(CostVerificationResultIndexNew::getUnitId, objectId)
                .eq(CostVerificationResultIndexNew::getAccountDate, startTime));
        if (costVerificationResultIndexNew != null) {
            vo.setResult(costVerificationResultIndexNew.getIndexCount() + "");
            return vo;
        }
        Map<String, Double> map = new HashMap<>();
        //拿到指标公式解析,获取到key集合
        Set<String> keys = ExpressionCheckHelper.expressionQuantity(costAccountIndex.getIndexFormula());
        //遍历key,拿到配置项
        // 使用线程池提交并行任务
        List<Future<?>> futures = new ArrayList<>();
        for (String key : keys) {
            futures.add(executorService.submit(() -> {
                try {
                    calculateKey(key, map, vo, startTime, endTime, objectId, type);
                    log.info("我是线程{}，我正在执行计算核算指标的值", Thread.currentThread().getName());
                } catch (Exception e) {
                    // 处理异常
                    log.error("计算核算指标的值异常,异常原因为{}", e);
                }
            }));
        }

        // 等待所有任务完成
        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (Exception e) {
                // 处理异常
                log.error("计算核算指标的值异常,异常原因为{}", e);
            }
        }

        //计算值
        String newExpression = costAccountIndex.getIndexFormula().replace("%", "/100");
        vo.setResult(ExpressionCheckHelper.calculate(map, newExpression));

        return vo;
    }

    private void calculateKey(String key, Map<String, Double> map, ValidatorResultVo vo, String startTime, String endTime, String objectId, String type) throws Exception {
        //根据key去查询核算配置项
        CostIndexConfigItem costIndexConfigItem = cacheUtils.getCostIndexConfigItem(key);
        //查询到配置项
        if (costIndexConfigItem != null) {
            ValidatorResultVo item = new ValidatorResultVo();
            String dimension = cacheUtils.getCostAccountItem(costIndexConfigItem.getConfigId()).getDimension();
            //计算配置项的值
            CostIndexConfigItemDto costIndexConfigItemDto = BeanUtil.copyProperties(costIndexConfigItem, CostIndexConfigItemDto.class);
            //先查定时任务计算表中的数据
            CostVerificationResultItem costVerificationResultItem = new CostVerificationResultItem().selectOne(new LambdaQueryWrapper<CostVerificationResultItem>()
                    .eq(CostVerificationResultItem::getUnitId, objectId)
                    .eq(CostVerificationResultItem::getAccountDate, startTime)
                    .eq(CostVerificationResultItem::getItemId, costIndexConfigItem.getConfigId()));
            if (costVerificationResultItem != null) {
                item.setResult(costVerificationResultItem.getItemCount() + "");
            } else {
                item = getItem(costIndexConfigItemDto, startTime, endTime, objectId, type, dimension, costIndexConfigItem.getAccountObject());
            }
            if (StrUtil.isBlank(item.getResult())) {
                item.setResult("0.0");
                log.warn("配置项" + costIndexConfigItemDto.getConfigId() + "查不到值");
            }
            //构造计算map
            map.put(key, Double.valueOf(item.getResult()));
        } else if (costIndexConfigItem == null) {
            //如果查询不到,则该key对应是为配置项指标
            //根据key查询配置指标
            CostIndexConfigIndex costIndexConfigIndex = cacheUtils.getCostIndexConfigIndex(key);
            //如果指标也查不到,说明不存在
            if (costIndexConfigIndex == null) {
                //计算不到数据,给出原因
                vo.setErrorMsg("指标中的" + key + "配置项==>查不到数据");
            }
            //根据指标配置项查询指标
            CostAccountIndex newCostAccountIndex = cacheUtils.getCostAccountIndex(costIndexConfigIndex.getConfigIndexId());
            //递归
            final ValidatorResultVo index = getIndex(newCostAccountIndex, startTime, endTime, objectId, type);
            //构造计算map
            map.put(key, Double.valueOf(index.getResult()));
        }
    }

    /**
     * 此方法用于解析计算核算项的值
     *
     * @param costIndexConfigItemDto
     * @param startTime
     * @param endTime
     * @param objectId
     * @param type
     * @return
     */
    public ValidatorResultVo getItem(CostIndexConfigItemDto costIndexConfigItemDto, String startTime, String endTime, String objectId, String type, String dimension, String objectIdType) {
        ValidatorResultVo vo = new ValidatorResultVo();
        //处理本周期
        if (costIndexConfigItemDto.getAccountPeriod().equals("current")) {
            vo = getResult(startTime, endTime, objectId, costIndexConfigItemDto, dimension, objectIdType);
        }
        //处理上一周期
        if (costIndexConfigItemDto.getAccountPeriod().equals("before")) {
            //环比上一周期,往前推一个月
            if ("RING_RATIO".equals(type)) {
                String newStartTime = BeforeDateUtils.getBeforeMonthDate(startTime, -01);
                String newEndTime = BeforeDateUtils.getBeforeMonthDate(endTime, -01);
                vo = getResult(newStartTime, newEndTime, objectId, costIndexConfigItemDto, dimension, objectIdType);
            }
            //同比上一周期,往前推一年
            if ("YEAR_ON_YEAR".equals(type)) {
                String newStartTime = BeforeDateUtils.getBeforeYearDate(startTime, -01);
                String newEndTime = BeforeDateUtils.getBeforeYearDate(endTime, -01);
                vo = getResult(newStartTime, newEndTime, objectId, costIndexConfigItemDto, dimension, objectIdType);
            }
        }
        return vo;
    }

    /**
     * 此方法用于执行核算配置项
     *
     * @param startTime
     * @param endTime
     * @param objectId
     * @param costIndexConfigItemDto
     * @return
     */
    public ValidatorResultVo getResult(String startTime, String endTime, String objectId, CostIndexConfigItemDto costIndexConfigItemDto, String dimension, String objectIdType) {
        ValidatorResultVo vo = new ValidatorResultVo();
        //构造计算配置
        Map<String, String> map = new HashMap<>();
        map.put("startTime", startTime);
        map.put("endTime", endTime);
        if (StrUtil.isBlank(dimension)) {
            vo.setErrorMsg("配置项" + costIndexConfigItemDto.getConfigId() + "未配置核算维度");
            return vo;
        }
        //核算粒度判断
        switch (new JSONObject(dimension).getStr("value")) {
            //科室单元
            case AccountObject.KPI_OBJECT_DEPT_UNIT:
                vo = getDeptUnit(costIndexConfigItemDto, objectIdType, objectId, map);
                break;
            //人员
            case AccountObject.KPI_OBJECT_PERSON:
                vo = getPerson(costIndexConfigItemDto, objectIdType, objectId, map);
                break;
            //科室
            case AccountObject.KPI_OBJECT_DEPT:
                vo = getDept(costIndexConfigItemDto, objectIdType, objectId, map);
                break;
            //todo 全院 等待数据
            case AccountObject.KPI_OBJECT_ALL:
                vo = getAll(costIndexConfigItemDto, objectIdType, objectId, map);
                break;
            default:
                break;
        }
        return vo;
    }

    /**
     * 此方法用于计算核算维度为全院的值
     *
     * @return
     */
    private ValidatorResultVo getAll(CostIndexConfigItemDto costIndexConfigItemDto, String objectIdType, String objectId, Map<String, String> map) {
        ValidatorResultVo vo = new ValidatorResultVo();
        //获取sql语句
        String deptConfig = cacheUtils.getCostAccountItem(costIndexConfigItemDto.getConfigId()).getConfig();
        if (StringUtil.isEmpty(deptConfig)) {
            vo.setErrorMsg("查询不到对应的sql语句");
            return vo;
        }
        String all = sqlUtil.executeSql(deptConfig, map);
        vo.setResult(all);
        return vo;
    }

    /**
     * 此方法用于计算核算维度为科室的值
     *
     * @return
     */
    private ValidatorResultVo getDept(CostIndexConfigItemDto costIndexConfigItemDto, String objectIdType, String objectId, Map<String, String> map) {
        ValidatorResultVo vo = new ValidatorResultVo();
        BigDecimal result = new BigDecimal(0.0);
        Map<Long, String> deptMap = new HashMap<>();
        //获取sql语句
        String deptConfig = cacheUtils.getCostAccountItem(costIndexConfigItemDto.getConfigId()).getConfig();
        if (StringUtil.isEmpty(deptConfig)) {
            vo.setErrorMsg("查询不到对应的sql语句");
            return vo;
        }
        //判断核算单元粒度
        if (objectIdType.equals(AccountObject.KPI_OBJECT_ALL)) {
//            if (deptConfig.contains("cost_report_detail") && deptConfig.contains("#(dept_code)")) {
//                Pattern pattern = Pattern.compile("AND", Pattern.CASE_INSENSITIVE);
//                Matcher matcher = pattern.matcher(deptConfig);
//                int lastIndex = -1;
//
//                while (matcher.find()) {
//                    lastIndex = matcher.start();
//                }
//
//                if (lastIndex >= 0) {
//                    deptConfig = deptConfig.substring(0, lastIndex);
//                }
//            }
            String dept = sqlUtil.executeSql(deptConfig, map);
            //todo 插入校验明细表
            if (StrUtil.isNotBlank(dept)) {
                result = result.add(new BigDecimal(dept));
            }
        } else {
            OUT:
            switch (costIndexConfigItemDto.getAccountObject()) {
                //科室单元
                case AccountObject.KPI_OBJECT_DEPT_UNIT:
                    //id类型为科室单元
                    if (ItemDimensionEnum.DEPT_UNIT.getCode().equals(objectIdType)) {
                        List<Long> unitIds = new ArrayList<>();
                        //如果是医护对应
                        if (CostAccountProportionType.DOCNURSEGROUP.getGroupArrange().equals(costIndexConfigItemDto.getAccountRange())) {
                            //根据核算对象id获取核算对象
                            CostAccountUnit costAccountUnit = cacheUtils.getCostAccountUnit(Long.valueOf(objectId));
                            //如果是医生组,查询对应的护理组
                            final String unitGroup = UnitMapEnum.getUnitGroup(CostAccountProportionType.DOCGROUP.getGroupArrange());
                            final String desc = UnitMapEnum.getDesc(CostAccountProportionType.DOCGROUP.getGroupArrange());
                            UnitInfo unitInfo = new UnitInfo();
                            unitInfo.setLabel(desc);
                            unitInfo.setValue(unitGroup);
                            final String groupCode = new Gson().toJson(unitInfo).toString();
                            if (costAccountUnit.getAccountGroupCode().equals(groupCode)) {
                                //查询该医生组对应的所有护理组
                                List<CostDocNRelation> costDocNRelationList = new CostDocNRelation().selectList(new LambdaQueryWrapper<CostDocNRelation>().eq(CostDocNRelation::getDocAccountGroupId, objectId));
                                unitIds = costDocNRelationList.stream().map(CostDocNRelation::getNurseAccountGroupId).collect(Collectors.toList());
                            } else {
                                //查询该护理组组对应的所有医生组
                                List<CostDocNRelation> costDocNRelationList = new CostDocNRelation().selectList(new LambdaQueryWrapper<CostDocNRelation>().eq(CostDocNRelation::getNurseAccountGroupId, objectId));
                                unitIds = costDocNRelationList.stream().map(CostDocNRelation::getDocAccountGroupId).collect(Collectors.toList());
                            }
                        } else {
                            unitIds.add(Long.valueOf(objectId));
                        }
                        //遍历科室单元下的科室
                        List<CostUnitRelateInfo> deptList = new CostUnitRelateInfo().selectList(Wrappers.<CostUnitRelateInfo>lambdaQuery()
                                .in(CostUnitRelateInfo::getAccountUnitId, unitIds)
                                .eq(CostUnitRelateInfo::getType, "dept"));
                        //获取科室的id并去重
                        List<Long> deptIds = deptList.stream().map(CostUnitRelateInfo::getRelateId).map(Long::parseLong).collect(Collectors.toList());
                        if (CollectionUtil.isEmpty(deptIds)) {
                            vo.setErrorMsg("科室单元" + objectId + "查不到对应的科室");
                            break OUT;
                        }
                        //获取科室code
                        deptMap = sqlUtil.getDeptCodesByDeptIds(deptIds);
                        for (Map.Entry<Long, String> entry : deptMap.entrySet()) {
                            String code = entry.getValue();
                            map.put("dept_code", code);
                            //执行查询到的sql
                            String dept = sqlUtil.executeSql(deptConfig, map);
                            map.remove("dept_code", code);
                            if (StrUtil.isNotBlank(dept)) {
                                result = result.add(new BigDecimal(dept));
                            } else {
                                result = result.add(new BigDecimal(0.0));
                            }
                        }
                    }
                    //id类型为科室
                    else if (ItemDimensionEnum.DEPT.getCode().equals(objectIdType)) {
                        map.put("dept_code", objectId);
                        //执行查询到的sql
                        String dept = sqlUtil.executeSql(deptConfig, map);
                        map.remove("dept_code", objectId);
                        if (StrUtil.isNotBlank(dept)) {
                            result = result.add(new BigDecimal(dept));
                        } else {
                            result = result.add(new BigDecimal(0.0));
                        }
                    } else {
                        vo.setErrorMsg("核算维度与核算对象不一致");
                        return vo;
                    }
                    break;
                //人员
                case AccountObject.KPI_OBJECT_PERSON:
                    vo.setErrorMsg("核算维度与核算对象不一致");
                    break;
                //科室
                case AccountObject.KPI_OBJECT_DEPT:
                    final List<String> deptCodes = ExpressionCheckHelper.getIds(costIndexConfigItemDto.getAccounts());
                    if (deptCodes.size() < 1) {
                        vo.setErrorMsg("请输入科室");
                    }
                    for (String deptCode : deptCodes) {
                        map.put("dept_code", deptCode);
                        //执行查询到的sql
                        String dept = sqlUtil.executeSql(deptConfig, map);
                        map.remove("dept_code", deptCode);
                        if (StrUtil.isNotBlank(dept)) {
                            result = result.add(new BigDecimal(dept));
                        } else {
                            result = result.add(new BigDecimal(0.0));
                        }
                    }
                    break;
                default:
                    break;
            }
        }

        vo.setResult(result + "");
        return vo;
    }

    /**
     * 此方法用于计算核算维度为人员的值
     *
     * @return
     */
    private ValidatorResultVo getPerson(CostIndexConfigItemDto costIndexConfigItemDto, String objectIdType, String objectId, Map<String, String> map) {
        ValidatorResultVo vo = new ValidatorResultVo();
        BigDecimal result = new BigDecimal(0.0);
        Map<Long, String> userMap = new HashMap<>();
        //获取sql语句
        String config = cacheUtils.getCostAccountItem(costIndexConfigItemDto.getConfigId()).getConfig();
        if (StringUtil.isEmpty(config)) {
            vo.setErrorMsg("查询不到对应的sql语句");
            return vo;
        }
        //核算对象颗粒度判断
        switch (costIndexConfigItemDto.getAccountObject()) {
            //科室单元
            case AccountObject.KPI_OBJECT_DEPT_UNIT:
                //科室单元id
                if (ItemDimensionEnum.DEPT_UNIT.getCode().equals(objectIdType)) {
                    //获取科室单元下所有科室
                    List<CostUnitRelateInfo> deptList = getDeptList(objectId, costIndexConfigItemDto);
                    //判断科室单元下是否有科室,没有则直接查询科室单元下的人员
                    if (deptList.size() > 0) {
                        //获取科室下所有的人员
                        List<Long> deptIds = deptList.stream().map(CostUnitRelateInfo::getRelateId).map(Long::parseLong).collect(Collectors.toList());
                        userMap = sqlUtil.getUserIdsAndNamesByDeptIds(deptIds);
                    }
                    //获取该科室单元下所有的人员
                    List<CostUnitRelateInfo> userList = new CostUnitRelateInfo().selectList(Wrappers.<CostUnitRelateInfo>lambdaQuery().
                            eq(CostUnitRelateInfo::getAccountUnitId, objectId).
                            eq(CostUnitRelateInfo::getType, "user"));
                    //获取该科室单元下所有不参与核算的人员
                    List<CostUnitExcludedInfo> excludedUserList = new CostUnitExcludedInfo().selectList(Wrappers.<CostUnitExcludedInfo>lambdaQuery().
                            eq(CostUnitExcludedInfo::getAccountUnitId, objectId).
                            eq(CostUnitExcludedInfo::getType, "user"));
                    Map<Long, String> idNames = userList.stream().collect(Collectors.toMap(info -> Long.parseLong(info.getRelateId()), CostUnitRelateInfo::getName));
                    Map<Long, String> excludedIdNames = excludedUserList.stream().collect(Collectors.toMap(info -> Long.parseLong(info.getRelateId()), CostUnitExcludedInfo::getName));
                    //将所有关联科室的人添加到部门获取的map
                    userMap.putAll(idNames);
                    //将所有不参与核算人员删除
                    excludedIdNames.forEach(userMap::remove);
                    if (userMap.isEmpty()) {
                        vo.setErrorMsg(objectId + "查询不到人员");
                        break;
                    }
                }
                //科室id
                else if (ItemDimensionEnum.DEPT.getCode().equals(objectIdType)) {
                    List<Long> deptIds = new ArrayList<>();
                    deptIds.add(Long.valueOf(objectId));
                    userMap = sqlUtil.getUserIdsAndNamesByDeptIds(deptIds);
                }
                //人员id
                else if (ItemDimensionEnum.USER.getCode().equals(objectIdType)) {
                    vo.setErrorMsg("核算维度与核算对象不一致");
                    break;
                }
                //遍历求值
                for (Map.Entry<Long, String> entry : userMap.entrySet()) {
                    Long userId = entry.getKey();
                    map.put("user_id", userId.toString());
                    //执行查询到的sql
                    String person = sqlUtil.executeSql(config, map);
                    map.remove("user_id", userId);
                    if (StrUtil.isNotBlank(person)) {
                        result = result.add(new BigDecimal(person));
                    }
                }
                break;
            //人员
            case AccountObject.KPI_OBJECT_PERSON:
                final List<String> userIds = ExpressionCheckHelper.getIds(costIndexConfigItemDto.getAccounts());
                if (userIds.size() < 1) {
                    vo.setErrorMsg("未输入核算人员");
                    break;
                }
                for (String userId : userIds) {
                    map.put("user_id", userId);
                    //执行查询到的sql
                    String person = sqlUtil.executeSql(config, map);
                    map.remove("user_id", userId);
                    if (StrUtil.isNotBlank(person)) {
                        result = result.add(new BigDecimal(person));
                    }
                }
                break;
            //科室
            case AccountObject.KPI_OBJECT_DEPT:
                //获取科室科室id
                final List<String> deptCode = ExpressionCheckHelper.getIds(costIndexConfigItemDto.getAccounts());
                List<Long> deptCodes = new ArrayList<>();
                for (String s : deptCode) {
                    deptCodes.add(Long.valueOf(s));
                }
                //获取科室下面的人员
                userMap = sqlUtil.getUserIdsAndNamesByDeptIds(deptCodes);
                if (userMap.isEmpty()) {
                    vo.setErrorMsg("科室" + costIndexConfigItemDto.getConfigId() + "查不到人员");
                    break;
                }
                //遍历求值
                for (Map.Entry<Long, String> entry : userMap.entrySet()) {
                    Long userId = entry.getKey();
                    map.put("user_id", userId.toString());
                    //执行查询到的sql
                    String person = sqlUtil.executeSql(config, map);
                    map.remove("user_id", userId);
                    if (StrUtil.isNotBlank(person)) {
                        result = result.add(new BigDecimal(person));
                    }
                }
                break;
            case AccountObject.KPI_OBJECT_ALL://全院
                String person = sqlUtil.executeSql(config, map);
                if (StrUtil.isNotBlank(person)) {
                    result = result.add(new BigDecimal(person));
                }
                break;
            default:
                break;
        }
        vo.setResult(result + "");
        return vo;
    }

    /**
     * 此方法用于计算核算维度为科室单元的值
     *
     * @return
     */
    private ValidatorResultVo getDeptUnit(CostIndexConfigItemDto costIndexConfigItemDto, String objectIdType, String objectId, Map<String, String> map) {
        ValidatorResultVo vo = new ValidatorResultVo();
        BigDecimal result = new BigDecimal(0.0);
        //获取sql语句
        String unitConfig = cacheUtils.getCostAccountItem(costIndexConfigItemDto.getConfigId()).getConfig();
        if (StringUtil.isEmpty(unitConfig)) {
            vo.setErrorMsg("配置项" + costIndexConfigItemDto.getConfigId() + "查询不到对应的sql语句");
            return vo;
        }
        //核算对象粒度判断
        switch (costIndexConfigItemDto.getAccountObject()) {
            //科室单元
            case AccountObject.KPI_OBJECT_DEPT_UNIT:
                //核算对象类型判断(科室单元)
//                if (ItemDimensionEnum.DEPT_UNIT.getCode().equals(objectIdType)) {

                //TODO 全院暂时选择一个判断，还未详细处理
                if (objectId == null) {
                    String all = sqlUtil.executeSql(unitConfig, map);
                    vo.setResult(all);
                    return vo;
                }
                //核算科室单元类型判断
                List<Long> unitIds = getUnitIdByAccountRange(costIndexConfigItemDto, objectId);
                if (CollUtil.isEmpty(unitIds)) {
                    vo.setErrorMsg("查询不到对应的科室单元");
                    return vo;
                }
                //遍历匹配
                for (Long id : unitIds) {
                    map.put("account_unit_id", id + "");
                    //执行查询到的sql
                    String unit = sqlUtil.executeSql(unitConfig, map);
                    map.remove("account_unit_id", id + "");
                    //如果执行结果没有,跳过
                    if (unit != null && unit != "") {
                        result = result.add(new BigDecimal(unit));
                    }
                }
//                } else {
//                    vo.setErrorMsg("核算维度与核算对象不一致");
//                    break;
//                }
                break;
            default:
                vo.setErrorMsg("核算维度与核算对象不一致");
                return vo;
        }
        vo.setResult(result + "");
        return vo;
    }

    /**
     * 此方法根据核算范围返回科室单元id集合
     *
     * @param objectId
     * @return
     */
    private List<Long> getUnitIdByAccountRange(CostIndexConfigItemDto costIndexConfigItemDto, String objectId) {
        List<Long> unitIds = new ArrayList<>();
        //本科室单元
        if (CostAccountProportionType.SELFGROUP.getGroupArrange().equals(costIndexConfigItemDto.getAccountRange())) {
            unitIds.add(Long.valueOf(objectId));
        }
        //医生组科室单元
        else if (CostAccountProportionType.DOCGROUP.getGroupArrange().equals(costIndexConfigItemDto.getAccountRange())) {
            //查询所有的医生组科室单元
            final String unitGroup = UnitMapEnum.getUnitGroup(CostAccountProportionType.DOCGROUP.getGroupArrange());
            final String desc = UnitMapEnum.getDesc(CostAccountProportionType.DOCGROUP.getGroupArrange());
            UnitInfo unitInfo = new UnitInfo();
            unitInfo.setLabel(desc);
            unitInfo.setValue(unitGroup);
            final String s = new Gson().toJson(unitInfo).toString();
            final List<CostAccountUnit> costAccountUnits = new CostAccountUnit().selectList(new LambdaQueryWrapper<CostAccountUnit>().eq(CostAccountUnit::getAccountGroupCode, s));
            unitIds = costAccountUnits.stream().map(CostAccountUnit::getId).collect(Collectors.toList());

        }
        //护理组科室单元
        else if (CostAccountProportionType.NURSEGROUP.getGroupArrange().equals(costIndexConfigItemDto.getAccountRange())) {
            //查询所有的护理组科室单元
            final String unitGroup = UnitMapEnum.getUnitGroup(CostAccountProportionType.NURSEGROUP.getGroupArrange());
            final String desc = UnitMapEnum.getDesc(CostAccountProportionType.NURSEGROUP.getGroupArrange());
            UnitInfo unitInfo = new UnitInfo();
            unitInfo.setLabel(desc);
            unitInfo.setValue(unitGroup);
            final String s = new Gson().toJson(unitInfo).toString();
            final List<CostAccountUnit> costAccountUnits = new CostAccountUnit().selectList(new LambdaQueryWrapper<CostAccountUnit>().eq(CostAccountUnit::getAccountGroupCode, s));
            unitIds = costAccountUnits.stream().map(CostAccountUnit::getId).collect(Collectors.toList());
        }
        //医技组科室单元
        else if (CostAccountProportionType.MEDICALTECHNOLOGYGROUP.getGroupArrange().equals(costIndexConfigItemDto.getAccountRange())) {
            //查询所有的护理组科室单元
            final String unitGroup = UnitMapEnum.getUnitGroup(CostAccountProportionType.MEDICALTECHNOLOGYGROUP.getGroupArrange());
            final String desc = UnitMapEnum.getDesc(CostAccountProportionType.MEDICALTECHNOLOGYGROUP.getGroupArrange());
            UnitInfo unitInfo = new UnitInfo();
            unitInfo.setLabel(desc);
            unitInfo.setValue(unitGroup);
            final String s = new Gson().toJson(unitInfo).toString();
            final List<CostAccountUnit> costAccountUnits = new CostAccountUnit().selectList(new LambdaQueryWrapper<CostAccountUnit>().eq(CostAccountUnit::getAccountGroupCode, s));
            unitIds = costAccountUnits.stream().map(CostAccountUnit::getId).collect(Collectors.toList());
        }
        //医护对应科室单元组
        else if (CostAccountProportionType.DOCNURSEGROUP.getGroupArrange().equals(costIndexConfigItemDto.getAccountRange())) {
            //根据核算对象id获取核算对象
            CostAccountUnit costAccountUnit = cacheUtils.getCostAccountUnit(Long.valueOf(objectId));
            //如果是医生组,查询对应的护理组
            final String unitGroup = UnitMapEnum.getUnitGroup(CostAccountProportionType.DOCGROUP.getGroupArrange());
            final String desc = UnitMapEnum.getDesc(CostAccountProportionType.DOCGROUP.getGroupArrange());
            UnitInfo unitInfo = new UnitInfo();
            unitInfo.setLabel(desc);
            unitInfo.setValue(unitGroup);
            final String groupCode = new Gson().toJson(unitInfo).toString();
            if (costAccountUnit.getAccountGroupCode().equals(groupCode)) {
                //查询该医生组对应的所有护理组
                List<CostDocNRelation> costDocNRelationList = new CostDocNRelation().selectList(new LambdaQueryWrapper<CostDocNRelation>().eq(CostDocNRelation::getDocAccountGroupId, objectId));
                unitIds = costDocNRelationList.stream().map(CostDocNRelation::getNurseAccountGroupId).collect(Collectors.toList());
            } else {
                //查询该护理组组对应的所有护理组
                List<CostDocNRelation> costDocNRelationList = new CostDocNRelation().selectList(new LambdaQueryWrapper<CostDocNRelation>().eq(CostDocNRelation::getNurseAccountGroupId, objectId));
                unitIds = costDocNRelationList.stream().map(CostDocNRelation::getDocAccountGroupId).collect(Collectors.toList());
            }
        }
        //行政组科室单元
        else if (CostAccountProportionType.OFFICEGROUP.getGroupArrange().equals(costIndexConfigItemDto.getAccountRange())) {
            //查询所有的行政组科室单元
            final String unitGroup = UnitMapEnum.getUnitGroup(CostAccountProportionType.DOCNURSEGROUP.getGroupArrange());
            final String desc = UnitMapEnum.getDesc(CostAccountProportionType.DOCNURSEGROUP.getGroupArrange());
            UnitInfo unitInfo = new UnitInfo();
            unitInfo.setLabel(desc);
            unitInfo.setValue(unitGroup);
            final String s = new Gson().toJson(unitInfo).toString();
            final List<CostAccountUnit> costAccountUnits = new CostAccountUnit().selectList(new LambdaQueryWrapper<CostAccountUnit>().eq(CostAccountUnit::getAccountGroupCode, s));
            unitIds = costAccountUnits.stream().map(CostAccountUnit::getId).collect(Collectors.toList());
        }
        //自定义科室单元
        else if (CostAccountProportionType.CUSTOMGROUP.getGroupArrange().equals(costIndexConfigItemDto.getAccountRange())) {
            //单元id在accounts里面
            unitIds = ExpressionCheckHelper.getIds(costIndexConfigItemDto.getAccounts()).stream()
                    .map(Long::valueOf)
                    .collect(Collectors.toList());
        }
        //药剂组科室单元
        else if (CostAccountProportionType.REAGENTGROUP.getGroupArrange().equals(costIndexConfigItemDto.getAccountRange())) {
            //查询所有的药剂组科室单元
            final String unitGroup = UnitMapEnum.getUnitGroup(CostAccountProportionType.REAGENTGROUP.getGroupArrange());
            final String desc = UnitMapEnum.getDesc(CostAccountProportionType.REAGENTGROUP.getGroupArrange());
            UnitInfo unitInfo = new UnitInfo();
            unitInfo.setLabel(desc);
            unitInfo.setValue(unitGroup);
            final String s = new Gson().toJson(unitInfo).toString();
            final List<CostAccountUnit> costAccountUnits = new CostAccountUnit().selectList(new LambdaQueryWrapper<CostAccountUnit>().eq(CostAccountUnit::getAccountGroupCode, s));
            unitIds = costAccountUnits.stream().map(CostAccountUnit::getId).collect(Collectors.toList());
        }
        return unitIds;
    }

    /**
     * 此方法用于获取科室单元下所有科室
     *
     * @param objectId
     * @param costIndexConfigItemDto
     * @return
     */
    private List<CostUnitRelateInfo> getDeptList(String objectId, CostIndexConfigItemDto costIndexConfigItemDto) {
        List<CostUnitRelateInfo> deptList = new ArrayList<>();
        //所有考核单元
        if (CostAccountProportionType.ALLGROUP.getGroupArrange().equals(costIndexConfigItemDto.getAccountRange())) {
            deptList = new CostUnitRelateInfo().selectList(Wrappers.<CostUnitRelateInfo>lambdaQuery()
                    .eq(CostUnitRelateInfo::getType, "dept"));
        }
        //医生组科室单元
        else if (CostAccountProportionType.DOCGROUP.getGroupArrange().equals(costIndexConfigItemDto.getAccountRange())) {
            //查询所有的医生组科室单元
            final String unitGroup = UnitMapEnum.getUnitGroup(CostAccountProportionType.DOCGROUP.getGroupArrange());
            final String desc = UnitMapEnum.getDesc(CostAccountProportionType.DOCGROUP.getGroupArrange());
            UnitInfo unitInfo = new UnitInfo();
            unitInfo.setLabel(desc);
            unitInfo.setValue(unitGroup);
            final String s = new Gson().toJson(unitInfo).toString();
            final List<CostAccountUnit> costAccountUnits = new CostAccountUnit().selectList(new LambdaQueryWrapper<CostAccountUnit>().eq(CostAccountUnit::getAccountGroupCode, s));
            final List<Long> unitIds = costAccountUnits.stream().map(CostAccountUnit::getId).collect(Collectors.toList());
            //遍历匹配
            for (Long unitId : unitIds) {
                deptList = new CostUnitRelateInfo().selectList(Wrappers.<CostUnitRelateInfo>lambdaQuery()
                        .eq(CostUnitRelateInfo::getAccountUnitId, unitId)
                        .eq(CostUnitRelateInfo::getType, "dept"));
                deptList = deptList.stream().distinct().collect(Collectors.toList());
            }
        }
        //护理组科室单元
        else if (CostAccountProportionType.NURSEGROUP.getGroupArrange().equals(costIndexConfigItemDto.getAccountRange())) {
            //查询所有的护理组科室单元
            final String unitGroup = UnitMapEnum.getUnitGroup(CostAccountProportionType.NURSEGROUP.getGroupArrange());
            final String desc = UnitMapEnum.getDesc(CostAccountProportionType.NURSEGROUP.getGroupArrange());
            UnitInfo unitInfo = new UnitInfo();
            unitInfo.setLabel(desc);
            unitInfo.setValue(unitGroup);
            final String s = new Gson().toJson(unitInfo).toString();
            final List<CostAccountUnit> costAccountUnits = new CostAccountUnit().selectList(new LambdaQueryWrapper<CostAccountUnit>().eq(CostAccountUnit::getAccountGroupCode, s));
            final List<Long> unitIds = costAccountUnits.stream().map(CostAccountUnit::getId).collect(Collectors.toList());
            //遍历匹配
            for (Long unitId : unitIds) {
                List<CostUnitRelateInfo> infos = new CostUnitRelateInfo().selectList(Wrappers.<CostUnitRelateInfo>lambdaQuery()
                        .eq(CostUnitRelateInfo::getAccountUnitId, unitId)
                        .eq(CostUnitRelateInfo::getType, "dept"));
                deptList = infos.stream().distinct().collect(Collectors.toList());
            }
        }
        //医技组科室单元
        else if (CostAccountProportionType.MEDICALTECHNOLOGYGROUP.getGroupArrange().equals(costIndexConfigItemDto.getAccountRange())) {
            //查询所有的护理组科室单元
            final String unitGroup = UnitMapEnum.getUnitGroup(CostAccountProportionType.MEDICALTECHNOLOGYGROUP.getGroupArrange());
            final String desc = UnitMapEnum.getDesc(CostAccountProportionType.MEDICALTECHNOLOGYGROUP.getGroupArrange());
            UnitInfo unitInfo = new UnitInfo();
            unitInfo.setLabel(desc);
            unitInfo.setValue(unitGroup);
            final String s = new Gson().toJson(unitInfo).toString();
            final List<CostAccountUnit> costAccountUnits = new CostAccountUnit().selectList(new LambdaQueryWrapper<CostAccountUnit>().eq(CostAccountUnit::getAccountGroupCode, s));
            final List<Long> unitIds = costAccountUnits.stream().map(CostAccountUnit::getId).collect(Collectors.toList());
            //遍历匹配
            //遍历匹配
            for (Long unitId : unitIds) {
                List<CostUnitRelateInfo> infos = new CostUnitRelateInfo().selectList(Wrappers.<CostUnitRelateInfo>lambdaQuery()
                        .eq(CostUnitRelateInfo::getAccountUnitId, unitId)
                        .eq(CostUnitRelateInfo::getType, "dept"));
                deptList = infos.stream().distinct().collect(Collectors.toList());
            }
        }
        //医护对应科室单元组
        else if (CostAccountProportionType.DOCNURSEGROUP.getGroupArrange().equals(costIndexConfigItemDto.getAccountRange())) {
            //根据核算对象id获取核算对象
            CostAccountUnit costAccountUnit = cacheUtils.getCostAccountUnit(Long.valueOf(objectId));
            //如果是医生组,查询对应的护理组
            final String unitGroup = UnitMapEnum.getUnitGroup(CostAccountProportionType.DOCGROUP.getGroupArrange());
            final String desc = UnitMapEnum.getDesc(CostAccountProportionType.DOCGROUP.getGroupArrange());
            UnitInfo unitInfo = new UnitInfo();
            unitInfo.setLabel(desc);
            unitInfo.setValue(unitGroup);
            final String groupCode = new Gson().toJson(unitInfo).toString();
            List<Long> unitIds = new ArrayList<>();
            if (costAccountUnit.getAccountGroupCode().equals(groupCode)) {
                //查询该医生组对应的所有护理组
                List<CostDocNRelation> costDocNRelationList = new CostDocNRelation().selectList(new LambdaQueryWrapper<CostDocNRelation>().eq(CostDocNRelation::getDocAccountGroupId, objectId));
                unitIds = costDocNRelationList.stream().map(CostDocNRelation::getNurseAccountGroupId).collect(Collectors.toList());
            } else {
                //查询该护理组组对应的所有护理组
                List<CostDocNRelation> costDocNRelationList = new CostDocNRelation().selectList(new LambdaQueryWrapper<CostDocNRelation>().eq(CostDocNRelation::getNurseAccountGroupId, objectId));
                unitIds = costDocNRelationList.stream().map(CostDocNRelation::getDocAccountGroupId).collect(Collectors.toList());
            }
            //遍历匹配
            for (Long unitId : unitIds) {
                List<CostUnitRelateInfo> infos = new CostUnitRelateInfo().selectList(Wrappers.<CostUnitRelateInfo>lambdaQuery()
                        .eq(CostUnitRelateInfo::getAccountUnitId, unitId)
                        .eq(CostUnitRelateInfo::getType, "dept"));
                deptList = infos.stream().distinct().collect(Collectors.toList());
            }
        }
        //行政组科室单元
        else if (CostAccountProportionType.OFFICEGROUP.getGroupArrange().equals(costIndexConfigItemDto.getAccountRange())) {

            //查询所有的行政组科室单元
            final String unitGroup = UnitMapEnum.getUnitGroup(CostAccountProportionType.DOCNURSEGROUP.getGroupArrange());
            final String desc = UnitMapEnum.getDesc(CostAccountProportionType.DOCNURSEGROUP.getGroupArrange());
            UnitInfo unitInfo = new UnitInfo();
            unitInfo.setLabel(desc);
            unitInfo.setValue(unitGroup);
            final String s = new Gson().toJson(unitInfo).toString();
            final List<CostAccountUnit> costAccountUnits = new CostAccountUnit().selectList(new LambdaQueryWrapper<CostAccountUnit>().eq(CostAccountUnit::getAccountGroupCode, s));
            final List<Long> unitIds = costAccountUnits.stream().map(CostAccountUnit::getId).collect(Collectors.toList());
            //遍历匹配
            for (Long unitId : unitIds) {
                List<CostUnitRelateInfo> infos = new CostUnitRelateInfo().selectList(Wrappers.<CostUnitRelateInfo>lambdaQuery()
                        .eq(CostUnitRelateInfo::getAccountUnitId, unitId)
                        .eq(CostUnitRelateInfo::getType, "dept"));
                deptList = infos.stream().distinct().collect(Collectors.toList());
            }
        }
        //自定义科室单元
        else if (CostAccountProportionType.CUSTOMGROUP.getGroupArrange().equals(costIndexConfigItemDto.getAccountRange())) {
            final List<String> ids = ExpressionCheckHelper.getIds(costIndexConfigItemDto.getAccounts());
            deptList = new CostUnitRelateInfo().selectList(new LambdaQueryWrapper<CostUnitRelateInfo>()
                    .eq(CostUnitRelateInfo::getType, "dept")
                    .in(CostUnitRelateInfo::getAccountUnitId, ids));
        }
        //药剂组科室单元
        else if (CostAccountProportionType.REAGENTGROUP.getGroupArrange().equals(costIndexConfigItemDto.getAccountRange())) {
            //查询所有的药剂组科室单元
            final String unitGroup = UnitMapEnum.getUnitGroup(CostAccountProportionType.REAGENTGROUP.getGroupArrange());
            final String desc = UnitMapEnum.getDesc(CostAccountProportionType.REAGENTGROUP.getGroupArrange());
            UnitInfo unitInfo = new UnitInfo();
            unitInfo.setLabel(desc);
            unitInfo.setValue(unitGroup);
            final String s = new Gson().toJson(unitInfo).toString();
            final List<CostAccountUnit> costAccountUnits = new CostAccountUnit().selectList(new LambdaQueryWrapper<CostAccountUnit>().eq(CostAccountUnit::getAccountGroupCode, s));
            final List<Long> unitIds = costAccountUnits.stream().map(CostAccountUnit::getId).collect(Collectors.toList());
            //遍历匹配
            for (Long unitId : unitIds) {
                deptList = new CostUnitRelateInfo().selectList(Wrappers.<CostUnitRelateInfo>lambdaQuery()
                        .eq(CostUnitRelateInfo::getAccountUnitId, unitId)
                        .eq(CostUnitRelateInfo::getType, "dept"));
                deptList = deptList.stream().distinct().collect(Collectors.toList());
            }
        } else {
            deptList = new CostUnitRelateInfo().selectList(Wrappers.<CostUnitRelateInfo>lambdaQuery()
                    .eq(CostUnitRelateInfo::getAccountUnitId, objectId)
                    .eq(CostUnitRelateInfo::getType, "dept"));
        }
        return deptList;
    }

    @Override
    //@Cacheable(value = CacheConstants.COST_INDEX, key = "#id", unless = "#result == null")
    public CostAccountIndexVo getAccountIndexById(Long id) {
        CostAccountIndex costAccountIndex = cacheUtils.getCostAccountIndex(id);
        CostAccountIndexVo costAccountIndexVo = BeanUtil.copyProperties(costAccountIndex, CostAccountIndexVo.class);
        List<CostIndexConfigItem> costIndexConfigItemList = costIndexConfigItemService.getByIndexId(costAccountIndex.getId());
        List<CostIndexConfigItemVo> costIndexConfigItemListVo = new ArrayList<>();
        for (CostIndexConfigItem costIndexConfigItem : costIndexConfigItemList) {
            CostAccountItem accountItem = cacheUtils.getCostAccountItem(costIndexConfigItem.getConfigId());
            CostIndexConfigItemVo costIndexConfigItemVo = BeanUtil.copyProperties(costIndexConfigItem, CostIndexConfigItemVo.class);
            costIndexConfigItemVo.setMeasureUnit(accountItem.getMeasureUnit());
            costIndexConfigItemVo.setRetainDecimal(accountItem.getRetainDecimal());
            costIndexConfigItemVo.setCarryRule(accountItem.getCarryRule());
            costIndexConfigItemListVo.add(costIndexConfigItemVo);
        }
        List<CostIndexConfigIndex> costIndexConfigIndexList = costIndexConfigIndexService.getByIndexId(costAccountIndex.getId());
        for (CostIndexConfigIndex costIndexConfigIndex : costIndexConfigIndexList) {
            if (costIndexConfigIndex.getConfigIndexId() != null) {
                CostAccountIndexVo accountIndexById = SpringContextHolder.getBean(this.getClass()).getAccountIndexById(costIndexConfigIndex.getConfigIndexId());
                costIndexConfigIndex.setCostAccountIndexVo(accountIndexById);
            }
        }
        costAccountIndexVo.setCostIndexConfigItemList(costIndexConfigItemListVo);
        costAccountIndexVo.setCostIndexConfigIndexList(costIndexConfigIndexList);
        return costAccountIndexVo;
    }

    @Override
    public void deleteById(Long id) {
        //删除指标时，查看该指标是否已被其他指标使用
        List<CostIndexConfigIndex> costIndexConfigIndexList = new CostIndexConfigIndex().selectList(new LambdaQueryWrapper<CostIndexConfigIndex>().eq(CostIndexConfigIndex::getConfigIndexId, id));
        if (CollUtil.isNotEmpty(costIndexConfigIndexList)) {
            List<Long> indexId = costIndexConfigIndexList.stream().map(CostIndexConfigIndex::getIndexId).collect(Collectors.toList());
            List<String> name = this.baseMapper.selectList(new LambdaQueryWrapper<CostAccountIndex>().in(CostAccountIndex::getId, indexId)).stream().map(CostAccountIndex::getName).collect(Collectors.toList());
            throw new BizException(name + "指标中包含该指标，不允许删除");
        }
        this.removeById(id);
    }

    /**
     * 修改系统指标
     * @param accountIndexDto
     */
    @Override
    public void updateSystemIndex(CostAccountIndexDto accountIndexDto) {
        CostAccountIndex costAccountIndex=new CostAccountIndex().selectById(accountIndexDto.getId());
        BeanUtil.copyProperties(accountIndexDto,costAccountIndex);
        updateById(costAccountIndex);
    }

    /**
     * 定时插入指标计算结果明细
     */
    @XxlJob("saveIndexResultDetail")
    public void saveIndexResultDetail() {
        BigDecimal result = new BigDecimal(0.0);

        List<CostVerificationResultIndex> resultIndexList = new ArrayList<>();
        //自定义传参
        String jobParam = XxlJobHelper.getJobParam();
        List<CostAccountIndex> costAccountIndexList = baseMapper.selectList(new LambdaQueryWrapper<CostAccountIndex>().eq(CostAccountIndex::getDelFlag, "0").eq(CostAccountIndex::getStatus, "0"));
        List<CostAccountUnit> costAccountUnitList = new CostAccountUnit().selectList(new LambdaQueryWrapper<CostAccountUnit>().eq(CostAccountUnit::getDelFlag, "0").eq(CostAccountUnit::getStatus, "0"));
        List<Long> unitIdList = costAccountUnitList.stream()
                .map(CostAccountUnit::getId)
                .collect(Collectors.toList());
        //获取所有的科室单元
        Integer count = 0;
        for (CostAccountIndex costAccountIndex : costAccountIndexList) {
            count++;
            //查看表中是否有
//            final CostVerificationResultIndex costVerificationResultIndex = new CostVerificationResultIndex().selectOne(new LambdaQueryWrapper<CostVerificationResultIndex>()
//                    .eq(CostVerificationResultIndex::getIndexId, costAccountIndex.getId()));
//            if (costVerificationResultIndex != null) {
//                continue;
//            }
            Integer unitCount = 0;
            for (Long unitId : unitIdList) {//遍历执行计算
                unitCount++;
                //查看表中是否有,有就跳过(针对增量数据,新增核算指标)
                final CostVerificationResultIndex newCostVerificationResultIndex = new CostVerificationResultIndex().selectOne(new LambdaQueryWrapper<CostVerificationResultIndex>()
                        .eq(CostVerificationResultIndex::getIndexId, costAccountIndex.getId())
                        .eq(CostVerificationResultIndex::getUnitId, unitId));
                if (newCostVerificationResultIndex != null) {
                    continue;
                }
                String startTime = "";
                String endTime = "";
                CostVerificationResultIndex resultIndex = new CostVerificationResultIndex();

                if (StrUtil.isNotEmpty(jobParam) && jobParam != "") {
                    log.info("本次为自定义传参调用");
                    String[] split = jobParam.split(",");
                    startTime = split[0];
                    endTime = split[1];
                    log.info("自定义传入参数开始时间为：" + split[0] + "结束时间为：" + split[1]);
                } else {
                    //获取当前时间的上一个月
                    String formattedYearMonth = LocalDateTime.now().minusMonths(1).format(DateTimeFormatter.ofPattern("yyyyMM"));
                    log.info("本次为定时任务传参调用");
                    startTime = formattedYearMonth;
                    endTime = formattedYearMonth;
                }
                resultIndex.setUnitId(Long.valueOf(unitId));
                resultIndex.setAccountDate(startTime);
                resultIndex.setOuterMostIndexId(Long.valueOf(costAccountIndex.getId()));
                //创建辅助final变量
                final String sTime = startTime;
                final String eTime = endTime;
                //多线程执行
//                Callable<ValidatorResultVo> task = () -> {
                try {
                    ValidatorResultVo indexVo = getVerificationIndex(costAccountIndex.getIndexFormula(), sTime, eTime, unitId.toString(), costAccountIndex.getStatisticalCycle() == null ? null : costAccountIndex.getStatisticalCycle().toString(), "Y", resultIndex, resultIndexList);
                    result = (indexVo == null || StrUtil.isBlank(indexVo.getResult()) ? new BigDecimal(0.0) : new BigDecimal(indexVo.getResult()));
                } catch (Exception e) {
                    log.info("错误:", e);
                    result = new BigDecimal(0.0);
                }
                //插入校验明细表
//                resultIndex.setIndexId(costAccountIndex.getId());
//                resultIndex.setCalculateFormulaDesc(costAccountIndex.getIndexFormula());
//                resultIndex.setIndexCount(result);
//                resultIndexList.add(resultIndex);
                //插入表中
                costVerificationResultIndexMapper.insertBatchSomeColumn(resultIndexList);
//                costVerificationResultIndexService.saveBatch(resultIndexList);
                //删除集合中的数据
                resultIndexList.clear();
            }
            //插入表中
            costVerificationResultIndexMapper.insertBatchSomeColumn(resultIndexList);
            //costVerificationResultIndexService.saveBatch(resultIndexList);
            //删除集合中的数据
            resultIndexList.clear();
        }
        //插入表中
        costVerificationResultIndexMapper.insertBatchSomeColumn(resultIndexList);
//        costVerificationResultIndexService.saveBatch(resultIndexList);
    }


    /**
     * 定时插入指标计算结果明细
     */
    @XxlJob("saveIndexResultDetailNew")
    public void saveIndexResultDetailNew() {
        CopyOnWriteArrayList<CostVerificationResultIndexNew> resultIndexList = new CopyOnWriteArrayList<>();
        //自定义传参
        String jobParam = XxlJobHelper.getJobParam();
        //查询所有可用核算指标
        List<CostAccountIndex> costAccountIndexList = baseMapper.selectList(new LambdaQueryWrapper<CostAccountIndex>()
                .eq(CostAccountIndex::getDelFlag, "0")
                .eq(CostAccountIndex::getStatus, "0"));
        //查询所有可用核算单元
        List<CostAccountUnit> costAccountUnitList = new CostAccountUnit().selectList(new LambdaQueryWrapper<CostAccountUnit>()
                .eq(CostAccountUnit::getDelFlag, "0")
                .eq(CostAccountUnit::getStatus, "0"));
        // 根据需求设置线程数量(此处等于指标数量)
        List<CostAccountUnit> synchronizedUnitList = Collections.synchronizedList(costAccountUnitList);
        List<CostAccountIndex> synchronizedIndexList = Collections.synchronizedList(costAccountIndexList);
        int threadCount = costAccountIndexList.size();
        CountDownLatch latch = new CountDownLatch(threadCount);
        Integer count = 0;
        for (CostAccountIndex costAccountIndex : synchronizedIndexList) {
            count++;
            executorService.execute(() -> {
                try {
                    BigDecimal result = new BigDecimal(0.0);
                    Integer unitCount = 0;
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
                    //遍历执行计算
                    for (CostAccountUnit costAccountUnit : synchronizedUnitList) {
                        //查看表中是否有,有就跳过(针对增量数据,新增核算指标)
                        final CostVerificationResultIndex newCostVerificationResultIndex = new CostVerificationResultIndex().selectOne(new LambdaQueryWrapper<CostVerificationResultIndex>()
                                .eq(CostVerificationResultIndex::getIndexId, costAccountIndex.getId())
                                .eq(CostVerificationResultIndex::getUnitId, costAccountUnit.getId()));
                        if (newCostVerificationResultIndex != null) {
                            continue;
                        }
                        unitCount++;
                        CostVerificationResultIndexNew resultIndex = new CostVerificationResultIndexNew();
                        resultIndex.setUnitId(costAccountUnit.getId());
                        resultIndex.setUnitName(costAccountUnit.getName());
                        resultIndex.setAccountDate(startTime);
                        resultIndex.setOuterMostIndexId(costAccountIndex.getId());
                        //计算
                        ValidatorResultVo indexVo = getVerificationIndexNew(costAccountIndex, startTime, endTime, costAccountUnit.getId(), resultIndex, resultIndexList);
                        result = (indexVo == null || StrUtil.isBlank(indexVo.getResult()) ? new BigDecimal(0.0) : new BigDecimal(indexVo.getResult()));
                        //插入校验明细表
                        resultIndex.setIndexId(costAccountIndex.getId());
                        resultIndex.setIndexCount(result);
                        resultIndex.setIndexName(costAccountIndex.getName());
                        resultIndexList.add(resultIndex);
                    }
                    //插入表中
                    //插入表中
                    int batchSize = 100000; // 每次插入的数据量
                    int totalSize = resultIndexList.size();
                    for (int i = 0; i < totalSize; i += batchSize) {
                        int end = Math.min(i + batchSize, totalSize);
                        List<CostVerificationResultIndexNew> subList = resultIndexList.subList(i, end);
                        costVerificationResultIndexNewMapper.insertBatchSomeColumn(subList);
                        subList.clear();
                    }
                    //costVerificationResultIndexService.saveBatch(resultIndexList);
                    //删除集合中的数据
                    resultIndexList.clear();
                } finally {
                    latch.countDown();
                }
            });
        }
        try {
            latch.await(); // 等待所有线程完成
        } catch (Exception e) {
            log.info("错误", e);
        }
        //插入表中
        costVerificationResultIndexNewMapper.insertBatchSomeColumn(resultIndexList);
        //costVerificationResultIndexService.saveBatch(resultIndexList);
        //删除集合中的数据
        resultIndexList.clear();
    }


    /**
     * 定时任务计算指标
     */
    public ValidatorResultVo getVerificationIndexNew(CostAccountIndex costAccountIndex, String startTime, String endTime, Long unitId, CostVerificationResultIndexNew resultIndex, List<CostVerificationResultIndexNew> resultIndexList) {
        ValidatorResultVo vo = new ValidatorResultVo();
        Map<String, Double> map = new HashMap<>();
        //获取指标周期
        final StatisticalPeriodEnum type = costAccountIndex.getStatisticalCycle();
        //拿到指标公式解析,获取到key集合
        Set<String> keys = ExpressionCheckHelper.expressionQuantity(costAccountIndex.getIndexFormula());
        //遍历key,拿到配置项
        for (String key : keys) {
            //根据key去查询核算配置项
            CostIndexConfigItem costIndexConfigItem = cacheUtils.getCostIndexConfigItem(key);
            //查询到配置项
            if (costIndexConfigItem != null) {
                BigDecimal result = new BigDecimal(0.0);
                //判断项是否是当前周期,环比上一周期,往前推一个月
                if ("RING_RATIO".equals(type) && "before".equals(costIndexConfigItem.getAccountPeriod())) {
                    startTime = BeforeDateUtils.getBeforeMonthDate(startTime, -01);
                    endTime = BeforeDateUtils.getBeforeMonthDate(endTime, -01);
                }
                //同比上一周期,往前推一年
                if ("YEAR_ON_YEAR".equals(type) && "before".equals(costIndexConfigItem.getAccountPeriod())) {
                    startTime = BeforeDateUtils.getBeforeYearDate(startTime, -01);
                    endTime = BeforeDateUtils.getBeforeYearDate(endTime, -01);
                }
                //查询定时任务计算核算项表
                CostVerificationResultItem costVerificationResultItem = new CostVerificationResultItem().selectOne(new LambdaQueryWrapper<CostVerificationResultItem>()
                        .eq(CostVerificationResultItem::getItemId, costIndexConfigItem.getConfigId())
                        .eq(CostVerificationResultItem::getUnitId, unitId)
                        .eq(CostVerificationResultItem::getAccountDate, startTime));
                //查询不到值,自行计算
                if (costVerificationResultItem == null) {
                    CostAccountItem costAccountItem = new CostAccountItem().selectById(costIndexConfigItem.getConfigId());
                    CostIndexConfigItemDto costIndexConfigItemDto = new CostIndexConfigItemDto();
                    BeanUtil.copyProperties(costIndexConfigItem, costIndexConfigItemDto);
                    final ValidatorResultVo itemVo = getResult(startTime, endTime, unitId + "", costIndexConfigItemDto, costAccountItem.getDimension(), ItemDimensionEnum.DEPT_UNIT.getCode());
                    result = itemVo.getResult() == null ? new BigDecimal(0.0) : new BigDecimal(itemVo.getResult());
                } else {
                    result = costVerificationResultItem.getItemCount();
                }
                //构造计算map
                map.put(key, result.doubleValue());
                //封装定时任务计算指标对象
                CostVerificationResultIndexNew costVerificationResultIndexNew = new CostVerificationResultIndexNew();
                BeanUtil.copyProperties(resultIndex, costVerificationResultIndexNew);
                costVerificationResultIndexNew.setItemCount(result);
                costVerificationResultIndexNew.setItemId(costIndexConfigItem.getConfigId());
                costVerificationResultIndexNew.setItemName(costIndexConfigItem.getConfigName());
                costVerificationResultIndexNew.setParentId(costAccountIndex.getId());
                resultIndexList.add(costVerificationResultIndexNew);
            } else if (costIndexConfigItem == null) {
                //根据key查询配置指标
                CostIndexConfigIndex costIndexConfigIndex = cacheUtils.getCostIndexConfigIndex(key);
                //如果指标也查不到,说明不存在
                if (costIndexConfigIndex == null) {
                    //计算不到数据,给出原因
                    vo.setErrorMsg("指标中的" + key + "查询不到对应的配置项和子指标");
                    return vo;
                }
                //根据指标配置项查询指标
                CostAccountIndex newCostAccountIndex = cacheUtils.getCostAccountIndex(costIndexConfigIndex.getConfigIndexId());
                if (newCostAccountIndex == null) {
                    //构造计算map
                    map.put(key, 0.0);
                    //封装定时任务计算指标对象
                    CostVerificationResultIndexNew costVerificationResultIndexNew = new CostVerificationResultIndexNew();
                    BeanUtil.copyProperties(resultIndex, costVerificationResultIndexNew);
                    costVerificationResultIndexNew.setIndexCount(new BigDecimal(0.0));
                    costVerificationResultIndexNew.setIndexId(costAccountIndex.getId());
                    costVerificationResultIndexNew.setIndexName(costAccountIndex.getName());
                    costVerificationResultIndexNew.setParentId(costAccountIndex.getId());
                    resultIndexList.add(costVerificationResultIndexNew);
                } else {
                    //递归
                    final ValidatorResultVo index = getVerificationIndexNew(newCostAccountIndex, startTime, endTime, unitId, resultIndex, resultIndexList);
                    //构造计算map
                    map.put(key, index.getResult() == null ? 0.0 : Double.valueOf(index.getResult()));
                    //封装定时任务计算指标对象
                    CostVerificationResultIndexNew costVerificationResultIndexNew = new CostVerificationResultIndexNew();
                    BeanUtil.copyProperties(resultIndex, costVerificationResultIndexNew);
                    costVerificationResultIndexNew.setIndexCount(index.getResult() == null ? new BigDecimal(0.0) : new BigDecimal(index.getResult()));
                    costVerificationResultIndexNew.setIndexId(costAccountIndex.getId());
                    costVerificationResultIndexNew.setIndexName(costAccountIndex.getName());
                    costVerificationResultIndexNew.setParentId(costAccountIndex.getId());
                    resultIndexList.add(costVerificationResultIndexNew);
                }
            }
        }
        //计算值
        String newExpression = costAccountIndex.getIndexFormula().replace("%", "/100");
        try {
            vo.setResult(ExpressionCheckHelper.checkAndCalculate(map,
                    newExpression,
                    costAccountIndex.getIndexUnit() == null ? null : costAccountIndex.getIndexUnit(),
                    costAccountIndex.getReservedDecimal(),
                    costAccountIndex.getCarryRule()));
        } catch (Exception e) {
            log.error("指标计算异常");
            vo.setResult("0.0");
        }
        return vo;
    }
}
