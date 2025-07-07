package com.hscloud.hs.cost.account.service.kpi;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hscloud.hs.cost.account.model.dto.kpi.KpiItemTableDto;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiItemTable;

/**
 * 核算项基础表 服务接口类
 */
public interface IKpiItemTableService extends IService<KpiItemTable> {
    /**
     * 新增或更新表
     *
     * @param dto 入参
     * @return id
     */
    Long saveOrUpdate(KpiItemTableDto dto);
}
