package com.hscloud.hs.cost.account.service.dataReport;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hscloud.hs.cost.account.model.entity.dataReport.CostClusterUnit;

/**
* 归集单元 服务接口类
*/
public interface ICostClusterUnitService extends IService<CostClusterUnit> {

    Boolean activate(CostClusterUnit costClusterUnit);

    Boolean initiate();

    Boolean saveData(CostClusterUnit costClusterUnit);

    Boolean editData(CostClusterUnit costClusterUnit);

    // Boolean setClusterUnit(Long unitId);

    Boolean del(Long id);

    IPage<CostClusterUnit> pageClusterUnit(Page<CostClusterUnit> page, QueryWrapper<CostClusterUnit> wrapper);
}
