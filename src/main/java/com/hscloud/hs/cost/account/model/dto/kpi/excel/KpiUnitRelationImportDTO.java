package com.hscloud.hs.cost.account.model.dto.kpi.excel;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * @author Administrator
 */
@Data
public class KpiUnitRelationImportDTO {
    @ExcelProperty(index = 0)
    private String docAccountName;

    @ExcelProperty(index = 1)
    private String nurseAccountName;

    @ExcelIgnore
    private String errorMessage;
}
