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
* 核算任务Model
* @author you
* @since 2024-09-13
*/

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TenantTable
@Schema(description = "核算任务表")
@TableName("kpi_account_task_child")
public class KpiAccountTaskChild extends Model<KpiAccountTaskChild>{

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    @Column(comment="", type = MySqlTypeConstant.BIGINT , isNull = false )
    private Long id;

    @TableField(value = "period")
    @Column(comment="周期", type = MySqlTypeConstant.INT , isNull = false )
    private Long period;

    @TableField(value = "task_id")
    @Column(comment="主任务id", type = MySqlTypeConstant.BIGINT)
    private Long taskId;

    @TableField(value = "issued_flag")
    @Column(comment="是否下发 Y/N", type = MySqlTypeConstant.CHAR, length = 1 )
    private String issuedFlag;

    @TableField(value = "issued_date",updateStrategy =FieldStrategy.IGNORED)
    @Column(comment="下发时间", type = MySqlTypeConstant.DATETIME)
    private Date issuedDate;

    @TableField(value = "status")
    @Column(comment="状态 0未开始 1指标项抽取，2抽取完成，3计算指标，分摊,4，绑定方案，99完成，", type = MySqlTypeConstant.INT)
    private Long status;

    @TableField(value = "status_name")
    @Column(comment="状态名称", type = MySqlTypeConstant.VARCHAR,length = 50)
    private String statusName;

    @TableField(value = "erro")
    @Column(comment="是否错误 1是0否", type = MySqlTypeConstant.CHAR, length = 1 )
    private String erro;

    @TableField(value = "run_log",updateStrategy = FieldStrategy.NEVER)
    @Column(comment="运行日志", type = MySqlTypeConstant.MEDIUMTEXT )
    private String runLog;

    @TableField(value = "ero_log",updateStrategy = FieldStrategy.NEVER)
    @Column(comment="错误日志", type = MySqlTypeConstant.TEXT )
    private String eroLog;

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


}