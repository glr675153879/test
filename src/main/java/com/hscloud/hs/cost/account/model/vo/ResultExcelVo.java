package com.hscloud.hs.cost.account.model.vo;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.pig4cloud.pigx.common.excel.annotation.ExcelLine;
import lombok.Data;

import java.io.Serializable;

@Data
public class ResultExcelVo extends BaseStatementExcelVo implements Serializable {

    private static final long serialVersionUID = 1L;


    /**
     * 导入时候回显行号
     */
    @ExcelLine
    @ExcelIgnore
    private Long lineNum;

    @ExcelProperty("任务名称")
    private String accountTaskName;

}
