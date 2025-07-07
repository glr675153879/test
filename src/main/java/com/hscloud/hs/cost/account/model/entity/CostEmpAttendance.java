package com.hscloud.hs.cost.account.model.entity;

import java.math.BigDecimal;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;

import java.io.Serializable;

import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 人员考勤表
 * </p>
 *
 * @author
 * @since 2023-12-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("cost_emp_attendance")
@Schema(description = "人员考勤表")
public class CostEmpAttendance extends Model<CostEmpAttendance> {

    private static final long serialVersionUID = 1L;

    @Schema(description = "时间id-年月")
    private String dt;

    @Schema(description = "系统用户id")
    private Long empId;

    @Schema(description = "员工工号")
    private String empCode;

    @Schema(description = "员工姓名")
    private String empName;

    @Schema(description = "核算单元名称")
    private String accountUnitName;

    @Schema(description = "考勤科室名称")
    private String attendDeptName;

    @Schema(description = "出勤天数")
    private BigDecimal attendDays;

    @Schema(description = "出勤天数整理")
    private BigDecimal attendDaysArrange;

    @Schema(description = "出勤系数")
    private BigDecimal attendRate;

    @Schema(description = "在册系数")
    private BigDecimal registeredRate;


}
