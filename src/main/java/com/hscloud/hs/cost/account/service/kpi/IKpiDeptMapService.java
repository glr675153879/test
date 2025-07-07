package com.hscloud.hs.cost.account.service.kpi;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiDeptMap;

/**
* 科室映射 服务接口类
*/
public interface IKpiDeptMapService extends IService<KpiDeptMap> {

    void cu(KpiDeptMap kpiDeptMap);

    IPage<KpiDeptMap> pageList(Page<KpiDeptMap> page, QueryWrapper<KpiDeptMap> wrapper);

}
