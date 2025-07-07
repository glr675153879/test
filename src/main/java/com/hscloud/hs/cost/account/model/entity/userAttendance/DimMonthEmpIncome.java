package com.hscloud.hs.cost.account.model.entity.userAttendance;

import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;

@Entity
@Data
@Table(name = "dim_month_emp_income")
public class DimMonthEmpIncome extends Model<DimMonthEmpIncome> {

    private static final long serialVersionUID = 110867904077641026L;
    @Column(name = "dt", nullable = false)
    private Integer dt; // 或者使用LocalDate等更合适的类型

    @Column(name = "dept_id", length = 4, nullable = false, columnDefinition = "VARCHAR(4) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci")
    private String deptId;

    @Column(name = "dept_name", columnDefinition = "MEDIUMTEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci")
    @Lob
    private String deptName;

    @Column(name = "emp_code", length = 6, nullable = true, columnDefinition = "VARCHAR(6) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci")
    private String empCode;

    @Column(name = "emp_name", length = 32, nullable = true, columnDefinition = "VARCHAR(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci")
    private String empName;
}
