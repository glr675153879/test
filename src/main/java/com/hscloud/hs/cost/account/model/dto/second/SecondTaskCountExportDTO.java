package com.hscloud.hs.cost.account.model.dto.second;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "总任务分配结果")
@HeadRowHeight(20)
@ColumnWidth(30)
public class SecondTaskCountExportDTO {

    @ExcelProperty("姓名")
    private String empName;

    @ExcelProperty("工号")
    private String empCode;

    @ExcelProperty("发放单元")
    private String grantUnitNames;

    @ExcelProperty("二次分配绩效")
    private String amt;


}
