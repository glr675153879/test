package com.hscloud.hs.cost.account.service.kpi;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hscloud.hs.cost.account.model.dto.kpi.*;
import com.hscloud.hs.cost.account.model.entity.kpi.*;
import com.hscloud.hs.cost.account.model.vo.kpi.KpiItemExtVO;
import com.hscloud.hs.cost.account.model.vo.kpi.KpiItemVO;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.concurrent.Future;

/**
 * 核算项(cost_account_item) 服务接口类
 *
 * @author Administrator
 */
public interface IKpiItemService extends IService<KpiItem> {
    /**
     * 获取明细
     *
     * @param id 主键id
     * @return 明细
     */
    KpiItemVO getKpiItem(Long id);

    /**
     * 新增或更新
     *
     * @param dto 入参
     * @return id
     */
    Long saveOrUpdate(KpiItemDTO dto);

    /**
     * 状态切换
     *
     * @param dto 入参
     */
    void switchStatus(BaseIdStatusDTO dto);

    /**
     * 删除核算项
     *
     * @param id 主键id
     */
    void deleteItem(Long id);

    /**
     * 核算项分页列表
     *
     * @param dto 入参
     * @return 结果集
     */
    IPage<KpiItemVO> getPage(@Validated KpiItemQueryDTO dto);

    IPage<KpiItemVO> getPageOld(@Validated KpiItemQueryDTO dto);

    /**
     * 核算项全部列表
     *
     * @param dto 入参
     * @return 结果集
     */
    List<KpiItemVO> getList(@Validated KpiItemQueryDTO dto);

    /**
     * 核算项结果列表
     *
     * @param id     主键id
     * @param period 周期
     * @return 结果集
     */
    String getResultList(Long id, String period);

    /**
     * 核算项重新计算
     *
     * @param id           id
     * @param item         核算项
     * @param period       周期
     * @param deleteFlag   是否执行删除 true:删除，false:不删除
     * @param userList     周期内的核算人员
     * @param relationList 周期内的匹配关系
     */
    void itemCalculate(Long id, KpiItem item, String period, Boolean deleteFlag, Long zhongzhiId,
                       List<KpiUserAttendance> userList, List<KpiItemResultRelation> relationList, List<KpiMember> members, List<KpiAccountUnit> units, String busiType);

    /**
     * 核算项批量计算
     *
     * @param ids            id集合
     * @param busiType       业务类型
     * @param period         周期
     * @param equivalentFlag 是否当量计算
     * @return
     */
    List<Future<Boolean>> itemBatchCalculate(List<Long> ids, String busiType, Long period, String equivalentFlag);

    /**
     * 重置计算相关信息
     *
     * @param ids      id集合
     * @param busiType 业务类型
     */
    void updateExtInfo(List<Long> ids, String busiType);

    /**
     * 查询各类计算数量
     *
     * @param dto 入参
     * @return 结果集
     */
    KpiItemExtVO getItemExtInfo(KpiItemQueryDTO dto);

    /**
     * 保存核算项查询条件
     *
     * @param dto 入参
     */
    void saveCond(KpiItemSaveCondDto dto);

    String getSql(Long id, String period);
}
