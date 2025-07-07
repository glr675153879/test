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
* 人员考勤备份Model
* @author you
* @since 2024-09-13
*/

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TenantTable
@Schema(description = "人员考勤备份")
@TableName("kpi_user_attendance_copy")
public class KpiUserAttendanceCopy extends Model<KpiUserAttendanceCopy>{

    @TableId(value = "id", type = IdType.AUTO)
    @IsAutoIncrement
    @Column(comment="", type = MySqlTypeConstant.BIGINT , isNull = false )
    private Long id;

    @TableField(value = "period")
    @Column(comment="周期", type = MySqlTypeConstant.INT , isNull = false )
    private Long period;

    @TableField(value = "emp_id")
    @Column(comment="员工工号", type = MySqlTypeConstant.VARCHAR, length = 255)
    private String empId;

    @TableField(value = "emp_name")
    @Column(comment="员工姓名", type = MySqlTypeConstant.VARCHAR, length = 32 , isNull = false )
    private String empName;

    @TableField(value = "attendance_group")
    @Column(comment="考勤组", type = MySqlTypeConstant.VARCHAR, length = 255)
    private String attendanceGroup;

    @TableField(value = "user_type")
    @Column(comment="人员类型", type = MySqlTypeConstant.VARCHAR, length = 255)
    private String userType;

    @TableField(value = "user_type_code")
    @Column(comment = "人员类型Code", type = MySqlTypeConstant.VARCHAR, length = 255)
    private String userTypeCode;

    @TableField(value = "duties_origin")
    @Column(comment = "职务关联", type = MySqlTypeConstant.VARCHAR, length = 255)
    private String dutiesOrigin;

    @TableField(value = "duties_code")
    @Column(comment = "职务code", type = MySqlTypeConstant.VARCHAR, length = 255)
    private String dutiesCode;

    @TableField(value = "titles_code")
    @Column(comment = "职称code", type = MySqlTypeConstant.VARCHAR, length = 255)
    private String titlesCode;

    @TableField(value = "duties_name")
    @Column(comment="职务", type = MySqlTypeConstant.VARCHAR, length = 255)
    private String dutiesName;

    @TableField(value = "account_group")
    @Column(comment="核算组别", type = MySqlTypeConstant.VARCHAR, length = 255)
    private String accountGroup;

    @TableField(value = "titles")
    @Column(comment="职称", type = MySqlTypeConstant.VARCHAR, length = 255)
    private String titles;

    @TableField(value = "account_unit")
    @Column(comment="科室单元", type = MySqlTypeConstant.BIGINT)
    private Long accountUnit;

    @TableField(value = "attend_count")
    @Column(comment="出勤次数", type = MySqlTypeConstant.INT)
    private Long attendCount;

    @TableField(value = "attend_rate")
    @Column(comment="出勤系数",type = MySqlTypeConstant.DECIMAL, length = 24, decimalLength = 6)
    private BigDecimal attendRate;

    @TableField(value = "registered_rate")
    @Column(comment="在册系数",type = MySqlTypeConstant.DECIMAL, length = 24, decimalLength = 6)
    private BigDecimal registeredRate;

    @TableField(value = "job_nature")
    @Column(comment="工作性质", type = MySqlTypeConstant.VARCHAR, length = 255)
    private String jobNature;

    @TableField(value = "attend_days")
    @Column(comment="实际出勤天数",type = MySqlTypeConstant.DECIMAL, length = 5, decimalLength = 1)
    private BigDecimal attendDays;

    @TableField(value = "post")
    @Column(comment="岗位", type = MySqlTypeConstant.VARCHAR, length = 255)
    private String post;

    @TableField(value = "reward")
    @Column(comment="是否拿奖金 0-否 1-是", type = MySqlTypeConstant.CHAR, length = 1 )
    private String reward;

    @TableField(value = "reward_index")
    @Column(comment="奖金系数",type = MySqlTypeConstant.DECIMAL, length = 24, decimalLength = 6)
    private BigDecimal rewardIndex;

