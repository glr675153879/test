package com.hscloud.hs.cost.account.model.entity.kpi;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.gitee.sunchenbin.mybatis.actable.annotation.Column;
import com.gitee.sunchenbin.mybatis.actable.annotation.Index;
import com.gitee.sunchenbin.mybatis.actable.annotation.IsAutoIncrement;
import com.gitee.sunchenbin.mybatis.actable.constants.MySqlTypeConstant;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.pig4cloud.pigx.common.core.util.TenantTable;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import java.math.BigDecimal;

import java.util.Date;
import java.util.List;
/**
* 人员考勤自定义字段Model
* @author you
* @since 2024-09-13
*/

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TenantTable
@Schema(description = "人员考勤自定义字段表")
@TableName("kpi_user_attendance_custom")
public class KpiUserAttendanceCustom extends Model<KpiUserAttendanceCustom>{

    @TableId(value = "id", type = IdType.AUTO)
    @IsAutoIncrement
    @Column(comment="", type = MySqlTypeConstant.BIGINT , isNull = false )
    private Long id;

    @TableField(value = "period")
    @Column(comment="核算周期", type = MySqlTypeConstant.INT , isNull = false )
    private Long period;

    @TableField(value = "emp_id")
    @Column(comment="员工工号", type = MySqlTypeConstant.VARCHAR, length = 255)
    private String empId;

    @TableField(value = "emp_name")
    @Column(comment="员工姓名", type = MySqlTypeConstant.VARCHAR, length = 255)
    private String empName;

    @TableField(value = "user_attendance_id")
    @Column(comment="人员考勤id", type = MySqlTypeConstant.BIGINT)
    private Long userAttendanceId;

    @TableField(value = "column_id")
    @Column(comment="字段ID", type = MySqlTypeConstant.BIGINT)
    private Long columnId;

    @TableField(value = "name")
    @Column(comment="字段名", type = MySqlTypeConstant.VARCHAR, length = 255)
    private String name;

    @TableField(value = "value")
    @Column(comment="数值",type = MySqlTypeConstant.DECIMAL, length = 10, decimalLength = 1)
    private BigDecimal value;

    @TableField(value = "del_flag")
    @Column(comment="删除标记，1:已删除,0:正常", type = MySqlTypeConstant.CHAR, length = 1  , isNull = false,defaultValue = "0" )
    private String delFlag;

    @TableField(value = "created_id")
    @Column(comment="创建人", type = MySqlTypeConstant.BIGINT)
    private Long createdId;

    @TableField(value = "created_date")
    @Column(comment="创建时间", type = MySqlTypeConstant.DATETIME)
    private Date createdDate;

    @TableField(value = "updated_id")
    @Column(comment="更新人", type = MySqlTypeConstant.BIGINT)
    private Long updatedId;

    @TableField(value = "updated_date")
    @Column(comment="更新时间", type = MySqlTypeConstant.DATETIME)
    private Date updatedDate;

    @TableField(value = "tenant_id")
    @Column(comment="租户号", type = MySqlTypeConstant.BIGINT)
    private Long tenantId;

    @TableField(value = "busi_type")
    @Column(comment = "业务类型，1，一次绩效，2，科室成本", isNull = false, defaultValue = "1", type = MySqlTypeConstant.VARCHAR, length = 20)
    @Index
    private String busiType= "1";

}