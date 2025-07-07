package com.hscloud.hs.cost.account.model.entity.kpi;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
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
* 指标公式适用对象Model
* @author you
* @since 2024-09-13
*/

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TenantTable
@Schema(description = "指标公式适用对象表")
@TableName("kpi_index_formula_obj_copy")
public class KpiIndexFormulaObjCopy extends Model<KpiIndexFormulaObjCopy>{

    @TableId(value = "id", type = IdType.AUTO)
    @IsAutoIncrement
    @Column(comment="", type = MySqlTypeConstant.BIGINT , isNull = false )
    private Long id;

    @TableField(value = "formula_id")
    @Column(comment="公式id", type = MySqlTypeConstant.BIGINT)
    private Long formulaId;

    @TableField(value = "index_code")
    @Column(comment="指标code", type = MySqlTypeConstant.VARCHAR, length = 255 , isNull = false )
    private String indexCode;

    @TableField(value = "plan_code")
    @Column(comment="方案编码", type = MySqlTypeConstant.VARCHAR, length = 255)
    private String planCode;

    @TableField(value = "plan_obj")
    @Column(comment="方案适用对象", type = MySqlTypeConstant.BIGINT)
    private Long planObj;

    @TableField(value = "plan_obj_category")
    @Column(comment = "方案适用对象分组", type = MySqlTypeConstant.VARCHAR, length = 100)
    private String planObjCategory;

    @TableField(value = "created_id")
    @Column(comment="创建人", type = MySqlTypeConstant.VARCHAR, length = 255)
    private String createdId;

    @TableField(value = "created_date")
    @Column(comment="创建时间", type = MySqlTypeConstant.DATETIME)
    private Date createdDate;

    @TableField(value = "updated_id")
    @Column(comment="更新人", type = MySqlTypeConstant.VARCHAR, length = 255)
    private String updatedId;

    @TableField(value = "updated_date")
    @Column(comment="更新时间", type = MySqlTypeConstant.DATETIME)
    private Date updatedDate;

    @TableField(value = "tenant_id")
    @Column(comment="租户号", type = MySqlTypeConstant.BIGINT)
    private Long tenantId;

    @TableField(value = "task_child_id")
    @Column(comment="", type = MySqlTypeConstant.BIGINT)
    private Long taskChildId;

    @TableField(value = "copy_date")
    @Column(comment="", type = MySqlTypeConstant.DATETIME)
    private Date copyDate;

    @TableField(value = "exclude_person")
    @Column(comment="所有人情况下排除的人", type = MySqlTypeConstant.VARCHAR, length = 2500 )
    private String excludePerson;

    @TableField(value = "exclude_dept")
    @Column(comment="所有科室情况下排除的科室", type = MySqlTypeConstant.VARCHAR, length = 2500 )
    private String excludeDept;

    @TableField(value = "plan_obj_account_type")
    @Column(comment="方案适用对象核算分组", type = MySqlTypeConstant.VARCHAR, length = 100 )
    private String planObjAccountType;
}