package com.hscloud.hs.cost.account.service.kpi;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hscloud.hs.cost.account.model.dto.kpi.KpiIndexDto;
import com.hscloud.hs.cost.account.model.dto.kpi.KpiIndexEnableDto;
import com.hscloud.hs.cost.account.model.dto.kpi.KpiIndexListDto;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiIndex;
import com.hscloud.hs.cost.account.model.vo.kpi.KpiIndexListVO;
import com.hscloud.hs.cost.account.model.vo.kpi.KpiIndexVO;

import java.util.List;

/**
* 指标表 服务接口类
*/
public interface IKpiIndexService extends IService<KpiIndex> {

    Long saveOrUpdateKpiIndex(KpiIndexDto dto);


    void enable(KpiIndexEnableDto dto);

    List<KpiIndexListVO> list(KpiIndexListDto dto);


    IPage<KpiIndexListVO> getPage(KpiIndexListDto input);

    void del(Long id);


    KpiIndexVO getInfo(Long id);
}
