package com.hscloud.hs.cost.account.model.vo;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;

import com.pig4cloud.pigx.common.excel.annotation.ExcelLine;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class ReportTaskExcelVo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 导入时候回显行号
     */
    @ExcelLine
    @ExcelIgnore
    private Long lineNum;

    @ExcelProperty("上报任务id")
    private Long id;

    @ExcelProperty("任务名称")
    private String taskName;

    @ExcelProperty("上报频率")
    private String frequency;

//    @ExcelProperty(value = "开始时间",converter = DateConverter.class)
    @ExcelProperty("开始时间")
    private LocalDateTime startTime;

//    @ExcelProperty(value = "结束时间",converter = DateConverter.class)
    @ExcelProperty("结束时间")
    private LocalDateTime endTime;

    @ExcelProperty("上报周期")
    private String period;
}
