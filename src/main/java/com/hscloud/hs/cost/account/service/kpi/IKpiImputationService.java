package com.hscloud.hs.cost.account.service.kpi;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hscloud.hs.cost.account.model.dto.kpi.*;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiImputation;

import java.util.List;

/**
 * 归集表 服务接口类
 */
public interface IKpiImputationService extends IService<KpiImputation> {


    IPage<KpiImputationDeptDto> pageImputationDeptUnit(KpiImputationSearchDto dto);


    Long addRule(KpiImputationAddDto dto);


    List<KpiImputationListDto> listImputation(KpiImputationListSearchDto dto);


    void removeRule(Long id);


    void refresh(String categoryCode, Long input_period, String busiType);

    void refresh(String categoryCode, Long input_period, String busiType,Long tenantId,Long period,boolean forceRefresh);
}
