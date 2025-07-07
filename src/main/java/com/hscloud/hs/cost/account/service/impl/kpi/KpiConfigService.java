package com.hscloud.hs.cost.account.service.impl.kpi;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hscloud.hs.cost.account.constant.enums.YesNoEnum;
import com.hscloud.hs.cost.account.constant.enums.kpi.ItemExtStatusEnum;
import com.hscloud.hs.cost.account.mapper.kpi.KpiConfigMapper;
import com.hscloud.hs.cost.account.mapper.kpi.KpiItemMapper;
import com.hscloud.hs.cost.account.model.dto.PageDto;
import com.hscloud.hs.cost.account.model.dto.kpi.KpiConfigSearchDto;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiConfig;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiItem;
import com.hscloud.hs.cost.account.model.vo.kpi.KpiConfigVO;
import com.hscloud.hs.cost.account.service.kpi.IKpiConfigService;
import com.pig4cloud.pigx.common.core.exception.BizException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * 配置表 服务实现类
 */
@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class KpiConfigService extends ServiceImpl<KpiConfigMapper, KpiConfig> implements IKpiConfigService {
    /**
     * 补充日期
     */
    private static final String START_OF_MONTH = "-01";
    /**
     * 公平锁
     */
    private static final ReentrantLock FAIR_LOCK = new ReentrantLock(true);
    private final KpiItemMapper kpiItemMapper;
    @Value("${kpi.period.month:1}")
    private Integer month;

    @Override
    public KpiConfigVO getConfig(KpiConfigSearchDto dto) {
        LambdaQueryWrapper<KpiConfig> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(KpiConfig::getPeriod, dto.getPeriod());
        KpiConfig config = this.getOne(queryWrapper);
        return KpiConfigVO.changeToVo(config);
    }

    @Override
    public String getLastCycle(Boolean isChangeString) {
        String period;
        LambdaQueryWrapper<KpiConfig> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(KpiConfig::getDefaultFlag, YesNoEnum.YES.getCode());
        KpiConfig kpiConfig = this.getOne(queryWrapper);
        if (null != kpiConfig) {
            period = kpiConfig.getPeriod().toString();
        } else {
            Date var1 = DateUtil.offsetMonth(new Date(), -month);
            period = DateUtil.format(var1, DatePattern.SIMPLE_MONTH_PATTERN);
            saveLastCycle(Long.valueOf(period), true, "1");
        }
        if (isChangeString && StringUtils.hasLength(period)) {
            period = period.substring(0, 4) + "-" + period.substring(4, 6);
        }
        return period;
    }

    @Override
    public KpiConfigVO getLastCycleInfo(KpiConfigSearchDto dto) {
        KpiConfigVO vo = new KpiConfigVO();
        LambdaQueryWrapper<KpiConfig> queryWrapper = new LambdaQueryWrapper<>();

        if (dto.getType().equals("2")) {
            queryWrapper.eq(KpiConfig::getDefaultKsFlag, YesNoEnum.YES.getCode());
        } else {
            queryWrapper.eq(KpiConfig::getDefaultFlag, YesNoEnum.YES.getCode());
        }
        KpiConfig kpiConfig = this.getOne(queryWrapper);
        if (null != kpiConfig) {
            vo = KpiConfigVO.changeToVo(kpiConfig);
        }
        return vo;
    }

    @Override
    public Long saveLastCycle(Long lastCycle, Boolean isDefault, String type) {
        LambdaQueryWrapper<KpiConfig> queryWrapper = Wrappers.<KpiConfig>lambdaQuery()
                .eq(KpiConfig::getPeriod, lastCycle);
        KpiConfig kpiConfig = this.getOne(queryWrapper);
        if (null == kpiConfig) {
            kpiConfig = new KpiConfig();
            kpiConfig.setPeriod(lastCycle);
            kpiConfig.setUserFlag(YesNoEnum.NO.getCode());
            kpiConfig.setUserFlagKs(YesNoEnum.NO.getCode());
            kpiConfig.setUserFlagKs(YesNoEnum.NO.getCode());
            kpiConfig.setIndexFlag("9");
            kpiConfig.setIndexFlagKs("9");
            kpiConfig.setIssuedFlag(YesNoEnum.NO.getCode());
            kpiConfig.setImputationFlag(YesNoEnum.NO.getCode());
            kpiConfig.setDefaultKsFlag(YesNoEnum.NO.getCode());
            this.save(kpiConfig);
        }
        if (isDefault) {
            updateLastCycle(null, null, kpiConfig, type);
        }
        return kpiConfig.getId();
    }

    @Override
    public void updateLastCycle(Long id, Long lastCycle, KpiConfig kpiConfig, String type) {
        if (null != id) {
            kpiConfig = this.getById(id);
        }
        if (null != lastCycle) {
            kpiConfig = this.getOne(Wrappers.<KpiConfig>lambdaQuery()
                    .eq(KpiConfig::getPeriod, lastCycle));
        }
        if (null == kpiConfig) {
            throw new BizException("配置记录不存在");
        }
        FAIR_LOCK.lock();
        try {
            if (type.equals("2")) {
                LambdaUpdateWrapper<KpiConfig> updateWrapper = Wrappers.<KpiConfig>lambdaUpdate()
                        .eq(KpiConfig::getDefaultKsFlag, YesNoEnum.YES.getCode())
                        .set(KpiConfig::getDefaultKsFlag, YesNoEnum.NO.getCode());
                this.update(updateWrapper);
                kpiConfig.setDefaultKsFlag(YesNoEnum.YES.getCode());
                this.updateById(kpiConfig);
            } else {
                LambdaUpdateWrapper<KpiConfig> updateWrapper = Wrappers.<KpiConfig>lambdaUpdate()
                        .eq(KpiConfig::getDefaultFlag, YesNoEnum.YES.getCode())
                        .set(KpiConfig::getDefaultFlag, YesNoEnum.NO.getCode());
                this.update(updateWrapper);
                kpiConfig.setDefaultFlag(YesNoEnum.YES.getCode());
                this.updateById(kpiConfig);
            }
        } finally {
            FAIR_LOCK.unlock();
        }
    }

    @Override
    public void issueCycle(Long lastCycle) {
        Long id = saveLastCycle(lastCycle, false, "1");
        KpiConfig kpiConfig = this.getById(id);
        kpiConfig.setIssuedFlag(YesNoEnum.YES.getCode());
        kpiConfig.setIssuedDate(new Date());
        this.updateById(kpiConfig);

        String period = String.valueOf(lastCycle);
        period = period.substring(0, 4) + "-" + period.substring(4, 6) + START_OF_MONTH;
        Date var1 = DateUtil.parse(period, DatePattern.NORM_DATE_PATTERN);
        var1 = DateUtil.offsetMonth(var1, 1);
        period = DateUtil.format(var1, DatePattern.NORM_DATE_PATTERN);
        lastCycle = Long.valueOf(period.substring(0, 7).replace("-", ""));
        saveLastCycle(lastCycle, true, "1");

        LambdaUpdateWrapper<KpiItem> updateWrapper = Wrappers.<KpiItem>lambdaUpdate()
                .set(KpiItem::getExtStatus, ItemExtStatusEnum.WAIT_EXT.getStatus())
                .set(KpiItem::getExtDate, null)
                .set(KpiItem::getErrorInfo, "");
        kpiItemMapper.update(null, updateWrapper);
    }

    @Override
    public IPage<KpiConfigVO> getPage(PageDto dto, String all_flag) {
        LambdaQueryWrapper<KpiConfig> qw = Wrappers.<KpiConfig>lambdaQuery().orderByDesc(KpiConfig::getPeriod);
        if (!"Y".equals(all_flag)) {
            qw.apply(" a.period not like '%13'");
        }
        return this.getBaseMapper().page2(new Page<>(dto.getCurrent(), dto.getSize()), qw);
    }

    @Override
    public List<KpiConfigVO> getList() {
        List<KpiConfig> list = this.list(Wrappers.<KpiConfig>lambdaQuery().orderByDesc(KpiConfig::getPeriod));
        return list.stream().map(KpiConfigVO::changeToVo).collect(Collectors.toList());
    }

    @Override
    public void editEquivalentPrice(Long period, BigDecimal equivalentPrice) {
        this.update(Wrappers.<KpiConfig>lambdaUpdate()
                .set(KpiConfig::getEquivalentPrice, equivalentPrice)
                .eq(KpiConfig::getPeriod, period));
    }

}
