package com.hscloud.hs.cost.account.service.kpi;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiServiceItemCategory;

import java.util.List;

/**
 * 医疗服务目录 服务接口类
 */
public interface IKpiServiceItemCategoryService extends IService<KpiServiceItemCategory> {

    void checkAndDelete(Long id);

    List<KpiServiceItemCategory> tree();

    boolean checkAndAdd(KpiServiceItemCategory kpiServiceItemCategory);

    boolean checkAndEdit(KpiServiceItemCategory kpiServiceItemCategory);

    List<KpiServiceItemCategory> cutTree(String selectedItemCodes);
}