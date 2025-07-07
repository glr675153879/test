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

import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TenantTable
@Schema(description = "当量核验任务")
@TableName("kpi_item_equivalent_task")
public class KpiItemEquivalentTask extends Model<KpiItemEquivalentTask> {
    @IsAutoIncrement
    @TableId(value = "id", type = IdType.AUTO)
    @Column(comment = "", type = MySqlTypeConstant.BIGINT)
    private Long id;

    @TableField(value = "period")
    @Column(comment = "周期", type = MySqlTypeConstant.INT)
    private Long period;

    @TableField(value = "item_id")
    @Column(comment = "核算项id", type = MySqlTypeConstant.BIGINT)
    private Long itemId;

    @TableField(value = "code")
    @Column(comment = "code用56转，带上前缀，核算项X_，分摊指标F_，核算指标Z_", type = MySqlTypeConstant.VARCHAR)
    private String code;

    @TableField(value = "account_unit_id")
    @Column(comment = "科室id", type = MySqlTypeConstant.BIGINT)
    private Long accountUnitId;

    @TableField(value = "status")
    @Column(comment = "-1:驳回 0:未提交 10:待审核 20:通过", type = MySqlTypeConstant.CHAR, isNull = false)
    private String status;

    @TableField(value = "committed_date")
    @Column(comment = "提交时间", type = MySqlTypeConstant.DATETIME)
    private Date committedDate;

    @TableField(value = "reason")
    @Column(comment = "驳回原因", type = MySqlTypeConstant.VARCHAR)
    private String reason;

    @TableField(value = "auto_issue")
    @Column(comment = "自动重新下发 0-否 1-是", type = MySqlTypeConstant.CHAR, length = 1, defaultValue = "0")
    private String autoIssue;

    @TableField(value = "created_id", fill = FieldFill.INSERT)
    @Column(comment = "创建人", type = MySqlTypeConstant.BIGINT)
    private Long createdId;

    @TableField(value = "created_date", fill = FieldFill.INSERT)
    @Column(comment = "创建时间", type = MySqlTypeConstant.DATETIME)
    private Date createdDate;

    @TableField(value = "updated_id", fill = FieldFill.INSERT_UPDATE)
    @Column(comment = "更新人", type = MySqlTypeConstant.BIGINT)
    private Long updatedId;

    @TableField(value = "updated_date", fill = FieldFill.INSERT_UPDATE)
    @Column(comment = "更新时间", type = MySqlTypeConstant.DATETIME)
    private Date updatedDate;

    @TableField(value = "tenant_id")
    @Column(comment = "租户号", type = MySqlTypeConstant.BIGINT)
    private Long tenantId;
}
