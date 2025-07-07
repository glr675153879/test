package com.hscloud.hs.cost.account.service.kpi;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hscloud.hs.cost.account.model.dto.kpi.BaseIdStatusDTO;
import com.hscloud.hs.cost.account.model.dto.kpi.KpiItemTableFieldDto;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiItemTableField;
import com.hscloud.hs.cost.account.model.vo.kpi.KpiItemTableFieldVO;
import org.springframework.validation.annotation.Validated;

import java.util.List;

public interface IKpiItemTableFieldService extends IService<KpiItemTableField> {
    /**
     * 根据表id查询字段
     *
     * @param tableId 表id
     * @return 字段列表
     */
    List<KpiItemTableFieldVO> getListByTableId(Long tableId);

    /**
     * 状态切换
     *
     * @param dto 入参
     */
    void switchStatus(BaseIdStatusDTO dto);

    /**
     * 保存表字段
     *
     * @param tableId 表id
     * @return 表字段列表
     */
    List<KpiItemTableField> saveFields(Long tableId);

    /**
     * 表字段列表分页列表
     *
     * @param dto 入参
     * @return 结果集
     */
    IPage<KpiItemTableFieldVO> getPage(@Validated KpiItemTableFieldDto dto);

    void updateDictCodeById(Long id, String dictCode);

    Long saveOrUpdate(KpiItemTableFieldDto dto);

    List<KpiItemTableFieldVO> getListByTableIds(List<Long> tableIdList);
}
