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
@Schema(description = "绩效签发 右侧不固定")
@TableName("kpi_sign_right")
@TenantTable
public class KpiSignRight extends Model<KpiSignRight> {
    @TableId(value = "id", type = IdType.AUTO)
    @IsAutoIncrement
    @Column(comment="", type = MySqlTypeConstant.BIGINT , isNull = false )
    private Long id;

    @TableField(value = "head_id")
    @Index
    @Column(comment="表头id", type = MySqlTypeConstant.BIGINT, isNull = false)
    private Long headId;

    @TableField(value = "user_id")
    @Index
    @Column(comment="", type = MySqlTypeConstant.BIGINT, isNull = false)
    private Long userId;

    @TableField(value = "value")
    @Column(comment="值",type = MySqlTypeConstant.DECIMAL, length = 24, decimalLength = 6, isNull = false)
    private BigDecimal value;

    @TableField(value = "value_a")
    @Column(comment="head表上的系数",type = MySqlTypeConstant.DECIMAL, length = 24, decimalLength = 6)
    private BigDecimal valueA;

    @TableField(value = "value_b")
    @Column(comment="导入 或 系统采集的值",type = MySqlTypeConstant.DECIMAL, length = 24, decimalLength = 6)
    private BigDecimal valueB;

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

    @TableField(value = "del_flag")
    @Index
    @Column(comment="是否删除：0：未删除 1：删除", type = MySqlTypeConstant.CHAR, length = 1, defaultValue = "0" )
    private String delFlag;
}
