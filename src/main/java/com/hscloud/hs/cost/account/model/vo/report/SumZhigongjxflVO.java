package com.hscloud.hs.cost.account.model.vo.report;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author tianbo
 * @version 1.0
 * @date 2024-12-10 16:38
 **/
@Data
public class SumZhigongjxflVO {

    @Schema(description = "管理绩效")
    private BigDecimal guanlijx;

    @Schema(description = "门诊绩效")
    private BigDecimal menzhenjx;

    @Schema(description = "二次分配总金额")
    private BigDecimal secondAmt;

    @Schema(description = "科室绩效")
    private BigDecimal secondAmtWithoutYz;

    @Schema(description = "鄞州门诊")
    private BigDecimal secondYzAmt;

    @Schema(description = "明湖院区")
    private BigDecimal secondYhAmt;

    @Schema(description = "合计")
    private BigDecimal totalAmt = BigDecimal.ZERO;
}