    @TableField(value = "no_reward_reason")
    @Column(comment="不拿奖金原因", type = MySqlTypeConstant.VARCHAR, length = 255)
    private String noRewardReason;

    @TableField(value = "attendance_group_days")
    @Column(comment="当前考勤组所在天数",type = MySqlTypeConstant.DECIMAL, length = 16, decimalLength = 6)
    private BigDecimal attendanceGroupDays;

    @TableField(value = "one_kpi_attend_days")
    @Column(comment="一次性绩效出勤天数",type = MySqlTypeConstant.DECIMAL, length = 16, decimalLength = 6)
    private BigDecimal oneKpiAttendDays;

    @TableField(value = "one_kpi_attend_rate")
    @Column(comment="一次性绩效出勤系数",type = MySqlTypeConstant.DECIMAL, length = 16, decimalLength = 6)
    private BigDecimal oneKpiAttendRate;

    @TableField(value = "is_locked")
    @Column(comment="锁定标记 1:锁定0:未锁定", type = MySqlTypeConstant.CHAR, length = 1 )
    private String isLocked;

    @TableField(value = "custom_fields")
    @Column(comment="自定义字段", type = MySqlTypeConstant.TEXT )
    private String customFields;

    @TableField(value = "origin_custom_fields")
    @Column(comment="原始自定义字段", type = MySqlTypeConstant.TEXT )
    private String originCustomFields;

    @TableField(value = "is_edited")
    @Column(comment="编辑标记 1:已编辑0:未编辑", type = MySqlTypeConstant.CHAR, length = 1 )
    private String isEdited;

    @TableField(value = "del_flag")
    @Column(comment="删除标记，1:已删除,0:正常,2:人为删除", type = MySqlTypeConstant.CHAR, length = 1  , isNull = false )
    private String delFlag;

    @TableField(value = "tenant_id")
    @Column(comment="租户ID", type = MySqlTypeConstant.BIGINT)
    private Long tenantId;

    @TableField(value = "created_time")
    @Column(comment="创建时间", type = MySqlTypeConstant.DATETIME)
    private Date createdTime;

    @TableField(value = "created_by")
    @Column(comment="创建人", type = MySqlTypeConstant.BIGINT)
    private Long createdBy;

    @TableField(value = "update_time")
    @Column(comment="修改时间", type = MySqlTypeConstant.DATETIME)
    private Date updateTime;

    @TableField(value = "update_by")
    @Column(comment="修改人", type = MySqlTypeConstant.BIGINT)
    private Long updateBy;

    @TableField(value = "treat_room_days")
    @Column(comment="中治室", type = MySqlTypeConstant.VARCHAR, length = 255)
    private String treatRoomDays;

    @TableField(value = "dept_code")
    @Column(comment="科室编码", type = MySqlTypeConstant.VARCHAR, length = 255)
    private String deptCode;

    @TableField(value = "dept_name")
    @Column(comment="科室名称", type = MySqlTypeConstant.VARCHAR, length = 255)
    private String deptName;

    @TableField(value = "source_type")
    @Column(comment="数据来源", type = MySqlTypeConstant.CHAR, length = 1 )
    private String sourceType;

    @TableField(value = "task_child_id")
    @Column(comment="子任务id", type = MySqlTypeConstant.BIGINT , isNull = false )
    private Long taskChildId;

    @TableField(value = "copy_date")
    @Column(comment="备份时间", type = MySqlTypeConstant.DATETIME , isNull = false )
    private Date copyDate;

    @TableField(value = "user_id")
    @Column(comment="用户id", type = MySqlTypeConstant.BIGINT)
    private Long userId;

    @TableField(value = "busi_type")
    @Column(comment = "业务类型，1，一次绩效，2，科室成本", isNull = false, defaultValue = "1", type = MySqlTypeConstant.VARCHAR, length = 20)
    @Index
    private String busiType= "1";

    @TableField(value = "account_unit_name")
    @Column(comment = "科室单元名称 留档", type = MySqlTypeConstant.VARCHAR, length = 100)
    private String accountUnitName;
}