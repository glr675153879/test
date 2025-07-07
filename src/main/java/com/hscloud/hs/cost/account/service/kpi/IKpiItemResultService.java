package com.hscloud.hs.cost.account.service.kpi;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hscloud.hs.cost.account.model.dto.kpi.KpiTransferInfoDTO;
import com.hscloud.hs.cost.account.model.dto.kpi.KpiTransferInfoSaveDTO;
import com.hscloud.hs.cost.account.model.dto.kpi.KpiTransferListDTO;
import com.hscloud.hs.cost.account.model.dto.kpi.KpiTransferSaveDTO;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiItem;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiItemResult;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiItemResultRelation;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiUserAttendance;
import com.hscloud.hs.cost.account.model.vo.kpi.KpiTransferInfoVO;
import com.hscloud.hs.cost.account.model.vo.kpi.KpiTransferInfoVO2;
import com.hscloud.hs.cost.account.model.vo.kpi.KpiTransferListVO;

import java.util.List;
import java.util.Map;

/**
* 核算项结果集 服务接口类
 * @author Administrator
 */
public interface IKpiItemResultService extends IService<KpiItemResult> {

    /**
     * 保存计算项结果
     * @param item 核算项
     * @param period 周期
     * @param result 结果
     * @param deleteFlag 是否执行删除
     * @param userMap 周期内的核算人员
     * @param relationMap 周期内的科室匹配关系
     */
    Integer saveItemResult(KpiItem item, Long period, String result, Boolean deleteFlag,Long zhongzhiId,
                        List<KpiUserAttendance> userMap,
                        List<KpiItemResultRelation> relationMap) throws IllegalAccessException;

    /**
     * 转科数据分页列表
     * @param dto 入参
     * @return 分页数据
     */
    IPage<KpiTransferListVO> getTransferPage(KpiTransferListDTO dto);

    /**
     * 转科数据明细列表
     * @param dto 入参
     * @return 转科数据明细列表
     */
    IPage<KpiTransferInfoVO> getTransferInfoPage(KpiTransferInfoDTO dto);

    List<KpiTransferInfoVO2> getTransferList(KpiTransferListDTO dto);

    /**
     * 转科数据保存
     * @param dto 入参
     */
    void transferSave(KpiTransferSaveDTO dto);

    /**
     * 批量保存转科关系数据
     * @param relationList 待插入的关系数据
     */
    void transferBatchSave(List<KpiItemResultRelation> relationList);

    /**
     * 转科数据批量保存
     * @param dto 入参
     */
    void transferSaveV2(KpiTransferSaveDTO dto);

    /**
     * 转科数据一键保存
     * @param dto 入参
     */
    void oneTouchSave(KpiTransferInfoSaveDTO dto);

    void oneTouchSave2(KpiTransferInfoSaveDTO dto);

}
