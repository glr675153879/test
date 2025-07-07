package com.hscloud.hs.cost.account.model.dto.second;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "职工绩效")
@HeadRowHeight(20)
@ColumnWidth(20)
public class RepotZhigongjxValueDTO {
    @ExcelIgnore
    @Schema(description = "周期")
    private String cycle;

    @ExcelIgnore
    @Schema(description = "userId")
    private String userId;

//    @ExcelIgnore
//    @Schema(description = "工号")
//    private String empCode;

    @ExcelProperty("职工姓名")
    private String userName;

    @ExcelProperty("职务")
    private String postName;

    @ExcelIgnore
    @Schema(description = "核算单元id")
    private String deptId;

    @ExcelProperty("科室单元")
    private String deptName;

    @ExcelProperty("总核算值")
    private BigDecimal totalAmt;

    @ExcelProperty("一次绩效值")
    private String amt;

    @ExcelProperty("二次绩效值")
    private BigDecimal secondAmt;

    @ExcelIgnore
    @Schema(description = "是否编外人员  Y编外")
    private String userType;

//    @ExcelIgnore
//    @Schema(description = "一次管理绩效值 临时字段，用于 绩效接口 amt = amt+glamt")
//    private String glAmt;

}
