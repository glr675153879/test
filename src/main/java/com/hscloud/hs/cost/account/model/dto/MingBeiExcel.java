package com.hscloud.hs.cost.account.model.dto;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author tianbo
 * @version 1.0
 * @date 2024-09-13 14:35
 **/
@Data
@Schema(description = "职工绩效分类")
@HeadRowHeight(20)
@ColumnWidth(20)
public class MingBeiExcel {

    @ExcelIgnore
    @Schema(description = "userId")
    private String userId;

    @ExcelProperty("职工姓名")
    private String userName;

    @ExcelProperty("考勤组")
    private String attendanceGroup;

    @ExcelProperty("合计")
    private BigDecimal totalAmt = BigDecimal.ZERO;


}
