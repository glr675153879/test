package com.hscloud.hs.cost.account.model.vo.dataReport;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.pig4cloud.pigx.common.excel.annotation.ExcelLine;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "上报项变更日志Excel")
public class CostReportItemLogExcelExportVO {

    /**
     * 导入时候回显行号
     */
    @ExcelLine
    @ExcelIgnore
    private Long lineNum;

    @ExcelProperty("应用名称")
    private String name;

    @ExcelProperty("操作类型")
    private String opsType;

    @ExcelProperty("操作项")
    private String opsItem;

    @ExcelProperty("操作人")
    private String opsBy;

    @ExcelProperty("操作人id")
    private Long opsById;

    @ExcelProperty("操作时间")
    private LocalDateTime opsTime;

    @ExcelProperty("工号")
    private String jobNumber;

    @ExcelProperty("描述")
    private String description;
}
