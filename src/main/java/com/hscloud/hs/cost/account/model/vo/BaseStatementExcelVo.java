package com.hscloud.hs.cost.account.model.vo;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.pig4cloud.pigx.common.excel.annotation.ExcelLine;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class BaseStatementExcelVo implements Serializable {

    private static final long serialVersionUID = 1L;


    /**
     * 导入时候回显行号
     */
    @ExcelLine
    @ExcelIgnore
    private Long lineNum;

    @ExcelProperty("核算业务周期")
    private String accountTime;

    @ExcelProperty("统计维度")
    private String dimension;

    @ExcelProperty("总核算值")
    private BigDecimal totalCount;

    @ExcelProperty("同比核算值")
    private BigDecimal totalCountYear;

    @ExcelProperty("同比")
    private BigDecimal yearRatio;

    @ExcelProperty("同比涨幅值")
    private BigDecimal yearIncrease;

    @ExcelProperty("环比核算值")
    private BigDecimal totalCountMonth;

    @ExcelProperty("环比")
    private BigDecimal monthRatio;

    @ExcelProperty("环比涨幅值")
    private BigDecimal monthIncrease;
}
