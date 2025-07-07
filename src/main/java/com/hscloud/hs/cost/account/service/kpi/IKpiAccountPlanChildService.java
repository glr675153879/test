package com.hscloud.hs.cost.account.service.kpi;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hscloud.hs.cost.account.model.dto.kpi.KpiAccountPlanChildAddDto;
import com.hscloud.hs.cost.account.model.dto.kpi.KpiAccountPlanChildListDto;
import com.hscloud.hs.cost.account.model.dto.kpi.KpiIndexEnableDto;
import com.hscloud.hs.cost.account.model.dto.kpi.KpiPlanVerifyDto;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiAccountPlanChild;
import com.hscloud.hs.cost.account.model.vo.kpi.KpiAccountPlanChildInfoVO;
import com.hscloud.hs.cost.account.model.vo.kpi.KpiAccountPlanChildListVO;
import com.hscloud.hs.cost.account.model.vo.kpi.KpiPlanConfigVO;

import java.util.List;

/**
* 核算子方案表 服务接口类
*/
public interface IKpiAccountPlanChildService extends IService<KpiAccountPlanChild> {

    List<KpiAccountPlanChildListVO> list(KpiAccountPlanChildListDto input);

    IPage<KpiAccountPlanChildListVO> getPage(KpiAccountPlanChildListDto input);

    void saveOrUpdate(KpiAccountPlanChildAddDto dto);

    KpiAccountPlanChildInfoVO getInfo(Long id);

    void del(Long id);

}
