package com.hscloud.hs.cost.account.service.kpi;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hscloud.hs.cost.account.model.dto.kpi.KpiAllocationRuleDto;
import com.hscloud.hs.cost.account.model.dto.kpi.KpiAllocationRuleListDto;
import com.hscloud.hs.cost.account.model.dto.kpi.KpiIndexDto;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiAllocationRule;
import com.hscloud.hs.cost.account.model.vo.kpi.KpiAllocationRuleListVO;
import com.hscloud.hs.cost.account.model.vo.kpi.KpiAllocationRuleVO;

/**
* 分摊公式表 服务接口类
*/
public interface IKpiAllocationRuleService extends IService<KpiAllocationRule> {

    void saveOrUpdate(KpiAllocationRuleDto dto);


    IPage<KpiAllocationRuleListVO> getRulePage(KpiAllocationRuleListDto dto);

    void del(Long id);

    KpiAllocationRuleVO info(Long id);
}
