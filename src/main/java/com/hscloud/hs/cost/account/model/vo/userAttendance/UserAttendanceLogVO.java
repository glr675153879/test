package com.hscloud.hs.cost.account.model.vo.userAttendance;

import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "变更日志导出表")
public class UserAttendanceLogVO {

    @ExcelProperty("操作类型 1：变更")
    private String opsType;

    @ExcelProperty("操作项")
    private String opsItem;

    @ExcelProperty("操作人")
    private String opsBy;

    @ExcelProperty("操作人id")
    private Long opsById;

    @ExcelProperty("操作时间")
    private LocalDateTime opsTime;

    @ExcelProperty("工号")
    private Long jobNumber;

    @ExcelProperty("描述")
    private String description;

}
