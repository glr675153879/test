package com.hscloud.hs.cost.account.model.entity.kpi;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.gitee.sunchenbin.mybatis.actable.annotation.Column;
import com.gitee.sunchenbin.mybatis.actable.annotation.Index;
import com.gitee.sunchenbin.mybatis.actable.annotation.IsAutoIncrement;
import com.gitee.sunchenbin.mybatis.actable.constants.MySqlTypeConstant;
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
@Schema(description = "绩效签发 左侧固定")
@TableName("kpi_sign_left")
@TenantTable
public class KpiSignLeft extends Model<KpiSignLeft> {
    @TableId(value = "id", type = IdType.AUTO)
    @IsAutoIncrement
    @Column(comment="", type = MySqlTypeConstant.BIGINT , isNull = false )
    private Long id;

    @TableField(value = "dept_id")
    @Column(comment="", type = MySqlTypeConstant.VARCHAR,  length = 50)
    private String deptId;

    @TableField(value = "dept_name")
    @Column(comment="", type = MySqlTypeConstant.VARCHAR, length = 50)
    private String deptName;

    @TableField(value = "user_id")
    @Column(comment="", type = MySqlTypeConstant.BIGINT, isNull = false)
    private Long userId;

    @TableField(value = "user_name")
    @Column(comment="", type = MySqlTypeConstant.VARCHAR, length = 50)
    private String userName;

    @TableField(value = "user_type")
    @Column(comment="", type = MySqlTypeConstant.VARCHAR, length = 50)
    private String userType;

    @TableField(value = "sum")
    @Column(comment="应发合计",type = MySqlTypeConstant.DECIMAL, length = 24, decimalLength = 6, isNull = false)
    private BigDecimal sum;

    @TableField(value = "amount")
    @Column(comment="绩效月奖",type = MySqlTypeConstant.DECIMAL, length = 24, decimalLength = 6, isNull = false)
    private BigDecimal amount;

    @TableField(value = "first_amount")
    @Column(comment="一次分配金额",type = MySqlTypeConstant.DECIMAL, length = 24, decimalLength = 6, isNull = false)
    private BigDecimal firstAmount;

    @TableField(value = "second_amount")
    @Column(comment="二次分配金额",type = MySqlTypeConstant.DECIMAL, length = 24, decimalLength = 6, isNull = false)
    private BigDecimal secondAmount;

    @TableField(value = "period")
    @Index
    @Column(comment="周期", type = MySqlTypeConstant.INT , isNull = false )
    private Long period;

    @TableField(value = "created_id",fill = FieldFill.INSERT)
    @Column(comment="创建人", type = MySqlTypeConstant.BIGINT)
    private Long createdId;

    @TableField(value = "created_date",fill = FieldFill.INSERT)
    @Column(comment="创建时间", type = MySqlTypeConstant.DATETIME)
    private Date createdDate;

    @TableField(value = "updated_id",fill = FieldFill.INSERT_UPDATE)
    @Column(comment="更新人", type = MySqlTypeConstant.BIGINT)
    private Long updatedId;

    @TableField(value = "updated_date",fill = FieldFill.INSERT_UPDATE)
    @Column(comment="更新时间", type = MySqlTypeConstant.DATETIME)
    private Date updatedDate;

    @TableField(value = "tenant_id")
    @Column(comment="", type = MySqlTypeConstant.BIGINT)
    private Long tenantId;
}
