package com.hscloud.hs.cost.account.service.kpi;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hscloud.hs.cost.account.model.dto.PageDto;
import com.hscloud.hs.cost.account.model.dto.kpi.KpiConfigSearchDto;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiConfig;
import com.hscloud.hs.cost.account.model.vo.kpi.KpiConfigVO;

import java.math.BigDecimal;
import java.util.List;

/**
* 配置表 服务接口类
 * @author Administrator
 */
public interface IKpiConfigService extends IService<KpiConfig> {
    /**
     * 获取详情
     * @param id id
     * @return 详情
     */
    KpiConfigVO getConfig(KpiConfigSearchDto dto);
    /**
     * 获取最新的周期
     *
     * @param isChangeString 是否转换成字符串 Y/N, 默认为false(202409)，如果为true(2024-09)
     * @return 周期
     */
    String getLastCycle(Boolean isChangeString);

    /**
     * 获取最新的周期记录
     * @return 周期记录
     */
    KpiConfigVO getLastCycleInfo(KpiConfigSearchDto dto);

    /**
     * 生成最新周期记录
     * @param lastCycle 最新周期
     * @param isDefault 是否为默认周期
     * @return 配置表id
     */
    Long saveLastCycle(Long lastCycle,Boolean isDefault,String type);

    /**
     * 设置当前在用默认周期 加锁确保只有一个
     * @param id 配置表id
     * @param lastCycle 周期
     * @param kpiConfig 配置记录
     */
    void updateLastCycle(Long id,Long lastCycle,KpiConfig kpiConfig,String type);

    /**
     * 下发周期
     * @param lastCycle 下发周期
     */
    void issueCycle(Long lastCycle);

    /**
     * 分页
     * @param dto 入参
     * @return 分页
     */
    IPage<KpiConfigVO> getPage(PageDto dto,String all_flag);

    /**
     * 列表
     * @return 数组
     */
    List<KpiConfigVO> getList();

    void editEquivalentPrice(Long period, BigDecimal equivalentPrice);
}
