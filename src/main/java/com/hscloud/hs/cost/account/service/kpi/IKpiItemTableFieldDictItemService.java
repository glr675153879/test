package com.hscloud.hs.cost.account.service.kpi;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hscloud.hs.cost.account.model.dto.kpi.BaseIdStatusDTO;
import com.hscloud.hs.cost.account.model.dto.kpi.KpiItemTableFieldDictItemDto;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiItemTableFieldDictItem;

import java.util.List;

public interface IKpiItemTableFieldDictItemService extends IService<KpiItemTableFieldDictItem> {
    Long saveOrUpdate(KpiItemTableFieldDictItemDto dto);

    IPage<KpiItemTableFieldDictItem> getPage(KpiItemTableFieldDictItemDto dto);

    List<KpiItemTableFieldDictItem> getByDictCode(String dictCode);

    KpiItemTableFieldDictItem getByItemCode(String itemCode);

    void switchStatus(BaseIdStatusDTO dto);
}
