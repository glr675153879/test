package com.hscloud.hs.cost.account.service.impl.imputation;

import cn.hutool.extra.template.AbstractTemplate;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hscloud.hs.cost.account.constant.enums.imputation.ImputationType;
import com.hscloud.hs.cost.account.model.entity.imputation.ImputationBaseEntity;
import com.hscloud.hs.cost.account.model.entity.second.Attendance;
import com.hscloud.hs.cost.account.model.vo.imputation.HistoryVO;
import com.hscloud.hs.cost.account.service.second.IAttendanceService;
import com.pig4cloud.pigx.common.core.exception.BizException;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.hscloud.hs.cost.account.constant.enums.imputation.ImputationType;
import lombok.RequiredArgsConstructor;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hscloud.hs.cost.account.mapper.imputation.ImputationMapper;
import com.hscloud.hs.cost.account.model.entity.imputation.Imputation;
import com.hscloud.hs.cost.account.service.imputation.IImputationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 归集主档 服务实现类
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ImputationService extends ServiceImpl<ImputationMapper, Imputation> implements IImputationService {

    private final IAttendanceService attendanceService;

    /**
     * 生成主档
     *
     * @return 最新月份 cycle
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public String generate() {
        Imputation imputation = this.getOne(Wrappers.<Imputation>lambdaQuery().orderByDesc(Imputation::getImputationCycle).last("limit 1"));
        Attendance attendance = attendanceService.getOne(Wrappers.<Attendance>lambdaQuery().orderByDesc(Attendance::getCycle).last("limit 1"));

        if (ObjectUtils.isNull(attendance)) {
            if (ObjectUtils.isNotNull(imputation)) {
                return imputation.getImputationCycle();
            } else {
                log.error("考勤表和主档表无数据！");
                throw new BizException("考勤表和主档表暂无数据，请维护考勤表数据");
            }
        }

        String cycle = attendance.getCycle();
        if (ObjectUtils.isNotNull(imputation) && imputation.getImputationCycle().compareTo(cycle) >= 0) {
            log.info("主档表最新月份为：{}， 考勤表最新月份为：{}", imputation.getImputationCycle(), cycle);
            return imputation.getImputationCycle();
        }

        //生成最新月份主档
        List<Imputation> imputations = new ArrayList<>();
        ImputationType[] imputationTypes = ImputationType.values();
        for (ImputationType imputationType : imputationTypes) {
            Imputation imp = new Imputation();
            imp.setImputationCycle(cycle);
            imp.setImputationName(imputationType.getName());
            imp.setImputationCode(imputationType.toString());
            imp.setKeyType(imputationType.getType());
            imp.setTenantId(attendance.getTenantId());
            imputations.add(imp);
        }
        this.saveBatch(imputations);

        return cycle;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean lock(String cycle) {
        log.info("锁定{}月的数据", cycle);
        List<Imputation> imputations = listImputationsByCycle(cycle);
        fillImputations(imputations, cycle, "1");
        return this.updateBatchById(imputations);
    }

    @Override
    public Imputation getByType(String currentCycle, ImputationType imputationType) {
        return this.getOne(Wrappers.<Imputation>lambdaQuery()
                .eq(Imputation::getImputationCycle, currentCycle)
                .eq(Imputation::getImputationCode, imputationType.toString()));
    }

    @Override
    public IPage<HistoryVO> pageHistory(Page<Imputation> page) {
        Page<Imputation> imputationPage = page(page, Wrappers.<Imputation>query().select("DISTINCT(imputation_cycle)"));
        return imputationPage.convert(HistoryVO::build);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean unlock(String cycle) {
        log.info("解锁{}月的数据", cycle);
        List<Imputation> imputations = listImputationsByCycle(cycle);
        fillImputations(imputations, cycle, "0");
        return this.updateBatchById(imputations);
    }

    private List<Imputation> listImputationsByCycle(String cycle) {
        return this.list(Wrappers.<Imputation>lambdaQuery().eq(Imputation::getImputationCycle, cycle));
    }

    private void fillImputations(List<Imputation> imputations, String cycle, String lockFlag) {
        if (CollectionUtils.isEmpty(imputations)) {
            log.error("月份为{}的主档数据不存在", cycle);
            throw new BizException("主档数据不存在！");
        }

        for (Imputation imputation : imputations) {
            imputation.setLockFlag(lockFlag);
        }
    }

    @Override
    public  <T extends ImputationBaseEntity<T>> void setImputation(T entity, Long imputationId){
        Imputation imputation = this.getById(imputationId);
        entity.setImputationCode(imputation.getImputationCode());
        entity.setImputationName(imputation.getImputationName());
        entity.setImputationCycle(imputation.getImputationCycle());
    }
}
