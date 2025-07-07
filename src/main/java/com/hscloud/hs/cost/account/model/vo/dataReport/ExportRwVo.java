package com.hscloud.hs.cost.account.model.vo.dataReport;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentStyle;
import com.alibaba.excel.enums.poi.HorizontalAlignmentEnum;
import com.pig4cloud.pigx.common.excel.annotation.ExcelLine;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.persistence.Column;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * RW上报项导出响应
 * 包含：病案号、RW值、姓名、出院科室、住院天数
 * @author banana
 * @create 2024-09-11 17:17
 */

@Data
@Schema(description = "RW上报项导出响应")
public class ExportRwVo {

    @ExcelLine
    @ExcelIgnore
    private Long lineNum;

    /*@ColumnWidth(10)
    @ExcelProperty({"RW上报项", "id"})
    @ContentStyle(horizontalAlignment = HorizontalAlignmentEnum.CENTER)
    *//*@NotNull(message = "id不能为空")*//*
    @Schema(description = "id")
    private Long id;*/

    @ColumnWidth(10)
    @ExcelProperty({"RW上报项", "核算单元id"})
    @ContentStyle(horizontalAlignment = HorizontalAlignmentEnum.CENTER)
    /*@NotNull(message = "id不能为空")*/
    @Schema(description = "核算单元id")
    private String accountId;

    @ColumnWidth(10)
    @ExcelProperty({"RW上报项", "核算单元"})
    @ContentStyle(horizontalAlignment = HorizontalAlignmentEnum.CENTER)
    /*@NotNull(message = "id不能为空")*/
    @Schema(description = "核算单元")
    private String accountUnit;

    @ColumnWidth(10)
    @ExcelProperty({"RW上报项", "病案号"})
    @ContentStyle(horizontalAlignment = HorizontalAlignmentEnum.CENTER)
    @Schema(description = "病案号")
    private String regCode;

    @ColumnWidth(15)
    @ExcelProperty({"RW上报项", "姓名"})
    @Schema(description = "姓名")
    @ContentStyle(horizontalAlignment = HorizontalAlignmentEnum.CENTER)
    private String personName;

    @ColumnWidth(10)
    @ExcelProperty({"RW上报项", "rw值"})
    @Schema(description = "rw值")
    @ContentStyle(horizontalAlignment = HorizontalAlignmentEnum.CENTER)
    private BigDecimal rw;

    @ColumnWidth(25)
    @ExcelProperty({"RW上报项", "出院科室"})
    @Schema(description = "出院科室")
    @ContentStyle(horizontalAlignment = HorizontalAlignmentEnum.CENTER)
    private String outDeptName;

    @ColumnWidth(25)
    @ExcelProperty({"RW上报项", "住院天数"})
    @ContentStyle(horizontalAlignment = HorizontalAlignmentEnum.CENTER)
    @Schema(description = "住院天数")
    private Integer zyts;
}
