package com.hscloud.hs.cost.account.service.kpi;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hscloud.hs.cost.account.model.dto.kpi.KpiAccountPlanAddDto;
import com.hscloud.hs.cost.account.model.dto.kpi.KpiAccountPlanListDto;
import com.hscloud.hs.cost.account.model.dto.kpi.KpiIndexEnableDto;
import com.hscloud.hs.cost.account.model.dto.kpi.KpiPlanVerifyDto;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiAccountPlan;
import com.hscloud.hs.cost.account.model.vo.kpi.KpiAccountPlanListVO;

import java.util.List;

/**
* 核算方案表(COST_ACCOUNT_PLAN) 服务接口类
*/
public interface IKpiAccountPlanService extends IService<KpiAccountPlan> {

    List<KpiAccountPlanListVO> list(KpiAccountPlanListDto input);

    IPage<KpiAccountPlanListVO> getPage(KpiAccountPlanListDto input);

    void saveOrUpdate(KpiAccountPlanAddDto dto);

    void enable(KpiIndexEnableDto dto);

    void del(Long id);

    KpiPlanVerifyDto.MissResult verify(List<Long> ids);
}
