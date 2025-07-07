package com.hscloud.hs.cost.account.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hscloud.hs.cost.account.model.dto.CostAccountItemInitDto;
import com.hscloud.hs.cost.account.model.dto.CostAccountItemQueryDto;
import com.hscloud.hs.cost.account.model.entity.CostAccountItem;

import java.util.List;

/**
 * @author Admin
 */
public interface CostAccountItemService extends IService<CostAccountItem> {
    /**
     * 根据分组id获取核算项
     *
     * @param costAccountItemQueryDto 查询对象
     * @return 核算项列表
     */
    IPage<CostAccountItem> listItem(CostAccountItemQueryDto costAccountItemQueryDto);


    /**
     * 初始化核算项
     * @param costAccountItemInitDto 初始化对象
     * @return 是否成功
     */
    Boolean initItem(CostAccountItemInitDto costAccountItemInitDto);


    /**
     * 启停用核算项
     */
    Boolean enableItem(Long id,String status);

    /**
     * 根据核算项名称获取核算项
     * @param name
     * @return
     */
    IPage<CostAccountItem> getItemsByName(String name);

    /**
     * 根据id获取核算项
     * @param id
     * @return
     */
    CostAccountItem getById(Long id);

    void deleteById(Long id);

    /**
     * *增量计算核算项的值
     * @param costAccountItem
     */
    void getItemResult(CostAccountItem costAccountItem);
}
