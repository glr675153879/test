package com.hscloud.hs.cost.account.service.kpi;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiItemResultRelation;

import java.util.List;

/**
* 核算项结果匹配关系 服务接口类
*/
public interface IKpiItemResultRelationService extends IService<KpiItemResultRelation> {

    /**
     * 查询周期上一个月的科室匹配关系
     * @param period 周期
     * @param code 核算项code
     * @return 结果
     */
    List<KpiItemResultRelation> getLastMonthRelationList(String period,String code);
}
