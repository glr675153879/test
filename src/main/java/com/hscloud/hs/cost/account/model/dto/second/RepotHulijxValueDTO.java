package com.hscloud.hs.cost.account.model.dto.second;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "护理绩效")
@HeadRowHeight(20)
@ColumnWidth(30)
public class RepotHulijxValueDTO {

    @Schema(description = "周期")
    @ExcelIgnore
    private String cycle;

    @Schema(description = "核算单元id")
    @ExcelIgnore
    private String deptId;

    @ExcelProperty("核算单元名称")
    private String deptName;

    @ExcelProperty("核算单元id")
    private String ksAmt;

    @ExcelProperty("核算单元名称")
    private String glAmt;

    @ExcelProperty("绩效值")
    private String hszAmt;
}
