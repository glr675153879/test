package com.hscloud.hs.cost.account.model.vo;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.pig4cloud.pigx.common.excel.annotation.ExcelLine;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "我的上报Excel")
public class ReportDetailExcelVo implements Serializable {


    private static final long serialVersionUID = 1L;

    /**
     * 导入时候回显行号
     */
    @ExcelLine
    @ExcelIgnore
    private Long lineNum;

    @ExcelProperty("任务名称")
    private String taskName;

    @ExcelProperty("驳回原因")
    private String rejCause;

    @ExcelProperty("上报项名称")
    private String itemName;

    @ExcelProperty("上报值")
    private Double value;

    @ExcelProperty("上报负责人")
    private String respBy;

    @ExcelProperty("上报维度")
    private String dimension;

    @ExcelProperty("上报项说明")
    private String description;

    @ExcelProperty("类型")
    private String type;

}
