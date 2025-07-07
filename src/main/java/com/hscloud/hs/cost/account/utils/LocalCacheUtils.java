package com.hscloud.hs.cost.account.utils;

import com.hscloud.hs.cost.account.model.entity.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class LocalCacheUtils {
    private Map<Long, CostAccountItem> itemMap;
    private Map<Long, CostAccountIndex> indexMap;
    private Map<String, CostIndexConfigItem> costIndexConfigItemMap;
    private Map<String, CostIndexConfigIndex> costIndexConfigIndexMap;
    private Map<Long, CostAccountUnit> accountUnitMap;

    /**
     * 将核算项,核算指标,配置项,配置指标,分摊规则,分摊规则配置项,分摊规则配置指标,核算单元关联科室/人员表
     */
    @PostConstruct
    public void init() {
        //核算项
        itemMap = new CostAccountItem().selectAll().stream().collect(Collectors.toMap(CostAccountItem::getId, Function.identity()));
        //核算指标
        indexMap = new CostAccountIndex().selectAll().stream().collect(Collectors.toMap(CostAccountIndex::getId, Function.identity()));
        //核算单元
        accountUnitMap = new CostAccountUnit().selectAll().stream().collect(Collectors.toMap(CostAccountUnit::getId, Function.identity()));
        //核算配置项
        costIndexConfigItemMap = new CostIndexConfigItem().selectAll().stream().collect(Collectors.toMap(CostIndexConfigItem::getConfigKey, Function.identity()));
        //核算配置指标
        costIndexConfigIndexMap = new CostIndexConfigIndex().selectAll().stream().collect(Collectors.toMap(CostIndexConfigIndex::getConfigKey, Function.identity()));
    }

    /**
     * 根据核算项id获取核算对象
     *
     * @param id
     * @return
     */
    public CostAccountItem getCostAccountItem(Long id) {
        return itemMap.get(id);
    }

    /**
     * 緩存中新增核算项
     * @param costAccountItem
     */
    public void setItemMap(CostAccountItem costAccountItem) {
        itemMap.put(costAccountItem.getId(), costAccountItem);
    }
    /**
     * 根据核算项id获取核算指标
     *
     * @param id
     * @return
     */
    public CostAccountIndex getCostAccountIndex(Long id) {
        return indexMap.get(id);
    }


    /**
     * 緩存中新增指标项
     * @param costAccountIndex
     */
    public void setIndexMap(CostAccountIndex costAccountIndex) {
        indexMap.put(costAccountIndex.getId(), costAccountIndex);
    }

    /**
     * 根据科室单元id获取科室单元对象
     *
     * @param id
     * @return
     */
    public CostAccountUnit getCostAccountUnit(Long id) {
        return accountUnitMap.get(id);
    }

    /**
     * 新增科室单元缓存
     * @param costAccountUnit
     */
    public void setAccountUnitMap(CostAccountUnit costAccountUnit) {
        accountUnitMap.put(costAccountUnit.getId(), costAccountUnit);
    }

    /**
     * 根据配置指标key查询配置项
     *
     * @param key
     * @return
     */
    public CostIndexConfigItem getCostIndexConfigItem(String key) {
        return costIndexConfigItemMap.get(key);
    }

    /**
     * 新增配置项缓存
     */
    public void setCostIndexConfigItemMap(List<CostIndexConfigItem> costIndexConfigItem) {
        Map<String, CostIndexConfigItem> map = new HashMap<>();
        for (CostIndexConfigItem indexConfigItem : costIndexConfigItem) {
            map.put(indexConfigItem.getConfigKey(), indexConfigItem);
        }
        costIndexConfigItemMap.putAll(map);
    }

    /**
     * 根据配置指标key查询配置指标
     *
     * @param key
     * @return
     */
    public CostIndexConfigIndex getCostIndexConfigIndex(String key) {
        return costIndexConfigIndexMap.get(key);
    }
    /**
     * 新增配置项缓存
     */
    public void setCostIndexConfigIndexMap(List<CostIndexConfigIndex> costIndexConfigIndex) {
        Map<String, CostIndexConfigIndex> map = new HashMap<>();
        for (CostIndexConfigIndex indexConfigIndex : costIndexConfigIndex) {
            map.put(indexConfigIndex.getConfigKey(), indexConfigIndex);
        }
        costIndexConfigIndexMap.putAll(map);
//        costIndexConfigIndexMap.put(costIndexConfigIndex.getConfigKey(), costIndexConfigIndex);
    }
    /**
     * 释放map
     */
    public void clearMap() {
        itemMap.clear();
    }

    /**
     * 刷新Map
     *
     * @param costAccountItemList
     */
    public void refresh(List<CostAccountItem> costAccountItemList) {
        itemMap.putAll(costAccountItemList.stream().collect(Collectors.toMap(costAccountItem -> costAccountItem.getId(), Function.identity(), (k1, k2) -> k1)));
    }
}
