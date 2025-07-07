package com.hscloud.hs.cost.account.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.ListMultimap;
import com.hscloud.hs.cost.account.constant.enums.CalculateEnum;
import com.hscloud.hs.cost.account.constant.enums.UnitMapEnum;
import com.hscloud.hs.cost.account.mapper.*;
import com.hscloud.hs.cost.account.model.dto.*;
import com.hscloud.hs.cost.account.model.entity.*;
import com.hscloud.hs.cost.account.model.pojo.*;
import com.hscloud.hs.cost.account.model.vo.*;
import com.hscloud.hs.cost.account.service.*;
import com.hscloud.hs.cost.account.utils.ExpressionCheckHelper;
import com.hscloud.hs.cost.account.utils.LocalCacheUtils;
import com.pig4cloud.pigx.common.core.exception.BizException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CostAccountPlanConfigServiceImpl extends ServiceImpl<CostAccountPlanConfigMapper, CostAccountPlanConfig> implements CostAccountPlanConfigService {

    @Autowired
    private CostAccountPlanConfigIndexService costAccountPlanConfigIndexService;


    @Autowired
    private ICostAccountIndexService costAccountIndexService;

    @Autowired
    private CostAccountPlanConfigFormulaMapper costAccountPlanConfigFormulaMapper;


    @Autowired
    private CostAccountIndexMapper costAccountIndexMapper;


    @Autowired
    private CostAccountPlanConfigCustomUnitService costAccountPlanConfigCustomUnitService;

    @Autowired
    private ICostAccountPlanConfigDistributionCustomUnitService costAccountPlanConfigDistributionCustomUnitService;

    @Autowired
    @Lazy
    private CostAccountPlanCostService costAccountPlanCostService;

    @Autowired
    private CostAccountPlanConfigIndexNewMapper costAccountPlanConfigIndexNewMapper;

    @Autowired
    private CostAccountProportionRelationMapper costAccountProportionRelationMapper;

    @Autowired
    private ICostAllocationRuleService costAllocationRuleService;

    private final CostAccountPlanConfigIndexNewMapper configIndexNewMapper;

    private final CostAccountItemService costAccountItemService;

    private final CostAccountPlanMapper costAccountPlanMapper;

    private final CostIndexConfigItemMapper costIndexConfigItemMapper;

    private final CostAccountProportionRelationMapper proportionRelationMapper;

    private final CostAccountProportionMapper costAccountProportionMapper;

    private final CostCommentGroupMapper costCommentGroupMapper;

    private final CostAccountPlanConfigFormulaService costAccountPlanConfigFormulaService;

    private final CostAccountPlanConfigCustomUnitMapper costAccountPlanConfigCustomUnitMapper;

    private final CostAccountIndexServiceImpl costAccountIndexServiceImpl;

    private final LocalCacheUtils cacheUtils;

    private final CostAccountPlanConfigDistributionCustomUnitMapper costAccountPlanConfigDistributionCustomUnitMapper;


    public IPage<CostAccountPlanConfigVo> newListConfig(CostAccountPlanConfigQueryDto queryDto) {
        Page<CostAccountPlanConfigVo> result = new Page<>(queryDto.getCurrent(), queryDto.getSize());
        List<CostAccountPlanConfigVo> configVos = new ArrayList<>();
        LambdaQueryWrapper<CostAccountPlanConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper
                .eq(queryDto.getPlanId() != null, CostAccountPlanConfig::getPlanId, queryDto.getPlanId())
                .eq(queryDto.getConfigId() != null, CostAccountPlanConfig::getId, queryDto.getConfigId())
                .eq(CostAccountPlanConfig::getDelFlag, '0');
        IPage<CostAccountPlanConfig> configs = page(new Page<>(queryDto.getCurrent(), queryDto.getSize()), wrapper);

        configs.getRecords().forEach(config -> {
            CostAccountPlan costAccountPlan=new CostAccountPlan().selectById(config.getPlanId());
            //封装configVo
            CostAccountIndexVo accountIndexVo = costAccountIndexService.getAccountIndexById(config.getIndexId());
            //填充数据
            CostAccountPlanConfigVo configVo = new CostAccountPlanConfigVo();
            configVo.setId(config.getId());
            configVo.setPlanName(costAccountPlan.getName());
            configVo.setName(accountIndexVo.getName());
            configVo.setIsSystemIndex(accountIndexVo.getIsSystemIndex());
            configVo.setIsRelevance(config.getIsRelevance());
            configVo.setIndexFormula(accountIndexVo.getIndexFormula());
            configVo.setIndexId(accountIndexVo.getId());
            configVo.setIndexUnit(accountIndexVo.getIndexUnit());
            configVo.setReservedDecimal(accountIndexVo.getReservedDecimal());
            configVo.setCarryRule(accountIndexVo.getCarryRule());
            configVo.setAccountProportionObject(config.getAccountProportionObject());
            configVo.setDistributionAccountProportionObject(config.getRelevanceAccountProportionObject());
            configVo.setConfigDesc(config.getConfigDesc());
            if (UnitMapEnum.CUSTOM.getPlanGroup().equals(config.getAccountProportionObject())) {
                List<CommonDTO> commonDTOList = new ArrayList<>();
                costAccountPlanConfigCustomUnitMapper.selectList(Wrappers.<CostAccountPlanConfigCustomUnit>lambdaQuery().eq(CostAccountPlanConfigCustomUnit::getPlanConfigId, config.getId())).forEach(customUnit -> {
                    CostAccountUnit costAccountUnit = cacheUtils.getCostAccountUnit(customUnit.getCustomUnitId());
                    CommonDTO commonDTO = new CommonDTO();
                    commonDTO.setId(costAccountUnit.getId() + "");
                    commonDTO.setName(costAccountUnit.getName());
                    commonDTOList.add(commonDTO);
                });
                configVo.setCostCustomUnitList(commonDTOList);
            }
            //关联指标自定义核算对象
            if (UnitMapEnum.CUSTOM.getPlanGroup().equals(config.getRelevanceAccountProportionObject())) {
                List<CommonDTO> commonDTOList = new ArrayList<>();
                costAccountPlanConfigDistributionCustomUnitMapper.selectList(Wrappers.<CostAccountPlanConfigDistributionCustomUnit>lambdaQuery().eq(CostAccountPlanConfigDistributionCustomUnit::getPlanConfigId, config.getId())).forEach(customUnit -> {
                    CostAccountUnit costAccountUnit = cacheUtils.getCostAccountUnit(customUnit.getDistributionCustomUnitId());
                    CommonDTO commonDTO = new CommonDTO();
                    commonDTO.setId(costAccountUnit.getId() + "");
                    commonDTO.setName(costAccountUnit.getName());
                    commonDTOList.add(commonDTO);
                });
                configVo.setDistributionCostCustomUnitList(commonDTOList);
            }

            //获得plan_config_index_new表数据
            LambdaQueryWrapper<CostAccountPlanConfigIndexNew> indexWrapper = new LambdaQueryWrapper<>();
            indexWrapper.eq(CostAccountPlanConfigIndexNew::getPlanConfigId, config.getId());
            indexWrapper.last(" limit 1");
            //根据plan config id获取核算方案配置的所有对应指标项
            CostAccountPlanConfigIndexNew configIndex = configIndexNewMapper.selectOne(indexWrapper);

            //获取index
            List<CostIndexConfigIndex> indexList = accountIndexVo.getCostIndexConfigIndexList();
            //构建指标结构
            if (configIndex != null) {
                configVo.setCostIndexConfigIndexList(newGetIndexVos("", indexList, configIndex));
                //先获取外层item
                //获取最外层的指标项
                List<CostIndexConfigItemVo> itemList = accountIndexVo.getCostIndexConfigItemList();
                configVo.setCostIndexConfigItemList(newGetItemsVo(itemList, configIndex));
            }
            configVos.add(configVo);
        });
        result.setTotal(configVos.size());
        result.setRecords(configVos);
        return result;
    }

    public List<CostAccountPlanConfigItemsVo> newGetItemsVo(List<CostIndexConfigItemVo> itemList, CostAccountPlanConfigIndexNew configIndexNew) {
        List<CostAccountPlanConfigItemsVo> itemsVos = new ArrayList<>();
        //判断传入的指标项列表是否为空
        if (itemList != null && !itemList.isEmpty()) {
            //itemList是从CostAccountIndexVo获得的项列表
            itemList.forEach(item -> {

                //在核算方案配置的指标项表中找到对应的指标项

                if (item.getIndexId().equals(configIndexNew.getIndexId())) {
                    //构建itemsVos 并返回
                    CostAccountPlanConfigItemsVo itemsVo = new CostAccountPlanConfigItemsVo();
                    itemsVo.setId(item.getId());
                    itemsVo.setIndexId(item.getIndexId());
                    itemsVo.setConfigId(item.getConfigId());
                    itemsVo.setConfigKey(item.getConfigKey());
                    itemsVo.setConfigName(item.getConfigName());
                    itemsVo.setConfigDesc(item.getConfigDesc());
                    itemsVo.setMeasureUnit(item.getMeasureUnit());
                    itemsVo.setRetainDecimal(item.getRetainDecimal());
                    itemsVo.setCarryRule(item.getCarryRule());


                    //补充核算项配置信息
                    LambdaQueryWrapper<CostAccountPlanConfigIndexNew> queryWrapper = new LambdaQueryWrapper<>();
                    queryWrapper.eq(CostAccountPlanConfigIndexNew::getConfigKey, item.getConfigKey())
                            .eq(CostAccountPlanConfigIndexNew::getIndexId, item.getIndexId())
                            .eq(CostAccountPlanConfigIndexNew::getItemId, item.getConfigId())
                            .eq(CostAccountPlanConfigIndexNew::getPlanConfigId, configIndexNew.getPlanConfigId());
                    CostAccountPlanConfigIndexNew costAccountPlanConfigIndexNew = costAccountPlanConfigIndexNewMapper.selectOne(queryWrapper);
                    if (costAccountPlanConfigIndexNew != null) {
                        itemsVo.setDocNurseAllocation(costAccountPlanConfigIndexNew.getDocNurseAllocation());
                        itemsVo.setOutpatientPublic(costAccountPlanConfigIndexNew.getOutpatientPublic());
                        itemsVo.setWardCosts(costAccountPlanConfigIndexNew.getWardCosts());
                        itemsVo.setAccountRange(costAccountPlanConfigIndexNew.getAccountRange());
                        itemsVo.setBizIds(JSON.parseArray(costAccountPlanConfigIndexNew.getCustomObject(), String.class));
                        itemsVo.setCommonDTOList(JSON.parseArray(costAccountPlanConfigIndexNew.getCustomInfo(), CommonDTO.class));
                        itemsVo.setBedAllocation(costAccountPlanConfigIndexNew.getBedAllocation());
                        itemsVo.setPlanAccountRange(costAccountPlanConfigIndexNew.getAccountRange());
                        itemsVo.setRuleFormulaId(costAccountPlanConfigIndexNew.getRuleFormulaId());
                        //设置分摊比例
                        Long allocateId = costAccountPlanConfigIndexNew.getAllocateId();
                        CostAccountPlanConfigItemProportionVo proportion = getProportion(allocateId);
                        itemsVo.setProportion(proportion);
                    }

                    itemsVos.add(itemsVo);
                }
            });
        }
        return itemsVos;
    }


    public List<CostAccountPlanConfigIndexList> newGetIndexVos(String path, List<CostIndexConfigIndex> indexList, CostAccountPlanConfigIndexNew configIndexNew) {
        Long planConfigId = configIndexNew.getPlanConfigId();
        List<CostAccountPlanConfigIndexList> configIndexList = new ArrayList<>();
        if (indexList != null && !indexList.isEmpty()) {
            for (CostIndexConfigIndex index : indexList) {
                //创建符合CostAccountIndexVo的结构 便于回显
                //设置指标信息
                CostAccountPlanConfigIndexList configIndex = new CostAccountPlanConfigIndexList();
                configIndex.setId(index.getId());
                configIndex.setIndexId(index.getIndexId());
                configIndex.setConfigIndexId(index.getConfigIndexId());
                configIndex.setConfigIndexName(index.getConfigIndexName());
                configIndex.setConfigKey(index.getConfigKey());
                CostAccountIndexVo indexVo = index.getCostAccountIndexVo();
                //设置指标的信息
                CostAccountPlanConfigIndexVo configIndexVo = new CostAccountPlanConfigIndexVo();
                configIndexVo.setId(indexVo.getId());
                configIndexVo.setName(indexVo.getName());
                configIndexVo.setIndexFormula(indexVo.getIndexFormula());
                List<CostIndexConfigIndex> indexLists = indexVo.getCostIndexConfigIndexList();

                if (CollectionUtil.isEmpty(indexLists)) {
                    //先处理核算项
                    fillItems(configIndexList, index, path + index.getConfigKey(), configIndexNew, configIndexVo, configIndex, planConfigId);
                } else {
                    path += index.getConfigKey() + ",";
                    fillItems(configIndexList, index, path.substring(0, path.length() - 1), configIndexNew, configIndexVo, configIndex, planConfigId);
                    //设置指标的指标
                    configIndexVo.setCostIndexConfigIndexList(newGetIndexVos(path, indexLists, configIndexNew));
                    path = path.substring(0, path.lastIndexOf(index.getConfigKey() + ","));
                }

            }
        }
        return configIndexList;
    }

    private void fillItems(List<CostAccountPlanConfigIndexList> configIndexList, CostIndexConfigIndex index, String path, CostAccountPlanConfigIndexNew configIndexNew, CostAccountPlanConfigIndexVo configIndexVo, CostAccountPlanConfigIndexList configIndex, Long planConfigId) {
        //设置核算项的数据
        List<CostAccountPlanConfigItemsVo> costIndexConfigItemList = new ArrayList<>();
        LambdaQueryWrapper<CostIndexConfigItem> configItemLambdaQueryWrapper = new LambdaQueryWrapper<>();
        configItemLambdaQueryWrapper.eq(CostIndexConfigItem::getIndexId, index.getConfigIndexId());
        List<CostIndexConfigItem> configItems = costIndexConfigItemMapper.selectList(configItemLambdaQueryWrapper);

        for (CostIndexConfigItem costIndexConfigItem : configItems) {

            CostAccountPlanConfigItemsVo costAccountPlanConfigItemsVo = new CostAccountPlanConfigItemsVo();
            //配置项填充数据
            BeanUtil.copyProperties(costIndexConfigItem, costAccountPlanConfigItemsVo);
            CostAccountItem accountItem = costAccountItemService.getById(costIndexConfigItem.getConfigId());
            if (accountItem != null) {
                costAccountPlanConfigItemsVo.setMeasureUnit(accountItem.getMeasureUnit());
                costAccountPlanConfigItemsVo.setRetainDecimal(accountItem.getRetainDecimal());
                costAccountPlanConfigItemsVo.setCarryRule(accountItem.getCarryRule());
            }

            LambdaQueryWrapper<CostAccountPlanConfigIndexNew> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(CostAccountPlanConfigIndexNew::getConfigKey, costIndexConfigItem.getConfigKey())
                    .eq(CostAccountPlanConfigIndexNew::getIndexId, configIndexNew.getIndexId())
                    .eq(CostAccountPlanConfigIndexNew::getPath, path)
                    .eq(CostAccountPlanConfigIndexNew::getItemId, costIndexConfigItem.getConfigId())
                    .eq(CostAccountPlanConfigIndexNew::getPlanConfigId, planConfigId);
            //填充方案配置信息
            CostAccountPlanConfigIndexNew costAccountPlanConfigIndexNew = costAccountPlanConfigIndexNewMapper.selectOne(queryWrapper);

            if (costAccountPlanConfigIndexNew != null) {
                costAccountPlanConfigItemsVo.setDocNurseAllocation(costAccountPlanConfigIndexNew.getDocNurseAllocation());
                costAccountPlanConfigItemsVo.setBedAllocation(costAccountPlanConfigIndexNew.getBedAllocation());
                costAccountPlanConfigItemsVo.setOutpatientPublic(costAccountPlanConfigIndexNew.getOutpatientPublic());
                costAccountPlanConfigItemsVo.setWardCosts(costAccountPlanConfigIndexNew.getWardCosts());
                costAccountPlanConfigItemsVo.setAccountRange(costAccountPlanConfigIndexNew.getAccountRange());
                costAccountPlanConfigItemsVo.setPlanAccountRange(costAccountPlanConfigIndexNew.getAccountRange());
                costAccountPlanConfigItemsVo.setRuleFormulaId(costAccountPlanConfigIndexNew.getRuleFormulaId());
                costAccountPlanConfigItemsVo.setBizIds(JSON.parseArray(costAccountPlanConfigIndexNew.getCustomObject(), String.class));
                costAccountPlanConfigItemsVo.setCommonDTOList(JSON.parseArray(costAccountPlanConfigIndexNew.getCustomInfo(), CommonDTO.class));
                //分摊比例id
                Long allocateId = costAccountPlanConfigIndexNew.getAllocateId();
                CostAccountPlanConfigItemProportionVo proportion = getProportion(allocateId);
                //封装分摊比例核算对象
                costAccountPlanConfigItemsVo.setProportion(proportion);
            }
            costIndexConfigItemList.add(costAccountPlanConfigItemsVo);
        }

        //设置当前指标的指标项
        configIndexVo.setCostIndexConfigItemList(costIndexConfigItemList);
        configIndex.setCostAccountIndexVo(configIndexVo);
        configIndexList.add(configIndex);
    }


    public static ListMultimap<String, String> getMatchingPaths(String path, List<CostIndexConfigIndex> costAccountIndexList, List<CostAccountPlanConfigIndexNew> configIndexs, ListMultimap<String, String> matchingPaths) {
        //遍历传入的list
        for (CostIndexConfigIndex costAccountIndexVo : costAccountIndexList) {
            String parentId = Long.toString(costAccountIndexVo.getIndexId());
            path += parentId + ",";
            //如果该list下包含子指标集合，则遍历子指标集合
            if (CollUtil.isNotEmpty(costAccountIndexVo.getCostAccountIndexVo().getCostIndexConfigIndexList())) {
                //先处理核算项的数据
                for (CostIndexConfigItemVo configItem : costAccountIndexVo.getCostAccountIndexVo().getCostIndexConfigItemList()) {
                    String configKey = configItem.getConfigKey();
                    String configId = Long.toString(configItem.getConfigId());
                    //将核算项和传入的配置信息对比，满足要求的封装到该 ListMultimap 集合中
                    for (CostAccountPlanConfigIndexNew configIndexNew : configIndexs) {
                        if (configKey.equals(configIndexNew.getConfigKey()) && configId.equals(Long.toString(configIndexNew.getItemId()))) {
                            matchingPaths.put(configKey, path + costAccountIndexVo.getCostAccountIndexVo().getId());
                        }
                    }
                }
                //递归查询封装不包含子指标只有核算项的数据
                getMatchingPaths(path, costAccountIndexVo.getCostAccountIndexVo().getCostIndexConfigIndexList(), configIndexs, matchingPaths);


            } else {
                CostAccountIndexVo indexVo = costAccountIndexVo.getCostAccountIndexVo();
                path += indexVo.getId() + ",";
                for (CostIndexConfigItemVo configItem : costAccountIndexVo.getCostAccountIndexVo().getCostIndexConfigItemList()) {
                    String configKey = configItem.getConfigKey();
                    String configId = Long.toString(configItem.getConfigId());
                    //将核算项和传入的配置信息对比，满足要求的封装到该 ListMultimap 集合中
                    for (CostAccountPlanConfigIndexNew configIndexNew : configIndexs) {
                        if (configKey.equals(configIndexNew.getConfigKey()) && configId.equals(Long.toString(configIndexNew.getItemId()))) {
                            matchingPaths.put(configKey, path.substring(0, path.length() - 1));
                            path = "";
                            break;
                        }
                    }
                }
            }
        }
        return matchingPaths;
    }

    /**
     * 获取分摊比例对象
     *
     * @param allocateId
     * @return
     */
    @NotNull
    private CostAccountPlanConfigItemProportionVo getProportion(Long allocateId) {
        CostAccountPlanConfigItemProportionVo proportion = new CostAccountPlanConfigItemProportionVo();
        if (allocateId != null) {
            //封装分摊比例对象
            proportion.setAllocateId(allocateId);
            CostAccountProportion costAccountProportion = new CostAccountProportion().selectById(allocateId);
            //封装核算项内容
            if (costAccountProportion.getCostAccountItemId() != null) {
                CostAccountItem costAccountItem = costAccountItemService.getById(costAccountProportion.getCostAccountItemId());
                proportion.setItemId(costAccountItem.getId());
                proportion.setItemName(costAccountItem.getAccountItemName());
            }
            //封装分组内容
            if (costAccountProportion.getTypeGroupId() != null) {
                CostBaseGroup costBaseGroup = costCommentGroupMapper.selectById(costAccountProportion.getTypeGroupId());
                proportion.setGroupId(costBaseGroup.getId());
                proportion.setGroupName(costBaseGroup.getName());
            }
        }
        return proportion;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveConfig(CostAccountPlanConfigDto configDto) {
        CostAccountPlanConfig config = new CostAccountPlanConfig();
        config.setPlanId(configDto.getPlanId());
        config.setIndexId(configDto.getIndexId());//给了indexid 还是id
        config.setConfigKey(configDto.getConfigKey());
        config.setConfigIndexName(configDto.getConfigIndexName());
        config.setAccountProportionObject(configDto.getAccountProportionObject());
        config.setIsRelevance(configDto.getIsRelevance());
        //查找此方案所有的公式
        List<CostAccountPlanCost> planCosts = costAccountPlanCostService.listAllCostFormula(config.getPlanId());
        //判断是否存在是否已存在核算对象
        AtomicBoolean isExist = new AtomicBoolean(false);
        planCosts.forEach(planCost -> {
            if (planCost.getAccountProportionObject().equals(config.getAccountProportionObject())) {
                isExist.set(true);
            }
        });

        //不存在 就添加到Cost_Account_Plan_Cost表中
        if (isExist.get() == false) {
            CostAccountPlanCost newPlanCost = new CostAccountPlanCost();
            newPlanCost.setPlanId(config.getPlanId());
            newPlanCost.setAccountProportionObject(config.getAccountProportionObject());
            costAccountPlanCostService.save(newPlanCost);
        }

        save(config);
        Long configId = config.getId();
        //获取指标项
        List<CostAccountPlanConfigItemsVo> itemList = configDto.getCostIndexConfigItemList();
        getItems(itemList, configId);

        List<CostAccountPlanConfigIndexList> costIndexConfigIndexList = configDto.getCostIndexConfigIndexList();
        //获取指标
        List<CostAccountPlanConfigIndexList> indexList = configDto.getCostIndexConfigIndexList();
        getIndexs(indexList, configId);

    }

    public void getItems(List<CostAccountPlanConfigItemsVo> itemList, Long configId) {
        if (itemList != null && !itemList.isEmpty()) {
            //当指标项不为空时，获取并保存
            itemList.forEach(item -> {
                CostAccountPlanConfigIndex saveItem = new CostAccountPlanConfigIndex();
                saveItem.setPlanConfigId(configId);
                saveItem.setItemId(item.getId());
                saveItem.setRuleFormulaId(item.getRuleFormulaId());
                saveItem.setAccountRange(item.getPlanAccountRange());
                //医护分摊
//                if (item.getMedicalAllocation() == null || item.getMedicalAllocation().equals("0")) {
//                    saveItem.setMedicalAllocation("0");
//
//                } else {
//
//                    saveItem.setMedicalAllocation("1");
//                    saveItem.setMedicalAllocationProportion(item.getMedicalAllocationProportion());
//                    saveItem.setBzid(item.getBzid());
//                }
                saveItem.setBedAllocation(item.getBedAllocation());

                costAccountPlanConfigIndexService.save(saveItem);
            });
        }
    }

    public void getIndexs(List<CostAccountPlanConfigIndexList> indexList, Long configId) {

        if (indexList != null && !indexList.isEmpty()) {
            indexList.forEach(index -> {
                CostAccountPlanConfigIndexVo indexVo = index.getCostAccountIndexVo();
                List<CostAccountPlanConfigItemsVo> itemLists = indexVo.getCostIndexConfigItemList();
                //存储指标的指标项
                getItems(itemLists, configId);
                List<CostAccountPlanConfigIndexList> indexLists = indexVo.getCostIndexConfigIndexList();
                //存储指标的指标
                getIndexs(indexLists, configId);
            });
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateConfig(CostAccountPlanConfigDto configDto) {
        //获得当前查询的方案配置项的指标id
        Long indexId = baseMapper.selectIndexIdById(configDto.getId());

        Long planId = configDto.getPlanId();
        //获得当前查询的方案配置项的指标id
        String accountProportionObject = baseMapper.selectObjectById(configDto.getId());
        if (accountProportionObject != null && !accountProportionObject.equals(configDto.getAccountProportionObject())) {
            //如果修改了核算对象  先取出此方案的所有公式的核算对象
            List<CostAccountPlanCost> planCosts = costAccountPlanCostService.listAllCostFormula(configDto.getPlanId());
            AtomicBoolean isExist = new AtomicBoolean(false);
            AtomicInteger exists = new AtomicInteger(0);
            AtomicLong costId = new AtomicLong();
            planCosts.forEach(planCost -> {
                ;
                if (planCost.getAccountProportionObject().equals(accountProportionObject)) {
                    isExist.set(true);
                    exists.incrementAndGet();
                    costId.set(planCost.getId());
                }
            });
            //如果修改后没有此核算对象 移除此条核算对象的公式
            if (exists.get() == 1) {
                costAccountPlanCostService.removeById(costId);
            }
            //如果不存在此核算对象的公式 在Cost_Account_Plan_Cost中添加可设置的空公式
            if (isExist.get() == false) {
                CostAccountPlanCost newPlanCost = new CostAccountPlanCost();
                newPlanCost.setPlanId(configDto.getPlanId());
                newPlanCost.setAccountProportionObject(configDto.getAccountProportionObject());
                costAccountPlanCostService.save(newPlanCost);
            }
        }

        //根据指标id是否修改判断是否修改最外层指标
        if (configDto.getIndexId().equals(indexId)) {
            //如果未修改指标
            CostAccountPlanConfig config = BeanUtil.copyProperties(configDto, CostAccountPlanConfig.class);
            config.setConfigKey(configDto.getConfigKey());
            updateById(config);
            List<CostAccountPlanConfigItemsVo> itemList = configDto.getCostIndexConfigItemList();

            //修改配置指标项
            updateIndex(itemList, planId);

            //获取第二层的指标或者指标项
            List<CostAccountPlanConfigIndexList> indexList = configDto.getCostIndexConfigIndexList();
            indexList.forEach(index -> {
                CostAccountPlanConfigIndexVo indexVo = index.getCostAccountIndexVo();
                List<CostAccountPlanConfigItemsVo> itemLists = indexVo.getCostIndexConfigItemList();
                //修改指标项
                updateIndex(itemLists, planId);
                List<CostAccountPlanConfigIndexList> indexLists = indexVo.getCostIndexConfigIndexList();

                //获取第三层的指标项
                indexLists.forEach(index1 -> {
                    CostAccountPlanConfigIndexVo indexVo1 = index1.getCostAccountIndexVo();
                    List<CostAccountPlanConfigItemsVo> itemList2 = indexVo1.getCostIndexConfigItemList();
                    updateIndex(itemList2, planId);
                });
            });
        } else {
            //修改的最外层指标 先删除原有信息
            baseMapper.deleteIndexbyPlanConfigId(configDto.getId());
            CostAccountPlanConfig config = BeanUtil.copyProperties(configDto, CostAccountPlanConfig.class);
            config.setConfigKey(configDto.getConfigKey());

            updateById(config);
            Long configId = config.getId();
            List<CostAccountPlanConfigItemsVo> itemList = configDto.getCostIndexConfigItemList();
            //重新保存指标和指标项  同/add
            getItems(itemList, configId);

            List<CostAccountPlanConfigIndexList> indexList = configDto.getCostIndexConfigIndexList();
            getIndexs(indexList, configId);
        }

    }

    public void updateIndex(List<CostAccountPlanConfigItemsVo> itemList, Long planId) {
        if (itemList != null && !itemList.isEmpty()) {
            itemList.forEach(item -> {
                //根据指标配置项所属的指标配置id和项的id取出cost_account_plan_config_index表中的主键id
                Long id = baseMapper.selectIdByPlanConfigIdAndItemId(planId, item.getConfigId());
                CostAccountPlanConfigIndex index = BeanUtil.copyProperties(item, CostAccountPlanConfigIndex.class);
                index.setId(id);
                costAccountPlanConfigIndexService.updateById(index);
                //todo 修改自定义科室单元
            });
        }
    }

    @Override
    public IPage<CostAccountPlanConfigVo> listConfig(CostAccountPlanConfigQueryDto queryDto) {
        Page<CostAccountPlanConfigVo> result = new Page<>(queryDto.getCurrent(), queryDto.getSize());
        List<CostAccountPlanConfigVo> configVos = new ArrayList<>();
        LambdaQueryWrapper<CostAccountPlanConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper
                .eq(queryDto.getPlanId() != null, CostAccountPlanConfig::getPlanId, queryDto.getPlanId())
                .eq(queryDto.getConfigId() != null, CostAccountPlanConfig::getId, queryDto.getConfigId())
                .eq(CostAccountPlanConfig::getDelFlag, '0');
        List<CostAccountPlanConfig> configs = list(wrapper);

        configs.forEach(c -> {
            CostAccountIndexQueryDto indexQueryDto = new CostAccountIndexQueryDto();
            indexQueryDto.setId(c.getIndexId());

            //根据最外层指标id 从cost_Account_Index表中获取指标结构accountIndexVo  方案的配置数据需要添加
            CostAccountIndexVo accountIndexVo = costAccountIndexService.getAccountIndexById(c.getIndexId());//getAccountIndexPage(indexQueryDto).getRecords();

            //构建configvo
            CostAccountPlanConfigVo configVo = BeanUtil.copyProperties(accountIndexVo, CostAccountPlanConfigVo.class);
            configVo.setAccountProportionObject(c.getAccountProportionObject());
            //获得plan_config_index表数据
            LambdaQueryWrapper<CostAccountPlanConfigIndex> indexWrapper = new LambdaQueryWrapper<CostAccountPlanConfigIndex>();
            indexWrapper.eq(CostAccountPlanConfigIndex::getPlanConfigId, c.getId());

            //根据plan config id获取核算方案配置的所有对应指标项
            List<CostAccountPlanConfigIndex> configIndexs = costAccountPlanConfigIndexService.list(indexWrapper);


            List<CostIndexConfigItemVo> itemList = accountIndexVo.getCostIndexConfigItemList();
            //先获取外层item
            //获取最外层的指标项
//            configVo.setCostIndexConfigItemList(getItemsVo(itemList, configIndexs));

            //获取index
            List<CostIndexConfigIndex> indexList = accountIndexVo.getCostIndexConfigIndexList();

            //构建指标结构
            configVo.setCostIndexConfigIndexList(getIndexVos(indexList, configIndexs));


            configVos.add(configVo);
        });

        result.setTotal(configVos.size());
        result.setRecords(configVos);
        return result;
    }

    public List<CostAccountPlanConfigItemsVo> getItemsVo(List<CostIndexConfigItemVo> itemList, List<CostAccountPlanConfigIndex> configIndexs) {
        List<CostAccountPlanConfigItemsVo> itemsVos = new ArrayList<>();
        //判断传入的指标项列表是否为空

        if (itemList != null && !itemList.isEmpty()) {
            //itemList是从CostAccountIndexVo获得的项列表
            itemList.forEach(item -> {

                //在核算方案配置的指标项表中找到对应的指标项
                configIndexs.forEach(configIndex -> {
                    if (item.getId().equals(configIndex.getItemId())) {
                        //构建itemsVos 并返回
                        CostAccountPlanConfigItemsVo itemsVo = new CostAccountPlanConfigItemsVo();
                        itemsVo.setId(configIndex.getItemId());
                        itemsVo.setIndexId(item.getIndexId());
                        itemsVo.setConfigId(item.getConfigId());
                        itemsVo.setConfigKey(item.getConfigKey());
                        itemsVo.setConfigName(item.getConfigName());
                        itemsVo.setConfigDesc(item.getConfigDesc());
                        itemsVo.setMeasureUnit(item.getMeasureUnit());
                        itemsVo.setRetainDecimal(item.getRetainDecimal());
                        itemsVo.setCarryRule(item.getCarryRule());
//                        itemsVo.setMedicalAllocation(configIndex.getMedicalAllocation());
//                        if (itemsVo.getMedicalAllocation().equals("1")) {
//                            itemsVo.setMedicalAllocationProportion(configIndex.getMedicalAllocationProportion());
//                            itemsVo.setBzid(configIndex.getBzid());
//                        }
                        itemsVo.setBedAllocation(configIndex.getBedAllocation());
                        itemsVo.setPlanAccountRange(configIndex.getAccountRange());
                        itemsVo.setRuleFormulaId(configIndex.getRuleFormulaId());
//                        CostAllocationRuleVo rule=costAllocationRuleService.getAllocationRuleById(configIndex.getRuleFormulaId());
//                        itemsVo.setRuleFormula(rule.getAllocationRuleFormula());
                        itemsVos.add(itemsVo);
                        //todo if(itemsVo.getAccountRange==?)
                            /*List<Long> unitId=costAccountPlanConfigIndexInfoMapper.selectUnitIdsByPlanIndexId(configIndex.getPlanConfigId());
                            List<AccountUnitIdAndNameDto> units=new ArrayList<>();
                            unitId.forEach(id->{
                                AccountUnitIdAndNameDto dto=new AccountUnitIdAndNameDto();
                                dto.setAccountUnitId(id);
                                dto.setAccountUnitName(costAccountUnitMapper.getByName(id));
                                units.add(dto);
                            });*/

                        //先


                    }
                });
            });
        }
        return itemsVos;
    }

    public List<CostAccountPlanConfigIndexList> getIndexVos(List<CostIndexConfigIndex> indexList, List<CostAccountPlanConfigIndex> configIndexs) {
        List<CostAccountPlanConfigIndexList> configIndexList = new ArrayList<>();
        if (indexList != null && !indexList.isEmpty()) {
            //indexList是从CostAccountIndexVo获得的指标列表
            indexList.forEach(index -> {
                //创建符合CostAccountIndexVo的结构 便于回显
                //设置指标信息
                CostAccountPlanConfigIndexList configIndex = new CostAccountPlanConfigIndexList();
                configIndex.setId(index.getId());
                configIndex.setIndexId(index.getIndexId());
                configIndex.setConfigIndexId(index.getConfigIndexId());
                configIndex.setConfigIndexName(index.getConfigIndexName());
                configIndex.setConfigKey(index.getConfigKey());

                CostAccountIndexVo indexVo = index.getCostAccountIndexVo();

                //设置指标的信息
                CostAccountPlanConfigIndexVo configIndexVo = new CostAccountPlanConfigIndexVo();
                configIndexVo.setId(indexVo.getId());
                configIndexVo.setName(indexVo.getName());
                configIndexVo.setIndexFormula(indexVo.getIndexFormula());

                List<CostIndexConfigIndex> indexLists = indexVo.getCostIndexConfigIndexList();

                //设置当前指标的指标
                configIndexVo.setCostIndexConfigIndexList(getIndexVos(indexLists, configIndexs));

                //设置当前指标的指标项
                List<CostIndexConfigItemVo> itemLists = indexVo.getCostIndexConfigItemList();
                configIndexVo.setCostIndexConfigItemList(getItemsVo(itemLists, configIndexs));

                configIndex.setCostAccountIndexVo(configIndexVo);
                configIndexList.add(configIndex);
            });
        }
        return configIndexList;
    }

    public List<CostAccountConfigNameDto> listConfigName(Long planId) {
        //列表显示指标名称
        List<CostAccountConfigNameDto> result = new ArrayList<>();
        LambdaQueryWrapper<CostAccountPlanConfig> wrapper = new LambdaQueryWrapper<CostAccountPlanConfig>();
        wrapper.eq(CostAccountPlanConfig::getPlanId, planId);
        List<CostAccountPlanConfig> configs = list(wrapper);
        configs.forEach(config -> {
            CostAccountConfigNameDto planNameDto = new CostAccountConfigNameDto();
            planNameDto.setName(costAccountIndexMapper.selectNameById(config.getIndexId()));
            planNameDto.setAccountProportionObject(config.getAccountProportionObject());
            planNameDto.setConfigId(config.getId());
            result.add(planNameDto);
        });
        return result;
    }

    /**
     * 核算方案总成本公式校验
     *
     * @param dto
     * @return
     */
    @Override
    public ValidatorResultVo verificationCostFormula(CostAccountPlanFormulaVerificationDto dto) {
        Long sTime = System.currentTimeMillis();
        ValidatorResultVo vo = new ValidatorResultVo();
        Map<String, Double> map = new HashMap<>();
        String result = "";
        String errorMsg = "";
        SimpleDateFormat odf = new SimpleDateFormat("yyyyMM");
        SimpleDateFormat ndf = new SimpleDateFormat("yyyy年M月");
        //解析公式,获取指标keys
        String expression = dto.getFormulaDto().getExpression();
        List<FormulaDto.FormulaParam> params = dto.getFormulaDto().getParams();
        //遍历集合,根据id拿到对应的配置项集合
        for (FormulaDto.FormulaParam param : params) {
            Map<String, Double> itemMap = new HashMap<>();
            //根据id拿到对应的配置项集合
            final List<CostAccountPlanConfigIndexNew> planConfigIndexNews = new CostAccountPlanConfigIndexNew().selectList(new LambdaQueryWrapper<CostAccountPlanConfigIndexNew>().eq(CostAccountPlanConfigIndexNew::getPlanConfigId, param.getValue()));
            //拿到外层的指标
            final List<Long> indexIds = planConfigIndexNews.stream().collect(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(s -> s.getIndexId()))))
                    .stream().map(CostAccountPlanConfigIndexNew::getIndexId).collect(Collectors.toList());
            //遍历求值
            for (Long indexId : indexIds) {
                //先获取指标的计算维度信息
                CostAccountIndex costAccountIndex = new CostAccountIndex().selectById(indexId);
                Set<String> keys = ExpressionCheckHelper.expressionQuantity(costAccountIndex.getIndexFormula());
                String path = "";
                for (String key : keys) {
                    //根据key匹配核算项id
                    CostIndexConfigItem costIndexConfigItem = new CostIndexConfigItem().selectOne(new LambdaQueryWrapper<CostIndexConfigItem>().eq(CostIndexConfigItem::getConfigKey, key));
                    if (costIndexConfigItem != null) {
                        CostAccountItem costAccountItem = cacheUtils.getCostAccountItem(costIndexConfigItem.getConfigId());
                        //判断是否是配置项
                        if (costAccountItem != null) {
                            ValidatorResultVo itemVo = new ValidatorResultVo();
                            //先查询定时任务计算表是否有值
                            CostVerificationResultItem costVerificationResultItem = new CostVerificationResultItem().selectOne(new LambdaQueryWrapper<CostVerificationResultItem>()
                                    .eq(CostVerificationResultItem::getItemId, costAccountItem.getId())
                                    .eq(CostVerificationResultItem::getUnitId, dto.getObjectId())
                                    .eq(CostVerificationResultItem::getAccountDate, dto.getStartTime()));
                            if (costVerificationResultItem != null) {
                                itemVo.setResult(costVerificationResultItem.getItemCount() + "");
                            } else {
                                //是配置项,调用方法计算值
                                itemVo = costAccountIndexService.verificationItem(costIndexConfigItem.getId(), dto.getStartTime(), dto.getEndTime(), dto.getObjectId(), costIndexConfigItem.getAccountObject());
                            }
                            if (itemVo.getResult() == null) {
                                Long eTime = System.currentTimeMillis();
                                itemVo.setExecuteTime(Integer.valueOf(String.valueOf(eTime - sTime)));
                                itemVo.setErrorMsg("配置项" + key + "查不到数据");
                                return itemVo;
                            }
                            //判断是否有分摊
                            CostAccountPlanConfigIndexNew planConfigIndexNew = new CostAccountPlanConfigIndexNew().selectOne(new LambdaQueryWrapper<CostAccountPlanConfigIndexNew>()
                                    .eq(CostAccountPlanConfigIndexNew::getPlanConfigId, param.getValue())
                                    .eq(CostAccountPlanConfigIndexNew::getConfigKey, key)
                                    .eq(CostAccountPlanConfigIndexNew::getIndexId, indexId)
                                    .isNull(CostAccountPlanConfigIndexNew::getPath)
                                    .eq(CostAccountPlanConfigIndexNew::getItemId, costAccountItem.getId()));
                            if (planConfigIndexNew == null) {
                                //没有分摊,直接插入配置项的值到map
                                itemMap.put(key, Double.valueOf(itemVo.getResult()));
                            } else {
                                ValidatorResultVo allocationRuleData = new ValidatorResultVo();
                                //查询分摊,计算分摊值,添加map
                                Map<String, Double> ruleMap = new HashMap<>();
                                //先查询定时任务计算表
                                CostVerificationResultRule costVerificationResultRule = new CostVerificationResultRule().selectOne(new LambdaQueryWrapper<CostVerificationResultRule>()
                                        .eq(CostVerificationResultRule::getRuleId, planConfigIndexNew.getRuleFormulaId())
                                        .eq(CostVerificationResultRule::getUnitId, dto.getObjectId())
                                        .eq(CostVerificationResultRule::getAccountDate, dto.getStartTime()));
                                if (costVerificationResultRule != null) {
                                    allocationRuleData.setResult(costVerificationResultRule.getRuleCount() + "");
                                } else {
                                    allocationRuleData = costAllocationRuleService.getAllocationRuleData(ruleMap, planConfigIndexNew.getRuleFormulaId(), dto.getStartTime(), dto.getEndTime(), dto.getObjectId(), costAccountItem.getDimension());
                                }
                                if (allocationRuleData.getResult() == null) {
                                    Long eTime = System.currentTimeMillis();
                                    allocationRuleData.setExecuteTime(Integer.valueOf(String.valueOf(eTime - sTime)));
                                    return allocationRuleData;
                                }
                                itemMap.put(key, new BigDecimal(itemVo.getResult()).multiply(new BigDecimal(allocationRuleData.getResult())).doubleValue());
                            }
                        }
                    } else {
                        ValidatorResultVo indexVo = new ValidatorResultVo();
                        //不是配置项,是指标
                        //根据key匹配核算指标
                        CostIndexConfigIndex costIndexConfigIndex = new CostIndexConfigIndex().selectOne(new LambdaQueryWrapper<CostIndexConfigIndex>().eq(CostIndexConfigIndex::getConfigKey, key));
                        CostAccountIndex accountIndex = new CostAccountIndex().selectOne(new LambdaQueryWrapper<CostAccountIndex>()
                                .eq(CostAccountIndex::getId, costIndexConfigIndex.getConfigIndexId()));
                        //先查询定时任务计算表
                        CostVerificationResultIndexNew costVerificationResultIndexNew = new CostVerificationResultIndexNew().selectOne(new LambdaQueryWrapper<CostVerificationResultIndexNew>()
                                .eq(CostVerificationResultIndexNew::getIndexId, indexId)
                                .eq(CostVerificationResultIndexNew::getUnitId, dto.getObjectId())
                                .eq(CostVerificationResultIndexNew::getAccountDate, dto.getStartTime()));
                        if (costVerificationResultIndexNew != null) {
                            indexVo.setResult(costVerificationResultIndexNew.getIndexCount() + "");
                        } else {
                            indexVo = getIndex(key, indexId, key, accountIndex, param.getValue(), dto.getObjectId(), dto.getStartTime(), dto.getEndTime());
                        }
                        if (indexVo.getResult() == null) {
                            Long eTime = System.currentTimeMillis();
                            indexVo.setExecuteTime(Integer.valueOf(String.valueOf(eTime - sTime)));
                            return indexVo;
                        }
                        itemMap.put(key, Double.valueOf(indexVo.getResult()));
                    }
                }
                //计算指标的值
                try {
                    String newExpression = costAccountIndex.getIndexFormula().replace("%", "/100");
                    result = ExpressionCheckHelper.checkAndCalculate(itemMap,
                            newExpression,
                            null,
                            dto.getReservedDecimal(),
                            dto.getCarryRule());
                } catch (NullPointerException e1) {
                    errorMsg = "表达式校验不通过，请检查表达式。";
                } catch (Exception e2) {
                    errorMsg = e2.getMessage();
                }
                //判断结果是否是数字格式
                if (result.equals("NaN")) {
                    result = "0.0";
                }
                map.put(param.getKey(), Double.valueOf(result));
            }
        }
        //计算总公式的值
        try {
            String newExpression = expression.replace("%", "/100");
            result = ExpressionCheckHelper.checkAndCalculate(map,
                    newExpression,
                    null,
                    dto.getReservedDecimal(),
                    dto.getCarryRule());
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
        vo.setResult(result);
        vo.setExecuteTime(Integer.valueOf(String.valueOf(eTime - sTime)));
        vo.setErrorMsg(errorMsg);
        return vo;
    }

    /**
     * 此方法用于计算总成本公式指标
     *
     * @param path             父级路径
     * @param parentId         指标id
     * @param parentKey        指标的key
     * @param costAccountIndex 指标项
     * @param planId           配置方案id
     * @param
     * @return
     */
    private ValidatorResultVo getIndex(String path, Long parentId, String parentKey, CostAccountIndex costAccountIndex, String planId, String objectId, String startTime, String endTime) {
        ValidatorResultVo vo = new ValidatorResultVo();
        String result = "";
        List<Long> items = new ArrayList<>();
        Map<String, Double> map = new HashMap<>();
        String newPath = path;
        //先获取指标的计算维度信息
        //解析指标
        Set<String> keys = ExpressionCheckHelper.expressionQuantity(costAccountIndex.getIndexFormula());
        for (String key : keys) {
            //根据key匹配核算项id
            CostIndexConfigItem costIndexConfigItem = new CostIndexConfigItem().selectOne(new LambdaQueryWrapper<CostIndexConfigItem>().eq(CostIndexConfigItem::getConfigKey, key));
            if (costIndexConfigItem != null) {
                CostAccountItem costAccountItem = cacheUtils.getCostAccountItem(costIndexConfigItem.getConfigId());
                //判断是否是配置项
                if (costAccountItem != null) {
                    ValidatorResultVo itemVo = new ValidatorResultVo();
                    //先查询定时任务计算表是否有值
                    CostVerificationResultItem costVerificationResultItem = new CostVerificationResultItem().selectOne(new LambdaQueryWrapper<CostVerificationResultItem>()
                            .eq(CostVerificationResultItem::getItemId, costAccountItem.getId())
                            .eq(CostVerificationResultItem::getUnitId, objectId)
                            .eq(CostVerificationResultItem::getAccountDate, startTime));
                    if (costVerificationResultItem != null) {
                        itemVo.setResult(costVerificationResultItem.getItemCount() + "");
                    } else {
                        //是配置项,调用方法计算值
                        itemVo = costAccountIndexService.verificationItem(costIndexConfigItem.getId(), startTime, endTime, objectId, costIndexConfigItem.getAccountObject());
                    }
                    if (itemVo.getResult() == null) {
                        itemVo.setErrorMsg("配置项" + key + "查不到数据");
                        return itemVo;
                    }
                    //判断是否有分摊
                    CostAccountPlanConfigIndexNew planConfigIndexNew = new CostAccountPlanConfigIndexNew().selectOne(new LambdaQueryWrapper<CostAccountPlanConfigIndexNew>()
                            .eq(CostAccountPlanConfigIndexNew::getPlanConfigId, parentId)
                            .eq(CostAccountPlanConfigIndexNew::getConfigKey, key)
                            .eq(CostAccountPlanConfigIndexNew::getIndexId, parentId)
                            .eq(CostAccountPlanConfigIndexNew::getPath, newPath)
                            .eq(CostAccountPlanConfigIndexNew::getItemId, costAccountItem.getId()));
                    if (planConfigIndexNew == null) {
                        //没有分摊,直接插入配置项的值到map
                        map.put(key, Double.valueOf(itemVo.getResult()));
                    } else {
                        //查询分摊,计算分摊值,添加map
                        Map<String, Double> ruleMap = new HashMap<>();
                        ValidatorResultVo allocationRuleData = new ValidatorResultVo();
                        //先查询定时任务计算表
                        CostVerificationResultRule costVerificationResultRule = new CostVerificationResultRule().selectOne(new LambdaQueryWrapper<CostVerificationResultRule>()
                                .eq(CostVerificationResultRule::getRuleId, planConfigIndexNew.getRuleFormulaId())
                                .eq(CostVerificationResultRule::getUnitId, objectId)
                                .eq(CostVerificationResultRule::getAccountDate, startTime));
                        if (costVerificationResultRule != null) {
                            allocationRuleData.setResult(costVerificationResultRule.getRuleCount() + "");
                        } else {
                            allocationRuleData = costAllocationRuleService.getAllocationRuleData(ruleMap, planConfigIndexNew.getRuleFormulaId(), startTime, endTime, objectId, costAccountItem.getDimension());
                        }
                        if (allocationRuleData.getResult() == null) {
                            map.put(key, Double.valueOf(itemVo.getResult()));
                        } else {
                            map.put(key, new BigDecimal(itemVo.getResult()).multiply(new BigDecimal(allocationRuleData.getResult())).doubleValue());
                        }
                    }
                }
            } else {
                ValidatorResultVo indexVo = new ValidatorResultVo();
                //不是配置项,是指标
                //根据key匹配核算指标
                CostIndexConfigIndex costIndexConfigIndex = new CostIndexConfigIndex().selectOne(new LambdaQueryWrapper<CostIndexConfigIndex>().eq(CostIndexConfigIndex::getConfigKey, key));
                CostAccountIndex accountIndex = new CostAccountIndex().selectOne(new LambdaQueryWrapper<CostAccountIndex>()
                        .eq(CostAccountIndex::getId, costIndexConfigIndex.getConfigIndexId()));
                newPath += "," + key;
                //先查询定时任务计算表
                CostVerificationResultIndexNew costVerificationResultIndexNew = new CostVerificationResultIndexNew().selectOne(new LambdaQueryWrapper<CostVerificationResultIndexNew>()
                        .eq(CostVerificationResultIndexNew::getIndexId, accountIndex.getId())
                        .eq(CostVerificationResultIndexNew::getUnitId, objectId)
                        .eq(CostVerificationResultIndexNew::getAccountDate, startTime));
                if (costVerificationResultIndexNew != null) {
                    indexVo.setResult(costVerificationResultIndexNew.getIndexCount() + "");
                } else {
                    indexVo = getIndex(newPath, costAccountIndex.getId(), key, accountIndex, planId, objectId, startTime, endTime);
                }
                if (indexVo.getResult() == null) {
                    indexVo.setErrorMsg("配置项" + key + "查不到数据");
                    return indexVo;
                }
                map.put(key, Double.valueOf(indexVo.getResult()));
            }
        }
        //计算值
        try {
            String newExpression = costAccountIndex.getIndexFormula().replace("%", "/100");
            result = ExpressionCheckHelper.checkAndCalculate(map, newExpression, null, null, null);
        } catch (Exception e) {
            log.error("计算指标出错,参数为{},失败原因为{}", JSON.toJSONString(costAccountIndex), e.getMessage());
        }
        //判断结果是否是数字格式
        if (result.equals("NaN")) {
            result = "0.0";
        }
        //返回结果
        vo.setResult(result);
        return vo;

    }

    /**
     * 新增核算指标配置项
     *
     * @param costAccountPlanConfigNewDto 核算方案配置的核算指标的配置项
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean saveConfigNew(CostAccountPlanConfigNewDto costAccountPlanConfigNewDto) {
        Long planId = costAccountPlanConfigNewDto.getPlanId();
        CalculationIndex calculationIndex = costAccountPlanConfigNewDto.getCalculationIndex();
        String path = "";
        Long configId;
        if (Objects.isNull(costAccountPlanConfigNewDto.getId())) {
            //保存最基础的配置
            CostAccountPlanConfig config = new CostAccountPlanConfig();
            config.setPlanId(planId);
            config.setIndexId(costAccountPlanConfigNewDto.getIndexId());
            config.setAccountProportionObject(costAccountPlanConfigNewDto.getAccountRange());
            config.setSeq(costAccountPlanConfigNewDto.getReq());
            config.setConfigKey(costAccountPlanConfigNewDto.getConfigKey());
            config.setIsRelevance(costAccountPlanConfigNewDto.getIsRelevance());
            config.setRelevanceAccountProportionObject(costAccountPlanConfigNewDto.getRelevanceAccountRange());
            config.setConfigDesc(costAccountPlanConfigNewDto.getConfigDesc());
            config.insert();
            configId = config.getId();
        } else {
            configId = costAccountPlanConfigNewDto.getId();
            //先删除原有的配置数据
            costAccountPlanConfigCustomUnitService.remove(new LambdaQueryWrapper<CostAccountPlanConfigCustomUnit>()
                    .eq(CostAccountPlanConfigCustomUnit::getPlanConfigId, configId));
            //删除原有的计算指标数据
            costAccountPlanConfigIndexNewMapper.delete(new LambdaQueryWrapper<CostAccountPlanConfigIndexNew>()
                    .eq(CostAccountPlanConfigIndexNew::getPlanConfigId, configId));
            costAccountPlanConfigDistributionCustomUnitService.remove(new LambdaQueryWrapper<CostAccountPlanConfigDistributionCustomUnit>()
                    .eq(CostAccountPlanConfigDistributionCustomUnit::getPlanConfigId, configId));
        }
        //保存自定义核算单元数据
        List<Long> customUnitIdList = costAccountPlanConfigNewDto.getCustomUnitIdList();
        if (CollectionUtil.isNotEmpty(customUnitIdList)) {
            List<CostAccountPlanConfigCustomUnit> customUnits = new ArrayList<>(customUnitIdList.size());
            Long finalConfigId = configId;
            customUnitIdList.forEach(s -> {
                CostAccountPlanConfigCustomUnit costAccountPlanConfigCustomUnit = new CostAccountPlanConfigCustomUnit();
                costAccountPlanConfigCustomUnit.setPlanConfigId(finalConfigId);
                costAccountPlanConfigCustomUnit.setCustomUnitId(s);
                costAccountPlanConfigCustomUnit.setPlanId(planId);
                customUnits.add(costAccountPlanConfigCustomUnit);
            });
            costAccountPlanConfigCustomUnitService.saveBatch(customUnits);
        }
        //保存关联指标自定义核算单元数据
        final List<CostAccountPlanConfigNewDto.distributionCustomUnitId> distributionCustomUnitIdList = costAccountPlanConfigNewDto.getDistributionCustomUnitIdList();
        if (CollectionUtil.isNotEmpty(distributionCustomUnitIdList)) {
            List<CostAccountPlanConfigDistributionCustomUnit> distributionCustomUnits = new ArrayList<>(distributionCustomUnitIdList.size());
            Long finalConfigId = configId;
            distributionCustomUnitIdList.forEach(s -> {
                CostAccountPlanConfigDistributionCustomUnit distributionCustomUnit = new CostAccountPlanConfigDistributionCustomUnit();
                distributionCustomUnit.setPlanConfigId(finalConfigId);
                distributionCustomUnit.setDistributionCustomUnitId(s.getId());
                distributionCustomUnit.setPlanId(planId);
                distributionCustomUnits.add(distributionCustomUnit);
            });
            costAccountPlanConfigDistributionCustomUnitService.saveBatch(distributionCustomUnits);
        }
        //系统指标不落入cost_account_plan_config_index_new表中
        if ("1".equals(new CostAccountIndex().selectById(calculationIndex.getId()).getIsSystemIndex())) {
            return true;
        }
        parseCalculateInfo(configId, calculationIndex, calculationIndex.getId(), path);
        return true;
    }


    private void parseCalculateInfo(Long planId, CalculationComponent calculationComponent, Long id, String path) {

        if (CalculateEnum.INDEX.getType().equals(calculationComponent.getType())) {
            CalculationIndex calculationIndex = (CalculationIndex) calculationComponent;
            List<CalculationComponent> children = calculationIndex.getChildren();
            if (StrUtil.isNotBlank(calculationIndex.getConfigKey())) {
                path += calculationIndex.getConfigKey() + ",";
            }
            if (children != null && !children.isEmpty()) {
                String finalPath = path;
                children.forEach(child -> {
                    parseCalculateInfo(planId, child, id, finalPath);
                });
            }
        } else if (CalculateEnum.ITEM.getType().equals(calculationComponent.getType())) {
            CalculationItem calculationItem = (CalculationItem) calculationComponent;
            PlanItem planItem = calculationItem.getPlanItem();
            if (StrUtil.isNotBlank(path)) {
                path = path.substring(0, path.length() - 1);
            }
            CostAccountPlanConfigIndexNew configIndex = new CostAccountPlanConfigIndexNew();
            configIndex.setPlanConfigId(planId);
            configIndex.setIndexId(id);
            configIndex.setPath(path);
            BeanUtils.copyProperties(planItem, configIndex);
            configIndex.setCustomObject(JSON.toJSONString(planItem.getBizList()));
            configIndex.setCustomInfo(JSON.toJSONString(planItem.getCommonDTOList()));

            configIndex.insert();
        } else {
            throw new BizException("不支持的类型");
        }

    }

    public CostAccountPlanReviewVo parsePlanConfig(Long planId) {
        CostAccountPlanReviewVo vo = new CostAccountPlanReviewVo();
        DistributionTaskGroup taskGroup = new DistributionTaskGroup().selectById(new CostAccountPlan().selectById(planId).getTaskGroupId());
        if (taskGroup==null) {
            return vo;
        }
        if ("TT001".equals(new JSONObject(taskGroup.getType()).getStr("value"))) {
            vo.setIsCost("1");
        }else {
            vo.setIsCost("0");
        }
        //总公式
        List<CostAccountPlanConfigFormula> costAccountPlanConfigFormulas = costAccountPlanConfigFormulaMapper.selectList(Wrappers.<CostAccountPlanConfigFormula>lambdaQuery()
                .eq(CostAccountPlanConfigFormula::getPlanId, planId));
        List<PlanCostPreviewVo> planCostPreviewVos = this.parsePlanCost(costAccountPlanConfigFormulas);
        vo.setCostAccountPlanCost(planCostPreviewVos);
        //方案
        CostAccountPlanConfigQueryDto costAccountPlanConfigQueryDto = new CostAccountPlanConfigQueryDto();
        costAccountPlanConfigQueryDto.setPlanId(planId);
        IPage listConfig = this.newListConfig(costAccountPlanConfigQueryDto);
        vo.setCostAccountPlanConfigVo(listConfig);
        return vo;
    }

    public List<PlanCostPreviewVo> parsePlanCost(List<CostAccountPlanConfigFormula> formulas) {

        List<PlanCostPreviewVo> previewVoList = formulas.stream().map(cost -> {


            List<ConfigList> configLists = new ArrayList<>();
            List<CostFormulaInfo> costFormulaInfos = JSON.parseArray(cost.getConfig(), CostFormulaInfo.class);
            costFormulaInfos.forEach(costFormulaInfo -> {
                ConfigList configList = new ConfigList();
                configList.setConfigKey(costFormulaInfo.getKey());
                configList.setConfigIndexName(costFormulaInfo.getName());
                configLists.add(configList);
            });
            PlanCostPreviewVo previewVo = BeanUtil.copyProperties(cost, PlanCostPreviewVo.class);
            if (!UnitMapEnum.CUSTOM.getPlanGroup().equals(cost.getAccountObject())) {
                previewVo.setAccountObjectName(UnitMapEnum.getDesc(cost.getAccountObject()));
            } else {
                Long customUnitId = cost.getCustomUnitId();
                CostAccountUnit costAccountUnit = cacheUtils.getCostAccountUnit(customUnitId);
                if (costAccountUnit == null) {
                    throw new BizException("自定义核算单元不存在");
                }
                previewVo.setAccountObjectName(costAccountUnit.getName());
            }
            previewVo.setConfigLists(configLists);
            return previewVo;
        }).collect(Collectors.toList());
        return previewVoList;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean saveFormula(CostAccountPlanConfigFormulaDto configFormulaDto) {
        List<CostFormulaInfo> formulaInfos = new ArrayList<>();
        //如果传了id就是修改
        if (configFormulaDto.getId() != null) {
            CostAccountPlanConfigFormula configFormula = new CostAccountPlanConfigFormula();
            BeanUtils.copyProperties(configFormulaDto, configFormula);
            configFormula.setPlanCostFormula(configFormulaDto.getFormulaDto().getExpression());
            dealParams(formulaInfos, configFormulaDto);

            configFormula.setConfig(JSON.toJSONString(formulaInfos));
            configFormula.updateById();
        } else {
            //新增
            FormulaDto formulaDto = configFormulaDto.getFormulaDto();
            List<FormulaDto.FormulaParam> params = formulaDto.getParams();
            if (CollectionUtil.isEmpty(params)) {
                throw new BizException("公式参数不能为空");
            }
            dealParams(formulaInfos, configFormulaDto);
            CostAccountPlanConfigFormula configFormula = new CostAccountPlanConfigFormula();
            BeanUtils.copyProperties(configFormulaDto, configFormula);
            configFormula.setPlanCostFormula(formulaDto.getExpression());
            configFormula.setConfig(JSON.toJSONString(formulaInfos));
            configFormula.insert();
        }
        return true;
    }

    private void dealParams(List<CostFormulaInfo> formulaInfos, CostAccountPlanConfigFormulaDto configFormulaDto) {
        configFormulaDto.getFormulaDto().getParams().forEach(p -> {
            if (p.getKey() == null) {
                throw new BizException("公式参数的定义key不能为空");
            }
            //关联key到对应的指标项
            String value = p.getValue();
            LambdaUpdateWrapper<CostAccountPlanConfig> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(CostAccountPlanConfig::getId, Long.parseLong(value));
            updateWrapper.set(CostAccountPlanConfig::getConfigKey, p.getKey());
            this.update(updateWrapper);
            CostFormulaInfo formulaInfo = new CostFormulaInfo();
            formulaInfo.setId(Long.parseLong(p.getValue()));
            formulaInfo.setKey(p.getKey());
            formulaInfo.setName(p.getName());
            formulaInfo.setType(p.getType());
            formulaInfos.add(formulaInfo);
        });
    }

    @Override
    public List<CostPlanCalculateInfo> getGroupUsefulConfig(List<String> groupCodes, Long planId) {

        //获取核算对象对应的方案总公式配置
        List<CostAccountPlanConfigFormula> costAccountPlanConfigFormulas = costAccountPlanConfigFormulaMapper.selectList(new LambdaQueryWrapper<CostAccountPlanConfigFormula>()
                .eq(CostAccountPlanConfigFormula::getPlanId, planId)
                .in(CostAccountPlanConfigFormula::getAccountObject, groupCodes));
        if (CollectionUtil.isEmpty(costAccountPlanConfigFormulas)) {
            return new ArrayList<>();
        }
        List<CostPlanCalculateInfo> costPlanCalculateInfos = new ArrayList<>(costAccountPlanConfigFormulas.size());
        for (CostAccountPlanConfigFormula costAccountPlanConfigFormula : costAccountPlanConfigFormulas) {
            CostPlanCalculateInfo costPlanCalculateInfo = new CostPlanCalculateInfo();
            costPlanCalculateInfo.setGroup(costAccountPlanConfigFormula.getAccountObject());
            costPlanCalculateInfo.setPlanExpressSion(costAccountPlanConfigFormula.getPlanCostFormula());
            //根据总公式配置获取对应的配置项
            List<CostFormulaInfo> costFormulaInfos = JSON.parseArray(costAccountPlanConfigFormula.getConfig(), CostFormulaInfo.class);
            costPlanCalculateInfo.setCostFormulaInfos(costFormulaInfos);
            costPlanCalculateInfos.add(costPlanCalculateInfo);
        }
        return costPlanCalculateInfos;
    }

    @Override
    public List<CostPlanCalculateInfo> getCustomUsefulConfig(List<Long> accountIds, Long planId) {
        //获取核算对象对应的方案总公式配置
        List<CostAccountPlanConfigFormula> costAccountPlanConfigFormulas = costAccountPlanConfigFormulaMapper.selectList(new LambdaQueryWrapper<CostAccountPlanConfigFormula>()
                .eq(CostAccountPlanConfigFormula::getPlanId, planId)
                .in(CostAccountPlanConfigFormula::getCustomUnitId, accountIds));
        if (CollectionUtil.isEmpty(costAccountPlanConfigFormulas)) {
            return new ArrayList<>();
        }
        List<CostPlanCalculateInfo> costPlanCalculateInfos = new ArrayList<>(costAccountPlanConfigFormulas.size());
        for (CostAccountPlanConfigFormula costAccountPlanConfigFormula : costAccountPlanConfigFormulas) {
            CostPlanCalculateInfo costPlanCalculateInfo = new CostPlanCalculateInfo();
            costPlanCalculateInfo.setUnitId(costAccountPlanConfigFormula.getCustomUnitId());
            costPlanCalculateInfo.setPlanExpressSion(costAccountPlanConfigFormula.getPlanCostFormula());
            //根据总公式配置获取对应的配置项
            List<CostFormulaInfo> costFormulaInfos = JSON.parseArray(costAccountPlanConfigFormula.getConfig(), CostFormulaInfo.class);
            costPlanCalculateInfo.setCostFormulaInfos(costFormulaInfos);
            costPlanCalculateInfos.add(costPlanCalculateInfo);
        }
        return costPlanCalculateInfos;

    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteConfig(Long id) {
        CostAccountPlanConfig config = baseMapper.selectById(id);

        List<CostAccountPlanConfig> configs = list(new LambdaQueryWrapper<>(new CostAccountPlanConfig())
                .eq(CostAccountPlanConfig::getPlanId, config.getPlanId())
                .eq(CostAccountPlanConfig::getAccountProportionObject, config.getAccountProportionObject()));
        //有多个时判断公式中是否存在此指标
        if (configs.size() > 1) {
            //查询公式中是否已经配置此指标
            LambdaQueryWrapper<CostAccountPlanConfigFormula> formulaWrapper = new LambdaQueryWrapper<>();
            formulaWrapper
                    .eq(CostAccountPlanConfigFormula::getPlanId, config.getPlanId())
                    .eq(CostAccountPlanConfigFormula::getAccountObject, config.getAccountProportionObject())
                    .eq(CostAccountPlanConfigFormula::getDelFlag, '0')
                    .orderByDesc(CostAccountPlanConfigFormula::getId);
            List<CostAccountPlanConfigFormula> formulas = costAccountPlanConfigFormulaService.list(formulaWrapper);
            CostAccountPlanConfigFormula formula = new CostAccountPlanConfigFormula();
            if (!formulas.isEmpty()) {
                //获取最新的公式
                formula = formulas.get(0);
            }
            if (formula != null && formula.getPlanCostFormula() != null) {
                Set<String> configKeys = ExpressionCheckHelper.expressionQuantity(formula.getPlanCostFormula());
                configKeys.forEach(key -> {
                    if (key.equals(config.getConfigKey())) {
                        throw new BizException("公式参数中包含此核算指标");
                    }
                });
            }
        } else if (configs.size() == 1) {
            UpdateWrapper<CostAccountPlanConfigFormula> formulaWrapper = new UpdateWrapper<>();
            formulaWrapper.set("del_flag", "1")
                    .eq("plan_id", config.getPlanId())
                    .eq("account_object", config.getAccountProportionObject());
            costAccountPlanConfigFormulaService.update(null, formulaWrapper);
        }

        //删除中间表config_index_new的记录
        UpdateWrapper<CostAccountPlanConfigIndexNew> indexWrapper = new UpdateWrapper<>();
        indexWrapper.set("del_flag", "1").eq("plan_config_id", id);
        costAccountPlanConfigIndexNewMapper.update(null, indexWrapper);

        //删除中间表config_custom_unit的记录
        UpdateWrapper<CostAccountPlanConfigCustomUnit> unitWrapper = new UpdateWrapper<>();
        unitWrapper.set("del_flag", "1").eq("plan_config_id", id);
        costAccountPlanConfigCustomUnitMapper.update(null, unitWrapper);

        //删除中间表cost_account_plan_config_distribution_custom_unit
        UpdateWrapper<CostAccountPlanConfigDistributionCustomUnit> distributionWrapper = new UpdateWrapper<>();
        distributionWrapper.set("del_flag", "1").eq("plan_config_id", id);
        costAccountPlanConfigDistributionCustomUnitMapper.update(null, distributionWrapper);
        return this.removeById(id);
    }


}

