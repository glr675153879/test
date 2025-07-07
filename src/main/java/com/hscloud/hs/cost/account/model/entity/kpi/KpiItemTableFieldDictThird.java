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
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TenantTable
@Schema(description = "核算项数据表字段字典映射表")
@TableName("kpi_item_table_field_dict_third")
public class KpiItemTableFieldDictThird extends Model<KpiItemTableFieldDictThird> {

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

    @TableField(value = "third_item_code")
    @Column(comment = "第三方字典值编码", type = MySqlTypeConstant.VARCHAR, length = 64, isNull = false)
    @Schema(description = "第三方字典值编码")
    private String thirdItemCode;

    @TableField(value = "third_item_name")
    @Column(comment = "第三方字典值名称", type = MySqlTypeConstant.VARCHAR, isNull = false)
    @Schema(description = "第三方字典值名称")
    private String thirdItemName;

    @TableField(value = "start_time", updateStrategy = FieldStrategy.ALWAYS)
    @Column(comment = "开始时间", type = MySqlTypeConstant.DATE)
    @Schema(description = "开始时间")
    private Date startTime;

    @TableField(value = "end_time",updateStrategy = FieldStrategy.ALWAYS)
    @Column(comment = "结束时间", type = MySqlTypeConstant.DATE)
    @Schema(description = "结束时间")
    private Date endTime;

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
