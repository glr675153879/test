package com.hscloud.hs.cost.account.service.impl.kpi;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bestvike.linq.Linq;
import com.pig4cloud.pigx.common.core.exception.BizException;
import io.debezium.annotation.ReadOnly;
import lombok.RequiredArgsConstructor;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hscloud.hs.cost.account.mapper.kpi.KpiDeptMapMapper;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiDeptMap;
import com.hscloud.hs.cost.account.service.kpi.IKpiDeptMapService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

/**
* 科室映射 服务实现类
*
*/
@Service
@RequiredArgsConstructor
@Slf4j
public class KpiDeptMapService extends ServiceImpl<KpiDeptMapMapper, KpiDeptMap> implements IKpiDeptMapService {

    @Autowired
    private KpiDeptMapMapper kpiDeptMapMapper;
    @Override
    public void cu(KpiDeptMap input) {
        if (input.getToDeptId() == null || input.getFromDeptId() == null
            || input.getBeginDate() == null || input.getEndDate() == null){
            throw new BizException("所有项必填");
        }

        List<KpiDeptMap> list = kpiDeptMapMapper.selectList(
                new QueryWrapper<KpiDeptMap>().eq("FROM_DEPT_ID", input.getFromDeptId())
                        .apply("begin_date like '"+input.getBeginDate().substring(0,6)+"%'")
        );
        boolean over = false;
        if (!list.isEmpty()) {
            if (input.getId() != null){
                list = Linq.of(list).where(x->!x.getId().equals(input.getId())).toList();
            }
            for (KpiDeptMap r : list) {
                if (isOverlapping(r.getBeginDate(), r.getEndDate(), input.getBeginDate(), input.getEndDate())) {
                    over = true;
                }
            }
        }
        if (over){
            throw new BizException("映射前科室设置时间段重复");
        }

        saveOrUpdate(input);
    }

    @Override
    public IPage<KpiDeptMap> pageList(Page<KpiDeptMap> page, QueryWrapper<KpiDeptMap> wrapper) {
        return kpiDeptMapMapper.pageList(page,wrapper);
    }

    private boolean isOverlapping(String startDateA, String endDateA, String startDateB, String endDateB) {
        return Integer.parseInt(startDateA)-Integer.parseInt(endDateB)<=0 && Integer.parseInt(endDateA)-Integer.parseInt(startDateB)>=0;
    }
}
