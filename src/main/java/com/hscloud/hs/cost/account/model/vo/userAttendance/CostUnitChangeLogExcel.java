package com.hscloud.hs.cost.account.model.vo.userAttendance;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.gitee.sunchenbin.mybatis.actable.annotation.Column;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author tianbo
 * @version 1.0
 * @date 2024-08-09 16:14
 **/
@Data
public class CostUnitChangeLogExcel {

    @ExcelProperty("操作类型")
    private String operationType;

    @ExcelProperty("操作项")
    private String operateItem;

    @ExcelProperty("操作时间")
    private LocalDateTime operationTime;


    @ExcelProperty("操作人")
    private String operatorName;

    @ExcelProperty("描述")
    private String description;



}
