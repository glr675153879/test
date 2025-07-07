package com.hscloud.hs.cost.account.service.kpi;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hscloud.hs.cost.account.model.dto.kpi.KpiItemEquivalentConfigCopyDTO;
import com.hscloud.hs.cost.account.model.dto.kpi.KpiItemEquivalentConfigDTO;
import com.hscloud.hs.cost.account.model.dto.userAttendance.ExcelImportDTO;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiItemEquivalentConfig;
import com.hscloud.hs.cost.account.model.vo.kpi.KpiItemEquivalentConfigVO;
import com.hscloud.hs.cost.account.model.vo.userAttendance.export.ImportErrVo;

import java.util.List;

/**
 * 当量配置 服务接口类
 */
public interface IKpiItemEquivalentConfigService extends IService<KpiItemEquivalentConfig> {
    /**
     * 新增或更新
     *
     * @param dtos 入参
     */
    void saveOrUpdate(List<KpiItemEquivalentConfigDTO> dtos);


    /**
     * 复制当量配置
     *
     * @param dto  入参
     */
    void copy(KpiItemEquivalentConfigCopyDTO dto);

    /**
     * 获取当量配置列表
     *
     * @param dto 入参
     * @return 结果集
     */
    List<KpiItemEquivalentConfigVO> getList(KpiItemEquivalentConfigDTO dto);


    /**
     * 导入
     *
     * @param xlsDataArr    excel数据
     * @param dto
     * @param accountUnitId 科室id
     * @return
     */
    ImportErrVo uploadFile(String[][] xlsDataArr, ExcelImportDTO dto, Long accountUnitId);

    KpiItemEquivalentConfigVO getInfo(Long id);

    IPage<KpiItemEquivalentConfigVO> getPage(KpiItemEquivalentConfigDTO dto);

    void updateInheritFlag(Long id, String inheritFlag);

    void updateSeq(List<KpiItemEquivalentConfigDTO> dtos);
}
