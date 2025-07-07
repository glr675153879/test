package com.hscloud.hs.cost.account.model.entity.kpi;

import com.baomidou.mybatisplus.annotation.*;
import com.gitee.sunchenbin.mybatis.actable.annotation.Column;
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
* 核算任务(cost_account_task)Model
* @author you
* @since 2024-09-13
*/

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TenantTable
@Schema(description = "核算任务表(cost_account_task)")
@TableName("kpi_account_task")
public class KpiAccountTask extends Model<KpiAccountTask>{

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    @Column(comment="", type = MySqlTypeConstant.BIGINT , isNull = false )
    private Long id;

    @TableField(value = "account_task_name")
    @Column(comment="核算任务名称", type = MySqlTypeConstant.VARCHAR, length = 255)
    private String accountTaskName;

    @TableField(value = "period")
    @Column(comment="核算周期", type = MySqlTypeConstant.INT , isNull = false )
    private Long period;

    @TableField(value = "plan_code")
    @Column(comment="核算方案", type = MySqlTypeConstant.VARCHAR, length = 255)
    private String planCode;

    @TableField(value = "status")
    @Column(comment="状态", type = MySqlTypeConstant.INT , isNull = false )
    private Long status;

    @TableField(value = "del_flag")
    @Column(comment="是否删除：0：未删除 1：删除", type = MySqlTypeConstant.CHAR, length = 1 )
    private String delFlag;

    @TableField(value = "issued_flag")
    @Column(comment="是否锁定 Y/N", type = MySqlTypeConstant.CHAR, length = 1 )
    private String issuedFlag;

    @TableField(value = "issued_date",updateStrategy =FieldStrategy.IGNORED)
    @Column(comment="锁定时间", type = MySqlTypeConstant.DATETIME)
    private Date issuedDate;

    @TableField(value = "send_flag")
    @Column(comment="是否下发 Y/N", type = MySqlTypeConstant.CHAR, length = 1 )
    private String sendFlag;

    @TableField(value = "send_date")
    @Column(comment="下发时间", type = MySqlTypeConstant.DATETIME)
    private Date sendDate;

    @TableField(value = "send_log")
    @Column(comment="下发错误日志", type = MySqlTypeConstant.TEXT )
    private String sendLog;

    @TableField(value = "task_child_id",updateStrategy =FieldStrategy.IGNORED)
    @Column(comment="最后一次子任务ID", type = MySqlTypeConstant.BIGINT)
    private Long taskChildId;

    @TableField(value = "created_id",fill = FieldFill.INSERT)
    @Column(comment="创建人", type = MySqlTypeConstant.BIGINT)
    private Long createdId;

    @TableField(value = "created_date",fill = FieldFill.INSERT)
    @Column(comment="创建时间", type = MySqlTypeConstant.DATETIME)
    private Date createdDate;

    @TableField(value = "updated_id",fill = FieldFill.INSERT_UPDATE)
    @Column(comment="更新人", type = MySqlTypeConstant.BIGINT)
    private Long updatedId;

    @TableField(value = "updated_date")
    @Column(comment="更新时间", type = MySqlTypeConstant.DATETIME)
    private Date updatedDate;

    @TableField(value = "tenant_id")
    @Column(comment="租户号", type = MySqlTypeConstant.BIGINT)
    private Long tenantId;

    @TableField(value = "index_code")
    @Column(comment="测试指标", type = MySqlTypeConstant.VARCHAR, length = 50 )
    private String indexCode;

    @TableField(value = "test_flag")
    @Column(comment="是否测试 Y/N", type = MySqlTypeConstant.CHAR, length = 1,defaultValue = "N")
    private String testFlag;

    @TableField(value = "report_id")
    @Column(comment="老报表id", type = MySqlTypeConstant.BIGINT)
    private Long reportId;

    @TableField(value = "send_grant_unit_ids")
    @Column(comment = "发放单元id",length = 3000)
    @Schema(description = "发放单元id")
    private String sendGrantUnitIds;

    @TableField(value = "send_grant_unit_names")
    @Column(comment = "发放单元名称",length = 3000)
    @Schema(description = "发放单元名称")
    private String sendGrantUnitNames;
}