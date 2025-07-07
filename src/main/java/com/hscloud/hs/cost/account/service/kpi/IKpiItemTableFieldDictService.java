package com.hscloud.hs.cost.account.service.kpi;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hscloud.hs.cost.account.model.dto.kpi.KpiItemTableFieldDictDto;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiItemTableFieldDict;

public interface IKpiItemTableFieldDictService extends IService<KpiItemTableFieldDict> {
    Long saveOrUpdate(KpiItemTableFieldDictDto dto);

    IPage<KpiItemTableFieldDict> getPage(KpiItemTableFieldDictDto dto);

    KpiItemTableFieldDict getByCode(String code);
}
