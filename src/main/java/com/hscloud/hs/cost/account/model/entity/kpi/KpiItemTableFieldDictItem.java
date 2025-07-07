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

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TenantTable
@Schema(description = "核算项基础表字段字典值")
@TableName("kpi_item_table_field_dict_item")
public class KpiItemTableFieldDictItem extends Model<KpiItemTableFieldDictItem> {
    @TableId(value = "id", type = IdType.AUTO)
    @IsAutoIncrement
    @Column(comment = "", type = MySqlTypeConstant.BIGINT, isNull = false)
    private Long id;

    @TableField(value = "dict_code")
    @Column(comment = "字典编码", type = MySqlTypeConstant.VARCHAR, length = 64, isNull = false)
    @Schema(description = "字典编码")
    private String dictCode;

    @TableField(value = "item_code")
    @Column(comment = "字典值编码", type = MySqlTypeConstant.VARCHAR, length = 64, isNull = false)
    @Schema(description = "字典值编码")
    private String itemCode;

    @TableField(value = "item_name")
    @Column(comment = "字典值名称", type = MySqlTypeConstant.VARCHAR, length = 64, isNull = false)
    @Schema(description = "字典值名称")
    private String itemName;

    @TableField(value = "item_desc")
    @Column(comment = "字典值描述", type = MySqlTypeConstant.VARCHAR, length = 255)
    @Schema(description = "字典值描述")
    private String itemDesc;

    @TableField(value = "status")
    @Column(comment = "启停用标记，0启用，1停用", type = MySqlTypeConstant.CHAR, length = 1, isNull = false, defaultValue = "0")
    @Schema(description = "启停用标记，0启用，1停用")
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

}
