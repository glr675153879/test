package com.hscloud.hs.cost.account.model.dto.kpi.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@HeadRowHeight(value = 20)
@ColumnWidth(value = 25)
public class ExcelKpiCalculateDTO {
    @ExcelProperty(index = 0)
    private String unit;
    @ExcelProperty(index = 1)
    private BigDecimal value;
}
