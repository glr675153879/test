package com.hscloud.hs.cost.account.service.kpi;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hscloud.hs.cost.account.model.dto.kpi.KpiItemTableFieldDictThirdDto;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiItemTableFieldDictThird;
import com.hscloud.hs.cost.account.model.vo.kpi.KpiItemTableFieldDictThirdVO;

public interface IKpiItemTableFieldDictThirdService extends IService<KpiItemTableFieldDictThird> {

    Long saveOrUpdate(KpiItemTableFieldDictThirdDto dto);

    IPage<KpiItemTableFieldDictThirdVO> getPage(KpiItemTableFieldDictThirdDto dto);
}
