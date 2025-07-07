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

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TenantTable
@Schema(description = "核算项当量")
@TableName("kpi_item_equivalent")
public class KpiItemEquivalent extends Model<KpiItemEquivalent> {
    @IsAutoIncrement
    @TableId(value = "id", type = IdType.AUTO)
    @Column(comment = "", type = MySqlTypeConstant.BIGINT)
    private Long id;

    @TableField(value = "item_id")
    @Column(comment = "核算项id", type = MySqlTypeConstant.BIGINT)
    private Long itemId;

    @TableField(value = "code")
    @Column(comment = "code用56转，带上前缀，核算项X_，分摊指标F_，核算指标Z_", type = MySqlTypeConstant.VARCHAR, length = 50)
    private String code;

    @TableField(value = "item_name")
    @Column(comment = "核算项名称", type = MySqlTypeConstant.VARCHAR, length = 64)
    private String itemName;

    @TableField(value = "acq_method")
    @Column(comment = "采集方式,1,sql,2手工", type = MySqlTypeConstant.CHAR, length = 1)
    private String acqMethod;

    @TableField(value = "caliber")
    @Column(comment = "口径颗粒度 1人2科室3归集4固定值5多条件", type = MySqlTypeConstant.CHAR, length = 1)
    private String caliber;

    @TableField(value = "period")
    @Column(comment = "周期", type = MySqlTypeConstant.INT)
    private Long period;

    @TableField(value = "assign_flag")
    @Column(comment = "是否当量分配 0-否 1-是", type = MySqlTypeConstant.CHAR)
    private String assignFlag;

    @TableField(value = "unit")
    @Column(comment = "单位", type = MySqlTypeConstant.VARCHAR, length = 50)
    private String unit;

    @TableField(value = "account_unit_id")
    @Column(comment = "科室id", type = MySqlTypeConstant.BIGINT)
    private Long accountUnitId;

    @TableField(value = "emp_id")
    @Column(comment = "工号", type = MySqlTypeConstant.VARCHAR, length = 50)
    private String empId;

    @TableField(value = "user_id")
    @Column(comment = "人", type = MySqlTypeConstant.BIGINT)
    private Long userId;

    @TableField(value = "equivalent_type")
    @Column(comment = "当量类型 1-人 2-科室", type = MySqlTypeConstant.CHAR)
    private String equivalentType;

    @TableField(value = "std_equivalent")
    @Column(comment = "标化当量", type = MySqlTypeConstant.DECIMAL, length = 24, decimalLength = 6)
    private BigDecimal stdEquivalent;

    @TableField(value = "total_workload")
    @Column(comment = "总工作量(原始值)", type = MySqlTypeConstant.DECIMAL, length = 24, decimalLength = 6)
    private BigDecimal totalWorkload;

    @TableField(value = "total_workload_admin")
    @Column(comment = "总工作量(校准值，审核通过)", type = MySqlTypeConstant.DECIMAL, length = 24, decimalLength = 6)
    private BigDecimal totalWorkloadAdmin;

    @TableField(value = "new_total_workload", updateStrategy = FieldStrategy.ALWAYS)
    @Column(comment = "总工作量(修改值)", type = MySqlTypeConstant.DECIMAL, length = 24, decimalLength = 6)
    private BigDecimal newTotalWorkload;

    @TableField(value = "total_equivalent")
    @Column(comment = "总工作当量", type = MySqlTypeConstant.DECIMAL, length = 24, decimalLength = 6)
    private BigDecimal totalEquivalent;

    @TableField(value = "distribute_type")
    @Column(comment = "0-平均分配，1-系数分配，2-自定义分配", type = MySqlTypeConstant.CHAR)
    private String distributeType;

    @TableField(value = "coefficient")
    @Column(comment = "系数", type = MySqlTypeConstant.DECIMAL, length = 24, decimalLength = 6)
    private BigDecimal coefficient;

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