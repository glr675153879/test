package com.hscloud.hs.cost.account.model.entity.kpi;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.gitee.sunchenbin.mybatis.actable.annotation.Column;
import com.gitee.sunchenbin.mybatis.actable.annotation.IsAutoIncrement;
import com.gitee.sunchenbin.mybatis.actable.constants.MySqlTypeConstant;
import com.pig4cloud.pigx.common.core.util.TenantTable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TenantTable
@TableName("kpi_item_equivalent_config")
public class KpiItemEquivalentConfig extends Model<KpiItemEquivalentConfig> {

    @IsAutoIncrement
    @TableId(value = "id", type = IdType.AUTO)
    @Column(comment = "", type = MySqlTypeConstant.BIGINT, isNull = false)
    private Long id;

    @TableField(value = "item_id")
    @Column(comment = "核算项id", type = MySqlTypeConstant.BIGINT, isNull = false)
    private Long itemId;

    @TableField(value = "item_code")
    @Column(comment = "code用56转，带上前缀，核算项X_，分摊指标F_，核算指标Z_", type = MySqlTypeConstant.VARCHAR, length = 50, isNull = false)
    private String itemCode;

    @TableField(value = "inherit_flag")
    @Column(comment = "是否继承 0-否 1-是", type = MySqlTypeConstant.CHAR, length = 1)
    private String inheritFlag;

    @TableField(value = "account_unit_id")
    @Column(comment = "科室id", type = MySqlTypeConstant.BIGINT, isNull = false)
    private Long accountUnitId;

    @TableField(value = "std_equivalent")
    @Column(comment = "标化当量", type = MySqlTypeConstant.DECIMAL, length = 10, decimalLength = 4)
    private BigDecimal stdEquivalent;

    @TableField(value = "seq")
    @Column(comment = "排序号", type = MySqlTypeConstant.INT)
    private Integer seq;

    @TableField(value = "del_flag", fill = FieldFill.INSERT)
    @TableLogic(value = "0", delval = "1")
    @Column(comment = "删除标记，0未删除，1已删除", type = MySqlTypeConstant.CHAR, length = 1, defaultValue = "0")
    private String delFlag;

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
