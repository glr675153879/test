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
* 核算子方案备份Model
* @author you
* @since 2024-09-13
*/

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TenantTable
@Schema(description = "核算子方案备份")
@TableName("kpi_account_plan_child_copy")
public class KpiAccountPlanChildCopy extends Model<KpiAccountPlanChildCopy>{

    @TableId(value = "id", type = IdType.AUTO)
    @IsAutoIncrement
    @Column(comment="", type = MySqlTypeConstant.BIGINT , isNull = false )
    private Long id;

    @TableField(value = "code")
    @Column(comment="子方案编码", type = MySqlTypeConstant.VARCHAR, length = 255)
    private String code;

    @TableField(value = "plan_code")
    @Column(comment="方案code", type = MySqlTypeConstant.VARCHAR, length = 255)
    private String planCode;

    @TableField(value = "index_code")
    @Column(comment="指标编码", type = MySqlTypeConstant.VARCHAR, length = 255)
    private String indexCode;

    @TableField(value = "object")
    @Column(comment="方案对象 1人员2科室", type = MySqlTypeConstant.CHAR, length = 1 )
    private String object;

    @TableField(value = "emp_id")
    @Column(comment="人员编码", type = MySqlTypeConstant.VARCHAR, length = 255)
    private String empId;

    @TableField(value = "user_id")
    @Column(comment="人员编码", type = MySqlTypeConstant.BIGINT)
    private Long userId;

    @TableField(value = "dept_id")
    @Column(comment="科室编码", type = MySqlTypeConstant.BIGINT)
    private Long deptId;

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

    @TableField(value = "status")
    @Column(comment="0，草稿，1启用，-1停用", type = MySqlTypeConstant.INT , isNull = false )
    private Long status;

    @TableField(value = "tenant_id")
    @Column(comment="租户号", type = MySqlTypeConstant.BIGINT, isNull = false)
    private Long tenantId;

    @TableField(value = "task_child_id")
    @Index
    @Column(comment="子任务id", type = MySqlTypeConstant.BIGINT , isNull = false )
    private Long taskChildId;

    @TableField(value = "copy_date")
    @Column(comment="备份时间", type = MySqlTypeConstant.DATETIME , isNull = false )
    private Date copyDate;

    @TableField(value = "plan_name")
    @Column(comment="子方案编码", type = MySqlTypeConstant.VARCHAR, length = 255)
    private String planName;

    @TableField(value = "del_flag")
    @Column(comment="是否删除：0：未删除 1：删除", type = MySqlTypeConstant.CHAR, length = 1,defaultValue = "0")
    private String delFlag;
}