package com.hscloud.hs.cost.account.service.impl.kpi;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.hscloud.hs.cost.account.utils.kpi.StringChangeUtil;
import lombok.RequiredArgsConstructor;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hscloud.hs.cost.account.mapper.kpi.KpiItemResultRelationMapper;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiItemResultRelation;
import com.hscloud.hs.cost.account.service.kpi.IKpiItemResultRelationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
* 核算项结果匹配关系 服务实现类
*
*/
@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class KpiItemResultRelationService extends ServiceImpl<KpiItemResultRelationMapper, KpiItemResultRelation> implements IKpiItemResultRelationService {

    @Override
    public List<KpiItemResultRelation> getLastMonthRelationList(String period,String code) {
        String var1 = StringChangeUtil.periodChange(period, DatePattern.SIMPLE_MONTH_PATTERN);
        long periodLong = Long.parseLong(var1);
        List<KpiItemResultRelation> relationList = this.list(Wrappers.<KpiItemResultRelation>lambdaQuery()
                .eq(KpiItemResultRelation::getPeriod, periodLong)
                .eq(ObjectUtils.isNotEmpty(code),KpiItemResultRelation::getCode, code)
        );
//        if (CollectionUtils.isEmpty(relationList)) {
//            Date date = DateUtil.parseDate(period + "-01");
//            date = DateUtil.offsetMonth(date, -1);
//            String lastPeriod = DateUtil.format(date, DatePattern.NORM_DATE_PATTERN);
//            long lastPeriodLong = Long.parseLong(lastPeriod.replace("-", "").substring(0, 6));
//            relationList = this.list(Wrappers.<KpiItemResultRelation>lambdaQuery()
//                    .eq(KpiItemResultRelation::getPeriod, lastPeriodLong)
//                    .eq(ObjectUtils.isNotEmpty(code),KpiItemResultRelation::getCode, code)
//            );
//        }
        return relationList;
    }
}
