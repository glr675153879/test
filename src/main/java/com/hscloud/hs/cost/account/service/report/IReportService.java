package com.hscloud.hs.cost.account.service.report;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hscloud.hs.cost.account.model.dto.report.FieldUseFormulaDto;
import com.hscloud.hs.cost.account.model.dto.report.ReportCellDataDto;
import com.hscloud.hs.cost.account.model.dto.report.ReportDataDto;
import com.hscloud.hs.cost.account.model.dto.report.RowConvert2SonParamsDto;
import com.hscloud.hs.cost.account.model.entity.report.Report;
import com.hscloud.hs.cost.account.model.entity.report.ReportFieldFormula;
import com.hscloud.hs.cost.account.model.vo.report.ParamVo;
import com.hscloud.hs.cost.account.model.vo.report.ReportCellDataVo;
import com.hscloud.hs.cost.account.model.vo.report.ReportTableDataVo;
import com.hscloud.hs.cost.account.model.vo.report.SonReportParamVo;

import java.util.List;
import java.util.Map;

/**
 * 报表设计表 服务接口类
 */
public interface IReportService extends IService<Report> {

    /**
     * 报告数据
     *
     * @param dto DTO
     * @return {@link ReportTableDataVo}
     */
    ReportTableDataVo reportData(ReportDataDto dto);

    Report getByReportCode(String reportCode);

    /**
     * 报表当前行字段使用公式
     *
     * @param dto DTO
     * @return {@link ReportFieldFormula}
     */
    ReportFieldFormula fieldUseFormula(FieldUseFormulaDto dto);

    /**
     * 行 convert2 son 参数
     *
     * @param dto DTO
     * @return {@link List}<{@link ParamVo}>
     */
    SonReportParamVo rowConvert2SonParams(RowConvert2SonParamsDto dto);

//    /**
//     * 获取报告条件
//     *
//     * @param id 同上
//     * @return {@link List}<{@link ReportConditionVo}>
//     */
//    List<ReportConditionVo> reportCondition(Long id);

    Boolean createOrEdit(Report report);

    ReportCellDataVo cellData(ReportCellDataDto dto);

    Map<String, ReportCellDataVo> cellDataList(ReportCellDataDto dto);
}
