package com.hscloud.hs.cost.account.service.report;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hscloud.hs.cost.account.model.dto.report.QueryFieldBySqlDto;
import com.hscloud.hs.cost.account.model.entity.report.ReportDb;
import com.hscloud.hs.cost.account.model.vo.report.MetaDataBySqlVo;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 数据集设计表 服务接口类
 */
public interface IReportDbService extends IService<ReportDb> {

    @Transactional(rollbackFor = Exception.class)
    Long createOrEdit(ReportDb entity);

    boolean removeById(Long id);

    ReportDb getByReportId(Long reportId);

    /**
     * 加载数据集数据（关联字段、字段公式、字段关联链接、入参数据）
     *
     * @param reportId
     * @return
     */
    ReportDb loadDbAllDataByReportId(Long reportId);

    /**
     * 加载数据集以及关联字段、入参数据
     *
     * @param id
     * @return
     */
    ReportDb loadDbData(Long id);

    /**
     * 根据sql查询字段
     *
     * @param dto
     * @return
     */
    List<MetaDataBySqlVo> queryFieldBySql(QueryFieldBySqlDto dto);

}
