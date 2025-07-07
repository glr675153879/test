package com.hscloud.hs.cost.account.model.vo.userAttendance;

import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 人员考勤导出表
 *
 * @JC
 * @since 2024-05-29
 */
@Data
@Schema(description = "人员考勤导出表")
public class CostUserAttendanceVO {

    @ExcelProperty("核算周期")
    private String dt;

    @ExcelProperty("工号")
    private String empId;

    @ExcelProperty("员工姓名")
    private String empName;

    @ExcelProperty("考勤组")
    private String attendanceGroup;

    @ExcelProperty("人员类型")
    private String userType;

    @ExcelProperty("职务")
    private String dutiesName;

    @ExcelProperty("核算组别")
    private String accountGroup;

    @ExcelProperty("职称")
    private String titles;

    @ExcelProperty("科室单元")
    private String accountUnit;

    @ExcelProperty("出勤系数")
    private BigDecimal attendRate;

    @ExcelProperty("在册系数")
    private BigDecimal registeredRate;

    @ExcelProperty("工作性质")
    private String jobNature;

    @ExcelProperty("出勤天数")
    private BigDecimal attendDays;

    @ExcelProperty("岗位")
    private String post;

    @ExcelProperty("是否拿奖金")
    private String reward;

    @ExcelProperty("奖金系数")
    private BigDecimal rewardIndex;

    @ExcelProperty("不拿奖金原因")
    private String noRewardReason;

    @ExcelProperty("当前考勤组所在天数")
    private BigDecimal attendanceGroupDays;

    @ExcelProperty("一次性绩效出勤天数")
    private BigDecimal oneKpiAttendDays;

}
