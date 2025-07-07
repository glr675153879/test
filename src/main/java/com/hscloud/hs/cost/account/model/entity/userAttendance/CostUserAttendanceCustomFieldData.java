package com.hscloud.hs.cost.account.model.entity.userAttendance;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gitee.sunchenbin.mybatis.actable.annotation.Column;
import com.hscloud.hs.cost.account.model.entity.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.persistence.Entity;

/**
 * 人员考勤自定义字段表
 *
 * @JC
 * @since 2024-06-28
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("cost_user_attendance_custom_field_data")
@Entity
@Schema(description = "人员考勤自定义字段表")
public class CostUserAttendanceCustomFieldData extends BaseEntity<CostUserAttendanceCustomFieldData> {

    private static final long serialVersionUID = 2628888972145287644L;
    @Column(comment = "dt")
    @Schema(description = "核算周期")
    private String dt;

    @Schema(description = "员工工号")
    @Column(comment = "emp_id")
    private String empId;

    @Schema(description = "员工姓名")
    @Column(comment = "emp_name")
    private String empName;

    @Schema(description = "人员考勤id")
    @Column(comment = "user_attendance_id")
    private Long userAttendanceId;

    @Schema(description = "字段名")
    @Column(comment = "name")
    private String name;

    @Schema(description = "字段Id")
    @Column(comment = "column_id")
    private String columnId;

    @Schema(description = "数值")
    @Column(comment = "value")
    private String value;

}
