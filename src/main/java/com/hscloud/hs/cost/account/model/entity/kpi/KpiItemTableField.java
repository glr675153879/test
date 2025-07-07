package com.hscloud.hs.cost.account.model.entity.kpi;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.gitee.sunchenbin.mybatis.actable.annotation.Column;
import com.gitee.sunchenbin.mybatis.actable.annotation.IsAutoIncrement;
import com.gitee.sunchenbin.mybatis.actable.constants.MySqlTypeConstant;
import com.pig4cloud.pigx.common.core.util.TenantTable;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * 核算项基础表字段
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TenantTable
@Schema(description = "核算项基础表字段")
@TableName("kpi_item_table_field")
public class KpiItemTableField extends Model<KpiItemTableField> {
    @TableId(value = "id", type = IdType.AUTO)
    @IsAutoIncrement
    @Column(comment = "", type = MySqlTypeConstant.BIGINT, isNull = false)
    private Long id;

    @TableField(value = "table_id")
    @Column(comment = "表名", type = MySqlTypeConstant.VARCHAR, length = 64, isNull = false)
    private Long tableId;

    @TableField(value = "field_name")
    @Column(comment = "字段名", type = MySqlTypeConstant.VARCHAR, length = 64, isNull = false)
    private String fieldName;

    @TableField(value = "field_comment")
    @Column(comment = "字段注释", type = MySqlTypeConstant.VARCHAR, length = 255)
    private String fieldComment;

    @TableField(value = "field_type")
    @Column(comment = "字段类型", type = MySqlTypeConstant.VARCHAR, length = 64, isNull = false)
    private String fieldType;

    @TableField(value = "status")
    @Column(comment = "启停用标记，0启用，1停用", type = MySqlTypeConstant.CHAR, length = 1, isNull = false, defaultValue = "0")
    private String status;

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

    @TableField(value = "dict_code", updateStrategy = FieldStrategy.IGNORED)
    @Column(comment = "字段字典编码", type = MySqlTypeConstant.VARCHAR, length = 50)
    private String dictCode;
}
